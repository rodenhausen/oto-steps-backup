package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class UpdateClassSubmissionException extends Exception {
	public UpdateClassSubmissionException() { }
	
	public UpdateClassSubmissionException(String message) {
        super(message);
    }
	
	public UpdateClassSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public UpdateClassSubmissionException(Throwable cause) {
		super(cause);
	}
}
