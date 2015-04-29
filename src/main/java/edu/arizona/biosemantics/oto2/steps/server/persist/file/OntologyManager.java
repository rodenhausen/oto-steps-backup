package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.server.persist.DAOManager;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.EntityQualityClass;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.ClassExistsException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyFileException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyNotFoundException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.UnsatisfiableClassesException;

public class OntologyManager implements Serializable {
	
	static {
		initializeDefaultOntologyManager();
	}
	
	private static OWLOntologyManager initializeDefaultOntologyManager() {
		OWLOntologyManager defaultOwlOntologyManager = OWLManager.createOWLOntologyManager();
		try {
			for(Ontology ontology : new edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO().getPermanentOntologies()) {
				File file = getPermanentOntologyFile(ontology);
				try {
					defaultOwlOntologyManager.addIRIMapper(createMapper(ontology));
					OWLOntology owlOntology = defaultOwlOntologyManager.loadOntologyFromOntologyDocument(file);
				} catch (OWLOntologyCreationException e) {
					Logger.getLogger(OntologyManager.class).error("Could not load ontology", e);
				}
			}
			saveDefaultOntologyManager(defaultOwlOntologyManager);
		} catch(QueryException e) {
			Logger.getLogger(OntologyManager.class).error("Could not get permanent ontologies", e);
		}
		return defaultOwlOntologyManager;
	}
	
	private static OWLOntologyIRIMapper createMapper(Ontology ontology) {
		if(ontology.hasCollectionId())
			return new SimpleIRIMapper(createOntologyIRI(ontology), getLocalOntologyIRI(ontology));
		else
			return new SimpleIRIMapper(createOntologyIRI(ontology), getLocalOntologyIRI(ontology));
	}

	private static IRI createClassIRI(OntologyClassSubmission submission) {
		return IRI.create(Configuration.etcOntologyBaseIRI + submission.getOntology().getCollectionId() + "/" +  
				submission.getOntology().getAcronym() + "#" + submission.getSubmissionTerm());
	}
	
	private static IRI createOntologyIRI(OntologySubmission submission) {
		return IRI.create(submission.getOntology().getIri());
	}
	
	private static IRI createOntologyIRI(Ontology ontology) {
		return IRI.create(ontology.getIri());
	}
	
	private static IRI getLocalOntologyIRI(Ontology ontology) {
		if(ontology.hasCollectionId()) {
			return IRI.create(getCollectionOntologyFile(ontology));
		} else 
			return IRI.create(getPermanentOntologyFile(ontology));
	}
	
