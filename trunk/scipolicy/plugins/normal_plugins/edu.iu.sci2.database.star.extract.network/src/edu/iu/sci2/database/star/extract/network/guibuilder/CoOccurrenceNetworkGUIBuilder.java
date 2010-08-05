package edu.iu.sci2.database.star.extract.network.guibuilder;

import java.util.Collection;
import java.util.Map;

import org.cishell.utility.datastructure.ObjectContainer;
import org.cishell.utility.swt.GUIBuilderUtilities;
import org.cishell.utility.swt.GUICanceledException;
import org.cishell.utility.swt.SWTUtilities;
import org.cishell.utility.swt.model.GUIModel;
import org.cishell.utility.swt.model.GUIModelField;
import org.cishell.utility.swt.model.datasynchronizer.DropDownDataSynchronizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.iu.sci2.database.star.extract.network.StarDatabaseDescriptor;
import edu.iu.sci2.database.star.extract.network.guibuilder.attribute.AttributeListWidget;

public class CoOccurrenceNetworkGUIBuilder extends GUIBuilder {
	public static final String INSTRUCTIONS_LABEL_TEXT =
		"Choose the leaf entity table to extract your co-occurrence network from.\n" +
		"Then, setup any node and edge attributes you want on your resulting network.\n" +
		"For more information see the Sci2 tutorial at: ";
	public static final String TUTORIAL_URL =
		"https://nwb.slis.indiana.edu/community/" +
		"?n=Sci2Algorithm.GenericCSVExtractCoOccurrenceNetwork";
	public static final String TUTORIAL_DISPLAY_URL = "Sci2 Tutorial";

	public static final String LEAF_FIELD_LABEL =
		"Choose the Leaf column to extract the co-occurrence network on: ";
	public static final String HEADER_GROUP_TEXT = "";

	public static final String LEAF_FIELD_NAME = "leafEntity";

	public GUIModel createGUI(
			String windowTitle,
			int windowWidth,
			int windowHeight,
			StarDatabaseDescriptor databaseDescriptor) throws GUICanceledException {
		// TODO: Verify that databaseDescriptor is valid for us.

		GUIModel model = new GUIModel();

		/* Create the GUI shell, and set up its basic structure (header, node aggregates,
		 *  edge aggregates).
		 */

		Display display = GUIBuilderUtilities.createDisplay();
		Shell shell = GUIBuilderUtilities.createShell(
			display, windowTitle, windowWidth, windowHeight, 1, false);
		Group instructionsGroup = createInstructionsGroup(shell);
		Group headerGroup = createHeaderGroup(shell);
		Group nodeAggregatesGroup = createAggregatesGroup(shell, NODE_ATTRIBUTES_GROUP_TEXT);
		Group edgeAggregatesGroup = createAggregatesGroup(shell, EDGE_ATTRIBUTES_GROUP_TEXT);
		Group footerGroup = createFooterGroup(shell);

    	createInstructionsLabel(instructionsGroup);

		// Create and setup the Leaf selection field.

		createLeafSelectionField(headerGroup, databaseDescriptor, model);

		// Create the widget that allows users to specify node and edge aggregate fields.

		AttributeListWidget nodeAggregatesTable = createAggregateWidget(
			model,
			NODE_ATTRIBUTE_FUNCTION_GROUP_NAME,
			NODE_CORE_ENTITY_COLUMN_GROUP_NAME,
			databaseDescriptor.getCoreTableDescriptor().getColumnNames(),
			databaseDescriptor.getCoreTableDescriptor().getColumnNamesByLabels(),
			NODE_RESULT_NAME_GROUP_NAME,
			NODE_TYPE,
			nodeAggregatesGroup);
		AttributeListWidget edgeAggregatesTable = createAggregateWidget(
			model,
			EDGE_ATTRIBUTE_FUNCTION_GROUP_NAME,
			EDGE_CORE_ENTITY_COLUMN_GROUP_NAME,
			databaseDescriptor.getCoreTableDescriptor().getColumnNames(),
			databaseDescriptor.getCoreTableDescriptor().getColumnNamesByLabels(),
			EDGE_RESULT_NAME_GROUP_NAME,
			EDGE_TYPE,
			edgeAggregatesGroup);

		// Create the finished button so the user can actually execute the resulting queries.

		final ObjectContainer<Boolean> userFinished = new ObjectContainer<Boolean>(false);
		Button finishedButton = createFinishedButton(footerGroup, 1, userFinished);
		shell.setDefaultButton(finishedButton);

		// Fill the aggregate widgets with some aggregate fields by default (for the user's ease).

		for (int ii = 0; ii < DEFAULT_AGGREGATE_WIDGET_COUNT; ii++) {
			nodeAggregatesTable.addComponent(SWT.NONE, null);
			edgeAggregatesTable.addComponent(SWT.NONE, null);
		}

		// Set the GUI up to be cancelable.

		final ObjectContainer<GUICanceledException> exceptionThrown =
			new ObjectContainer<GUICanceledException>();
		GUIBuilderUtilities.setCancelable(shell, exceptionThrown);

		// Run the GUI and return the model with the data that the user entered.

		runGUI(display, shell, windowHeight);

		if ((userFinished.object == false) && (exceptionThrown.object != null)) {
    		throw new GUICanceledException(
    			exceptionThrown.object.getMessage(), exceptionThrown.object);
    	}

		return model;
	}

	private static StyledText createInstructionsLabel(Composite parent) {
		StyledText instructionsLabel =
			new StyledText(parent, SWT.LEFT | SWT.READ_ONLY | SWT.WRAP);
		instructionsLabel.setBackground(
			parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		instructionsLabel.setLayoutData(createInstructionsLabelLayoutData());
		instructionsLabel.getCaret().setVisible(false);

		SWTUtilities.styledPrint(
			instructionsLabel,
			INSTRUCTIONS_LABEL_TEXT,
			parent.getDisplay().getSystemColor(SWT.COLOR_BLACK),
			SWT.NORMAL);
		SWTUtilities.printURL(
			parent,
			instructionsLabel,
			TUTORIAL_URL,
			TUTORIAL_DISPLAY_URL,
			parent.getDisplay().getSystemColor(SWT.COLOR_BLUE),
			SWT.BOLD);

		return instructionsLabel;
	}

	private static GridData createInstructionsLabelLayoutData() {
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		layoutData.horizontalSpan = 2;

		return layoutData;
	}

	private static GUIModelField<String, Combo, DropDownDataSynchronizer> createLeafSelectionField(
			Composite parent, StarDatabaseDescriptor databaseDescriptor, GUIModel model) {
		Label label = new Label(parent, SWT.READ_ONLY);
		label.setLayoutData(createLeafSelectionFieldLabelLayoutData());
		label.setText(LEAF_FIELD_LABEL);

		Collection<String> columnNames = databaseDescriptor.getLeafTableNames();
		Map<String, String> columnOptions = databaseDescriptor.getTableNameOptionsWithoutCore();
		GUIModelField<String, Combo, DropDownDataSynchronizer> leafField =
			model.addDropDown(
			HEADER_GROUP_NAME,
			LEAF_FIELD_NAME,
			0,
			columnNames,
			columnOptions,
			parent,
			SWT.BORDER | SWT.READ_ONLY);
		leafField.getWidget().setLayoutData(createLeafSelectionFieldLayoutData());

		return leafField;
	}

	private static GridData createLeafSelectionFieldLabelLayoutData() {
		GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);

		return layoutData;
	}

	private static GridData createLeafSelectionFieldLayoutData() {
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);

		return layoutData;
	}
}