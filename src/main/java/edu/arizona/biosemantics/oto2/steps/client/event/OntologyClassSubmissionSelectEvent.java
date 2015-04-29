package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.OntologyClassSubmissionSelectEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;

public class OntologyClassSubmissionSelectEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(OntologyClassSubmissionSelectEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
    
    private OntologyClassSubmission ontologyClassSubmission;
    
    public OntologyClassSubmissionSelectEvent(OntologyClassSubmission ontologyClassSubmission) {
        this.ontologyClassSubmission = ontologyClassSubmission;
    }
	
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public OntologyClassSubmission getOntologyClassSubmission() {
		return ontologyClassSubmission;
	}

}
