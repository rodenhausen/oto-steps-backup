package edu.arizona.biosemantics.oto2.steps.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.OntologySynonymSubmissionSelectEvent.Handler;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

public class OntologySynonymSubmissionSelectEvent extends GwtEvent<Handler> {

	public interface Handler extends EventHandler {
		void onSelect(OntologySynonymSubmissionSelectEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
    
    private OntologySynonymSubmission ontologySynonymSubmission;
    
    public OntologySynonymSubmissionSelectEvent(OntologySynonymSubmission ontologySynonymSubmission) {
        this.ontologySynonymSubmission = ontologySynonymSubmission;
    }
	
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public OntologySynonymSubmission getOntologySynonymSubmission() {
		return ontologySynonymSubmission;
	}

}
