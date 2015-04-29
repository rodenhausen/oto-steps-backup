package edu.arizona.biosemantics.oto2.steps.client.toontology;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelAppearance;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelBottomAppearance;

public class CreateSubmissionView implements IsWidget {

	private SubmitClassView submitClassView;
	private SubmitSynonymView submitSynonymView;
	private TabPanel tabPanel;

	public CreateSubmissionView(EventBus eventBus) {
		//tabPanel = new TabPanel(GWT.<TabPanelAppearance> create(TabPanelBottomAppearance.class));
		tabPanel = new TabPanel();
		
		submitClassView = new SubmitClassView(eventBus);
		submitSynonymView = new SubmitSynonymView(eventBus);
		
		tabPanel.add(submitClassView, "Class");
		tabPanel.add(submitSynonymView, "Synonym");
	}

	@Override
	public Widget asWidget() {
		return tabPanel;
	}

}
