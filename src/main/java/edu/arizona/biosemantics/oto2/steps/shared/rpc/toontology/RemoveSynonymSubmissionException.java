package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class RemoveSynonymSubmissionException extends Exception {
	public RemoveSynonymSubmissionException() { }
	
	public RemoveSynonymSubmissionException(String message) {
        super(message);
    }
	
	public RemoveSynonymSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public RemoveSynonymSubmissionException(Throwable cause) {
		super(cause);
	}
}
