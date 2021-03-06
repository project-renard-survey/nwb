package edu.iu.cns.database.merge.generic.detect.redundant_author;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.service.database.Database;
import org.cishell.utilities.DatabaseUtilities;

import prefuse.data.Table;
import prefuse.data.Tuple;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This class holds the information for a merge group. It is also integrated
 * with checking for duplicate authors within the same document. If this code is
 * to be reused, it should be changed so the merge checking happens only when
 * required.
 * 
 */
public class MergeGroup {
	private final int groupID;
	private final Collection<Integer> peoplePKs;
	private final Database database;	
	private final MergeGroupSettings mergeGroupSettings;
	
	public MergeGroup(int groupID, Collection<Integer> peoplePKs,
			Database database, final MergeGroupSettings mergeGroupSettings) {
		this.groupID = groupID;
		this.peoplePKs = peoplePKs;
		this.database = database;
		this.mergeGroupSettings = mergeGroupSettings;
	}
	
	/**
	 * Given a mergeTable and a database, this will return a list of
	 * <code>MergeGroup</code>s.
	 */
	public static List<MergeGroup> createMergeGroups(Table mergeTable,
			Database database, final MergeGroupSettings mergeGroupSettings) {
		Multimap<Integer, Integer> mergeGroupToPersonPK = mapGroupToPeople(
				mergeTable,
				mergeGroupSettings);

		List<MergeGroup> mergeGroups = new ArrayList<MergeGroup>(
				mergeGroupToPersonPK.size());

		for (Integer groupID : mergeGroupToPersonPK.keySet()) {
			Collection<Integer> personPKs = mergeGroupToPersonPK.get(groupID);
			mergeGroups.add(new MergeGroup(groupID.intValue(), personPKs,
					database, mergeGroupSettings));
		}

		return mergeGroups;
	}
	
	
	/**
	 * This will check a mergeTable using the database to determine if there are 
	 * authors who are in the same merge group but appeared on the same document.
	 * A {code}DuplicateAuthorException{code} error will be thrown if such a conflict is found.
	 * 
	 * @throws DuplicateAuthorException 
	 */
	public static void checkForAuthorDuplication(Collection<MergeGroup> mergeGroups)
			throws AlgorithmExecutionException, SQLException, DuplicateAuthorException {

		boolean hasAuthorDuplication = false;
		StringBuilder errorMessages = new StringBuilder();
		
		for (MergeGroup mergeGroup : mergeGroups) {
			try {
				mergeGroup.checkForAuthorDuplication();
			} catch (DuplicateAuthorException e) {
				hasAuthorDuplication = true;
				errorMessages.append(e.getMessage());
			}
		}	
			
		if (!hasAuthorDuplication) {
			return;
		}
		// SOMEDAY output a table with this info.
		String conflictMessage = "Merge Identical People is likely not suitable"
				+ " for the dataset.\n"
				+ "The following merge groups had authors that were"
				+ " deemed to be identical, but they occured on the same document.\n"
				+ "\tMergeGroup\tAuthor\tPK\tDocument\tPK\n"
				+ errorMessages; 
		throw new DuplicateAuthorException(conflictMessage);

	}

