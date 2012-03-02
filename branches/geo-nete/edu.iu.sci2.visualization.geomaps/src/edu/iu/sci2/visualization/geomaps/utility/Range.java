package edu.iu.sci2.visualization.geomaps.utility;

import java.util.Arrays;
import java.util.NoSuchElementException;

import com.google.common.base.Objects;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Ordering;

/* TODO We may wish to replace this with com.google.common.collect.Range.
 * To do so, we will need to only parameterize on Comparable types.
 * To do that, we will need to stop talking of a Range<Color> and instead use some Comparable
 * proxy that is interpreted externally as a Color.
 */
/**
 * @param <T>	Not necessarily a Comparable type, but one such that between any two values there
 * 				is conceptually a continuum of intermediate values of the same type
 * 				(e.g. Double, Color).
 */
public class Range<T> {
	private final T pointA;
	private final T pointB;

	private Range(T pointA, T pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
	}
	public static <T> Range<T> between(T pointA, T pointB) {
		return new Range<T>(pointA, pointB);
	}
	/**
	 * @return	The min and max of values present in {@code iterable} according to its element
	 * 			type's natural comparator.
	 * @throw	{@link NoSuchElementException}	if {@code iterable} is empty
	 */
	public static <C extends Comparable<? super C>> Range<C> over(Iterable<? extends C> iterable) {
		return between(Ordering.natural().min(iterable), Ordering.natural().max(iterable));
	}
	public static <C extends Comparable<? super C>> Range<C> over(C first, C... rest) {
		return over(Arrays.asList(ObjectArrays.concat(first, rest)));
	}
	
	
	public T getPointA() {
		return pointA;
	}

	public T getPointB() {
		return pointB;
	}

	public boolean isEmpty() {
		return Objects.equal(pointA, pointB);
	}

	@Override
	public boolean equals(Object thatObject) {
		if (!(thatObject instanceof Range<?>)) {
			return false;
		}		
		Range<?> that = (Range<?>) thatObject;
		
		return Objects.equal(this.pointA, that.pointA) &&
			   Objects.equal(this.pointB, that.pointB);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(pointA, pointB);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("pointA", pointA)
				.add("pointB", pointB)
				.toString();
	}
}
