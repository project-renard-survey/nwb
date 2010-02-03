/*
 * calibrate.cpp
 * Locate the best-matching unit in the given codebook for each vector in the given training data.
 *  Created on: Feb 2, 2009
 *      Author: jrbibers
 */

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <limits>
#include <map>
#include <math.h>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <ctime>


#include "mpi.h"

using namespace std;
using std::cerr;
using std::endl;


struct Coordinate {
	int row;
	int column;

	Coordinate(int r, int c) { row = r; column = c; }
};

struct TrainingDatum {
	string id;
	map<int, float> sparseVector;

	TrainingDatum(string i, map<int, float> v) { id = i; sparseVector = v; }
};

typedef vector<TrainingDatum> training_data_t;


string g_initialCodebookPath;
string g_inTrainingDataPath;
string g_outTrainingDataPath;


// set by loadInitialCodebook()
int g_rows = -1;
int g_columns = -1;
int g_dim = -1;

int g_numberOfJobs = 1;

// set by loadTrainingVectors()
int g_numberOfVectors = -1;


char* getAsctime() {
	time_t rawTime;
	time(&rawTime);
	struct tm* localTime = localtime(&rawTime);
	return asctime(localTime);
}

void zeroOut(float* array, int size) {
	for (int i = 0; i < size; i++) {
		array[i] = 0.0;
	}
}

float* calculateNodeIndex(float* net, Coordinate coord) {
	return net + (g_dim * (coord.row * g_columns + coord.column));
}


float calculateCosineSimilarity(
		float* netVector, TrainingDatum* trainingDatum, float recentSquaredNorm) {
	float dotProduct = 0.0;
	float sparseSquaredNorm = 0.0;

	map<int, float> sparseVector = trainingDatum->sparseVector;

	for (map<int, float>::const_iterator it = sparseVector.begin(); it != sparseVector.end(); it++) {
		dotProduct += (it->second) * netVector[it->first];
		sparseSquaredNorm += (it->second) * (it->second);
	}

	return (dotProduct / (sqrt(sparseSquaredNorm) * sqrt(recentSquaredNorm)));
}

/* NOTE: This entire notion breaks down when the cosine similarity may be negative.
 * This will not happen with the data we're currently considering.
 */
float calculateCosineDissimilarity(float* netVector, TrainingDatum* trainingDatum, float recentSquaredNorm) {
	return 1 - calculateCosineSimilarity(netVector, trainingDatum, recentSquaredNorm);
}


// From SOM_PAK
float hexa_dist(int bx, int by, int tx, int ty){
	float ret, diff;

	diff = bx - tx;

	if (((by - ty) % 2) != 0) {
		if ((by % 2) == 0) {
			diff -= 0.5;
		}
		else {
			diff += 0.5;
		}
	}

	ret = diff * diff;
	diff = by - ty;
	ret += 0.75 * diff * diff;

	return ret;
}

float hexa_dist(int xDelta, int yDelta) {
	return hexa_dist(0, 0, xDelta, yDelta);
}


// myRank is given only to limit reporting.
float* loadInitialCodebook(int myRank) {
	cout << "Loading initial codebook.. " << endl;

	if (myRank == 0) {
		cout << "Starting initial codebook load at " << getAsctime() << endl;
	}

	float* net;

	ifstream codebookFile(g_initialCodebookPath.c_str());
	if (codebookFile.is_open()) {
		// Read parameters on first line.
		string topology; // Ignored for now.
		string neighborhood; // Ignored for now.
		codebookFile >> g_dim >> topology >> g_columns >> g_rows >> neighborhood;

		int numberOfNodes = g_rows * g_columns;
		net = new float[numberOfNodes * g_dim];

		for (int i = 0; i < numberOfNodes; i++) {
			float* currentNode = net + i * g_dim;

			bool allZero = true;
			for (int weightIndex = 0; weightIndex < g_dim; weightIndex++) {
				codebookFile >> currentNode[weightIndex];

				if (currentNode[weightIndex] != 0) {
					allZero = false;
				}
			}

			if (allZero) {
				cerr << "ERROR: Codebook contained an all-zero vector.  Quitting now." << endl;
				exit(1);
			}
		}

		if (myRank == 0) {
			cout << "Finishing initial codebook load at " << getAsctime() << endl;
		}

		codebookFile.close();
	} else {
		cerr << "Error opening initial codebook file!" << endl;
		exit(1);
	}

	cout << "Done." << endl;

	return net;
}

