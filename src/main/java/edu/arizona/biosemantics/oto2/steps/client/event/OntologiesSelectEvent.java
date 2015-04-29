package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.OntologiesSelectEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;

public class OntologiesSelectEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(OntologiesSelectEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Set<Ontology> ontologies;

    public OntologiesSelectEvent(Set<Ontology> ontologies) {
    	this.ontologies = ontologies;
    }
    
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public Set<Ontology> getOntologies() {
		return ontologies;
	}
	
}
