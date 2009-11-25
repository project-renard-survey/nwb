package edu.iu.epic.modelbuilder.gui;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import edu.umd.cs.piccolox.pswing.PComboBox;

public class InfectorComboBox extends PComboBox {

	private static final long serialVersionUID = -2697730594337025625L;

	public InfectorComboBox(final InfectorInformationPanel infectorInformationPanel, 
							String infectorCompartmentName) {
		
		super();
		
		InfectorComboBoxModel infectorComboBoxModel = 
			new InfectorComboBoxModel(CompartmentIDToLableMap.getCompartmentIDToLableMap());
		
		this.setModel(infectorComboBoxModel);
		
		if (infectorCompartmentName != null 
				&& !infectorCompartmentName.equalsIgnoreCase("")) {
			this.setSelectedItem(infectorCompartmentName);
		}
		
		
		this.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent e) {
				
				if (e.getStateChange() == ItemEvent.SELECTED) {
					infectorInformationPanel
						.handleInfectorComboBoxSelectedInfectorChangeEvent(e.getItem().toString());
				}
			}
		});
	}
	
	public String getSelectedCompartmentName() {
		return (String) this.getSelectedItem();
	}

}