	private static Multimap<Integer, Integer> mapDuplicatePersonPKToDocumentPK(
			Collection<Integer> peoplePKs, Database database,
			final MergeGroupSettings mergeGroupSettings)
			throws AlgorithmExecutionException, SQLException {
			
		Connection connection = DatabaseUtilities.connect(database,
					"Connection to database failed.\n");

		try {
	
		 Statement statement = DatabaseUtilities.createStatement(connection,
					"Creating a statement failed.\n");
	// Incase you have trouble reading the crazy string formatting, here is what it was.
	//		String innerQuery = String.format(
	//				"SELECT APP.AUTHORS.AUTHORS_DOCUMENT_FK " + "\n"
	//				+ "FROM APP.AUTHORS " + "\n"
	//				+ "WHERE APP.AUTHORS.AUTHORS_PERSON_FK IN %s " + "\n"
	//				+ "GROUP BY APP.AUTHORS.AUTHORS_DOCUMENT_FK " + "\n"
	//				+ "HAVING COUNT(APP.AUTHORS.AUTHORS_PERSON_FK)>1",
	//				prepareCollectionForQuery(peoplePKs));
	
	//		String outterQuery = String.format(
	//				"SELECT APP.AUTHORS.AUTHORS_DOCUMENT_FK, APP.AUTHORS.AUTHORS_PERSON_FK " + "\n" 
	//				+ "FROM APP.AUTHORS " + "\n" 
	//				+ "WHERE APP.AUTHORS.AUTHORS_DOCUMENT_FK IN ( " + innerQuery + ") " + "\n" 
	//				+ "AND APP.AUTHORS.AUTHORS_PERSON_FK IN %s ", prepareCollectionForQuery(peoplePKs));
	
			String innerQuery = String.format("SELECT %s \n" 
					+ "FROM %s \n" 
					+ "WHERE %s IN %s \n" 
					+ "GROUP BY %s \n"
					+ "HAVING COUNT(%s)>1", 
					mergeGroupSettings.AUTHORS_DOCUMENT_FK,
					mergeGroupSettings.AUTHORS_TABLE,
					mergeGroupSettings.AUTHORS_PERSON_FK,
					formatCollectionAsSqlList(peoplePKs),
					mergeGroupSettings.AUTHORS_DOCUMENT_FK,
					mergeGroupSettings.AUTHORS_PERSON_FK
					);
			
			// SOMEDAY Combine this with the other createConflictMessage query.
			// SOMEDAY use string templating so this is easier to read.
			String outerQuery = String.format("SELECT %s, %s " + "\n" 
					+ "FROM %s " + "\n" 
					+ "WHERE %s IN (%s) " + "\n" 
					+ "AND %s IN %s ",
					mergeGroupSettings.AUTHORS_DOCUMENT_FK,
					mergeGroupSettings.AUTHORS_PERSON_FK,
					mergeGroupSettings.AUTHORS_TABLE,
					mergeGroupSettings.AUTHORS_DOCUMENT_FK,
					innerQuery,
					mergeGroupSettings.AUTHORS_PERSON_FK,
					formatCollectionAsSqlList(peoplePKs));
			
			statement.execute(outerQuery);
			
			ResultSet resultSet = statement.getResultSet();
			
			/*
			 * Only documents and authors that are false duplicates will be returned
			 * from the query.
			 */
			Multimap<Integer, Integer> authorDocumentPKs = ArrayListMultimap
					.create();
			
			while (resultSet.next()) {
				// Indices from the query.
				int documentPKIndex = 1;
				int personPKIndex = 2;
				Integer documentPK = Integer.valueOf(resultSet.getInt(documentPKIndex));
				Integer personPK = Integer.valueOf(resultSet.getInt(personPKIndex));
				authorDocumentPKs.put(personPK, documentPK);
			}
			
			statement.close();
			
			return authorDocumentPKs;

		} finally {
			connection.close();
		}

	}
	
