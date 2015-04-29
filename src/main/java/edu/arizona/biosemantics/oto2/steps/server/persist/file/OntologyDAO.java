package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.CollectionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyClassSubmissionStatusDAO;
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

public class OntologyDAO {
	
	private static edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO staticOntologyDAO = 
			new  edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO();
	
	private edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO ontologyDAO;
	private CollectionDAO collectionDAO;
	private OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO; 
	
	private static Map<Ontology, OWLOntology> referencedOntologies = new HashMap<Ontology, OWLOntology>();
	
	//one manager manages all the ontologies files, check if an ontology is already being managed before load it, 
	//create ontologyIRI and documentIRI mapping for each loaded ontology.
	private static OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
	private static OWLDataFactory owlDataFactory = owlOntologyManager.getOWLDataFactory();
	private static OWLClass entityClass = null;
	private static OWLClass qualityClass = null;
	private static OWLObjectProperty partOfProperty = null;
	private static OWLAnnotationProperty labelProperty = null;
	private static OWLAnnotationProperty synonymProperty = null;
	private static OWLAnnotationProperty definitionProperty =null;
	private static OWLAnnotationProperty creationDateProperty = null;
	private static OWLAnnotationProperty createdByProperty = null;
	private static OWLAnnotationProperty relatedSynonymProperty = null;
	private static OWLAnnotationProperty narrowSynonymProperty = null;
	private static OWLAnnotationProperty exactSynonymProperty = null;
	private static OWLAnnotationProperty broadSynonymProperty = null;
	private static OWLReasonerFactory owlReasonerFactory = new StructuralReasonerFactory();
	private static  ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
	private static  OWLReasonerConfiguration owlReasonerConfig = new SimpleConfiguration(progressMonitor);
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
	static{
		entityClass = owlDataFactory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		qualityClass = owlDataFactory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		partOfProperty = owlDataFactory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"));
		labelProperty = owlDataFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		synonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"));
		definitionProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		creationDateProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#creation_date"));
		createdByProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#created_by"));
		relatedSynonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"));
		narrowSynonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym"));
		exactSynonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"));
		broadSynonymProperty = owlDataFactory.getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym"));
		
		// loading ontologies takes time, preload them
		try {
			for(Ontology ontology : staticOntologyDAO.getPermanentOntologies()) {
				File file = getPermanentOntologyFile(ontology);
				try {
					owlOntologyManager.addIRIMapper(createMapper(ontology));
					OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(file);
					referencedOntologies.put(ontology, owlOntology);
				} catch (OWLOntologyCreationException e) {
					Logger.getLogger(OntologyDAO.class).error("Could not load ontology", e);
				}
			}
		} catch (QueryException e) {
			Logger.getLogger(OntologyDAO.class).error("Could not load ontologies", e);
		}
	}

	public void insertOntology(Ontology ontology) throws OntologyFileException {		
		owlOntologyManager.addIRIMapper(createMapper(ontology));
		OWLOntology owlOntology = null;
		try {
			owlOntology = owlOntologyManager.createOntology(getGlobalEtcIRI(ontology));
		} catch (OWLOntologyCreationException e) {
			log(LogLevel.ERROR, "Couldn't create ontology", e);
			throw new OntologyFileException(e);
		}
		
		try {
			addRelevantOntologies(collectionDAO.get(ontology.getCollectionId()), owlOntology);
		} catch (QueryException | IOException e) {
			throw new OntologyFileException(e);
		}
		addOntologyAxioms(owlOntology);
		
		try {
			owlOntologyManager.saveOntology(owlOntology, getLocalOntologyIRI(ontology));
		} catch (OWLOntologyStorageException e) {
			log(LogLevel.ERROR, "Couldn't save ontology", e);
			throw new OntologyFileException(e);
		}
		
		if (!referencedOntologies.containsKey(ontology.getIri().toLowerCase())) {
			/*try {
				owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(getCollectionOntologyFile(ontology));
			} catch (OWLOntologyCreationException e) {
				throw new OntologyFileException(e);
			}*/
			referencedOntologies.put(ontology, owlOntology);
		}
	}

	public String insertClassSubmission(Collection collection, OntologyClassSubmission ontologyClassSubmission) throws OntologyFileException, ClassExistsException {	
		OWLOntology owlOntology = getOwlOntology(collection, ontologyClassSubmission);
		OWLReasoner owlReasoner = owlReasonerFactory.createReasoner(owlOntology, owlReasonerConfig);
		PrefixManager prefixManager = createPrefixManager(ontologyClassSubmission);
		
		OWLClass newOwlClass = createNewOwlClass(collection, owlOntology, ontologyClassSubmission, prefixManager, owlReasoner);
		
		addSuperClassModuleAxioms(owlOntology, ontologyClassSubmission, newOwlClass);
		addSynonymAxioms(owlOntology, ontologyClassSubmission, newOwlClass);
		addSuperclassAxioms(collection, owlOntology, ontologyClassSubmission, newOwlClass, owlReasoner, prefixManager);
		addPartOfAxioms(collection, owlOntology, ontologyClassSubmission, newOwlClass, owlReasoner, prefixManager);
				
		try {
			checkConsistency(owlReasoner);
		} catch (UnsatisfiableClassesException e) {
			throw new OntologyFileException(e);
		}
		try {
			owlOntologyManager.saveOntology(owlOntology, IRI.create(getCollectionOntologyFile(ontologyClassSubmission.getOntology()).toURI()));
		} catch (OWLOntologyStorageException e) {
			throw new OntologyFileException(e);
		}
		return newOwlClass.getIRI().toString();
	}
	
