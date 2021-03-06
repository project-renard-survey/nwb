package edu.iu.sci2.visualization.scimaps;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import oim.vivo.scimapcore.journal.Discipline;
import oim.vivo.scimapcore.journal.Edge;
import oim.vivo.scimapcore.journal.Journal;
import oim.vivo.scimapcore.journal.Node;
import oim.vivo.scimapcore.journal.Nodes;
import oim.vivo.scimapcore.mapping.DetailedScienceMappingResult;
import oim.vivo.scimapcore.mapping.ScienceMapping;

/*
 * This class represents the Map of Science.
 */
public class MapOfScience {
	public static DecimalFormat formatter = new DecimalFormat("###,###");

	private DetailedScienceMappingResult mappingResult;

	/**
	 * Creates a map of science. You must provide a mapping from the journal
	 * name to the hits for that journal.
	 * 
	 * @param journalNameAndHitCount
	 * @throws NumberFormatException
	 */
	public MapOfScience(Map<String, Integer> journalNameAndHitCount) {

		try {
			this.mappingResult = ScienceMapping.generateDetailedScienceMappingResult(journalNameAndHitCount);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	public MapOfScience(DetailedScienceMappingResult mappingResult){
		this.mappingResult = mappingResult;
	}
	

	/**
	 * Return a float representing the number of found publications.
	 * 
	 * @return
	 */
	public float countOfMappedPublications() {
		return this.mappingResult.getMappedJournals().size();
	}

	/**
	 * Return a string representing a pretty version of the found publications.
	 * 
	 * @return
	 */
	public String prettyCountOfMappedPublications() {
		float count = countOfMappedPublications();
		return formatter.format(count);
	}

	/**
	 * Return a float representing the number of unfound publications.
	 * 
	 * @return
	 */
	public float countOfUnmappedPublications() {
		return this.mappingResult.getUnmappedJournals().size();
	}

	/**
	 * Return a string representing a pretty version of the unfound
	 * publications.
	 * 
	 * @return
	 */
	public String prettyCountOfUnmappedPublications() {
		float count = countOfUnmappedPublications();
		return formatter.format(count);
	}

	/**
	 * Return a float representing the number of total publications.
	 * 
	 * @return
	 */
	public float countOfPublications() {
		return this.mappingResult.getJournals().size();
	}

	/**
	 * Return a string representing a pretty version of the total publications.
	 * 
	 * @return
	 */
	public String prettyCountOfPublications() {
		float count = countOfPublications();
		return formatter.format(count);
	}

	/**
	 * Return a float representing the number of found subdiciplines.
	 * 
	 * @return
	 */
	public float countOfMappedSubdiciplines() {
		return this.mappingResult.getMappedResult().size();
	}

	/**
	 * Return a string representing a pretty version of the found subdiciplines.
	 * 
	 * @return
	 */
	public String prettyCountOfMappedSubdiciplines() {
		float count = countOfMappedSubdiciplines();
		return formatter.format(count);
	}

	/**
	 * Return a float representing the number of disciplines that contain all the
	 * publications.
	 * 
	 * @return
	 */
	public float countOfDisciplinesUsed() {
		Set<Discipline> disciplinesUsed = new HashSet<Discipline>();

		Set<Integer> usedNodeIds = getMappedResults().keySet();
		for (int usedNodeId : usedNodeIds) {
			Discipline nodeDiscipline = Nodes.getNodeByID(usedNodeId).getDiscipline();
			disciplinesUsed.add(nodeDiscipline);
		}

		return disciplinesUsed.size();
	}

	/**
	 * Return a string representing a pretty version of the number of disciplines
	 * that contain all the publications.
	 * 
	 * @return
	 */
	public String prettyCountOfDisciplinesUsed() {
		float count = countOfDisciplinesUsed();
		return formatter.format(count);
	}

	/**
	 * Return a mapping of found publication node ids to their weight.
	 * 
	 * @return
	 */
	public Map<Integer, Float> getMappedResults() {
		return this.mappingResult.getMappedResult();
	}

	/**
	 * Return a mapping of unfound publication names to the hitcount of those
	 * publications
	 * 
	 * @return
	 */
	public Map<String, Float> getUnmappedResults() {
		return this.mappingResult.getUnmappedResult();
	}

	/**
	 * Return the total weights for all found node ids
	 * 
	 * @return
	 */
	public Collection<Float> getMappedWeights() {
		return getMappedResults().values();
	}

	/**
	 * Return a set of all the node ids that matched publications found.
	 * 
	 * @return
	 */
	public Set<Integer> getMappedIds() {
		return getMappedResults().keySet();
	}

	/**
	 * This will return a list of publication node's ids that were found ordered
	 * by the weight of that node.
	 * 
	 * @return
	 */
	public List<Integer> getMappedIdsByWeight() {
		Integer[] mappedIds = getMappedIds().toArray(new Integer[] {});
		Arrays.sort(mappedIds, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return getIdWeightMapping().get(o1).compareTo(
						getIdWeightMapping().get(o2));
			}
		});

		return Arrays.asList(mappedIds);
	}

