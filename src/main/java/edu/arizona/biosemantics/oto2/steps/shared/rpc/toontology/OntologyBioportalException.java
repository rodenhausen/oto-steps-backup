package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class OntologyBioportalException extends Exception {

	public OntologyBioportalException() { }
	
	public OntologyBioportalException(String message) {
        super(message);
    }
	
	public OntologyBioportalException(String message, Throwable cause) {
        super(message);
    }
	
	public OntologyBioportalException(Throwable cause) {
		super(cause);
	}
}
