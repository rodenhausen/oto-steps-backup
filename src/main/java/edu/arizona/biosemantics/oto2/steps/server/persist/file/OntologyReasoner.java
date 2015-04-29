package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.UnsatisfiableClassesException;

public class OntologyReasoner implements Serializable  {
	
	private StructuralReasonerFactory owlReasonerFactory;
	private ConsoleProgressMonitor progressMonitor;
	private SimpleConfiguration owlReasonerConfig;
	
	public OntologyReasoner() {
		owlReasonerFactory = new StructuralReasonerFactory();
		progressMonitor = new ConsoleProgressMonitor();
		owlReasonerConfig = new SimpleConfiguration(progressMonitor);
	}
	
	public boolean isSubclass(OWLOntology owlOntology, OWLClass subclass, OWLClass superclass) {
	    OWLReasoner reasoner = owlReasonerFactory.createReasoner(owlOntology, owlReasonerConfig);
	    reasoner.precomputeInferences();
	    return reasoner.getSuperClasses(subclass, false).containsEntity(superclass); //false: retrieval all ancestors.
	}
	
	public void checkConsistency(OWLOntology owlOntology) throws UnsatisfiableClassesException {
		OWLReasoner reasoner = owlReasonerFactory.createReasoner(owlOntology, owlReasonerConfig);
		reasoner.precomputeInferences();
		boolean consistent = reasoner.isConsistent();
		if(!consistent){
			Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
			Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
			if (!unsatisfiable.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append("Warning: After the additions, the following classes have become unsatisfiable. Edit the ontology in protege to correct the problems. \n");
				for (OWLClass cls : unsatisfiable) {
					sb.append("    " + cls+"\n");
				}
				throw new UnsatisfiableClassesException(sb.toString());
			} 
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		 out.defaultWriteObject();
	 }
}
