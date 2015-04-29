package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyFileException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyNotFoundException;

public class AxiomAdder implements Serializable  {

	private OWLOntologyManager owlOntologyManager;
	private ModuleCreator moduleCreator;
	
	private OWLClass entityClass;
	private OWLClass qualityClass;
	private OWLObjectProperty partOfProperty;
	private OWLAnnotationProperty labelProperty;
	private OWLAnnotationProperty synonymProperty;
	private OWLAnnotationProperty definitionProperty;
	private OWLAnnotationProperty creationDateProperty;
	private OWLAnnotationProperty createdByProperty;
	private OWLAnnotationProperty relatedSynonymProperty;
	private OWLAnnotationProperty narrowSynonymProperty;
	private OWLAnnotationProperty exactSynonymProperty;
	private OWLAnnotationProperty broadSynonymProperty;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private OntologyReasoner ontologyReasoner;

	public AxiomAdder(OWLOntologyManager owlOntologyManager, ModuleCreator moduleCreator, OntologyReasoner ontologyReasoner) {
		this.owlOntologyManager = owlOntologyManager;
		this.moduleCreator = moduleCreator;
		this.ontologyReasoner = ontologyReasoner;
		
		entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		partOfProperty = owlOntologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"));
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		synonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"));
		definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		creationDateProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#creation_date"));
		createdByProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#created_by"));
		relatedSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"));
		narrowSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym"));
		exactSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym"));
		broadSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym"));
	}
	