void readConfigurationFile() {
	cout << "Reading configuration file.." << endl;

	ifstream configurationFile("calibrate-paths.cfg");

	if (configurationFile.is_open()) {
		configurationFile >> g_initialCodebookPath;
		configurationFile >> g_inTrainingDataPath;
		configurationFile >> g_outTrainingDataPath;
	}

	configurationFile.close();

	cout << "Done." << endl;
}


// Assumes that the file contains lines which give indices to non-zeroes in a binary-valued vector.
training_data_t* loadMyTrainingDataFromSparse(int myRank) {
	cout << "Loading training vectors (from sparse representation).. " << endl;

	if (myRank == 0) {
		cout << "Starting training vectors load at " << getAsctime() << endl;
	}

	ifstream trainingFile(g_inTrainingDataPath.c_str());

	if (trainingFile.is_open()) {
		// Process 0 (only) will echo out the first line and all comments.
		ofstream outFile;
		if (myRank == 0) {
			outFile.open(g_outTrainingDataPath.c_str(), ios::app);
		}

		string firstLine;
		getline(trainingFile, firstLine);
		istringstream firstLineStream(firstLine);
		int dimensions;
		firstLineStream >> g_numberOfVectors >> dimensions;
		if (dimensions != g_dim) {
			cerr << "Dimensionality of the training set (" << dimensions << ") does not agree with the dimensionality of the codebook vectors (" << g_dim << ")." << endl;
			exit(1);
		}

		// Echo first line to output.
		if (myRank == 0) {
			outFile << g_numberOfVectors << " " << g_dim << endl;
		}

		const string commentMarker("#");

		training_data_t* trainingData = new training_data_t();

		int skippedVectorCount = 0;
		int lineNumber = 0;
		string line;
		while (getline(trainingFile, line)) {
			// Comments are ignored by the algorithm, but are echoed to the output file.
			if (0 == line.find(commentMarker)) {
				if (myRank == 0) {
					outFile << line << endl;
				}

				continue;
			}

			// Only read lines assigned to my rank.
			if (lineNumber % g_numberOfJobs == myRank) {
				istringstream linestream(line);

				// Read and ignore document ID.
				string documentID;
				linestream >> documentID;

				map<int, float> trainingVector;

				int oneBasedIndex;
				while (linestream >> oneBasedIndex) {
					int zeroBasedIndex = oneBasedIndex - 1;

					if (zeroBasedIndex < g_dim) {
						trainingVector[zeroBasedIndex] = 1;
					} else {
						cerr << "Dimensionality of training vectors exceeds that of the codebook." << endl;
						exit(1);
					}
				}

				if (trainingVector.empty()) {
					skippedVectorCount++;
				} else {
					TrainingDatum trainingDatum(documentID, trainingVector);
					trainingData->push_back(trainingDatum);
				}
			}

			lineNumber++;
		}

		if (myRank == 0) {
			outFile.close();
		}

		trainingFile.close();

		if (myRank == 0) {
			cout << "Finishing training vectors load at " << getAsctime() << endl;
		}

		if (skippedVectorCount > 0) {
			if (myRank == 0) {
				cerr << "WARNING: Skipped " << skippedVectorCount
					<< " training vectors with no non-zero coordinates.  "
					<< "If you are not using the cosine similarity metric than remove this check/skip!"
					<< endl;
			}
		}



		cout << "Done." << endl;

		return trainingData;
	} else {
		cerr << "Error opening training data file!" << endl;
		exit(1);
	}
}

