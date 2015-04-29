package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.RefreshSubmissionsEvent.Handler;

public class RefreshSubmissionsEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(RefreshSubmissionsEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
    
    public RefreshSubmissionsEvent() {
    }
	
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

}
