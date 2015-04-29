package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class OntologyExistsException extends Exception {

	public OntologyExistsException() { }
	
	public OntologyExistsException(String message) {
        super(message);
    }


    public OntologyExistsException(String message, Throwable cause) {
        super(message, cause);
    }


    public OntologyExistsException(Throwable cause) {
        super(cause);
    }
    
}

