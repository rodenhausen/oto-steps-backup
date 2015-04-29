package edu.arizona.biosemantics.oto2.steps.client.hierarchy;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;

public class HierarchyView implements IsWidget {

	private TermsView termsView;
	private TreeView treeView;
	private BorderLayoutContainer borderLayoutContainer;

	public HierarchyView(EventBus eventBus) {
		termsView = new TermsView(eventBus);
		treeView = new TreeView(eventBus);
			
		borderLayoutContainer = new BorderLayoutContainer();
		ContentPanel cp = new ContentPanel();
		cp.setHeadingText("Terms");
		cp.add(termsView);
		BorderLayoutData d = new BorderLayoutData(.20);
		// d.setMargins(new Margins(0, 1, 1, 1));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		borderLayoutContainer.setWestWidget(cp, d);

		cp = new ContentPanel();
		cp.setHeadingText("Ontologies containing the term");
		cp.add(treeView);
		d = new BorderLayoutData();
		d.setMargins(new Margins(0, 0, 0, 0));
		borderLayoutContainer.setCenterWidget(cp, d);
		
	}

	@Override
	public Widget asWidget() {
		return borderLayoutContainer;
	}
	
}
