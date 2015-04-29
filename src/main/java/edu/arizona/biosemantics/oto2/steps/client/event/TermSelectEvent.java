package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public class TermSelectEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(TermSelectEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
    
    private Term term;

    public TermSelectEvent(Term term) {
        this.term = term;
    }
	
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public Term getTerm() {
		return term;
	}
	
	

}