Coordinate findWinnerCosine(TrainingDatum* trainingDatum, float* net, float* recentSquaredNorms) {
    float leastDissimilarity = numeric_limits<float>::max();
    Coordinate winner(0, 0);

    for (int row = 0; row < g_rows; row++) {
    	for (int column = 0; column < g_columns; column++) {
    		Coordinate coord(row, column);

    		float recentSquaredNorm = recentSquaredNorms[(row * g_columns) + column];

    		float dissimilarity =
    				calculateCosineDissimilarity(calculateNodeIndex(net, coord), trainingDatum, recentSquaredNorm);

    		if (dissimilarity < leastDissimilarity) {
				leastDissimilarity = dissimilarity;
				winner = coord;
    		}
    	}
    }

    return winner;
}


void calculateSquaredNorms(float* oldSquaredNorms, float* net) {
	int numberOfNodes = g_rows * g_columns;

	for (int nodeIndex = 0; nodeIndex < numberOfNodes; nodeIndex++) {
		float* currentNode = net + nodeIndex * g_dim;
		float* currentNorm = oldSquaredNorms + nodeIndex;

		for (int weightIndex = 0; weightIndex < g_dim; weightIndex++) {
			float weight = currentNode[weightIndex];

			(*currentNorm) += weight * weight;
		}
	}
}


void calibrate(int myRank, float* net, training_data_t* myTrainingData) {
	if (myRank == 0) {
		cout << "Calibrating.. " << endl;
	}

	const int numberOfNodes = g_rows * g_columns;
	const int flatSize = numberOfNodes * g_dim;

	float* recentSquaredNorms = new float[numberOfNodes];
	zeroOut(recentSquaredNorms, numberOfNodes);
	calculateSquaredNorms(recentSquaredNorms, net);

	ofstream outFile;
	outFile.open(g_outTrainingDataPath.c_str(), ios::app);

	for (training_data_t::const_iterator vecIt = myTrainingData->begin();
			vecIt != myTrainingData->end();
			vecIt++) {
		TrainingDatum myTrainingDatum = *vecIt;

		Coordinate winnerCoordinate = findWinnerCosine(&myTrainingDatum, net, recentSquaredNorms);
		float* winner = calculateNodeIndex(net, winnerCoordinate);

		float recentSquaredNorm =
				recentSquaredNorms[(winnerCoordinate.row * g_columns) + winnerCoordinate.column];
		float dissimilarity =
				calculateCosineDissimilarity(winner, &myTrainingDatum, recentSquaredNorm);

		ostringstream ss;
		ss << myTrainingDatum.id << " ";
		ss << winnerCoordinate.row << " " << winnerCoordinate.column << " ";
		ss << dissimilarity << " ";
		map<int, float> trainingVector = myTrainingDatum.sparseVector;
		bool beyondFirstCoordinate = false;
		for (map<int, float>::const_iterator it = trainingVector.begin(); it != trainingVector.end(); it++) {
			if (beyondFirstCoordinate) {
				ss << " ";
			}

			ss << (1 + it->first);

			beyondFirstCoordinate = true;
		}
		ss << endl;

		outFile << ss.str();
	}

	outFile.close();

	if (myRank == 0) {
		cout << "Done." << endl;
	}
}


int main(int argc, char *argv[]) {
	srand(time(0));

	MPI::Init(argc, argv);
	MPI::COMM_WORLD.Set_errhandler(MPI::ERRORS_THROW_EXCEPTIONS);

	int myRank = MPI::COMM_WORLD.Get_rank();

	if (myRank == 0) {
		cout << "Program starting at " << getAsctime() << endl;
	}

	cout << "My rank is " << myRank << endl;

	g_numberOfJobs = MPI::COMM_WORLD.Get_size();

	readConfigurationFile();

	float* net = loadInitialCodebook(myRank);

	// TODO Manually swapping out these function names is lame.
	training_data_t* myTrainingData = loadMyTrainingDataFromSparse(myRank);

	/* Ensure process 0 gets the time it needs to write out the first line and comments
	 * (see loadMyTrainingDataFromSparse).
	 */
	MPI::COMM_WORLD.Barrier();

	calibrate(myRank, net, myTrainingData);

	if (myRank == 0) {
		cout << "Program finishing at " << getAsctime() << endl;
	}

	MPI::Finalize();

	return 0;
}
