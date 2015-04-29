package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.SaveEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;

public class SaveEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSave(SaveEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Collection collection;

    public SaveEvent(Collection collection) {
    	this.collection = collection;
    }
    
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSave(this);
	}

	public Collection getCollection() {
		return collection;
	}
	
}