	private void checkForAuthorDuplication() throws AlgorithmExecutionException,
			SQLException, DuplicateAuthorException {

		if (this.peoplePKs.size() <= 1) {
			return;
		}

		Multimap<Integer, Integer> personPKToDuplicateDocumentPK = mapDuplicatePersonPKToDocumentPK(
				this.peoplePKs, this.database, this.mergeGroupSettings);

		if (personPKToDuplicateDocumentPK.isEmpty()) {
			return;
		}
		
		/*
		 * There is author duplication.
		 */
		
		StringBuilder conflictMessage = new StringBuilder();
		
		Collection<Integer> authorPKs = personPKToDuplicateDocumentPK
				.keySet();
		Collection<Integer> documentPKs = personPKToDuplicateDocumentPK
				.values();

		Connection connection = DatabaseUtilities.connect(
				this.getDatabase(), "Connection to database failed.\n");

		try {
			Statement statement = DatabaseUtilities.createStatement(
					connection, "Creating a statement failed.\n");
			
//			String query = String.format(
//					"SELECT APP.PERSON.UNSPLIT_NAME, APP.DOCUMENT.ISI_UNIQUE_ARTICLE_IDENTIFIER, APP.PERSON.PK, APP.DOCUMENT.PK " + "\n" +
//					"FROM APP.PERSON " + "\n" + 
//					"INNER JOIN APP.AUTHORS ON APP.AUTHORS.AUTHORS_PERSON_FK=APP.PERSON.PK " + "\n" +
//					"INNER JOIN APP.DOCUMENT ON APP.DOCUMENT.PK=APP.AUTHORS.AUTHORS_DOCUMENT_FK  " + "\n" +
//					"WHERE APP.PERSON.PK IN %s " + "\n" +
//					"AND APP.DOCUMENT.PK IN %s",
//					prepareCollectionForQuery(authorPKs), prepareCollectionForQuery(documentPKs));

			// SOMEDAY Combine this with the getPersonPKToDuplicateDocumentPK query.
			// SOMEDAY use string templating so this is easier to read.
			String query = String.format(
					"SELECT %s, %s, %s, %s " + "\n"
					+ "FROM %s " + "\n"
					+ "INNER JOIN %s ON %s=%s " + "\n"
					+ "INNER JOIN %s ON %s=%s  " + "\n"
					+ "WHERE %s IN %s " + "\n"
					+ "AND %s IN %s",
					this.mergeGroupSettings.PERSON_ID,
					this.mergeGroupSettings.DOCUMENT_ID,
					this.mergeGroupSettings.PERSON_PK,
					this.mergeGroupSettings.DOCUMENT_PK,
					this.mergeGroupSettings.PERSON_TABLE,
					this.mergeGroupSettings.AUTHORS_TABLE,
					this.mergeGroupSettings.AUTHORS_PERSON_FK,
					this.mergeGroupSettings.PERSON_PK,
					this.mergeGroupSettings.DOCUMENT_TABLE,
					this.mergeGroupSettings.DOCUMENT_PK,
					this.mergeGroupSettings.AUTHORS_DOCUMENT_FK,
					this.mergeGroupSettings.PERSON_PK,
					formatCollectionAsSqlList(authorPKs),
					this.mergeGroupSettings.DOCUMENT_PK,
					formatCollectionAsSqlList(documentPKs));

			statement.execute(query);

			ResultSet resultSet = statement.getResultSet();

			while (resultSet.next()) {
				// These are the indexs as they appear in the results from the query.
				final int authorIDIndex = 1;
				final int documentIDIndex = 2;
				final int authorPKIndex = 3;
				final int documentPKIndex = 4;
				
				String authorID = resultSet.getString(authorIDIndex);
				String documentID = resultSet.getString(documentIDIndex);
				int authorPK = resultSet.getInt(authorPKIndex);
				int documentPK = resultSet.getInt(documentPKIndex);
				conflictMessage.append("\t" + this.getGroupID() + "\t"
						+ authorID + "\t" + authorPK + "\t" + documentID
						+ "\t" + documentPK + "\n");
			}

			statement.close();
		} finally {
			connection.close();
		}

		throw new DuplicateAuthorException(conflictMessage.toString());
	}
	

	private static Multimap<Integer, Integer> mapGroupToPeople(
			Table mergeTable, final MergeGroupSettings mergeGroupSettings) {
		Multimap<Integer, Integer> groupToAuthorPK = ArrayListMultimap.create();

		for (@SuppressWarnings("unchecked")
		Iterator<Tuple> rows = mergeTable.tuples(); rows.hasNext();) {
			Tuple row = rows.next();

			String rowGroup = row
					.getString(mergeGroupSettings.MERGE_TABLE_GROUP_COLUMN);
			String rowPK = row
					.getString(mergeGroupSettings.MERGE_TABLE_PERSON_PK_COLUMN);
			Integer group = Integer.valueOf(Integer.parseInt(rowGroup));
			Integer authorPK = Integer.valueOf(Integer.parseInt(rowPK));
			groupToAuthorPK.put(group, authorPK);
		}

		return groupToAuthorPK;
	}
	
	private static String formatCollectionAsSqlList(Collection<?> collection) {
		return "(" + Joiner.on(",").join(collection) + ")";
	}

	public static class DuplicateAuthorException extends Exception {

		private static final long serialVersionUID = -3326392156364646986L;

		public DuplicateAuthorException(String message) {
			super(message);
		}

	}
	
	public int getGroupID() {
		return this.groupID;
	}

	public Collection<Integer> getPeoplePKs() {
		return this.peoplePKs;
	}

	public Database getDatabase() {
		return this.database;
	}
}