	private static File getPermanentOntologyFile(Ontology ontology) {
		return new File(Configuration.permanentOntologyDirectory, ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	private static File getCollectionOntologyFile(Ontology ontology) {
		return new File(getCollectionOntologyDirectory(ontology), ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	private static File getCollectionOntologyDirectory(Ontology ontology) {
		return new File(Configuration.collectionOntologyDirectory + File.separator + ontology.getCollectionId() + File.separator + ontology.getAcronym());
	}
	
	private static void saveDefaultOntologyManager(OWLOntologyManager manager) {
		File dir = new File(Configuration.collectionOntologyDirectory);
		File file = new File(dir, "defaultOntologyManager.ser");
		try (ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			output.writeObject(manager);
		} catch (Exception e) {
			Logger.getLogger(OntologyManager.class).error("Serialization of ontology manager failed", e);
		}
	}

	private static OWLOntologyManager loadDefaultOntologyManager() {
		File dir = new File(Configuration.collectionOntologyDirectory);
		File file = new File(dir, "defaultOntologyManager.ser");
		try(ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			return (OWLOntologyManager) input.readObject();
		} catch (IOException | ClassNotFoundException e) {
			Logger.getLogger(OntologyManager.class).error("Serialization of ontology manager failed", e);
			return initializeDefaultOntologyManager();
		}
	}
	
	
	private Collection collection;
	private OWLOntologyManager owlOntologyManager;
	private transient edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO ontologyDBDAO = 
			 new edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO();

	private transient AxiomAdder axiomAdder;
	private transient ModuleCreator moduleCreator;
	private transient OWLOntologyRetriever owlOntologyRetriever;
	private transient AnnotationsReader annotationsReader;
	private transient OntologyReasoner ontologyReasoner;
	
	//OWL entities
	private transient OWLClass entityClass;
	private transient OWLClass qualityClass;
	private transient OWLAnnotationProperty labelProperty;
	private transient OWLAnnotationProperty definitionProperty;

	public OntologyManager(Collection collection) {
		this.collection = collection;
		this.owlOntologyManager = loadDefaultOntologyManager();
		
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		ontologyReasoner = new OntologyReasoner();
		owlOntologyRetriever = new OWLOntologyRetriever(owlOntologyManager, ontologyDBDAO);
		annotationsReader = new AnnotationsReader(owlOntologyRetriever);
		moduleCreator = new ModuleCreator(owlOntologyManager, owlOntologyRetriever, annotationsReader);
		axiomAdder = new AxiomAdder(owlOntologyManager, moduleCreator, ontologyReasoner);
	}

	public void insertOntology(Ontology ontology) throws OntologyFileException {	
		OWLOntology owlOntology = null;
		try {
			owlOntology = owlOntologyManager.createOntology(IRI.create(ontology.getIri()));
		} catch (OWLOntologyCreationException e) {
			log(LogLevel.ERROR, "Couldn't create ontology", e);
			throw new OntologyFileException(e);
		}
		addRelevantOntologies(collection, owlOntology);
		axiomAdder.addOntologyAxioms(owlOntology);
	}
	
	private void addImportDeclaration(OWLOntology owlOntology, Ontology ontology) throws OntologyFileException {
		IRI relevantIRI = IRI.create(ontology.getIri());
		OWLImportsDeclaration importDeclaraton = owlOntologyManager.getOWLDataFactory().getOWLImportsDeclaration(relevantIRI);
		owlOntologyManager.applyChange(new AddImport(owlOntology, importDeclaraton));
	}

	private void addRelevantOntologies(Collection collection, OWLOntology owlOntology) throws OntologyFileException {
		List<Ontology> relevantOntologies = new LinkedList<Ontology>();
		try {
			relevantOntologies = ontologyDBDAO.getRelevantOntologiesForCollection(collection);
		} catch (QueryException e) {
			log(LogLevel.ERROR, "Could not add relevant ontologies", e);
		}
		for(Ontology relevantOntology : relevantOntologies) {
			if(!relevantOntology.hasCollectionId()) {
				addImportDeclaration(owlOntology, relevantOntology);
			}
		}
	}

	public void removeOntology(Ontology ontology) {
		OWLOntology owlOntology = owlOntologyManager.getOntology(IRI.create(ontology.getIri()));
		if(owlOntology != null) 
			owlOntologyManager.removeOntology(owlOntology);
	}

	public String insertClassSubmission(OntologyClassSubmission submission) throws OntologyFileException, ClassExistsException {	
		OWLClass newOwlClass = null;		
		if(submission.hasClassIRI()) {
			try {
				newOwlClass = addModuleOfClass(collection, submission, submission);
			} catch (OWLOntologyCreationException | OWLOntologyStorageException	| OntologyNotFoundException e) {
				throw new OntologyFileException(e);
			}
		} else {
			newOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(createClassIRI(submission));
		}
		
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		if(containsOwlClass(targetOwlOntology, newOwlClass)) {
			try {
				String definition = annotationsReader.get(collection, newOwlClass, definitionProperty);
				throw new ClassExistsException("class '"+ submission.getSubmissionTerm() + 
						"' exists and defined as:" + definition);
			} catch (OntologyNotFoundException e) {
				throw new ClassExistsException("class '"+ submission.getSubmissionTerm() + 
						"' exists.");
			}
		}
		
		axiomAdder.addSuperClassModuleAxioms(targetOwlOntology, submission, newOwlClass);
		axiomAdder.addSynonymAxioms(targetOwlOntology, submission, newOwlClass);
		axiomAdder.addSuperclassAxioms(collection, submission, newOwlClass);
		axiomAdder.addPartOfAxioms(collection, submission, newOwlClass);
		
		/*try {
			ontologyReasoner.checkConsistency(targetOwlOntology);
		} catch (UnsatisfiableClassesException e) {
			throw new OntologyFileException(e);
		}*/
		
		return newOwlClass.getIRI().toString();
	}
	
	public void updateClassSubmission(OntologyClassSubmission submission) throws ClassExistsException, OntologyFileException {
		this.removeClassSubmission(submission);
		this.insertClassSubmission(submission);
	}
	
	public void removeClassSubmission(OntologyClassSubmission submission) {
		this.setDepreceated(createClassIRI(submission), owlOntologyManager.getOntology(createOntologyIRI(submission)));
	}
		
	private boolean containsOwlClass(OWLOntology owlOntology, OWLClass owlClass) {
		return owlOntology.getClassesInSignature(true).contains(owlClass);
	}
	
	private OWLClass addModuleOfClass(Collection collection, OntologySubmission submission, EntityQualityClass isEntityQuality) throws OWLOntologyCreationException, OWLOntologyStorageException, OntologyNotFoundException {
		OWLClass newOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(isEntityQuality.getClassIRI()));
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		OWLOntology moduleOwlOntology = moduleCreator.createModuleFromOwlClass(collection, submission, newOwlClass);
		//make all added class subclass of quality/entity
		if (isEntityQuality.isEntity()) {
			if (ontologyReasoner.isSubclass(targetOwlOntology, newOwlClass, entityClass)) {
				//result.setMessage(result.getMessage()
				//		+ " Can not add the quality term '" + newTerm
				//		+ "' as a child to entity term '" + newTerm + "'.");
			} else {
				for (OWLClass owlClass : moduleOwlOntology.getClassesInSignature()) {
					axiomAdder.addEntitySubclassAxiom(targetOwlOntology, owlClass);
				}
			}
		}
		if (isEntityQuality.isQuality()) {
			if (ontologyReasoner.isSubclass(targetOwlOntology, newOwlClass, qualityClass)) {
				//result.setMessage(result.getMessage()
				//		+ " Can not add the entity term '" + newTerm
				//		+ "' as a child to quality term '" + newTerm + "'.");
			} else {
				for (OWLClass owlClass : moduleOwlOntology.getClassesInSignature()) {
					axiomAdder.addQualitySubclassAxiom(targetOwlOntology, owlClass);
				}
			}
		}
		return newOwlClass;
	}	
	
	private void removeModuleOfClass(Collection collection, OntologySynonymSubmission submission, OntologySynonymSubmission isEntityQuality) {

	}

	public String insertSynonymSubmission(OntologySynonymSubmission submission) throws OntologyFileException { 
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(IRI.create(submission.getOntology().getIri()));
		List<OWLClass> affectedClasses = new ArrayList<OWLClass>();
		if(submission.hasClassIRI()) {
			String[] classIRIs = submission.getClassIRI().split("\\s*,\\s*");
			for(String classIRI : classIRIs){ //add syn to each of the classes in current ontology class signature
				if(classIRI.isEmpty()) 
					continue;
				
				try {
					OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI));
					boolean isContained = containsOwlClass(targetOwlOntology, owlClass);
					String label = annotationsReader.get(collection, owlClass, labelProperty);
					if(isContained && label != null && !label.equals(submission.getSubmissionTerm())) {
						//class exists in current/imported ontology => add syn
						affectedClasses.add(owlClass);
						
						axiomAdder.addSynonymAxioms(targetOwlOntology, submission.getSubmissionTerm(), owlClass);
					} else if(!isContained){
						//an external class does not exist => add class, then add syn	
						owlClass = addModuleOfClass(collection, submission, submission);  
						axiomAdder.addSynonymAxioms(targetOwlOntology, submission.getSubmissionTerm(), owlClass);
					}
				} catch(OntologyNotFoundException | OWLOntologyCreationException | OWLOntologyStorageException e) {
					throw new OntologyFileException(e);
				} 
			}
		}

		//add other additional synonyms
		String [] synonyms = submission.getSynonyms().split("\\s*,\\s*");
		for(String synonym : synonyms){
			if(!synonym.isEmpty()) {
				for(OWLClass affectedClass : affectedClasses){
					axiomAdder.addSynonymAxioms(targetOwlOntology, synonym, affectedClass);
				}
			}
		}
		
		//fill in more info to submission so the UI can present complete matching info.
		StringBuilder termString = new StringBuilder();
		StringBuilder defString = new StringBuilder();
		StringBuilder idString = new StringBuilder();
		StringBuilder superString = new StringBuilder();
		for(OWLClass affectedClass : affectedClasses) {
			try {
				//OWLOntology affectedOwlOntology = this.getOWLOntology(affectedClass);
				
				termString.append(annotationsReader.get(collection, affectedClass, labelProperty) + ";");
				defString.append(annotationsReader.get(collection, affectedClass, definitionProperty) + ";");
				idString.append(affectedClass.getIRI().toString() + ";");
				superString.append(getSuperClassesString(collection, affectedClass) + ";");
			} catch(OntologyNotFoundException e) {
				throw new OntologyFileException(e);
			}
		}
		
		
		//submission.setClassID(termString.replaceFirst(";$", ""));
		//submission.setDefinition(defString.replaceFirst(";$", ""));
		//submission.setPermanentID(idString.replaceFirst(";$", ""));
		//submission.setSuperClass(superString.replaceFirst(";$", ""));	
		//submission.setTmpID(""); 
		return submission.getClassIRI().toString();
	}	

	public void updateSynonymSubmission(OntologySynonymSubmission submission) throws OntologyFileException {
		this.removeSynonymSubmission(submission);
		this.insertSynonymSubmission(submission);
	}	

	public void removeSynonymSubmission(OntologySynonymSubmission submission) throws OntologyFileException {
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(IRI.create(submission.getOntology().getIri()));
		List<OWLClass> affectedClasses = new ArrayList<OWLClass>();
		if(submission.hasClassIRI()) {
			String[] classIRIs = submission.getClassIRI().split("\\s*,\\s*");
			for(String classIRI : classIRIs){ //add syn to each of the classes in current ontology class signature
				if(classIRI.isEmpty()) 
					continue;
				
				try {
					OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI));
					boolean isContained = containsOwlClass(targetOwlOntology, owlClass);
					String label = annotationsReader.get(collection, owlClass, labelProperty);
					if(isContained && label != null && !label.equals(submission.getSubmissionTerm())) {
						//class exists in current/imported ontology => add syn
						affectedClasses.add(owlClass);
						
						axiomAdder.removeSynonymAxioms(targetOwlOntology, submission.getSubmissionTerm(), owlClass);
					} else if(!isContained){
						//an external class does not exist => add class, then add syn	
						removeModuleOfClass(collection, submission, submission);  
						axiomAdder.removeSynonymAxioms(targetOwlOntology, submission.getSubmissionTerm(), owlClass);
					}
				} catch(OntologyNotFoundException e) {
					throw new OntologyFileException(e);
				} 
			}
		}

