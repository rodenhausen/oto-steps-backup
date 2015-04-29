package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

public class OntologyNotFoundException extends Exception {

	public OntologyNotFoundException(String message) {
		super(message);
	}

	public OntologyNotFoundException(String message, Throwable t) {
		super(message, t);
	}

}
