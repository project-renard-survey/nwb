package edu.iu.epic.modeling.compartment.converter.in;

import java.util.Dictionary;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmFactory;
import org.cishell.framework.data.Data;
import org.osgi.service.log.LogService;

public class ModelReaderAlgorithmFactory implements AlgorithmFactory {
	@SuppressWarnings("unchecked") // Raw Dictionary
	public Algorithm createAlgorithm(Data[] data, Dictionary parameters, CIShellContext context) {
		LogService logger = (LogService) context.getService(LogService.class.getName());

		return new ModelReaderAlgorithm(data, logger);
	}
}