	/**
	 * Returns a map linking the node id to the weight for that node.
	 * 
	 * @return
	 */
	public Map<Integer, Float> getIdWeightMapping() {
		return this.mappingResult.getMappedResult();
	}

	/**
	 * Get a set of all the journals.
	 * 
	 * @return
	 */
	public Set<Journal> getJournals() {
		return this.mappingResult.getJournals();
	}

	/**
	 * Get a set of all the edges used in the map of science.
	 * 
	 * @return
	 */
	public static Set<Edge> getEdges() {
		return ScienceMapping.getEdges();
	}

	/**
	 * Get a set of all the disciplines used in the map of science.
	 * 
	 * @return
	 */
	public static Set<Discipline> getDisciplines() {
		return ScienceMapping.getDisciplines();
	}

	/**
	 * Get a set of all the nodes used in the map of science.
	 * 
	 * @return
	 */
	public static Set<Node> getNodes() {
		return ScienceMapping.getNodes();
	}

	/**
	 * Get a set of all the journals that were found for the map of science.
	 * 
	 * @return
	 */
	public Set<Journal> getMappedJournals() {
		return this.mappingResult.getMappedJournals();
	}

	/**
	 * Get a set of all the journals that were not found for the map of science.
	 * 
	 * @return
	 */
	public Set<Journal> getUnmappedJournals() {
		return this.mappingResult.getUnmappedJournals();
	}

	/**
	 * This gives a mapping for all the disciplines used by the mapped (used)
	 * journals.
	 * 
	 * @return
	 */
	public SortedMap<Discipline, SortedSet<Journal>> getMappedJournalsByDiscipline() {
		return getJournalsByDiscipline(getMappedJournals());
	}

	/**
	 * This gives a mapping for all the disciplines used by unused journals. This
	 * should only return the NONE discipline.
	 * 
	 * @return
	 */
	public SortedMap<Discipline, SortedSet<Journal>> getUnmappedJournalsByDiscipline() {
		return getJournalsByDiscipline(getUnmappedJournals());
	}

	/**
	 * Given a set of journals, it will return a mapping from the discipline used
	 * by those journals to a list of the journals in that discipline.
	 * 
	 * @param journals
	 * @return
	 */
	public static SortedMap<Discipline, SortedSet<Journal>> getJournalsByDiscipline(
			Set<Journal> journals) {
		SortedMap<Discipline, SortedSet<Journal>> disciplinesByJournal = new TreeMap<Discipline, SortedSet<Journal>>();

		for (Journal journal : journals) {
			Discipline discipline = journal.getJournalDiscipline();
			SortedSet<Journal> journalsForDiscipline = disciplinesByJournal
					.get(discipline);

			if (journalsForDiscipline == null) {
				journalsForDiscipline = new TreeSet<Journal>();
				journalsForDiscipline.add(journal);
			} else {
				journalsForDiscipline.add(journal);
			}

			disciplinesByJournal.put(discipline, journalsForDiscipline);
		}
		return disciplinesByJournal;
	}

}
