package edu.iu.sci2.database.scholarly.model.entity;

import java.util.Dictionary;

import edu.iu.cns.database.load.framework.DBField;
import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.Entity;
import edu.iu.cns.database.load.framework.Schema;
import edu.iu.cns.database.load.framework.utilities.DatabaseTableKeyGenerator;
import edu.iu.nwb.shared.isiutil.database.ISI;

public class Document extends Entity<Document> {
	public static enum Field implements DBField {
		ABSTRACT_TEXT(DerbyFieldType.TEXT),
		ARTICLE_NUMBER(DerbyFieldType.TEXT),
		BEGINNING_PAGE(DerbyFieldType.INTEGER),
		CITED_REFERENCE_COUNT(DerbyFieldType.INTEGER),
		CITED_YEAR(DerbyFieldType.INTEGER),
		DIGITAL_OBJECT_IDENTIFIER(DerbyFieldType.TEXT),
		DOCUMENT_SOURCE_FK(DerbyFieldType.FOREIGN_KEY), // FK
		DOCUMENT_TYPE(DerbyFieldType.TEXT),
		DOCUMENT_VOLUME(DerbyFieldType.INTEGER),
		ENDING_PAGE(DerbyFieldType.INTEGER),
		FIRST_AUTHOR_FK(DerbyFieldType.FOREIGN_KEY),    // FK
		FUNDING_AGENCY_AND_GRANT_NUMBER(DerbyFieldType.TEXT),
		FUNDING_TEXT(DerbyFieldType.TEXT),
		ISBN(DerbyFieldType.TEXT),
		ISI_DOCUMENT_DELIVERY_NUMBER(DerbyFieldType.TEXT),
		ISI_UNIQUE_ARTICLE_IDENTIFIER(DerbyFieldType.TEXT),
		ISSUE(DerbyFieldType.TEXT),
		LANGUAGE(DerbyFieldType.TEXT),
		PAGE_COUNT(DerbyFieldType.INTEGER),
		PART_NUMBER(DerbyFieldType.TEXT),
		PUBLICATION_DATE(DerbyFieldType.TEXT),
		PUBLICATION_YEAR(DerbyFieldType.INTEGER),
		SPECIAL_ISSUE(DerbyFieldType.TEXT),
		SUBJECT_CATEGORY(DerbyFieldType.TEXT),
		SUPPLEMENT(DerbyFieldType.TEXT),
		TIMES_CITED(DerbyFieldType.INTEGER),
		TITLE(DerbyFieldType.TEXT);
		
		private final DerbyFieldType fieldType;

		private Field(DerbyFieldType type) {
			this.fieldType = type;
		}

		public DerbyFieldType type() {
			return this.fieldType;
		}
	}

	
	public static final Schema<Document> SCHEMA = new Schema<Document>(
			true, // autogen primary key
			Field.values())
			.FOREIGN_KEYS(
					Field.DOCUMENT_SOURCE_FK.toString(), ISI.SOURCE_TABLE_NAME,
					Field.FIRST_AUTHOR_FK.toString(), ISI.PERSON_TABLE_NAME);

	public Document(DatabaseTableKeyGenerator keyGenerator,
			Dictionary<String, Object> attributes) {
		super(keyGenerator, attributes);
	}

	@Override
	public Object createMergeKey() {
		return getAttributes().get("PK");
	}

	@Override
	public void merge(Document otherItem) {
	}

}
