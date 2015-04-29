package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class CreateSynonymSubmissionException extends Exception {
	public CreateSynonymSubmissionException() { }
	
	public CreateSynonymSubmissionException(String message) {
        super(message);
    }
	
	public CreateSynonymSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public CreateSynonymSubmissionException(Throwable cause) {
		super(cause);
	}
}
