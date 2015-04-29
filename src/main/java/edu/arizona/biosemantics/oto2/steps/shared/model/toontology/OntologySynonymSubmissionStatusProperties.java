package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface OntologySynonymSubmissionStatusProperties extends PropertyAccess<OntologySynonymSubmissionStatus> {

	  @Path("id")
	  ModelKeyProvider<OntologySynonymSubmissionStatus> key();
	 
	  ValueProvider<OntologySynonymSubmissionStatus, Integer> ontologyClassSubmissionId();
	  
	  ValueProvider<OntologySynonymSubmissionStatus, String> status();

	  ValueProvider<OntologySynonymSubmissionStatus, String> iri();
}