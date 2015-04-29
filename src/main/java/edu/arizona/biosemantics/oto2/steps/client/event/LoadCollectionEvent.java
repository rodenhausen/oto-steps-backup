package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;

public class LoadCollectionEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onLoad(LoadCollectionEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Collection collection;

    public LoadCollectionEvent(Collection collection) {
    	this.collection = collection;
    }
    
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onLoad(this);
	}

	public Collection getCollection() {
		return collection;
	}

}
