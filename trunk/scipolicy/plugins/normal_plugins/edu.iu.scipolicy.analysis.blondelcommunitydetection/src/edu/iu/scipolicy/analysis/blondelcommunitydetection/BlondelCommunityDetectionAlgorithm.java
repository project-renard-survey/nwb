package edu.iu.scipolicy.analysis.blondelcommunitydetection;

import java.io.File;
import java.util.Dictionary;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.data.Data;

import edu.iu.scipolicy.analysis.blondelcommunitydetection.algorithmstages.CommunityDetectionRunner;
import edu.iu.scipolicy.analysis.blondelcommunitydetection.algorithmstages.NWBAndTreeFilesMerger;
import edu.iu.scipolicy.analysis.blondelcommunitydetection.algorithmstages.NWBToBINConverter;

public class BlondelCommunityDetectionAlgorithm implements Algorithm {
	
	private AlgorithmFactory blondelCommunityDetectionAlgorithmFactory;
	
	private Data inputData;
	File inputNWBFile;
	
	private Dictionary parameters;
	private CIShellContext context;
    
    public BlondelCommunityDetectionAlgorithm(
    		AlgorithmFactory blondelCommunityDetectionAlgorithmFactory,
    		Data[] data,
    		Dictionary parameters,
    		CIShellContext context) {
    	this.blondelCommunityDetectionAlgorithmFactory =
    		blondelCommunityDetectionAlgorithmFactory;
    	
        this.inputData = data[0];
        this.inputNWBFile = (File)inputData.getData();
        //TODO: Get weight attribute out HERE, and don't assign parameters to
        //be a instance variable
        this.parameters = parameters;
        this.context = context;
    }

    // The C++ implementation of Blondel's community detection algorithm
    // expects the input to be a BIN network file, and it outputs a tree
    // hierarchy file that contains the generated community structures.
    // This algorithm first converts the input NWB to a BIN file and passes
    // the result into the compiled C++ community detection program.
    // This algorithm then merges the resulting tree file with the input NWB
    // file, producing a new NWB with nodes that are annotated with the
    // appropriate community attributes.
    public Data[] execute() throws AlgorithmExecutionException {
    	NetworkInfo networkInfo = new NetworkInfo();
    	
    	// Convert the NWB file to a BIN file.
    	
    	File binFile =
    		NWBToBINConverter.convertNWBFileToBINFile(this.inputNWBFile,
    												  networkInfo);
    	
    	/*
    	 *  Run community detection on the BIN file,
    	 *  producing a TREE file with community-annotation.
    	 */
    	
    	CommunityDetectionRunner communityDetectionRunner =
    		new CommunityDetectionRunner(
    			this.blondelCommunityDetectionAlgorithmFactory,
    			this.parameters,
    			this.context);
    	
    	File communityTreeFile =
    		communityDetectionRunner.runCommunityDetection(binFile,
    													   this.inputData);
    	
    	/*
    	 *  Merge the TREE file with community-annotation and the original
    	 *  NWB file,
    	 *  producing a community-annotated NWB file.
    	 */
    	 
    	
    	File outputNWBFile =
    		NWBAndTreeFilesMerger.mergeCommunitiesFileWithNWBFile(
    			communityTreeFile, this.inputNWBFile, networkInfo);
    	
    	// Wrap the community-annotated NWB file in Data[] for output.
    	
        Data[] outData = Utilities.wrapFileAsOutputData(outputNWBFile,
        												"file:text/nwb",
        												this.inputData);
        
        // Return Data[].
        
        return outData;
    }
}