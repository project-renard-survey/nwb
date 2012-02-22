package edu.iu.sci2.visualization.geomaps.viz.ps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.utilities.FileUtilities;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.operation.TransformException;
import org.osgi.service.log.LogService;

import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.iu.sci2.visualization.geomaps.GeoMapsAlgorithm;
import edu.iu.sci2.visualization.geomaps.geo.projection.GeometryProjector;
import edu.iu.sci2.visualization.geomaps.geo.projection.GeometryProjector.GeometryProjectorException;
import edu.iu.sci2.visualization.geomaps.geo.projection.KnownProjectedCRSDescriptor;
import edu.iu.sci2.visualization.geomaps.geo.shapefiles.Shapefile;
import edu.iu.sci2.visualization.geomaps.viz.Circle;
import edu.iu.sci2.visualization.geomaps.viz.Constants;
import edu.iu.sci2.visualization.geomaps.viz.FeatureView;
import edu.iu.sci2.visualization.geomaps.viz.legend.LegendComposite;

public class PSWriter {
	/* Percentage of the data range to add to each side of the map as a buffer.
	 * Between 0 and 1.
	 */
	public static final double MAP_BOUNDING_BOX_BUFFER_RATIO = 0.1;

	public static final String OUTPUT_FILE_EXTENSION = "ps";
	
	public static final String TITLE = "Geo Map";
	public static final String INDENT = "  ";
	
	
	private final GeometryProjector geometryProjector;
	private final MapDisplayer mapDisplayer;
	private final double pageHeightInPoints;
	private final LegendComposite legendComposite = new LegendComposite();
	private Shapefile shapefile;
	private KnownProjectedCRSDescriptor knownProjectedCRSDescriptor;
	private final GeometryFactory defaultGeometryFactory = JTSFactoryFinder.getGeometryFactory(
			new Hints(Hints.CRS, DefaultGeographicCRS.WGS84));
	
	private Collection<FeatureView> featureViews = ImmutableSet.of(); // TODO try to generalize to "hooks", and hook into a feature-drawn event
	private Collection<Circle> circles = new ArrayList<Circle>(); // TODO try to generalize to "overlays"
	private String subtitle = "";



	public PSWriter(
			Shapefile shapefile,
			KnownProjectedCRSDescriptor knownProjectedCRSDescriptor,
			String subtitle,
			Collection<Circle> circles,
			Collection<FeatureView> featureViews,
			Collection<PostScriptable> legends)
				throws ShapefilePostScriptWriterException {
		try {			
			this.shapefile = shapefile;
			this.knownProjectedCRSDescriptor = knownProjectedCRSDescriptor;
			this.subtitle = subtitle;
			this.featureViews = featureViews;
			this.circles = circles;
			
			legendComposite.addAll(legends);
			
			this.geometryProjector = new GeometryProjector(shapefile.detectCRS(), knownProjectedCRSDescriptor);
			this.mapDisplayer = calculateMapBoundingBox();
			
			this.pageHeightInPoints =
				Constants.calculatePageHeightInPoints(mapDisplayer.getMapHeightInPoints());
			
			
		} catch (TransformException e) {
			throw new ShapefilePostScriptWriterException(e);
		} catch (GeometryProjectorException e) {
			throw new ShapefilePostScriptWriterException(e);
		}
	}

	public File writePostScriptToFile(String authorName, String dataLabel)
				throws IOException, AlgorithmExecutionException, TransformException {		
		File psFile =
			FileUtilities.createTemporaryFileInDefaultTemporaryDirectory("geoMaps", OUTPUT_FILE_EXTENSION);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(psFile));

		writeCodeHeader(out, psFile.getName());
		
		out.write(GeoMapsAlgorithm.group.getInstanceOf("utilityDefinitions").toString());
		out.write("\n");
		
		out.write((new PageHeader(dataLabel, pageHeightInPoints)).toPostScript() + "\n");
		out.write((new PageFooter()).toPostScript() + "\n");
		
		out.write("% Save the default clipping path so we can clip the map safely" + "\n");
		out.write("gsave" + "\n");
		out.write("\n");

		out.write(mapDisplayer.toPostScript());
		out.write("\n");
		
		FeaturePrinter featurePrinter =
			new FeaturePrinter(
					shapefile.viewOfFeatureCollection(),
					geometryProjector,
					mapDisplayer,
					shapefile.featureAttributeName());
		featurePrinter.printFeatures(out, featureViews);

		out.write(GeoMapsAlgorithm.group.getInstanceOf("circlePrinterDefinitions").toString());
		
		
		out.write("% Circle annotations" + "\n");		
		out.write("gsave" + "\n");
		out.write("\n");
		
