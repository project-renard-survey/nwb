package edu.iu.sci2.visualization.geomaps.data.interpolation;
import java.awt.Color;

import edu.iu.sci2.visualization.geomaps.utility.ColorTuples;
import edu.iu.sci2.visualization.geomaps.utility.Range;

/**
 * Wrapper for a 3-dimensional InterpolatorND that interprets its 3-tuples as RGB coordinates
 * for a Color.
 */
public class ColorInterpolator implements Interpolator<Color> {	
	private final InterpolatorND interpolator3D;

	private ColorInterpolator(Range<Double> inRange, Range<Color> outRange) {
		final double[] outMinTuple = ColorTuples.asTuple(outRange.getPointA());
		final double[] outMaxTuple = ColorTuples.asTuple(outRange.getPointB());
		
		this.interpolator3D = InterpolatorND.between(
				inRange, Range.between(outMinTuple, outMaxTuple));
	}
	public static ColorInterpolator between(Range<Double> inRange, Range<Color> outRange) {
		return new ColorInterpolator(inRange, outRange);
	}

	
	public Color apply(Double value) {
		return ColorTuples.asColor(interpolator3D.apply(value));
	}

	public Range<Double> inRange() {
		return interpolator3D.inRange();
	}

	public Range<Color> outRange() {
		return Range.between(
				ColorTuples.asColor(interpolator3D.outRange().getPointA()),
				ColorTuples.asColor(interpolator3D.outRange().getPointB()));
	}
}
