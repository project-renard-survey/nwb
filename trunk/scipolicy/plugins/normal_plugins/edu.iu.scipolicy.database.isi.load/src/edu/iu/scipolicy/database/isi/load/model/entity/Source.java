package edu.iu.scipolicy.database.isi.load.model.entity;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.cishell.utilities.StringUtilities;
import org.cishell.utilities.dictionary.DictionaryEntry;
import org.cishell.utilities.dictionary.DictionaryUtilities;

import edu.iu.cns.database.load.framework.DerbyFieldType;
import edu.iu.cns.database.load.framework.Entity;
import edu.iu.cns.database.load.framework.Schema;
import edu.iu.cns.database.load.framework.utilities.DatabaseTableKeyGenerator;
import edu.iu.nwb.shared.isiutil.database.ISI;

public class Source extends Entity<Source> {
	public final static Schema<Source> SCHEMA = new Schema<Source>(
		true,
		ISI.BOOK_SERIES_TITLE, DerbyFieldType.TEXT,
		ISI.BOOK_SERIES_SUBTITLE, DerbyFieldType.TEXT,
		ISI.CONFERENCE_HOST, DerbyFieldType.TEXT,
		ISI.CONFERENCE_LOCATION, DerbyFieldType.TEXT,
		ISI.CONFERENCE_SPONSORS, DerbyFieldType.TEXT,
		ISI.CONFERENCE_TITLE, DerbyFieldType.TEXT,
		ISI.FULL_TITLE, DerbyFieldType.TEXT,
		ISI.ISO_TITLE_ABBREVIATION, DerbyFieldType.TEXT,
		ISI.ISSN, DerbyFieldType.TEXT,
		ISI.PUBLICATION_TYPE, DerbyFieldType.TEXT,
		ISI.TWENTY_NINE_CHARACTER_SOURCE_TITLE_ABBREVIATION, DerbyFieldType.TEXT);

	private String bookSeriesTitle;
	private String bookSeriesSubtitle;
	private String conferenceHost;
	private String conferenceLocation;
	private String conferenceSponsors;
	private String conferenceTitle;
	private String fullTitle;
	private String isoTitleAbbreviation;
	private String issn;
	private String publicationType;
	private String twentyNineCharacterSourceTitleAbbreviation;

	public Source(
			DatabaseTableKeyGenerator keyGenerator,
			String bookSeriesTitle,
			String bookSeriesSubtitle,
			String conferenceHost,
			String conferenceLocation,
			String conferenceSponsors,
			String conferenceTitle,
			String fullTitle,
			String isoTitleAbbreviation,
			String issn,
			String publicationType,
			String twentyNineCharacterSourceTitleAbbreviation) {
		super(
			keyGenerator,
			createAttributes(
				bookSeriesTitle,
				bookSeriesSubtitle,
				conferenceHost,
				conferenceLocation,
				conferenceSponsors,
				conferenceTitle,
				fullTitle,
				isoTitleAbbreviation,
				issn,
				publicationType,
				twentyNineCharacterSourceTitleAbbreviation));
		/*if (getPrimaryKey() == 11) {
			throw new RuntimeException("meep");
		}*/
		this.bookSeriesTitle = bookSeriesTitle;
		this.bookSeriesSubtitle = bookSeriesSubtitle;
		this.conferenceHost = conferenceHost;
		this.conferenceLocation = conferenceLocation;
		this.conferenceSponsors = conferenceSponsors;
		this.conferenceTitle = conferenceTitle;
		this.fullTitle = fullTitle;
		this.isoTitleAbbreviation = isoTitleAbbreviation;
		this.issn = issn;
		this.publicationType = publicationType;
		this.twentyNineCharacterSourceTitleAbbreviation =
			twentyNineCharacterSourceTitleAbbreviation;
	}

	public String getBookSeriesTitle() {
		return this.bookSeriesTitle;
	}

	public String getBookSeriesSubtitle() {
		return this.bookSeriesSubtitle;
	}

	public String getConferenceHost() {
		return this.conferenceHost;
	}

	public String getConferenceLocation() {
		return this.conferenceLocation;
	}

	public String getConferenceSponsors() {
		return this.conferenceSponsors;
	}

	public String getConferenceTitle() {
		return this.conferenceTitle;
	}

	public String getFullTitle() {
		return this.fullTitle;
	}

	public String getISOTitleAbbreviation() {
		return this.isoTitleAbbreviation;
	}

	public String getISSN() {
		return this.issn;
	}

	public String getPublicationType() {
		return this.publicationType;
	}

	public String get29CharacterSourceTitleAbbreviation() {
		return this.twentyNineCharacterSourceTitleAbbreviation;
	}

	/*@Override
	public boolean shouldMerge(Source otherSource) {
		return (
			StringUtilities.areValidAndEqual(this.issn, otherSource.getISSN()) ||
			StringUtilities.areValidAndEqual(
				this.twentyNineCharacterSourceTitleAbbreviation,
				otherSource.get29CharacterSourceTitleAbbreviation()) ||
			StringUtilities.areValidAndEqual(this.fullTitle, otherSource.getFullTitle()));
	}*/

