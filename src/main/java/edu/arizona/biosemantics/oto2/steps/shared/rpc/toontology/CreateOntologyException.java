package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class CreateOntologyException extends Exception {
	public CreateOntologyException() { }
	
	public CreateOntologyException(String message) {
        super(message);
    }
	
	public CreateOntologyException(String message, Throwable cause) {
        super(message);
    }
	
	public CreateOntologyException(Throwable cause) {
		super(cause);
	}
}
