package edu.iu.sci2.visualization.geomaps.viz.legend;

import java.awt.geom.Point2D;

import org.antlr.stringtemplate.StringTemplate;

import edu.iu.sci2.visualization.geomaps.GeoMapsAlgorithm;
import edu.iu.sci2.visualization.geomaps.viz.Circle;
import edu.iu.sci2.visualization.geomaps.viz.legend.numberformat.NumberFormatFactory;
import edu.iu.sci2.visualization.geomaps.viz.legend.numberformat.UnsignedZeroFormat;
import edu.iu.sci2.visualization.geomaps.viz.ps.GeoMapViewPS;
import edu.iu.sci2.visualization.geomaps.viz.ps.PostScriptable;

/* Create PostScript to draw three Circles representing the extrema (minimum,
 * midpoint, and maximum) of the interpolated range and label each with the
 * respective value in the raw range (which is intended to be the respective
 * value before scalingLabel and before interpolation).
 * 
 * This legend component has two captions:
 * - legendDescription would be like "Circle Area"
 * - columnName would be like "Number of papers published"
 */
public class LabeledReferenceCircles implements PostScriptable {
	// Brightness for PostScript's setgray command. 0 is black, 1 is white.
	public static final double CIRCLE_BRIGHTNESS = 0.5;
	
	public static final double EXTREMA_LABEL_BRIGHTNESS = 0.0;
	public static final double EXTREMA_LABEL_FONT_SIZE = 8;
	public static final double TYPE_LABEL_BRIGHTNESS = 0.0;
	public static final double TYPE_LABEL_FONT_SIZE = 10;
	public static final double SCALING_LABEL_BRIGHTNESS = 0.25;
	public static final double KEY_LABEL_BRIGHTNESS = 0.5;
	public static final double KEY_LABEL_FONT_SIZE = 8;

	private AreaLegend areaLegend;
	private final Point2D.Double keyTextLowerLeft;
	
	private boolean hasPrintedDefinitions;


	public LabeledReferenceCircles(
			AreaLegend areaLegend, Point2D.Double keyTextLowerLeft) {
		this.areaLegend = areaLegend;
		this.keyTextLowerLeft = keyTextLowerLeft;
		
		this.hasPrintedDefinitions = false;
	}

	
	/* TODO? Draw circles using the line width set
	 * in CirclePrinter.CIRCLE_LINE_WIDTH?
	 */
	@Override
	public String toPostScript() {
		String s = "";
		
		if (!hasPrintedDefinitions) {
			StringTemplate definitionsTemplate =
				GeoMapsAlgorithm.TEMPLATE_GROUP.getInstanceOf(
						"circleAreaLegendDefinitions");
			
			s += definitionsTemplate.toString();
			
			this.hasPrintedDefinitions = true;
		}
		
		StringTemplate invocationTemplate =
			GeoMapsAlgorithm.TEMPLATE_GROUP.getInstanceOf("circleAreaLegend");
		
		invocationTemplate.setAttribute("x", keyTextLowerLeft.x);
		invocationTemplate.setAttribute("y", keyTextLowerLeft.y);
		
		invocationTemplate.setAttribute(
				"minRadius",
				Circle.calculateRadiusFromArea(areaLegend.getVizRange().getPointA()));
		invocationTemplate.setAttribute(
				"midRadius",
				Circle.calculateRadiusFromArea(areaLegend.getMidpointArea()));
		invocationTemplate.setAttribute(
				"maxRadius",
				Circle.calculateRadiusFromArea(areaLegend.getVizRange().getPointB()));
		
		invocationTemplate.setAttribute("circleBrightness", CIRCLE_BRIGHTNESS);
		
		UnsignedZeroFormat doubleFormatter =
			NumberFormatFactory.getNumberFormat(
					areaLegend.numericFormatType(),
					areaLegend.getDataRange().getPointA(), areaLegend.getDataValueForOutputMidpoint(), areaLegend.getDataRange().getPointB());
		invocationTemplate.setAttribute(
				"minLabel", doubleFormatter.format(areaLegend.getDataRange().getPointA()));
		invocationTemplate.setAttribute(
				"midLabel", doubleFormatter.format(areaLegend.getDataValueForOutputMidpoint()));
		invocationTemplate.setAttribute(
				"maxLabel", doubleFormatter.format(areaLegend.getDataRange().getPointB()));

		invocationTemplate.setAttribute(
				"extremaLabelBrightness", EXTREMA_LABEL_BRIGHTNESS);
		invocationTemplate.setAttribute(
				"extremaLabelFontSize", EXTREMA_LABEL_FONT_SIZE);
		
		invocationTemplate.setAttribute("typeLabel", areaLegend.legendDescription());
		invocationTemplate.setAttribute(
				"typeLabelBrightness", TYPE_LABEL_BRIGHTNESS);
		invocationTemplate.setAttribute(
				"typeLabelFontSize", TYPE_LABEL_FONT_SIZE);
		
		invocationTemplate.setAttribute(
				"scalingLabel", "(" + areaLegend.scalingLabel() + ")");
		invocationTemplate.setAttribute(
				"scalingLabelBrightness", SCALING_LABEL_BRIGHTNESS);
		
		invocationTemplate.setAttribute("keyLabel", areaLegend.columnName());
		invocationTemplate.setAttribute(
				"keyLabelBrightness", KEY_LABEL_BRIGHTNESS);
		invocationTemplate.setAttribute(
				"keyLabelFontSize", KEY_LABEL_FONT_SIZE);
		
		invocationTemplate.setAttribute("fontName", GeoMapViewPS.CONTENT_FONT.getName());

		s += invocationTemplate.toString();

		return s;
	}
}
