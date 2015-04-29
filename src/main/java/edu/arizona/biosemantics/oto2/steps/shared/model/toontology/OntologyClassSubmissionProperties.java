package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import java.util.List;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public interface OntologyClassSubmissionProperties extends PropertyAccess<OntologyClassSubmission> {

	  @Path("id")
	  ModelKeyProvider<OntologyClassSubmission> key();
	 
	  ValueProvider<OntologyClassSubmission, Term> term();
	  
	  ValueProvider<OntologyClassSubmission, String> submissionTerm();
	  
	  ValueProvider<OntologyClassSubmission, Ontology> ontology();
	  
	  ValueProvider<OntologyClassSubmission, String> superclassIRI();
	  
	  ValueProvider<OntologyClassSubmission, String> definition();
	  
	  ValueProvider<OntologyClassSubmission, String> synonyms();
	  
	  ValueProvider<OntologyClassSubmission, String> source();
	  
	  ValueProvider<OntologyClassSubmission, String> sampleSentence();
	  
	  ValueProvider<OntologyClassSubmission, String> partOfIRI();

	  ValueProvider<OntologyClassSubmission, Boolean> entity();
	  
	  ValueProvider<OntologyClassSubmission, Boolean> quality();
	  
	  ValueProvider<OntologyClassSubmission, String> user();
	  
	  ValueProvider<OntologyClassSubmission, List<OntologyClassSubmissionStatus>> submissionStatuses();
	
}