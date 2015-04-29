package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.SetUserEvent.Handler;

public class SetUserEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSet(SetUserEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private String user;

    public SetUserEvent(String user) {
    	this.user = user;
    }
    
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSet(this);
	}

	public String getUser() {
		return user;
	}	
}
