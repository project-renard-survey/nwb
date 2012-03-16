package edu.iu.sci2.visualization.geomaps.viz.ps;

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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import edu.iu.sci2.visualization.geomaps.GeoMapsAlgorithm;
import edu.iu.sci2.visualization.geomaps.utility.Dimension;
import edu.iu.sci2.visualization.geomaps.viz.Circle;
import edu.iu.sci2.visualization.geomaps.viz.PageLayout;
import edu.iu.sci2.visualization.geomaps.viz.model.GeoMap;

public class GeoMapViewPS {
	/* Percentage of the data range to add to each side of the map as a buffer.
	 * Between 0 and 1.
	 */
	public static final double MAP_BOUNDING_BOX_BUFFER_RATIO = 0.1;
	public static final String INDENT = "  ";
	
	private final GeoMap geoMap;
	private final PageLayout pageLayout;
	private final String howToReadText;
	private final GeoMapViewPageArea geoMapViewPageArea;

	public GeoMapViewPS(GeoMap geoMap, PageLayout pageLayout, String howToReadText) throws ShapefilePostScriptWriterException {
		try {
			this.geoMap = geoMap;
			this.pageLayout = pageLayout;
			this.howToReadText = howToReadText;
			
			this.geoMapViewPageArea = new GeoMapViewPageArea(calculateMapBoundingRectangle(), pageLayout);
		} catch (TransformException e) {
			throw new ShapefilePostScriptWriterException(e);
		}
	}

	
	public File writeToPSFile(String dataLabel)
				throws IOException, TransformException {		
		File psFile =
			FileUtilities.createTemporaryFileInDefaultTemporaryDirectory("geoMaps", GeoMapsAlgorithm.OUTPUT_FILE_EXTENSION);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(psFile));

		writeCodeHeader(out, psFile.getName(), pageLayout);
		
		out.write(GeoMapsAlgorithm.TEMPLATE_GROUP.getInstanceOf("utilityDefinitions").toString());
		out.write("\n");
		
		out.write((new PageFooter(new Point2D.Double(
				pageLayout.pageWidth() / 2.0,
				0.75 * pageLayout.pageMargin()),
				pageLayout)).toPostScript() + "\n");
		
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
			PageHeader pageHeader = new PageHeader(geoMap.getTitle(), pageLayout.headerLowerLeft().get(), pageLayout,
					String.format("Generated from %s", PSUtility.escapeForPostScript(dataLabel)),
					timestamp());
			out.write(pageHeader.toPostScript());
			out.write("\n");
		}
		
		out.write(geoMap.getLegendarium().toPostScript());
		out.write("\n");
		
		if (pageLayout.howToReadLowerLeft().isPresent()) {
			out.write(new HowToRead(
					pageLayout.howToReadLowerLeft().get(),
					pageLayout,
					howToReadText)
			.toPostScript());
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
			BufferedWriter out, String outputPSFileName, PageLayout pageLayout) throws IOException {
		GeoMapsAlgorithm.logger.log(LogService.LOG_INFO, "Printing PostScript.." + "\n");

		// TODO Replace pageWidth and pageHeight with pageDimensions
		out.write((new DSCProlog(outputPSFileName, Dimension.ofSize(pageLayout.pageWidth(), pageLayout.pageHeight())).toPostScript()));
		
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