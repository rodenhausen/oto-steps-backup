package edu.arizona.biosemantics.oto2.steps.client.info;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabPanel;

public class TermInfoView extends TabPanel {
	
	private DictionaryView dictionaryView;
	private ContextView contextView;
	//private OntologiesView ontologiesView;

	public TermInfoView(final EventBus eventBus) {
		super(GWT.<TabPanelAppearance> create(TabPanelBottomAppearance.class));
		dictionaryView = new DictionaryView(eventBus);
		contextView = new ContextView(eventBus);
		//ontologiesView = new OntologiesView(eventBus);
		
		add(contextView, "Context");
		//add(ontologiesView, "Ontologies");
		add(dictionaryView, "Dictionary");
		
		this.addBeforeSelectionHandler(new BeforeSelectionHandler<Widget>() {
			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Widget> event) {
				/*if(event.getItem().equals(ontologiesView) && 
						(ontologiesView.getSelectedOntologies() == null ||
						ontologiesView.getSelectedOntologies().isEmpty())) {
					Dialog dialog = new SelectOntologiesDialog(eventBus);
					dialog.show();
					Alerter.alertNoOntoloygySelected();/
					//event.cancel();
				}*/
			}
		});
	}
}