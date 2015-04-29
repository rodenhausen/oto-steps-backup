package edu.arizona.biosemantics.oto2.steps.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;

public class OtoSteps implements EntryPoint {

	public static String user = "";
	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	
	@Override
	public void onModuleLoad() {
		final EventBus eventBus = new SimpleEventBus();
		RootLayoutPanel.get().add(new OtoStepsView(eventBus));
		
		int id = 1;
		String secret = "my secret";
		collectionService.get(id, secret, new AsyncCallback<Collection>() {
			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}
			@Override
			public void onSuccess(Collection result) {
				if(result != null)
					eventBus.fireEvent(new LoadCollectionEvent(result));
			} 
		});
	}

	public void setUser(String user) {
		OtoSteps.user = user;
	}
}
