package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyNotFoundException;

public class ModuleCreator implements Serializable  {
	
	private OWLOntologyManager owlOntologyManager;
	private OWLOntologyRetriever owlOntologyRetriever;
	private AnnotationsReader annotationsReader;
	private StructuralReasonerFactory owlReasonerFactory;
	private ConsoleProgressMonitor progressMonitor;
	private SimpleConfiguration owlReasonerConfig;
	private OWLAnnotationProperty labelProperty;

	public ModuleCreator(OWLOntologyManager owlOntologyManager, OWLOntologyRetriever owlOntologyRetriever, 
			AnnotationsReader annotationsReader) {
		this.owlOntologyManager = owlOntologyManager;
		this.owlOntologyRetriever = owlOntologyRetriever;
		this.annotationsReader = annotationsReader;
		owlReasonerFactory = new StructuralReasonerFactory();
		progressMonitor = new ConsoleProgressMonitor();
		owlReasonerConfig = new SimpleConfiguration(progressMonitor);
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	}
	
	public OWLOntology createModuleFromOwlClass(Collection collection, OntologySubmission submission, OWLClass owlClass) throws OntologyNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException {
		OWLOntology owlClassOntology = owlOntologyRetriever.getOWLOntology(collection, owlClass);
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		Set<OWLEntity> seeds = new HashSet<OWLEntity>();
		seeds.add(owlClass);
		
		IRI moduleIRI = createModuleIRI(collection, submission, owlClass);
		SyntacticLocalityModuleExtractor syntacticLocalityModuleExtractor = new SyntacticLocalityModuleExtractor(
				owlOntologyManager, owlClassOntology, ModuleType.STAR);
		OWLReasoner owlReasoner = owlReasonerFactory.createReasoner(owlClassOntology, owlReasonerConfig);
		OWLOntology moduleOntology = syntacticLocalityModuleExtractor.extractAsOntology(seeds, moduleIRI, -1, 0, owlReasoner); //take all superclass and no subclass into the seeds.
		OWLImportsDeclaration importDeclaration = owlOntologyManager.getOWLDataFactory().getOWLImportsDeclaration(moduleIRI);
		owlOntologyManager.applyChange(new AddImport(targetOwlOntology, importDeclaration));
		return moduleOntology;
	}
	
	private IRI createModuleIRI(Collection collection, OntologySubmission ontologySubmission, OWLClass owlClass) throws OntologyNotFoundException {
		return IRI.create(Configuration.etcOntologyBaseIRI + collection.getId() + "/" + ontologySubmission.getOntology().getAcronym() + "/" + 
				"module." + annotationsReader.get(collection, owlClass, labelProperty) + ".owl");
	}
	
	private IRI createOntologyIRI(OntologySubmission submission) {
		return IRI.create(submission.getOntology().getIri());
	}
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		 out.defaultWriteObject();
	 }
}
