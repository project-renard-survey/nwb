package edu.iu.sci2.database.star.extract.network.query.aggregateorganizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cishell.utility.datastructure.datamodel.field.DataModelField;
import org.cishell.utility.datastructure.datamodel.group.DataModelGroup;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import edu.iu.sci2.database.star.common.parameter.ColumnDescriptor;
import edu.iu.sci2.database.star.extract.common.aggregate.LeafAggregate;
import edu.iu.sci2.database.star.extract.network.aggregate.MirroredJoinAggregate;

public class MirroredJoinAggregateOrganizer implements AggregateOrganizer<MirroredJoinAggregate> {
	private Collection<ColumnDescriptor> nonAggregatedColumns = new ArrayList<ColumnDescriptor>();
	private List<MirroredJoinAggregate> coreAggregates = new ArrayList<MirroredJoinAggregate>();
	private List<LeafAggregate> leafTableAggregates = new ArrayList<LeafAggregate>();
	private String nodeLeafTableName;
	private String coreTableName;

	public MirroredJoinAggregateOrganizer(String nodeLeafTableName, String coreTableName) {
		this.nodeLeafTableName = nodeLeafTableName;
		this.coreTableName = coreTableName;
	}

	public void organizeAggregates(
			String coreTableName,
			String objectType,
			DataModelGroup aggregateFunctionGroup,
			DataModelGroup coreEntityColumnGroup,
			DataModelGroup attributeNameGroup,
			Map<String, ColumnDescriptor> columnDescriptors) {
		Map<String, ColumnDescriptor> workingNonAggregatedColumns =
			new HashMap<String, ColumnDescriptor>();
		workingNonAggregatedColumns.putAll(columnDescriptors);

		for (DataModelField<?> aggregateFunctionField : aggregateFunctionGroup.getFields()) {
			String id = aggregateFunctionField.getName();
			String columnName = (String) coreEntityColumnGroup.getField(id).getValue();
			String aggregateFunctionName = (String) aggregateFunctionField.getValue();
			String attributeName = (String) attributeNameGroup.getField(id).getValue();
			ColumnDescriptor columnUsed = columnDescriptors.get(columnName);

			if (columnUsed.isCoreColumn()) {
				this.coreAggregates.add(new MirroredJoinAggregate(
					objectType + "_" + attributeName,
					aggregateFunctionName,
					columnName,
					columnDescriptors,
					this.nodeLeafTableName,
					this.coreTableName));
				workingNonAggregatedColumns.remove(columnName);
			} else {
				this.leafTableAggregates.add(new LeafAggregate(
					attributeName,
					aggregateFunctionName,
					columnName,
					columnDescriptors,
					columnUsed.getNameForDatabase(),
					coreTableName));
			}
		}

		this.nonAggregatedColumns.addAll(Collections2.filter(
			workingNonAggregatedColumns.values(), new Predicate<ColumnDescriptor>() {
				public boolean apply(ColumnDescriptor columnDescriptor) {
					return columnDescriptor.isCoreColumn();
				}
			}));
	}

	public Collection<ColumnDescriptor> getResultingNonAggregatedColumns() {
		return this.nonAggregatedColumns;
	}

	public Collection<MirroredJoinAggregate> getResultingCoreAggregates() {
		return this.coreAggregates;
	}

	public Collection<LeafAggregate> getResultingLeafTableAggregates() {
		return this.leafTableAggregates;
	}
}