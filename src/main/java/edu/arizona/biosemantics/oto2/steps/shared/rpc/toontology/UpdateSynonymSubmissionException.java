package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class UpdateSynonymSubmissionException extends Exception {
	public UpdateSynonymSubmissionException() { }
	
	public UpdateSynonymSubmissionException(String message) {
        super(message);
    }
	
	public UpdateSynonymSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public UpdateSynonymSubmissionException(Throwable cause) {
		super(cause);
	}
}