	public void addSuperClassModuleAxioms(OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, OWLClass newOwlClass) {
		boolean extractedSuperclassModule = ontologyClassSubmission.hasClassIRI();
		
		if(!extractedSuperclassModule){
			this.addDeclarationAxiom(owlOntology, newOwlClass);
			this.addDefinitionAxiom(owlOntology, newOwlClass, ontologyClassSubmission.getDefinition());
			this.addLabelAxiom(owlOntology, newOwlClass, owlOntologyManager.getOWLDataFactory().getOWLLiteral(ontologyClassSubmission.getSubmissionTerm(), "en"));
			this.addCreatedByAxiom(owlOntology, newOwlClass);
			this.addCreationDateAxiom(owlOntology, newOwlClass);
			
			//add source info as comment
			if(ontologyClassSubmission.hasSource() || ontologyClassSubmission.hasSampleSentence()){
				OWLAnnotation commentAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getRDFSComment(), 
						owlOntologyManager.getOWLDataFactory().getOWLLiteral("source: " + ontologyClassSubmission.getSampleSentence() + "[taken from: " + ontologyClassSubmission.getSource() + "]", "en"));
				OWLAxiom commentAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(newOwlClass.getIRI(), commentAnnotation);
				owlOntologyManager.addAxiom(owlOntology, commentAxiom);
			}
		}
	}
	
	public void addSuperclassAxioms(Collection collection, OntologyClassSubmission submission,	OWLClass owlClass) throws OntologyFileException {
		boolean extractedSuperclassModule = submission.hasClassIRI();
		OWLOntology owlOntology = owlOntologyManager.getOntology(IRI.create(submission.getOntology().getIri()));
		
		//add subclass axioms
		//if superTerm is an IRI (of known ontologies): 
		//if superTerm is a term (to local ontology):
		if(submission.hasSuperclassIRI() && !extractedSuperclassModule){
			String[] superclassIRIs = submission.getSuperclassIRI().split("\\s*,\\s*");
			for(String superclass : superclassIRIs){ //IRIs or terms
				if(superclass.isEmpty()) 
					continue;
				IRI superclassIRI = IRI.create(superclass);
				
				//to hold all classes related to the superClass
				Set<OWLClass> introducedClasses = new HashSet<OWLClass> ();
				
				OWLClass superOwlClass = null;
				if(superclassIRI.getScheme().equals("http")) {
					
					//extract mireot module related to superClass
					superOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(superclassIRI); 
					OWLOntology moduleOntology;
					try {
						moduleOntology = moduleCreator.createModuleFromOwlClass(collection, submission, superOwlClass);
					} catch (OWLOntologyCreationException
							| OWLOntologyStorageException
							| OntologyNotFoundException e) {
						throw new OntologyFileException(e);
					}
					introducedClasses.addAll(moduleOntology.getClassesInSignature());
				
				} /*else {
					//allow to create a new superClass in local ontology, which will be a subclass of entity/quality
					superOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(":" + superclass.replaceAll("\\s+", "_"), prefixManager); //use ID here.
					introducedClasses.add(superOwlClass);
					
					this.addLabelAxiom(owlOntology, superOwlClass, owlOntologyManager.getOWLDataFactory().getOWLLiteral(superclass, "en"));
					this.addCreatedByAxiom(owlOntology, superOwlClass);
					this.addCreationDateAxiom(owlOntology, superOwlClass);
					//what about definition for superClass?
				}*/
				
				//make all added class subclass of quality/entity
				if(submission.isQuality()) {
					if(ontologyReasoner.isSubclass(owlOntology, superOwlClass, entityClass)) {
						//result.setMessage(result.getMessage()+" Can not add the quality term '"+newTerm+"' as a child to entity term '"+superclassIRI+"'.");
					} else {
						OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, superOwlClass);
						owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						for(OWLClass introducedClass : introducedClasses){
							subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(introducedClass, entityClass);
							owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						}
					}
				}
				
				if(submission.isEntity()) {
					if(ontologyReasoner.isSubclass(owlOntology, superOwlClass, qualityClass)) {
						//result.setMessage(result.getMessage()+" Can not add the entity term '"+newTerm+"' as a child to quality term '"+superclassIRI+"'.");
					} else {
						OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, superOwlClass);
						owlOntologyManager.addAxiom(owlOntology, subclassAxiom);  
						for(OWLClass introducedClass : introducedClasses){
							subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(introducedClass, qualityClass);
							owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
						}
					}
				}    	
			}
		}
	}
	
	public void addSynonymAxioms(OWLOntology owlOntology, OntologyClassSubmission ontologyClassSubmission, OWLClass newOwlClass) {
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
	
	public void addSynonymAxioms(OWLOntology owlOntology, String synonym, OWLClass owlClass) {
		OWLAnnotation synonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(exactSynonymProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(synonym, "en"));
		OWLAxiom synonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), synonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, synonymAxiom);
	}
	
	public void addDeclarationAxiom(OWLOntology owlOntology, OWLClass newOwlClass) {
		OWLAxiom declarationAxiom = owlOntologyManager.getOWLDataFactory().getOWLDeclarationAxiom(newOwlClass);
		owlOntologyManager.addAxiom(owlOntology, declarationAxiom);
	}

	public void addDefinitionAxiom(OWLOntology owlOntology, OWLClass owlClass, String definition) {
		OWLAnnotation definitionAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(definitionProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(definition, "en")); 
		OWLAxiom definitionAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), definitionAnnotation); 
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);
	}
	
	public void addPartOfAxioms(Collection collection, OntologyClassSubmission submission, OWLClass newOwlClass) throws OntologyFileException {
		OWLOntology owlOntology = owlOntologyManager.getOntology(IRI.create(submission.getOntology().getIri()));
		
		//add part_of restrictions
		if(submission.hasPartOfIRI()) {
			if(submission.isQuality()) {
				//result.setMessage(result.getMessage()+" Part Of terms are not allowed for quality terms.");
			} else {
				String[] partOfIRIs = submission.getPartOfIRI().split("\\s*,\\s*");
				
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
						wholeOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(partOfIRI); //extract module
						OWLOntology moduleOntology;
						try {
							moduleOntology =  moduleCreator.createModuleFromOwlClass(collection, submission, wholeOwlClass);
						} catch (OWLOntologyCreationException
								| OWLOntologyStorageException
								| OntologyNotFoundException e) {
							throw new OntologyFileException(e);
						}
						introducedClasses.addAll(moduleOntology.getClassesInSignature());
					} /*else {
						//local
						wholeOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(":" + partOf.replaceAll("\\s+", "_"), prefixManager); 
						introducedClasses.add(wholeOwlClass);
						
						//what about definition for wholeTerm?
						this.addLabelAxiom(owlOntology, wholeOwlClass, owlDataFactory.getOWLLiteral(partOf, "en"));
						this.addCreatedByAxiom(owlOntology, wholeOwlClass);
						this.addCreationDateAxiom(owlOntology, wholeOwlClass);
					}*/

					if(ontologyReasoner.isSubclass(owlOntology, wholeOwlClass, qualityClass)) {
						//result.setMessage(result.getMessage()+" Entity '" + newTerm + "' can not be a part of quality '"+partOfIRI+"'.");	        		
					} else {
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

	public void addPartOfAxiom(OWLOntology owlOntology, OWLClass wholeOwlClass, OWLClass partOwlClass) {
		OWLClassExpression partOfExpression = owlOntologyManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(partOfProperty, wholeOwlClass);
		OWLAxiom partOfAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(partOwlClass, partOfExpression);
		owlOntologyManager.addAxiom(owlOntology, partOfAxiom);
	}

	public void addQualitySubclassAxiom(OWLOntology owlOntology,
			OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
	}
	
	public void addEntitySubclassAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, entityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
	}

	public void addLabelAxiom(OWLOntology owlOntology, OWLClass owlClass, OWLLiteral classLabelLiteral) {
		OWLAnnotation labelAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, classLabelLiteral);
		OWLAxiom labelAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), labelAnnotation);
		owlOntologyManager.addAxiom(owlOntology, labelAxiom);
	}

	public void addCreationDateAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation creationDateAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(creationDateProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(dateFormat.format(new Date())));
		OWLAxiom creationDateAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), creationDateAnnotation);
		owlOntologyManager.addAxiom(owlOntology, creationDateAxiom);
	}

	public void addCreatedByAxiom(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation createdByAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(createdByProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(OtoSteps.user));
		OWLAxiom createdByAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), createdByAnnotation);		
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);
	}
	
	public void addOntologyAxioms(OWLOntology owlOntology) {
		//add annotation properties
		//OWLAnnotationProperty label = factory
		//		.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		/*
		    <owl:AnnotationProperty rdf:about="http://purl.obolibrary.org/obo/IAO_0000115">
	        	<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">definition</rdfs:label>
	    	</owl:AnnotationProperty>
		 */
		//OWLAnnotationProperty annotation = factory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		OWLLiteral definitionLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("definition");
		OWLAnnotation definitionAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, definitionLiteral);
		OWLAxiom definitionAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(definitionProperty.getIRI(), definitionAnnotation);
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);

		/*<owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym">
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_broad_synonym</rdfs:label>
    	</owl:AnnotationProperty>*/
		
		OWLLiteral hasBroadSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_broad_synonym");
		OWLAnnotation broadSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasBroadSynonymLiteral);
		OWLAxiom broadSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(broadSynonymProperty.getIRI(), broadSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, broadSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasExactSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_exact_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasExactSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_exact_synonym");
		OWLAnnotation exactSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasExactSynonymLiteral);
		OWLAxiom exactSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(exactSynonymProperty.getIRI(), exactSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, exactSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_narrow_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasNarrowSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_narrow_synonym");
		OWLAnnotation narrowSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasNarrowSynonymLiteral);
		OWLAxiom narrowSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(narrowSynonymProperty.getIRI(), narrowSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, narrowSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_related_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasRelatedSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_related_synonym");
		OWLAnnotation relatedSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasRelatedSynonymLiteral);
		OWLAxiom relatedSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(relatedSynonymProperty.getIRI(), relatedSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, relatedSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#created_by"/>*/
		OWLLiteral createdByLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("created_by");
		OWLAnnotation createdByAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, createdByLiteral);
		OWLAxiom createdByAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(createdByProperty.getIRI(), createdByAnnotation);
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#creation_date"/>*/
		OWLLiteral creationDateLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("creation_date");
		OWLAnnotation createionDateAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, creationDateLiteral);
		OWLAxiom createionDateAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(creationDateProperty.getIRI(), createionDateAnnotation);
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
		OWLAxiom disjointClassesAxiom = owlOntologyManager.getOWLDataFactory().getOWLDisjointClassesAxiom(entityClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, disjointClassesAxiom);
	}

	public void removeSynonymAxioms(OWLOntology targetOwlOntology,
			String submissionTerm, OWLClass owlClass) {
		// TODO Auto-generated method stub
		
	}
	
	private void writeObject(java.io.ObjectOutputStream out)  throws IOException {
		 out.defaultWriteObject();
	 }
	
}