		double circleLineWidth = Circle.DEFAULT_CIRCLE_LINE_WIDTH;
		out.write(INDENT + circleLineWidth + " setlinewidth" + "\n");
		out.write("\n");

		for (Circle circle : circles) {
			out.write(circle.toPostScript(geometryProjector, mapDisplayer));
		}

		out.write("grestore" + "\n");
		out.write("\n");
		
		
		out.write("% Restore the default clipping path" + "\n");
		out.write("grestore" + "\n");
		out.write("\n");
		
		
		PageMetadata pageMetadata = new PageMetadata(TITLE, subtitle);
		pageMetadata.add(knownProjectedCRSDescriptor.displayName() + " Projection");
		pageMetadata.add(timestamp());
		pageMetadata.add(authorName);
		out.write(pageMetadata.toPostScript());
		out.write("\n");
		
		out.write(legendComposite.toPostScript());
		out.write("\n");
		
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
	public Coordinate coordinateToPagePosition(Coordinate coordinate) { // TODO what the hell is up with the exception handling here
		Geometry coordinatePointGeometry = defaultGeometryFactory.createPoint(coordinate);
		
		Coordinate intermediateCoord;
		try {
			intermediateCoord = geometryProjector.projectGeometry(coordinatePointGeometry).getCoordinate();
		} catch (TransformException e) {
			// I think this should only happen for particularly weird input coordinates
			// http://docs.geotools.org/latest/javadocs/org/opengis/referencing/operation/TransformException.html
			return null;
		}
		
		if (intermediateCoord != null) {
			return mapDisplayer.getDisplayCoordinate(intermediateCoord);
		} else {
			// can happen if the point would not be displayed (so the Geometry becomes empty)
			return null;
		}
	}
	

	private MapDisplayer calculateMapBoundingBox() throws TransformException {
		/* Identify extreme values for the X and Y dimensions
		 * among the Geometries in our featureCollection.
		 * Note that this is *after* Geometry preparation (cropping and projecting).
		 */
		double dataMinX = Double.POSITIVE_INFINITY;
		double dataMinY = Double.POSITIVE_INFINITY;
		double dataMaxX = Double.NEGATIVE_INFINITY;
		double dataMaxY = Double.NEGATIVE_INFINITY;

		FeatureIterator<SimpleFeature> it = shapefile.viewOfFeatureCollection().features();
		while (it.hasNext()) {
			SimpleFeature feature = it.next();
			Geometry rawGeometry = (Geometry) feature.getDefaultGeometry();
			Geometry geometry = rawGeometry;
			try {
				geometry = geometryProjector.projectGeometry(rawGeometry);
			} catch (IllegalArgumentException e) {
				// TODO !
				System.out.println("iae for feature " + feature.getAttribute("NAME"));
				System.out.println(e.getMessage());
				continue;
			}

			for (int gg = 0; gg < geometry.getNumGeometries(); gg++) {
				Geometry subgeometry = geometry.getGeometryN(gg);

				Coordinate[] coordinates = subgeometry.getCoordinates();

				for (Coordinate coordinate : coordinates) {
					dataMinX = Math.min(coordinate.x, dataMinX);
					dataMinY = Math.min(coordinate.y, dataMinY);
					dataMaxX = Math.max(coordinate.x, dataMaxX);
					dataMaxY = Math.max(coordinate.y, dataMaxY);
				}
			}
		}
		it.close();
		
		// Exaggerate the data range a bit to provide a buffer around it in the map.
		double xRange = dataMaxX - dataMinX;
		double xBufferSize = MAP_BOUNDING_BOX_BUFFER_RATIO * xRange;
		double bufferedDataMinX = dataMinX - xBufferSize;
		double bufferedDataMaxX = dataMaxX + xBufferSize;
		
		double yRange = dataMaxY - dataMinY;
		double yBufferSize = MAP_BOUNDING_BOX_BUFFER_RATIO * yRange;
		double bufferedDataMinY = dataMinY - yBufferSize;
		double bufferedDataMaxY = dataMaxY + yBufferSize;

		return new MapDisplayer(
				bufferedDataMinX,
				bufferedDataMinY,
				bufferedDataMaxX,
				bufferedDataMaxY);
	}

	private void writeCodeHeader(
			BufferedWriter out, String outputPSFileName) throws IOException {
		GeoMapsAlgorithm.logger.log(LogService.LOG_INFO, "Printing PostScript.." + "\n");

		out.write((new DSCProlog(outputPSFileName, pageHeightInPoints)).toPostScript());
		
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