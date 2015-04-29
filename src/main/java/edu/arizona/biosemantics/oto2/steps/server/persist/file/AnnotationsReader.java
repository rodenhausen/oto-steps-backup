package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.Serializable;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyNotFoundException;

public class AnnotationsReader implements Serializable {
	
	private OWLOntologyRetriever owlOntologyRetriever;

	public AnnotationsReader(OWLOntologyRetriever owlOntologyRetriever) {
		this.owlOntologyRetriever = owlOntologyRetriever;
	}

	public String get(Collection collection, OWLClass owlClass, OWLAnnotationProperty annotationProperty) throws OntologyNotFoundException {
		OWLOntology owlOntology = owlOntologyRetriever.getOWLOntology(collection, owlClass);
		for (OWLAnnotation annotation : owlClass.getAnnotations(owlOntology, annotationProperty)) {
			if (annotation.getValue() instanceof OWLLiteral) {
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				//if (val.hasLang("en")) {
				return val.getLiteral();
				//}
			}
		}
		return null;
	}
	
}
