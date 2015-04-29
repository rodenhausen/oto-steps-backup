package edu.arizona.biosemantics.oto2.steps.shared.model.toontology;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface OntologyClassSubmissionStatusProperties extends PropertyAccess<OntologyClassSubmissionStatus> {

	  @Path("id")
	  ModelKeyProvider<OntologyClassSubmissionStatus> key();
	 
	  ValueProvider<OntologyClassSubmissionStatus, Integer> ontologyClassSubmissionId();
	  
	  ValueProvider<OntologyClassSubmissionStatus, String> status();

	  ValueProvider<OntologyClassSubmissionStatus, String> iri();
}