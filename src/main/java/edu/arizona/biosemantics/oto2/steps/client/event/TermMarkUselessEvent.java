package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.TermMarkUselessEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public class TermMarkUselessEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(TermMarkUselessEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
    
    private Collection<Term> terms;
	private boolean useless;
	
	public TermMarkUselessEvent(Collection<Term> terms, boolean useless) {
		this.terms = terms;
		this.useless = useless;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public Collection<Term> getTerms() {
		return terms;
	}

	public boolean isUseless() {
		return useless;
	}	

}
