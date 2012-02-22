package edu.iu.sci2.visualization.geomaps.viz.ps;

import java.util.Collection;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import edu.iu.sci2.visualization.geomaps.geo.projection.GeometryProjector;
import edu.iu.sci2.visualization.geomaps.geo.projection.GeometryProjector.GeometryProjectorException;
import edu.iu.sci2.visualization.geomaps.geo.projection.KnownProjectedCRSDescriptor;
import edu.iu.sci2.visualization.geomaps.geo.shapefiles.Shapefile;
import edu.iu.sci2.visualization.geomaps.viz.Circle;
import edu.iu.sci2.visualization.geomaps.viz.FeatureView;
import edu.iu.sci2.visualization.geomaps.viz.legend.LegendComposite;

public class GeoMap {
	private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory(new Hints(Hints.CRS, DefaultGeographicCRS.WGS84));
	
	private final String subtitle;
	private final Shapefile shapefile;
	private final KnownProjectedCRSDescriptor knownProjectedCRSDescriptor;
	private final Collection<FeatureView> featureViews;
	private final Collection<Circle> circles;
	
	private final LegendComposite legendComposite;
	private final GeometryFactory geometryFactory;
	private final GeometryProjector geometryProjector;

	public GeoMap(
			String subtitle,
			Shapefile shapefile,
			KnownProjectedCRSDescriptor knownProjectedCRSDescriptor,
			Collection<FeatureView> featureViews,
			Collection<Circle> circles,
			Collection<PostScriptable> legends) throws GeoMapException {
		this.subtitle = subtitle;
		this.shapefile = shapefile;
		this.knownProjectedCRSDescriptor = knownProjectedCRSDescriptor;
		this.featureViews = featureViews;
		this.circles = circles;
		
		this.legendComposite = new LegendComposite();
		this.legendComposite.addAll(legends);
		
		this.geometryFactory = DEFAULT_GEOMETRY_FACTORY;
		
		try {
			this.geometryProjector = new GeometryProjector(shapefile.detectCRS(), knownProjectedCRSDescriptor);
		} catch (GeometryProjectorException e) {
			throw new GeoMapException("TODO", e);
		}
	}

	public GeometryProjector getGeometryProjector() {
		return geometryProjector;
	}

	public LegendComposite getLegendComposite() {
		return legendComposite;
	}

	public Shapefile getShapefile() {
		return shapefile;
	}

	public KnownProjectedCRSDescriptor getKnownProjectedCRSDescriptor() {
		return knownProjectedCRSDescriptor;
	}

	public Collection<FeatureView> getFeatureViews() {
		return featureViews;
	}

	public Collection<Circle> getCircles() {
		return circles;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public Coordinate project(Coordinate coordinate) {
		Geometry coordinatePointGeometry = geometryFactory.createPoint(coordinate);
		
		try {
			return project(coordinatePointGeometry).getCoordinate();
		} catch (TransformException e) {
			// I think this should only happen for particularly weird input coordinates
			// http://docs.geotools.org/latest/javadocs/org/opengis/referencing/operation/TransformException.html
			return null;
		}
	}

	Geometry project(Geometry geometry) throws TransformException {
		return getGeometryProjector().projectGeometry(geometry);
		
	}
}