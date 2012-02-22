package edu.iu.sci2.visualization.geomaps;

import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.List;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmCreationFailedException;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.algorithm.ParameterMutator;
import org.cishell.framework.data.Data;
import org.cishell.utilities.ColumnNotFoundException;
import org.cishell.utilities.TableUtilities;
import org.cishell.utilities.mutateParameter.dropdown.DropdownMutator;
import org.osgi.service.log.LogService;
import org.osgi.service.metatype.ObjectClassDefinition;

import prefuse.data.Table;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;

import edu.iu.sci2.visualization.geomaps.metatype.GeoCoordinateParameterFinder;
import edu.iu.sci2.visualization.geomaps.metatype.Shapefiles;
import edu.iu.sci2.visualization.geomaps.viz.CircleAnnotationMode;
import edu.iu.sci2.visualization.geomaps.viz.CircleDimension;
import edu.iu.sci2.visualization.geomaps.viz.VizDimension;

public class GeoMapsCirclesFactory implements AlgorithmFactory, ParameterMutator {
	public static final String SUBTITLE = "Circles";

	public Algorithm createAlgorithm(
			Data[] data, Dictionary<String, Object> parameters,	CIShellContext ciShellContext) {
		String latitudeColumnName = (String) parameters.get(GeoMapsNetworkFactory.Parameter.LATITUDE.id());
		String longitudeColumnName = (String) parameters.get(GeoMapsNetworkFactory.Parameter.LONGITUDE.id());
		
		return new GeoMapsAlgorithm<Coordinate, CircleDimension>(
				data,
				parameters,
				new CircleAnnotationMode(longitudeColumnName, latitudeColumnName),
				"Geo Maps (Circles)",
				(LogService) ciShellContext.getService(LogService.class.getName()));
	}

	public ObjectClassDefinition mutateParameters(Data[] data, ObjectClassDefinition oldParameters) {
		Table table = (Table) data[0].getData();
		
		List<String> numericColumnNames = Collections.emptyList();
		try {
			numericColumnNames =
					ImmutableList.copyOf(TableUtilities.getValidNumberColumnNamesInTable(table));
		} catch (ColumnNotFoundException e) {
			String message =
				"Table does not seem to have any purely numeric columns.  "
				+ "If your table does not have columns for the latitudes and longitudes of records,"
				+ " you may wish to use one of the geocoders under Analysis > Geospatial.";
			throw new AlgorithmCreationFailedException(message, e);
		}
		
		DropdownMutator mutator = new DropdownMutator();
		
		Shapefiles.addShapefileAndProjectionParameters(mutator);
	
		GeoCoordinateParameterFinder.addLatitudeParameter(mutator, numericColumnNames, GeoMapsNetworkFactory.Parameter.LATITUDE.id());
		GeoCoordinateParameterFinder.addLongitudeParameter(mutator, numericColumnNames, GeoMapsNetworkFactory.Parameter.LONGITUDE.id());
		
		for (VizDimension dimension : EnumSet.allOf(CircleDimension.class)) {
			dimension.addOptionsToAlgorithmParameters(mutator, numericColumnNames);
		}

		return mutator.mutate(oldParameters);
	}
}