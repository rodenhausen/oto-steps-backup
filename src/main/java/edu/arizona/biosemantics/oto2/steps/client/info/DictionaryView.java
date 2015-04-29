package edu.arizona.biosemantics.oto2.steps.client.info;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DictionaryView implements IsWidget {
	
	private EventBus eventBus;
	private Label label;
	
	public DictionaryView(EventBus eventBus) {		
		label = new Label("TODO");
		
		bindEvents();
	}
	
	private void bindEvents() {
		
	}
	
	protected void refresh() {
		
	}

	@Override
	public Widget asWidget() {
		return label;
	}
}
