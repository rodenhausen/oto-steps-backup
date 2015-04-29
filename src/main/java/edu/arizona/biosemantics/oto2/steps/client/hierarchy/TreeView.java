package edu.arizona.biosemantics.oto2.steps.client.hierarchy;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TreeView implements IsWidget {

	private Label label;

	public TreeView(EventBus eventBus) {
		label = new Label("test");
	}

	@Override
	public Widget asWidget() {
		return label;
	}

}
