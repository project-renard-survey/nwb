package edu.iu.sci2.visualization.geomaps.viz.ps;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.cishell.utilities.FileUtilities;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;
import org.osgi.service.log.LogService;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import edu.iu.sci2.visualization.geomaps.GeoMapsAlgorithm;
import edu.iu.sci2.visualization.geomaps.viz.Circle;
import edu.iu.sci2.visualization.geomaps.viz.PageLayout;
import edu.iu.sci2.visualization.geomaps.viz.model.GeoMap;

public class GeoMapViewPS {
	// TODO Fonts seem to be missing/ignored?  Getting monospace fonts right now
	public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);
	public static final Font CONTENT_FONT = new Font("Arial", Font.PLAIN, 10);

	/* Percentage of the data range to add to each side of the map as a buffer.
	 * Between 0 and 1.
	 */
	public static final double MAP_BOUNDING_BOX_BUFFER_RATIO = 0.1;

	public static final String OUTPUT_FILE_EXTENSION = "ps";
	
	public static final String TITLE = "Geospatial Map";
	public static final String INDENT = "  ";
	
	private final GeoMap geoMap;
	private final PageLayout pageLayout;
	private final GeoMapViewPageArea geoMapViewPageArea;

	public GeoMapViewPS(GeoMap geoMap, PageLayout pageLayout) throws ShapefilePostScriptWriterException {
		try {
			this.geoMap = geoMap;
			this.pageLayout = pageLayout;
			this.geoMapViewPageArea = new GeoMapViewPageArea(calculateMapBoundingRectangle(), pageLayout);
		} catch (TransformException e) {
			throw new ShapefilePostScriptWriterException(e);
		}
	}
	
	
	/**
	 * Looks for a font that will work on this system.  It tries several that are likely to
	 * be present on a Windows or Linux system, and falls back to Java's default font.
	 * @return
	 */
	private static Font findBasicFont() { // TODO Copied from bipartitenet.  Instead expose and import.
		final String JAVA_FALLBACK_FONT = "Dialog";
		ImmutableList<String> fontFamiliesToTry =
				ImmutableList.of("Arial", "Helvetica", "FreeSans", "Nimbus Sans");
		
		Font thisFont = new Font(JAVA_FALLBACK_FONT, Font.PLAIN, 12);
		for (String family : fontFamiliesToTry) {
			thisFont = new Font(family, Font.PLAIN, 12);
			if (! thisFont.getFamily().equals(JAVA_FALLBACK_FONT)) {
				// found one that the system has!
				break;
			}
		}
		
		return thisFont;
	}
	
	public File writeToPSFile(String authorName, String dataLabel)
				throws IOException, TransformException {		
		File psFile =
			FileUtilities.createTemporaryFileInDefaultTemporaryDirectory("geoMaps", OUTPUT_FILE_EXTENSION);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(psFile));

		writeCodeHeader(out, psFile.getName());
		
		out.write(GeoMapsAlgorithm.TEMPLATE_GROUP.getInstanceOf("utilityDefinitions").toString());
		out.write("\n");
		
		out.write((new PageFooter()).toPostScript() + "\n");
		
		out.write("% Save the default clipping path so we can clip the map safely" + "\n");
		out.write("gsave" + "\n");
		out.write("\n");

		out.write(geoMapViewPageArea.toPostScript());
		out.write("\n");
		
		FeaturePrinter featurePrinter =
			new FeaturePrinter(
					geoMap.getShapefile(),
					geoMap.getShapefile().viewOfFeatureCollection(),
					geoMap.getGeometryProjector(),
					geoMapViewPageArea,
					geoMap.getShapefile().getFeatureAttributeName());
		featurePrinter.printFeatures(out, geoMap.getFeatureViews());

		out.write(GeoMapsAlgorithm.TEMPLATE_GROUP.getInstanceOf("circlePrinterDefinitions").toString());
		
		
		out.write("% Circle annotations" + "\n");		
		out.write("gsave" + "\n");
		out.write("\n");
		
		double circleLineWidth = Circle.DEFAULT_CIRCLE_LINE_WIDTH;
		out.write(INDENT + circleLineWidth + " setlinewidth" + "\n");
		out.write("\n");

		for (Circle circle : geoMap.getCircles()) {
			out.write(circle.toPostScript(geoMap.getGeometryProjector(), geoMapViewPageArea));
		}

		out.write("grestore" + "\n");
		out.write("\n");
		
		
		out.write("% Restore the default clipping path" + "\n");
		out.write("grestore" + "\n");
		out.write("\n");
		
		
		if (pageLayout.headerLowerLeft().isPresent()) {
			PageHeader pageHeader = new PageHeader(TITLE, geoMap.getSubtitle(), pageLayout.headerLowerLeft().get(),
					String.format("Generated from %s", PSUtility.escapeForPostScript(dataLabel)),
					String.format("%s Projection", geoMap.getKnownProjectedCRSDescriptor().getNiceName()),
					timestamp(),
					authorName);
			out.write(pageHeader.toPostScript());
			out.write("\n");
		}
		
		out.write(geoMap.getLegendarium().toPostScript());
		out.write("\n");
		
		if (pageLayout.howToReadLowerLeft().isPresent()) {
			out.write(new HowToRead(pageLayout.howToReadLowerLeft().get()).toPostScript());
		}
		
		out.write("showpage" + "\n");

		out.close();

		GeoMapsAlgorithm.logger.log(LogService.LOG_INFO, "Done.");
		
		return psFile;
	}
	
	private static String timestamp() {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy | hh:mm:ss aa");
	    return sdf.format(cal.getTime());
	}

	/**
	 * Given a latitude and longitude in a {@link Coordinate} object, projects it onto the
	 * current map as well as possible.
	 */
	public Point2D.Double coordinateToPagePoint(Coordinate coordinate) { // TODO what the hell is up with the exception handling here
		Coordinate intermediateCoord = geoMap.project(coordinate);
		
		if (intermediateCoord != null) {
			return geoMapViewPageArea.displayPointFor(intermediateCoord);
		} else {
			// can happen if the point would not be displayed (so the Geometry becomes empty)
			return null;
		}
	}
	
	/**
	 * Identify extreme values for the X and Y dimensions among the projected features from our featureCollection.
	 * Note that this is <em>after</em> Geometry preparation (cropping and projecting).
	 */	
	private Rectangle2D.Double calculateMapBoundingRectangle() throws TransformException {	
		Rectangle2D.Double rectangle = null;

		FeatureIterator<SimpleFeature> it = geoMap.getShapefile().viewOfFeatureCollection().features();
		while (it.hasNext()) {
			SimpleFeature feature = it.next();
			Geometry geometry;
			try {				
				geometry = geoMap.project(geoMap.getShapefile().inset(geoMap.getShapefile().extractFeatureName(feature), (Geometry) feature.getDefaultGeometry()));
			} catch (IllegalArgumentException e) {
				// TODO !
				System.err.println("IllegalArgumentException for feature " + geoMap.getShapefile().extractFeatureName(feature));
				System.err.println(e.getMessage());
				continue;
			}

			for (int gg = 0; gg < geometry.getNumGeometries(); gg++) {
				Geometry subgeometry = geometry.getGeometryN(gg);

				Coordinate[] coordinates = subgeometry.getCoordinates();

				for (Coordinate coordinate : coordinates) {
					Point2D.Double point = new Point2D.Double(coordinate.x, coordinate.y);
					
					if (rectangle == null) {
						rectangle = new Rectangle2D.Double(point.x, point.y, 0, 0);
					}
					
					rectangle.add(point);
				}
			}
		}
		it.close();
		
		Rectangle2D.Double bufferedRectangle = addSmallBufferAround(rectangle);

		return bufferedRectangle;
	}

	private static Rectangle2D.Double addSmallBufferAround(Rectangle2D.Double rectangle) {
		return rectangle; // TODO actually add the buffer ... or don't, the map looks pretty good without it
	}


	private static void writeCodeHeader(
			BufferedWriter out, String outputPSFileName) throws IOException {
		GeoMapsAlgorithm.logger.log(LogService.LOG_INFO, "Printing PostScript.." + "\n");

		out.write((new DSCProlog(outputPSFileName, PageLayout.pageHeight())).toPostScript());
		
//		/* TODO We're using setpagedevice to force page dimensions
//		 * corresponding to US Letter landscape.  This command
//		 * is forbidden in Encapsulated PostScript, so if we
//		 * wish to maintain that format, we'll need a different
//		 * method to specify landscape-style dimensions (that popular
//		 * PostScript-to-PDF renderers will all respect).
//		 */
//		out.write("/setpagedevice where" + "\n"
//				+ "{ pop 1 dict" + "\n"
//				+ "dup /PageSize [ "
//					+ Constants.PAGE_WIDTH_IN_POINTS + " "
//					+ pageHeightInPoints + " "
//				+ "] put" + "\n"
//				+ "setpagedevice" + "\n"
//				+ "} if" + "\n");
//		out.write("\n");
	}

	public static class ShapefilePostScriptWriterException extends Exception {
		private static final long serialVersionUID = -4207770884445237065L;
	
		public ShapefilePostScriptWriterException(String message, Throwable cause) {
			super(message, cause);
		}

		public ShapefilePostScriptWriterException(Throwable cause) {
			super(cause);
		}
	}
}