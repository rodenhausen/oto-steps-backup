package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class RemoveClassSubmissionException extends Exception {
	public RemoveClassSubmissionException() { }
	
	public RemoveClassSubmissionException(String message) {
        super(message);
    }
	
	public RemoveClassSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public RemoveClassSubmissionException(Throwable cause) {
		super(cause);
	}
}