		//add other additional synonyms
		String [] synonyms = submission.getSynonyms().split("\\s*,\\s*");
		for(String synonym : synonyms){
			if(!synonym.isEmpty()) {
				for(OWLClass affectedClass : affectedClasses){
					axiomAdder.removeSynonymAxioms(targetOwlOntology, synonym, affectedClass);
				}
			}
		}
	}
	


	private String getSuperClassesString(Collection collection, OWLClass owlClass) throws OntologyNotFoundException {
		Set<OWLClassExpression> supers = owlClass.getSuperClasses(owlOntologyRetriever.getOWLOntology(collection, owlClass));
		Iterator<OWLClassExpression> it = supers.iterator();
		String superClassesString ="";
		while(it.hasNext()){
			OWLClassExpression owlClassExpression = it.next();
			if(owlClassExpression instanceof OWLClass){ 
				superClassesString += annotationsReader.get(collection, (OWLClass)owlClassExpression, labelProperty) + ",";
			}
		}
		return superClassesString;
	}
	
	private void setDepreceated(IRI iri, OWLOntology owlOntology) {
		OWLAxiom depreceatedAxiom = owlOntologyManager.getOWLDataFactory().getDeprecatedOWLAnnotationAssertionAxiom(iri);
		owlOntologyManager.applyChange(new AddAxiom(owlOntology, depreceatedAxiom));
	}

	public Collection getCollection() {
		return collection;
	}
	
	
	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();

		this.ontologyDBDAO = new edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO();
		
		this.labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		this.entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		this.qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		this.definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		
		this.ontologyReasoner = new OntologyReasoner();
		this.owlOntologyRetriever = new OWLOntologyRetriever(owlOntologyManager, ontologyDBDAO);
		this.annotationsReader = new AnnotationsReader(owlOntologyRetriever);
		this.moduleCreator = new ModuleCreator(owlOntologyManager, owlOntologyRetriever, annotationsReader);
		this.axiomAdder = new AxiomAdder(owlOntologyManager, moduleCreator, ontologyReasoner);
	}
	
	/* private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		 out.defaultWriteObject();
	 }*/
}
