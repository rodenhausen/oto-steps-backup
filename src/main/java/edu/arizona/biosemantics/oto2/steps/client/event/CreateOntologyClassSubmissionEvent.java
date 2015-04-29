package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.CreateOntologyClassSubmissionEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;

public class CreateOntologyClassSubmissionEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSubmission(CreateOntologyClassSubmissionEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private OntologyClassSubmission classSubmission;

    public CreateOntologyClassSubmissionEvent(OntologyClassSubmission classSubmission) {
    	this.classSubmission = classSubmission;
    }
    
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSubmission(this);
	}

	public OntologyClassSubmission getClassSubmission() {
		return classSubmission;
	}
}
