package edu.arizona.biosemantics.oto2.steps.client.toontology;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

public class ToOntologyView implements IsWidget {

	private TermsView termsView;
	//private OntologiesView ontologiesView;
	private CreateSubmissionView submissionView;
	private SubmissionsView ontologiesView;
	private BorderLayoutContainer borderLayoutContainer;

	public ToOntologyView(EventBus eventBus) {
		termsView = new TermsView(eventBus);
		ontologiesView = new SubmissionsView(eventBus);
		submissionView = new CreateSubmissionView(eventBus);
			
		borderLayoutContainer = new BorderLayoutContainer();
		ContentPanel cp = new ContentPanel();
		cp.setHeadingText("Candidate Terms");
		cp.add(termsView);
		BorderLayoutData d = new BorderLayoutData(0.15);
		d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		borderLayoutContainer.setWestWidget(cp, d);

		cp = new ContentPanel();
		cp.setHeadingText("Ontology Submissions");
		cp.add(ontologiesView);
		d = new BorderLayoutData();
		d.setMargins(new Margins(0, 0, 0, 0));
		borderLayoutContainer.setCenterWidget(cp, d);
		
		cp = new ContentPanel();
		cp.setHeadingText("Create Ontology Submission");
		cp.add(submissionView);
		d = new BorderLayoutData(0.30);
		d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		borderLayoutContainer.setEastWidget(cp, d);
	}

	@Override
	public Widget asWidget() {
		return borderLayoutContainer;
	}
	
}
