package edu.iu.scipolicy.visualization.horizontalbargraph;

public final class HeaderAndFooterPositioningData {
//	public static final double X_POSITION = 0.5;
	public static final double X_POSITION = 36.0;

//	public static final double HEADER_SPACING_DIFFERENCE = 0.0136;
//	public static final double DATE_TIME_Y = 0.9727;
//	public static final double INPUT_DATA_Y =
//		DATE_TIME_Y - HEADER_SPACING_DIFFERENCE;
//	public static final double INPUT_DATA_Y = 0.9591;
//
//	public static final double DATASET_NAME_Y = 0.9273;
//
//	public static final double SUBHEADER_SPACING_DIFFERENCE = 0.01275;
//	public static final double LABEL_COLUMN_Y = 0.9;
//	public static final double START_DATE_COLUMN_Y =
//		LABEL_COLUMN_Y - SUBHEADER_SPACING_DIFFERENCE;
//	public static final double END_DATE_COLUMN_Y =
//		START_DATE_COLUMN_Y - SUBHEADER_SPACING_DIFFERENCE;
//	public static final double SIZE_BY_COLUMN_Y =
//		END_DATE_COLUMN_Y - SUBHEADER_SPACING_DIFFERENCE;
//	public static final double DATE_FORMAT_Y =
//		SIZE_BY_COLUMN_Y - SUBHEADER_SPACING_DIFFERENCE;
//	public static final double YEAR_LABEL_FONT_SIZE_Y =
//		DATE_FORMAT_Y - SUBHEADER_SPACING_DIFFERENCE;
//	public static final double BAR_LABEL_FONT_SIZE_Y =
//		YEAR_LABEL_FONT_SIZE_Y - SUBHEADER_SPACING_DIFFERENCE;
//
//	public static final double FOOTER_Y = 0.0318;

	public static final double HEADER_SPACING_DIFFERENCE = 0.1496;
	public static final double DATE_TIME_Y = 0.3003;
	public static final double INPUT_DATA_Y = DATE_TIME_Y + HEADER_SPACING_DIFFERENCE;

	public static final double DATASET_NAME_Y = 0.7997;

	public static final double SUBHEADER_SPACING_DIFFERENCE = 0.14025;
	public static final double LABEL_COLUMN_Y = 1.1;
	public static final double START_DATE_COLUMN_Y = LABEL_COLUMN_Y + SUBHEADER_SPACING_DIFFERENCE;
	public static final double END_DATE_COLUMN_Y =
		START_DATE_COLUMN_Y + SUBHEADER_SPACING_DIFFERENCE;
	public static final double SIZE_BY_COLUMN_Y = END_DATE_COLUMN_Y + SUBHEADER_SPACING_DIFFERENCE;
	public static final double DATE_FORMAT_Y = SIZE_BY_COLUMN_Y + SUBHEADER_SPACING_DIFFERENCE;
	public static final double YEAR_LABEL_FONT_SIZE_Y =
		DATE_FORMAT_Y + SUBHEADER_SPACING_DIFFERENCE;
	public static final double BAR_LABEL_FONT_SIZE_Y =
		YEAR_LABEL_FONT_SIZE_Y + SUBHEADER_SPACING_DIFFERENCE;

	public static final double FOOTER_Y = 0.3498;
}