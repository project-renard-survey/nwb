package edu.iu.nwb.analysis.extractnetfromtable.components;

import java.util.TreeSet;
import java.util.Vector;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

public class EdgeContainer {
	private static void createEdge(Vector edgeVector, Graph graph, Table table, int rowNumber,
			AggregateFunctionMappings afm) {
		if (!graph.isDirected()) {
			Integer src = new Integer(((Node) edgeVector.get(0)).getRow());
			Integer tgt = new Integer(((Node) edgeVector.get(1)).getRow());
			TreeSet edgeSet = new TreeSet();
			edgeSet.add(src);
			edgeSet.add(tgt);
			edgeVector.set(0, graph.getNode(((Integer) edgeSet.first()).intValue()));
			edgeVector.set(1, graph.getNode(((Integer) edgeSet.last()).intValue()));
		}

		Node source = (Node) edgeVector.get(0);
		Node target = (Node) edgeVector.get(1);

		final Edge edge = graph.addEdge(source, target);

		ValueAttributes va = new ValueAttributes(edge.getRow());
		va = FunctionContainer.mutateFunctions(edge, table, rowNumber, va, afm,
				AggregateFunctionMappings.SOURCE_AND_TARGET);
		afm.addFunctionRow(edgeVector, va);
	}

	protected static void mutateEdge(Node source, Node target, Graph graph, Table table,
			int rowNumber, AggregateFunctionMappings afm) {
		if (source == null || target == null)
			return;
		final Vector edgeVector = new Vector(2);
		if (!graph.isDirected()) {
			Integer src = new Integer(source.getRow());
			Integer tgt = new Integer(target.getRow());
			TreeSet edgeSet = new TreeSet();
			edgeSet.add(src);
			edgeSet.add(tgt);
			edgeVector.add(graph.getNode(((Integer) edgeSet.first()).intValue()));
			edgeVector.add(graph.getNode(((Integer) edgeSet.last()).intValue()));
		} else {
			edgeVector.add(source);
			edgeVector.add(target);
		}

		ValueAttributes va = afm.getFunctionRow(edgeVector);

		// If we don't find a ValueAttributes object, we haven't seen this edge
		// before; create a new one.
		if (va == null) {
			createEdge(edgeVector, graph, table, rowNumber, afm);
		} else {
			int edgeNumber = va.getRowNumber();
			FunctionContainer.mutateFunctions(graph.getEdge(edgeNumber), table, rowNumber, va, afm,
					AggregateFunctionMappings.SOURCE_AND_TARGET);
		}
	}

}
