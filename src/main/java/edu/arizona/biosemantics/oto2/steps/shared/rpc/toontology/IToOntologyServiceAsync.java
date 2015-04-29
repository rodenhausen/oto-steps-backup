package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

public interface IToOntologyServiceAsync {

	public void getOntologies(Collection collection, AsyncCallback<List<Ontology>> callback);

	public void createClassSubmission(OntologyClassSubmission submission, AsyncCallback<OntologyClassSubmission> callback);

	public void createSynonymSubmission(OntologySynonymSubmission submission, AsyncCallback<OntologySynonymSubmission> callback);

	public void getClassSubmissions(Collection collection, AsyncCallback<List<OntologyClassSubmission>> callback);

	public void getSynonymSubmissions(Collection collection, AsyncCallback<List<OntologySynonymSubmission>> callback);

	public void createOntology(Collection collection, Ontology ontology, AsyncCallback<Ontology> callback);
	
	public void refreshSubmissionStatuses(Collection collection, AsyncCallback<Void> callback);
	
	public void updateClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> ontologyClassSubmissions, 
			AsyncCallback<Void> callback);

	public void updateSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> ontologySynonymSubmissions, 
			AsyncCallback<Void> callback);
	
	public void removeClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> ontologyClassSubmissions, 
			AsyncCallback<Void> callback);
	
	public void removeSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> ontologySynonymSubmissions, 
			AsyncCallback<Void> callbackw);
}