	public String insertSynonymSubmission(Collection collection, OntologySynonymSubmission ontologySynonymSubmission) throws OntologyFileException { 
		OWLOntology owlOntology = getOwlOntology(collection, ontologySynonymSubmission);
		OWLReasoner owlReasoner = owlReasonerFactory.createReasoner(owlOntology, owlReasonerConfig);
		
		List<OWLClass> affectedClasses = new ArrayList<OWLClass>();
		if(ontologySynonymSubmission.hasClassIRI()) {
			String[] classIRIs = ontologySynonymSubmission.getClassIRI().split("\\s*,\\s*");
			for(String classIRI : classIRIs){ //add syn to each of the classes in current ontology class signature
				if(classIRI.isEmpty()) 
					continue;
				
				try {
					OWLClass owlClass = owlDataFactory.getOWLClass(IRI.create(classIRI));
					boolean isContained = containsOwlClass(owlOntology, owlClass);
					String label = this.getProperty(collection, owlClass, labelProperty);
					if(isContained && label != null && !label.equals(ontologySynonymSubmission.getSubmissionTerm())) {
						//class exists in current/imported ontology => add syn
						affectedClasses.add(owlClass);
						
						this.addSynonymAxioms(owlOntology, ontologySynonymSubmission.getSubmissionTerm(), owlClass);
					} else if(!isContained){
						//an external class does not exist => add class, then add syn	
						owlClass = addModuleOfClass(collection, ontologySynonymSubmission, owlOntology, owlReasoner, ontologySynonymSubmission);  
						this.addSynonymAxioms(owlOntology, ontologySynonymSubmission.getSubmissionTerm(), owlClass);
					}
				} catch(OntologyNotFoundException | OWLOntologyCreationException | OWLOntologyStorageException e) {
					throw new OntologyFileException(e);
				} 
			}
		}

		//add other additional synonyms
		String [] synonyms = ontologySynonymSubmission.getSynonyms().split("\\s*,\\s*");
		for(String synonym : synonyms){
			if(!synonym.isEmpty()) {
				for(OWLClass affectedClass : affectedClasses){
					this.addSynonymAxioms(owlOntology, synonym, affectedClass);
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
				
				termString.append(getProperty(collection, affectedClass, labelProperty) + ";");
				defString.append(getProperty(collection, affectedClass, definitionProperty) + ";");
				idString.append(affectedClass.getIRI().toString() + ";");
				superString.append(getSuperClassesString(collection, affectedClass) + ";");
			} catch(OntologyNotFoundException e) {
				throw new OntologyFileException(e);
			}
		}
		
		/*
		submission.setClassID(termString.replaceFirst(";$", ""));
		submission.setDefinition(defString.replaceFirst(";$", ""));
		submission.setPermanentID(idString.replaceFirst(";$", ""));
		submission.setSuperClass(superString.replaceFirst(";$", ""));	
		submission.setTmpID(""); */
		return ontologySynonymSubmission.getClassIRI().toString();
	}
	

	private String getSuperClassesString(Collection collection, OWLClass owlClass) throws OntologyNotFoundException {
		Set<OWLClassExpression> supers = owlClass.getSuperClasses(this.getOWLOntology(collection, owlClass));
		Iterator<OWLClassExpression> it = supers.iterator();
		String superClassesString ="";
		while(it.hasNext()){
			OWLClassExpression owlClassExpression = it.next();
			if(owlClassExpression instanceof OWLClass){ 
				superClassesString += this.getProperty(collection, (OWLClass)owlClassExpression, labelProperty) + ",";
			}
		}
		return superClassesString;
	}

	private OWLClass addModuleOfClass(Collection collection, OntologySubmission ontologySubmission, OWLOntology owlOntology, OWLReasoner owlReasoner, EntityQualityClass isEntityQuality) throws OWLOntologyCreationException, OWLOntologyStorageException, OntologyNotFoundException {
		OWLClass newOwlClass = owlDataFactory.getOWLClass(IRI.create(isEntityQuality.getClassIRI()));
		OWLOntology moduleOntology = extractModule(collection, ontologySubmission, owlOntology, owlReasoner, newOwlClass);
		//make all added class subclass of quality/entity
		if (isEntityQuality.isEntity()) {
			if (isSubclass(owlOntology, newOwlClass, entityClass)) {
				//result.setMessage(result.getMessage()
				//		+ " Can not add the quality term '" + newTerm
				//		+ "' as a child to entity term '" + newTerm + "'.");
			} else {
				for (OWLClass owlClass : moduleOntology.getClassesInSignature()) {
					this.addEntitySubclassAxiom(owlOntology, owlClass);
				}
			}
		}
		if (isEntityQuality.isQuality()) {
			if (this.isSubclass(owlOntology, newOwlClass, qualityClass)) {
				//result.setMessage(result.getMessage()
				//		+ " Can not add the entity term '" + newTerm
				//		+ "' as a child to quality term '" + newTerm + "'.");
			} else {
				for (OWLClass owlClass : moduleOntology.getClassesInSignature()) {
					this.addQualitySubclassAxiom(owlOntology, owlClass);
				}
			}
		}
		return newOwlClass;
	}
		
	private OWLOntology extractModule(Collection collection, OntologySubmission ontologySubmission, OWLOntology owlOntology, OWLReasoner owlReasoner, OWLClass owlClass) throws OntologyNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException {
		OWLOntology ontology = getOWLOntology(collection, owlClass);
		//create and save inference-entailment module as an ontology file
		
		Set<OWLEntity> seeds = new HashSet<OWLEntity>();
		seeds.add(owlClass);
		
		File moduleFile = new File(getCollectionOntologyDirectory(ontologySubmission.getOntology()), "module." + getProperty(collection, owlClass, labelProperty) + ".owl");
		IRI moduleIRI = IRI.create(moduleFile);
		if (moduleFile.exists()) {
			// remove the existing module -- in effect replace the old module with the new one.
			owlOntologyManager.removeOntology(owlOntologyManager.getOntology(moduleIRI));
			moduleFile.delete();
		}
		SyntacticLocalityModuleExtractor syntacticLocalityModuleExtractor = new SyntacticLocalityModuleExtractor(owlOntologyManager, ontology, ModuleType.STAR);
		OWLOntology moduleOntology = syntacticLocalityModuleExtractor.extractAsOntology(seeds, moduleIRI, -1, 0, owlReasoner); //take all superclass and no subclass into the seeds.
		owlOntologyManager.saveOntology(moduleOntology, moduleIRI);
		//import the module ontology to current onto in memory
		OWLImportsDeclaration importDeclaration = owlDataFactory.getOWLImportsDeclaration(moduleIRI);
		owlOntologyManager.applyChange(new AddImport(owlOntology, importDeclaration));
		if (owlOntologyManager.getOntology(moduleIRI) == null)
			owlOntologyManager.loadOntology(moduleIRI);
		return moduleOntology;
	}
	
	private boolean containsOwlClass(OWLOntology owlOntology, OWLClass owlClass) {
		return owlOntology.getClassesInSignature(true).contains(owlClass);
	}

	private OWLClass createNewOwlClass(Collection collection, OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, PrefixManager prefixManager, 
			OWLReasoner owlReasoner) throws OntologyFileException, ClassExistsException {
		OWLClass newOwlClass = null;		
		if(ontologyClassSubmission.hasClassIRI()) {
			try {
				newOwlClass = addModuleOfClass(collection, ontologyClassSubmission, owlOntology, owlReasoner, ontologyClassSubmission);
			} catch (OWLOntologyCreationException | OWLOntologyStorageException
					| OntologyNotFoundException e) {
				throw new OntologyFileException(e);
			}
		} else {
			newOwlClass = owlDataFactory.getOWLClass(":" + ontologyClassSubmission.getSubmissionTerm().replaceAll("\\s+", "_"), prefixManager);
		}
		
		if(containsOwlClass(owlOntology, newOwlClass)) {
			try {
				String definition = this.getProperty(collection, newOwlClass, definitionProperty);
				throw new ClassExistsException("class '"+ ontologyClassSubmission.getSubmissionTerm() + 
						"' exists and defined as:" + definition);
			} catch (OntologyNotFoundException e) {
				throw new ClassExistsException("class '"+ ontologyClassSubmission.getSubmissionTerm() + 
						"' exists.");
			}
		}
		
		return newOwlClass;
	}

	private OWLOntology getOwlOntology(Collection collection, OntologySubmission hasOntology) throws OntologyFileException {
		File ontologyFile = getCollectionOntologyFile(hasOntology.getOntology());
		OWLOntology owlOntology = null;
		if (!referencedOntologies.containsKey(hasOntology.getOntology())) {
			
			owlOntologyManager.addIRIMapper(createMapper(hasOntology.getOntology()));
			try {
				owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(ontologyFile);
			} catch (OWLOntologyCreationException e) {
				throw new OntologyFileException(e);
			}
			referencedOntologies.put(hasOntology.getOntology(), owlOntology);
		} else {
			owlOntology = referencedOntologies.get(hasOntology.getOntology());
		}
		return owlOntology;
	}
	
	private void checkConsistency(OWLReasoner owlReasoner) throws UnsatisfiableClassesException {
		//consistency checking, if not consistent, the problem need to be fixed manually. 
		owlReasoner.precomputeInferences();
		boolean consistent = owlReasoner.isConsistent();
		if(!consistent){
			Node<OWLClass> bottomNode = owlReasoner.getUnsatisfiableClasses();
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
	
	/**
	 * A OWLClass can be contained in a set of ontologies. It is however defined in a single ontology.
	 * The single ontology gives the class it's record and also it's IRI, e.g. is has a prefix of the OWLOntology IRI
	 * 
	 * Given a owlClass getting the OWLOntology is thus not a simple call. The OWLOntology it stems from may not be loaded at all.
	 * Hence, maintain referencedOntologies that maps ontology IRIs to OWLOntologies that we have in memory.
	 * Map OWLClass IRI to ontology IRI to retrieve OWLOntology from referenced ontologies.
	 */
	//this is still very hacky, have to think about a better/more robust way of doing this
	//IRIs come in different formats, is there a standard?
	//http://purl.bioontology.org/obo/hao_01234
	//http://purl.obolibrary.org/obo/hao
	//http://www.etc-project.org/owl/ontologies/1/my_ontology#term
	private OWLOntology getOWLOntology(Collection collection, OWLClass owlClass) throws OntologyNotFoundException {
		List<Ontology> relevantOntologies;
		try {
			relevantOntologies = ontologyDAO.getOntologiesForCollection(collection);
		} catch (QueryException e) {
			throw new OntologyNotFoundException("Could not find ontology for class " + owlClass.getIRI().toString(), e);
		}
		
		String owlClassIRI = owlClass.getIRI().toString().toLowerCase();
		String hackyOwlClassIdentifier = owlClassIRI;
		if(owlClassIRI.contains("/")) {
			hackyOwlClassIdentifier = owlClassIRI.substring(owlClassIRI.lastIndexOf("/") + 1);
			if(hackyOwlClassIdentifier.contains("_"))
				hackyOwlClassIdentifier = hackyOwlClassIdentifier.substring(0, hackyOwlClassIdentifier.indexOf("_"));
			else if(hackyOwlClassIdentifier.contains("#"))
				hackyOwlClassIdentifier = hackyOwlClassIdentifier.substring(0, hackyOwlClassIdentifier.indexOf("#"));
		}
		
		for(Ontology ontology : relevantOntologies) {
			String ontologyIRI = ontology.getIri().toLowerCase();
			String hackyOntologyIdentifier = ontologyIRI;
			if(ontologyIRI.contains("/")) {
				hackyOntologyIdentifier = ontologyIRI.substring(ontologyIRI.lastIndexOf("/") + 1);
				if(hackyOntologyIdentifier.contains("_"))
					hackyOntologyIdentifier = hackyOntologyIdentifier.substring(0, hackyOntologyIdentifier.indexOf("_"));
				else if(hackyOntologyIdentifier.contains("#"))
					hackyOntologyIdentifier = hackyOntologyIdentifier.substring(0, hackyOntologyIdentifier.indexOf("#"));
			}
			if(hackyOntologyIdentifier.equals(hackyOwlClassIdentifier))
				return referencedOntologies.get(ontology);
		}
		
		/*for(OWLOntology ontology : owlOntologyManager.getOntologies()) {
			if(owlClass.isDefined(ontology))
				return ontology;
		}*/
		/*String iri = owlClass.getIRI().toString();
		if(iri.startsWith(Configuration.oboOntologyBaseIRI)) {
			String ontologyIRI = iri.substring(0, iri.lastIndexOf("_")).toLowerCase();
			OWLOntology ontology = referencedOntologies.get(ontologyIRI);
			return ontology;
		}
		if(iri.startsWith(Configuration.etcOntologyBaseIRI)) {
			String ontologyIRI = iri.substring(0, iri.lastIndexOf("#")).toLowerCase();
			OWLOntology ontology = referencedOntologies.get(ontologyIRI);
			return ontology;
		}*/
		throw new OntologyNotFoundException("Could not find ontology for class " + owlClass.getIRI().toString());
	}
	
	private boolean isSubclass(OWLOntology owlOntology, OWLClass subclass, OWLClass superclass) {
	    OWLReasoner reasoner = owlReasonerFactory.createReasoner(owlOntology, owlReasonerConfig);
	    reasoner.precomputeInferences();
	    return reasoner.getSuperClasses(subclass, false).containsEntity(superclass); //false: retrieval all ancestors.
	}

	private String getProperty(Collection collection, OWLClass owlClass, OWLAnnotationProperty annotationProperty) throws OntologyNotFoundException {
		OWLOntology owlOntology = getOWLOntology(collection, owlClass);
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
	
	private void addPartOfAxioms(Collection collection, OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, 
			OWLClass newOwlClass, OWLReasoner owlReasoner, PrefixManager prefixManager) throws OntologyFileException {
		//add part_of restrictions
		if(ontologyClassSubmission.hasPartOfIRI()) {
			if(ontologyClassSubmission.isQuality()) {
				//result.setMessage(result.getMessage()+" Part Of terms are not allowed for quality terms.");
			} else {
				String[] partOfIRIs = ontologyClassSubmission.getPartOfIRI().split("\\s*,\\s*");
				
				//subclasses of Entity
				for(String partOf : partOfIRIs) {
					//IRIs or terms
					if(partOf.isEmpty()) 
						continue;
					
					IRI partOfIRI = IRI.create(partOf);
					
					//to hold all classes related to the superClass
					Set<OWLClass> introducedClasses = new HashSet<OWLClass> ();
					
					OWLClass wholeOwlClass = null;
					if(partOfIRI.getScheme().equals("http")){
						//external 
						wholeOwlClass = owlDataFactory.getOWLClass(partOfIRI); //extract module
						OWLOntology moduleOntology;
						try {
							moduleOntology = extractModule(collection, ontologyClassSubmission, owlOntology, owlReasoner,  wholeOwlClass);
						} catch (OWLOntologyCreationException
								| OWLOntologyStorageException
								| OntologyNotFoundException e) {
							throw new OntologyFileException(e);
						}
						introducedClasses.addAll(moduleOntology.getClassesInSignature());
					} else {
						//local
						wholeOwlClass = owlDataFactory.getOWLClass(":" + partOf.replaceAll("\\s+", "_"), prefixManager); 
						introducedClasses.add(wholeOwlClass);
						
						//what about definition for wholeTerm?
						this.addLabelAxiom(owlOntology, wholeOwlClass, owlDataFactory.getOWLLiteral(partOf, "en"));
						this.addCreatedByAxiom(owlOntology, wholeOwlClass);
						this.addCreationDateAxiom(owlOntology, wholeOwlClass);
					}

					if(this.isSubclass(owlOntology, wholeOwlClass, qualityClass)) {
						//result.setMessage(result.getMessage()+" Entity '" + newTerm + "' can not be a part of quality '"+partOfIRI+"'.");	        		
					}else{
						//part of restriction
						addPartOfAxiom(owlOntology, wholeOwlClass, newOwlClass);
						for(OWLClass introducedClass : introducedClasses){
							addEntitySubclassAxiom(owlOntology, introducedClass);
						}
					}
				}
			}
		}
	}

	private void addSuperclassAxioms(Collection collection, OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission,
			OWLClass newOwlClass, OWLReasoner owlReasoner, PrefixManager prefixManager) throws OntologyFileException {
		boolean extractedSuperclassModule = ontologyClassSubmission.hasClassIRI();
		
		//add subclass axioms
		//if superTerm is an IRI (of known ontologies): 
		//if superTerm is a term (to local ontology):
		if(ontologyClassSubmission.hasSuperclassIRI() && !extractedSuperclassModule){
			String[] superclassIRIs = ontologyClassSubmission.getSuperclassIRI().split("\\s*,\\s*");
			for(String superclass : superclassIRIs){ //IRIs or terms
				if(superclass.isEmpty()) 
					continue;
				IRI superclassIRI = IRI.create(superclass);
				
				//to hold all classes related to the superClass
				Set<OWLClass> introducedClasses = new HashSet<OWLClass> ();
				
				OWLClass superOwlClass = null;
				if(superclassIRI.getScheme().equals("http")) {
					
					//extract mireot module related to superClass
					superOwlClass = owlDataFactory.getOWLClass(superclassIRI); 
					OWLOntology moduleOntology;
					try {
						moduleOntology = extractModule(collection, ontologyClassSubmission, owlOntology, owlReasoner, superOwlClass);
					} catch (OWLOntologyCreationException
							| OWLOntologyStorageException
							| OntologyNotFoundException e) {
						throw new OntologyFileException(e);
					}
					introducedClasses.addAll(moduleOntology.getClassesInSignature());
				
				} else {
					//allow to create a new superClass in local ontology, which will be a subclass of entity/quality
					superOwlClass = owlDataFactory.getOWLClass(":" + superclass.replaceAll("\\s+", "_"), prefixManager); //use ID here.
					introducedClasses.add(superOwlClass);
					
					this.addLabelAxiom(owlOntology, superOwlClass, owlDataFactory.getOWLLiteral(superclass, "en"));
					this.addCreatedByAxiom(owlOntology, superOwlClass);
					this.addCreationDateAxiom(owlOntology, superOwlClass);
					//what about definition for superClass?
				}
				
				//make all added class subclass of quality/entity
				if(ontologyClassSubmission.isQuality()) {
					if(this.isSubclass(owlOntology, superOwlClass, entityClass)) {
						//result.setMessage(result.getMessage()+" Can not add the quality term '"+newTerm+"' as a child to entity term '"+superclassIRI+"'.");
					} else {
						OWLAxiom subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(newOwlClass, superOwlClass);
						owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						for(OWLClass introducedClass : introducedClasses){
							subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(introducedClass, entityClass);
							owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						}
					}
				}
				
				if(ontologyClassSubmission.isEntity()) {
					if(this.isSubclass(owlOntology, superOwlClass, qualityClass)) {
						//result.setMessage(result.getMessage()+" Can not add the entity term '"+newTerm+"' as a child to quality term '"+superclassIRI+"'.");
					} else {
						OWLAxiom subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(newOwlClass, superOwlClass);
						owlOntologyManager.addAxiom(owlOntology, subclassAxiom);  
						for(OWLClass introducedClass : introducedClasses){
							subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(introducedClass, qualityClass);
							owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						}
					}
				}    	
			}
		}
	}

	private void addSynonymAxioms(OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, OWLClass newOwlClass) {
		//add synonyms
		if(ontologyClassSubmission.hasSynonyms()) {
			String[] synonyms =	ontologyClassSubmission.getSynonyms().split("\\s*,\\s*");
			for(String synonym : synonyms) {
				if(synonym.isEmpty())
					continue;
				this.addSynonymAxioms(owlOntology, synonym, newOwlClass);
			}
		}
	}
	
	private void addSynonymAxioms(OWLOntology owlOntology, String synonym, OWLClass owlClass) {
		OWLAnnotation synonymAnnotation = owlDataFactory.getOWLAnnotation(exactSynonymProperty, owlDataFactory.getOWLLiteral(synonym, "en"));
		OWLAxiom synonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), synonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, synonymAxiom);
	}

	private void addSuperClassModuleAxioms(OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, OWLClass newOwlClass) {
		boolean extractedSuperclassModule = ontologyClassSubmission.hasClassIRI();
		
		if(!extractedSuperclassModule){
			this.addDeclarationAxiom(owlOntology, newOwlClass);
			this.addDefinitionAxiom(owlOntology, newOwlClass, ontologyClassSubmission.getDefinition());
			this.addLabelAxiom(owlOntology, newOwlClass, owlDataFactory.getOWLLiteral(ontologyClassSubmission.getSubmissionTerm(), "en"));
			this.addCreatedByAxiom(owlOntology, newOwlClass);
			this.addCreationDateAxiom(owlOntology, newOwlClass);
			
			//add source info as comment
			if(ontologyClassSubmission.hasSource() || ontologyClassSubmission.hasSampleSentence()){
				OWLAnnotation commentAnnotation = owlDataFactory.getOWLAnnotation(owlDataFactory.getRDFSComment(), 
						owlDataFactory.getOWLLiteral("source: " + ontologyClassSubmission.getSampleSentence() + "[taken from: " + ontologyClassSubmission.getSource() + "]", "en"));
				OWLAxiom commentAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(newOwlClass.getIRI(), commentAnnotation);
				owlOntologyManager.addAxiom(owlOntology, commentAxiom);
			}
		}
	}
	
	private void addRelevantOntologies(Collection collection, OWLOntology owlOntology) throws OntologyFileException {
		List<Ontology> relevantOntologies = new LinkedList<Ontology>();
		try {
			relevantOntologies = ontologyDAO.getRelevantOntologiesForCollection(collection);
		} catch (QueryException e) {
			log(LogLevel.ERROR, "Could not add relevant ontologies", e);
		}
		for(Ontology relevantOntology : relevantOntologies) {
			if(!relevantOntology.hasCollectionId()) {
				addImportDeclaration(owlOntology, relevantOntology);
			}
		}
	}
	
	private void addImportDeclaration(OWLOntology owlOntology, Ontology relevantOntology) throws OntologyFileException {
		IRI relevantIRI = this.getLocalOntologyIRI(relevantOntology);
		OWLImportsDeclaration importDeclaraton = owlDataFactory.getOWLImportsDeclaration(relevantIRI);
		owlOntologyManager.applyChange(new AddImport(owlOntology, importDeclaraton));
		if (owlOntologyManager.getOntology(relevantIRI) == null)
			try {
				owlOntologyManager.addIRIMapper(createMapper(relevantOntology));
				try {
					owlOntologyManager.loadOntology(relevantIRI);
				} catch (OWLOntologyDocumentAlreadyExistsException e) {
					
				}
			} catch (OWLOntologyCreationException e) {
				log(LogLevel.ERROR, "Couldn't load ontology", e);
				throw new OntologyFileException(e);
			}
	}

	private void addDeclarationAxiom(OWLOntology owlOntology, OWLClass newOwlClass) {
		OWLAxiom declarationAxiom = owlDataFactory.getOWLDeclarationAxiom(newOwlClass);
		owlOntologyManager.addAxiom(owlOntology, declarationAxiom);
	}

	private void addDefinitionAxiom(OWLOntology owlOntology, OWLClass owlClass, String definition) {
		OWLAnnotation definitionAnnotation = owlDataFactory.getOWLAnnotation(definitionProperty, owlDataFactory.getOWLLiteral(definition, "en")); 
		OWLAxiom definitionAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), definitionAnnotation); 
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);
	}

	private void addPartOfAxiom(OWLOntology owlOntology, OWLClass wholeOwlClass, OWLClass partOwlClass) {
		OWLClassExpression partOfExpression = owlDataFactory.getOWLObjectSomeValuesFrom(partOfProperty, wholeOwlClass);
		OWLAxiom partOfAxiom = owlDataFactory.getOWLSubClassOfAxiom(partOwlClass, partOfExpression);
		owlOntologyManager.addAxiom(owlOntology, partOfAxiom);
	}

	private void addQualitySubclassAxiom(OWLOntology owlOntology,
			OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(owlClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
	}
	
	private void addEntitySubclassAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlDataFactory.getOWLSubClassOfAxiom(owlClass, entityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
	}

	private void addLabelAxiom(OWLOntology owlOntology, OWLClass owlClass, OWLLiteral classLabelLiteral) {
		OWLAnnotation labelAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, classLabelLiteral);
		OWLAxiom labelAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), labelAnnotation);
		owlOntologyManager.addAxiom(owlOntology, labelAxiom);
	}

	private void addCreationDateAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation creationDateAnnotation = owlDataFactory.getOWLAnnotation(creationDateProperty, owlDataFactory.getOWLLiteral(dateFormat.format(new Date())));
		OWLAxiom creationDateAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), creationDateAnnotation);
		owlOntologyManager.addAxiom(owlOntology, creationDateAxiom);
	}

	private void addCreatedByAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation createdByAnnotation = owlDataFactory.getOWLAnnotation(createdByProperty, owlDataFactory.getOWLLiteral(OtoSteps.user));
		OWLAxiom createdByAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlClass.getIRI(), createdByAnnotation);		
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);
	}
	
	private void addOntologyAxioms(OWLOntology owlOntology) {
		//add annotation properties
		//OWLAnnotationProperty label = factory
		//		.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		/*
		    <owl:AnnotationProperty rdf:about="http://purl.obolibrary.org/obo/IAO_0000115">
	        	<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">definition</rdfs:label>
	    	</owl:AnnotationProperty>
		 */
		//OWLAnnotationProperty annotation = factory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		OWLLiteral definitionLiteral = owlDataFactory.getOWLLiteral("definition");
		OWLAnnotation definitionAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, definitionLiteral);
		OWLAxiom definitionAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(definitionProperty.getIRI(), definitionAnnotation);
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);

		/*<owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym">
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_broad_synonym</rdfs:label>
    	</owl:AnnotationProperty>*/
		
		OWLLiteral hasBroadSynonymLiteral = owlDataFactory.getOWLLiteral("has_broad_synonym");
		OWLAnnotation broadSynonymAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, hasBroadSynonymLiteral);
		OWLAxiom broadSynonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(broadSynonymProperty.getIRI(), broadSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, broadSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasExactSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_exact_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasExactSynonymLiteral = owlDataFactory.getOWLLiteral("has_exact_synonym");
		OWLAnnotation exactSynonymAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, hasExactSynonymLiteral);
		OWLAxiom exactSynonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(exactSynonymProperty.getIRI(), exactSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, exactSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_narrow_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasNarrowSynonymLiteral = owlDataFactory.getOWLLiteral("has_narrow_synonym");
		OWLAnnotation narrowSynonymAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, hasNarrowSynonymLiteral);
		OWLAxiom narrowSynonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(narrowSynonymProperty.getIRI(), narrowSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, narrowSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_related_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasRelatedSynonymLiteral = owlDataFactory.getOWLLiteral("has_related_synonym");
		OWLAnnotation relatedSynonymAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, hasRelatedSynonymLiteral);
		OWLAxiom relatedSynonymAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(relatedSynonymProperty.getIRI(), relatedSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, relatedSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#created_by"/>*/
		OWLLiteral createdByLiteral = owlDataFactory.getOWLLiteral("created_by");
		OWLAnnotation createdByAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, createdByLiteral);
		OWLAxiom createdByAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(createdByProperty.getIRI(), createdByAnnotation);
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#creation_date"/>*/
		OWLLiteral creationDateLiteral = owlDataFactory.getOWLLiteral("creation_date");
		OWLAnnotation createionDateAnnotation = owlDataFactory.getOWLAnnotation(labelProperty, creationDateLiteral);
		OWLAxiom createionDateAxiom = owlDataFactory.getOWLAnnotationAssertionAxiom(creationDateProperty.getIRI(), createionDateAnnotation);
		owlOntologyManager.addAxiom(owlOntology, createionDateAxiom);

		//entity and quality classes and part_of, has_part relations are imported from ro, a "general" ontology
		//PrefixManager pm = new DefaultPrefixManager(
		//		Configuration.etc_ontology_baseIRI+prefix.toLowerCase()+"#");

		
		/*OWLClass entity = factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		OWLLiteral clabel = factory.getOWLLiteral("material anatomical entity", "en");
		axiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ont, axiom);
		axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), factory.getOWLAnnotation(label, clabel));
		manager.addAxiom(ont, axiom);

		OWLClass quality = factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		clabel = factory.getOWLLiteral("quality", "en");
		axiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ont, axiom);
		axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), factory.getOWLAnnotation(label, clabel));
		manager.addAxiom(ont, axiom);

		//has_part/part_of inverse object properties
		OWLObjectProperty hasPart = factory.getOWLObjectProperty(":has_part", pm);
		OWLObjectProperty partOf = factory.getOWLObjectProperty(":part_of", pm);
		manager.addAxiom(ont,
				factory.getOWLInverseObjectPropertiesAxiom(hasPart, partOf));

		manager.addAxiom(ont, factory.getOWLTransitiveObjectPropertyAxiom(partOf));
		manager.addAxiom(ont, factory.getOWLTransitiveObjectPropertyAxiom(hasPart));
		*/
		
		//disjoint entity and quality classes
		OWLAxiom disjointClassesAxiom = owlDataFactory.getOWLDisjointClassesAxiom(entityClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, disjointClassesAxiom);
	}
	
	private static File getCollectionOntologyDirectory(Ontology ontology) {
		File dir = new File(Configuration.collectionOntologyDirectory + File.separator + ontology.getCollectionId() + File.separator + ontology.getAcronym());
		dir.mkdirs();
		return dir;
	}
	
	private static File getCollectionOntologyFile(Ontology ontology) {
		return new File(getCollectionOntologyDirectory(ontology), ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	private static File getPermanentOntologyFile(Ontology ontology) {
		return new File(Configuration.permanentOntologyDirectory, ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	private static IRI getGlobalEtcIRI(Ontology ontology) {
		return IRI.create(ontology.getIri());
		//return IRI.create(Configuration.etcOntologyBaseIRI + ontology.getCollectionId() + "/" + ontology.getAcronym().toLowerCase());
	}
	
	private static IRI getGlobalOboIRI(Ontology ontology) {
		return IRI.create(ontology.getIri());
		//return IRI.create(Configuration.oboOntologyBaseIRI + ontology.getAcronym().toLowerCase());
	}

	private static IRI getLocalOntologyIRI(Ontology ontology) {
		if(ontology.hasCollectionId()) {
			return IRI.create(getCollectionOntologyFile(ontology));
		} else 
			return IRI.create(getPermanentOntologyFile(ontology));
	}

	private static OWLOntologyIRIMapper createMapper(Ontology ontology) {
		if(ontology.hasCollectionId())
			return new SimpleIRIMapper(getGlobalEtcIRI(ontology), getLocalOntologyIRI(ontology));
		else
			return new SimpleIRIMapper(getGlobalOboIRI(ontology), getLocalOntologyIRI(ontology));
	}
	
	private PrefixManager createPrefixManager(OntologyClassSubmission ontologyClassSubmission) {
		return new DefaultPrefixManager(Configuration.etcOntologyBaseIRI + ontologyClassSubmission.getOntology().getCollectionId() + "/" + 
				ontologyClassSubmission.getOntology().getAcronym() + "#");
	}
	
	public void setOntologyDAO(edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO ontologyDAO) {
		this.ontologyDAO = ontologyDAO;
	}

	public void setCollectionDAO(CollectionDAO collectionDAO) {
		this.collectionDAO = collectionDAO;
	}

	public void updateClassSubmission(Collection collection, OntologyClassSubmission submission) throws ClassExistsException, OntologyFileException {
		//OWLOntology owlOntology = this.getOwlOntology(collection, submission);
		//OWLClass owlClass = owlDataFactory.getOWLClass(IRI.create(submission.getClassIRI()));
		//owlClass
		this.removeClassSubmission(collection, submission);
		this.insertClassSubmission(collection, submission);
	}

	public void updateSynonymSubmission(Collection collection,  OntologySynonymSubmission submission) {
		this.removeSynonymSubmission(collection, submission);
		this.removeSynonymSubmission(collection, submission);
	}

	public void removeClassSubmission(Collection collection, OntologyClassSubmission submission) {
		
	}

	public void removeSynonymSubmission(Collection collection, OntologySynonymSubmission submission) {
		OWLOntology owlOntology = this.getOwlOntology(collection, submission);
		OWLClass owlClass = owlDataFactory.getOWLClass(IRI.create(submission.getClassIRI()));
		owlOntologyManager.
		owlOntologyManager.remove
	}

	public void removeOntology(Ontology ontology) {
		OWLOntology owlOntology = referencedOntologies.get(ontology);
		if(owlOntology != null) {
			owlOntologyManager.removeOntology(owlOntology);
			referencedOntologies.remove(ontology);
		}
	}
}
