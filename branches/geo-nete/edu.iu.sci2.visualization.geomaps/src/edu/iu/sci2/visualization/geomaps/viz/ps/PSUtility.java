package edu.iu.sci2.visualization.geomaps.viz.ps;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class PSUtility {
	public static final ImmutableMap<Integer, String> PS_FONT_NAME_SUFFIX_FOR_FONT_STYLE = ImmutableMap.of(
			Font.BOLD, "-Bold",
			Font.ITALIC, "-Italic");
	
	private PSUtility() {}

	/**
	 * Generates a {@code setrgbcolor} command for {@code color}.
	 */
	public static String makeSetRGBColorCommand(Color color) {
		float[] colorComponents = new float[3];
		color.getColorComponents(colorComponents);
		float red = colorComponents[0];
		float green = colorComponents[1];
		float blue = colorComponents[2];
		
		return String.format("%f %f %f setrgbcolor ", red, green, blue);
	}
	
	public static String showAndNewLine(String text, double fontSize) {
		return String.format("(%s) dup show stringwidth pop neg %f rmoveto \n", text, -fontSize);
	}

	/**
	 * Replaces each backslash with two backslashes.
	 */
	public static String escapeForPostScript(String string) {
		return string.replace("\\", "\\\\");
	}

	public static String findscalesetfont(Font font) {
		String name = font.getName();
		int style = font.getStyle();
		if (PS_FONT_NAME_SUFFIX_FOR_FONT_STYLE.containsKey(style)) {
			name += PS_FONT_NAME_SUFFIX_FOR_FONT_STYLE.get(style);
		}
			
		return String.format("/%s findfont %d scalefont setfont ", name, font.getSize());
	}

	public static String setgray(double brightness) {
		return String.format("%f setgray ", brightness);
	}
	
	public static String path(List<? extends Point2D.Double> points) {
		if (points.isEmpty()) {
			return " "; // TODO ?
		} else {
			return path(
					Iterables.getFirst(points, null),
					Iterables.toArray(Iterables.skip(points, 1), Point2D.Double.class));
		}
	}
	
	public static String closedPath(List<? extends Point2D.Double> points) {
		return path(points) + " closepath ";
	}
	
	public static String path(Point2D.Double first, Point2D.Double... rest) {
		StringBuilder builder = new StringBuilder();
		builder.append(" newpath ");
		builder.append(String.format("%s moveto ", xy(first)));
		
		for (Point2D.Double point : rest) {
			builder.append(String.format("%s lineto ", xy(point)));
		}
		
		return builder.toString();		
	}
	
	public static String closedPath(Point2D.Double first, Point2D.Double... rest) {
		return path(first, rest) + " closepath ";
	}
	
	public static String xy(Point2D.Double point) {
		return String.format("%f %f ", point.x, point.y);
	}
}
