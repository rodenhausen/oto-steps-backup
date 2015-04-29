package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class CreateClassSubmissionException extends Exception {
	public CreateClassSubmissionException() { }
	
	public CreateClassSubmissionException(String message) {
        super(message);
    }
	
	public CreateClassSubmissionException(String message, Throwable cause) {
        super(message);
    }
	
	public CreateClassSubmissionException(Throwable cause) {
		super(cause);
	}
}
