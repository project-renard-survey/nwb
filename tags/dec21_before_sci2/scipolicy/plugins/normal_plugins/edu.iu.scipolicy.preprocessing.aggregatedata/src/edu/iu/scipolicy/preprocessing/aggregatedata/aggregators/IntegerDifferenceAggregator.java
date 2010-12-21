package edu.iu.scipolicy.preprocessing.aggregatedata.aggregators;

import java.util.List;

import edu.iu.scipolicy.preprocessing.aggregatedata.SingleFunctionAggregator;

public class IntegerDifferenceAggregator implements SingleFunctionAggregator<Integer> {

	public Integer aggregateValue(List<Integer> objectsToAggregate) {
		Integer currentDifferenceValue = new Integer(0);
		
		for (Integer currentValue : objectsToAggregate) {
			currentDifferenceValue = currentValue.intValue()
											- currentDifferenceValue.intValue();
		}
		
		return currentDifferenceValue.intValue();
	}

}