	@Override
	public List<Object> createMergeKey() {
		List<Object> mergeKey = new ArrayList<Object>();
		Integer primaryKey = getPrimaryKey();
		addStringOrAlternativeToMergeKey(mergeKey, this.bookSeriesTitle, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.bookSeriesSubtitle, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.conferenceHost, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.conferenceLocation, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.conferenceSponsors, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.conferenceTitle, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.fullTitle, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.isoTitleAbbreviation, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.issn, null);
		addStringOrAlternativeToMergeKey(mergeKey, this.publicationType, null);
		addStringOrAlternativeToMergeKey(
			mergeKey, this.twentyNineCharacterSourceTitleAbbreviation, primaryKey);

		return mergeKey;
	}

	@Override
	public void merge(Source otherSource) {
		this.bookSeriesTitle =
			StringUtilities.simpleMerge(this.bookSeriesTitle, otherSource.getBookSeriesTitle());
		this.bookSeriesSubtitle = StringUtilities.simpleMerge(
			this.bookSeriesSubtitle, otherSource.getBookSeriesSubtitle());
		this.conferenceHost = StringUtilities.simpleMerge(
			this.conferenceHost, otherSource.getConferenceHost());
		this.conferenceLocation = StringUtilities.simpleMerge(
			this.conferenceLocation, otherSource.getConferenceLocation());
		this.conferenceSponsors = StringUtilities.simpleMerge(
			this.conferenceSponsors, otherSource.getConferenceSponsors());
		this.conferenceTitle = StringUtilities.simpleMerge(
			this.conferenceTitle, otherSource.getConferenceTitle());
		this.fullTitle = StringUtilities.simpleMerge(this.fullTitle, otherSource.getFullTitle());
		this.isoTitleAbbreviation = StringUtilities.simpleMerge(
			this.isoTitleAbbreviation, otherSource.getISOTitleAbbreviation());	
		this.issn = StringUtilities.simpleMerge(this.issn, otherSource.getISSN());
		this.publicationType =
			StringUtilities.simpleMerge(this.publicationType, otherSource.getPublicationType());
		this.twentyNineCharacterSourceTitleAbbreviation = StringUtilities.simpleMerge(
			this.twentyNineCharacterSourceTitleAbbreviation,
			otherSource.get29CharacterSourceTitleAbbreviation());

		DictionaryUtilities.addIfNotNull(
			getAttributes(),
			new DictionaryEntry<String, Object>(ISI.BOOK_SERIES_TITLE, this.bookSeriesTitle),
			new DictionaryEntry<String, Object>(
				ISI.BOOK_SERIES_SUBTITLE, this.bookSeriesSubtitle),
			new DictionaryEntry<String, Object>(ISI.CONFERENCE_HOST, this.conferenceHost),
			new DictionaryEntry<String, Object>(
				ISI.CONFERENCE_LOCATION, this.conferenceLocation),
			new DictionaryEntry<String, Object>(
				ISI.CONFERENCE_SPONSORS, this.conferenceSponsors),
			new DictionaryEntry<String, Object>(ISI.CONFERENCE_TITLE, this.conferenceTitle),
			new DictionaryEntry<String, Object>(ISI.FULL_TITLE, this.fullTitle),
			new DictionaryEntry<String, Object>(
				ISI.ISO_TITLE_ABBREVIATION, this.isoTitleAbbreviation),
			new DictionaryEntry<String, Object>(ISI.ISSN, issn),
			new DictionaryEntry<String, Object>(ISI.PUBLICATION_TYPE, this.publicationType),
			new DictionaryEntry<String, Object>(
				ISI.TWENTY_NINE_CHARACTER_SOURCE_TITLE_ABBREVIATION,
				this.twentyNineCharacterSourceTitleAbbreviation));
	}

	public static Dictionary<String, Object> createAttributes(
			String bookSeriesTitle,
			String bookSeriesSubtitle,
			String conferenceHost,
			String conferenceLocation,
			String conferenceSponsors,
			String conferenceTitle,
			String fullTitle,
			String isoTitleAbbreviation,
			String issn,
			String publicationType,
			String twentyNineCharacterSourceTitleAbbreviation) {
		Dictionary<String, Object> attributes = new Hashtable<String, Object>();
		DictionaryUtilities.addIfNotNull(
			attributes,
			new DictionaryEntry<String, Object>(ISI.BOOK_SERIES_TITLE, bookSeriesTitle),
			new DictionaryEntry<String, Object>(
				ISI.BOOK_SERIES_SUBTITLE, bookSeriesSubtitle),
			new DictionaryEntry<String, Object>(ISI.CONFERENCE_HOST, conferenceHost),
			new DictionaryEntry<String, Object>(
				ISI.CONFERENCE_LOCATION, conferenceLocation),
			new DictionaryEntry<String, Object>(
				ISI.CONFERENCE_SPONSORS, conferenceSponsors),
			new DictionaryEntry<String, Object>(ISI.CONFERENCE_TITLE, conferenceTitle),
			new DictionaryEntry<String, Object>(ISI.FULL_TITLE, fullTitle),
			new DictionaryEntry<String, Object>(
				ISI.ISO_TITLE_ABBREVIATION, isoTitleAbbreviation),
			new DictionaryEntry<String, Object>(ISI.ISSN, issn),
			new DictionaryEntry<String, Object>(ISI.PUBLICATION_TYPE, publicationType),
			new DictionaryEntry<String, Object>(
				ISI.TWENTY_NINE_CHARACTER_SOURCE_TITLE_ABBREVIATION,
				twentyNineCharacterSourceTitleAbbreviation));

		return attributes;
	}
}