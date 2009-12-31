package edu.iu.scipolicy.loader.isi.db.utilities.parser;

import org.cishell.utilities.StringUtilities;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.fail;

import edu.iu.cns.database.loader.framework.utilities.DatabaseTableKeyGenerator;
import edu.iu.scipolicy.loader.isi.db.model.entity.Source;

public class ReferenceDataParserTest {
	public static final String ZERO_REFERENCE_TOKENS_STRING = "";
	public static final String ONE_REFERENCE_TOKEN_STRING = "token1";
	public static final String SIX_REFERENCE_TOKENS_STRING =
		"token1,token2,token3,token4,token5,token6";

	public static final String NO_ANNOTATION = "";
	public static final String NO_SOURCE = "";

	public static final int YEAR = 1984;
	public static final String YEAR_STRING = "" + YEAR;
	public static final String SOURCE_STRING = "SCI SCI CITATION IND";
	public static final String PERSON_STRING = "SMITH J";
	public static final int VOLUME_NUMBER = 100;
	public static final String VOLUME_STRING = "V" + VOLUME_NUMBER;
	public static final int PAGE_NUMBER = 200;
	public static final String PAGE_NUMBER_STRING = "P" + PAGE_NUMBER;
	public static final String SOME_OTHER_NUMBER_STRING = "S" + PAGE_NUMBER;

	public static final String NO_POST_STRING = "";

	private DatabaseTableKeyGenerator personKeyGenerator;
	private DatabaseTableKeyGenerator sourceKeyGenerator;

	@Before
	public void setUp() throws Exception {
		this.personKeyGenerator = new DatabaseTableKeyGenerator();
		this.sourceKeyGenerator = new DatabaseTableKeyGenerator();
	}

	@After
	public void tearDown() throws Exception {
	}

	protected void testSourceWithAnnotation(
			String rawString,
			String sourceString,
			String providedAnnotation,
			boolean exceptionFails) throws Exception {
		ReferenceDataParser result = runTest(rawString, exceptionFails);

		Source source = result.getSource();
		String abbreviation = source.get29CharacterSourceTitleAbbreviation();
		String annotation = result.getAnnotation();

		if (!abbreviation.equals(sourceString)) {
			String failMessage =
				"Source was not parsed properly." +
				"\n\tParsed abbreviation: \"" + abbreviation + "\"" +
				"\n\tProvided source string: \"" + sourceString + "\"";
			fail(failMessage);
		}

		if (!StringUtilities.isEmptyOrWhiteSpace(annotation) &&
				!annotation.equalsIgnoreCase(providedAnnotation)) {
			String failMessage =
				"Source prefix was not parsed properly." +
				"\n\tParsed annotation: \"" + annotation + "\"" +
				"\n\tProvided annotation: \"" + providedAnnotation + "\"";
			fail(failMessage);
		}
	}

	protected void testSourceAsAnnotations(String rawPre, String rawPost, boolean exceptionFails)
			throws Exception {
		testSourceWithAnnotations(rawPre, rawPost, NO_SOURCE, exceptionFails);
	}

	protected void testSourceWithAnnotations(
			String rawPre, String rawPost, String sourceString, boolean exceptionFails)
			throws Exception {
		String processedPre = processPre(rawPre);
		String processedPost = processPost(rawPost);

		for (String annotation : ReferenceDataParser.SOURCE_ANNOTATIONS) {
			String rawString = processedPre + annotation + " " + sourceString + processedPost;
			testSourceWithAnnotation(rawString, sourceString, annotation, exceptionFails);
		}
	}

	protected ReferenceDataParser runTest(String rawString, boolean exceptionFails)
			throws Exception {
		try {
			return new ReferenceDataParser(
				this.personKeyGenerator, this.sourceKeyGenerator, rawString);
		} catch (ReferenceParsingException e) {
			if (exceptionFails) {
				fail("No exception should be thrown: " + e.getMessage());
			}

			throw e;
		}
	}

	private String processPre(String rawPre) {
		if (!StringUtilities.isEmptyOrWhiteSpace(rawPre)) {
			return rawPre + " ";
		} else {
			return "";
		}
	}

	private String processPost(String rawPost) {
		if (!StringUtilities.isEmptyOrWhiteSpace(rawPost)) {
			return rawPost;
		} else {
			return "";
		}
	}
}