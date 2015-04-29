package edu.arizona.biosemantics.oto2.steps.server.persist.file;

import java.io.Serializable;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.arizona.biosemantics.oto2.steps.server.persist.DAOManager;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyNotFoundException;

public class OWLOntologyRetriever implements Serializable  {
	
	private OWLOntologyManager owlOntologyManager;
	private OntologyDAO ontologyDBDAO;
	
	public OWLOntologyRetriever(OWLOntologyManager owlOntologyManager, OntologyDAO ontologyDBDAO) {
		this.owlOntologyManager = owlOntologyManager;
		this.ontologyDBDAO = ontologyDBDAO;
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
	public OWLOntology getOWLOntology(Collection collection, OWLClass owlClass) throws OntologyNotFoundException {
		List<Ontology> relevantOntologies;
		try {
			relevantOntologies = ontologyDBDAO.getOntologiesForCollection(collection);
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
				return owlOntologyManager.getOntology(IRI.create(ontology.getIri()));
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
}
