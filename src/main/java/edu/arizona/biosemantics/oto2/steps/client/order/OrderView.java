package edu.arizona.biosemantics.oto2.steps.client.order;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class OrderView implements IsWidget {
	
	private Label label;

	public OrderView(EventBus eventBus) {

		label = new Label("test");
	}

	@Override
	public Widget asWidget() {
		return label;
	}
	
	

}
