package edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

@RemoteServiceRelativePath("toOntology")
public interface IToOntologyService extends RemoteService {
	
	public List<Ontology> getOntologies(Collection collection) throws Exception;
	
	public OntologyClassSubmission createClassSubmission(OntologyClassSubmission submission) throws CreateClassSubmissionException, OntologyBioportalException, OntologyFileException, ClassExistsException;

	public OntologySynonymSubmission createSynonymSubmission(OntologySynonymSubmission submission) throws CreateSynonymSubmissionException, OntologyFileException, OntologyBioportalException;
	
	public List<OntologyClassSubmission> getClassSubmissions(Collection collection) throws  Exception;

	public List<OntologySynonymSubmission> getSynonymSubmissions(Collection collection) throws Exception;
	
	public Ontology createOntology(Collection collection, Ontology ontology) throws CreateOntologyException;
	
	public void refreshSubmissionStatuses(Collection collection) throws Exception;
	
	public void updateClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> ontologyClassSubmissions) throws UpdateClassSubmissionException, OntologyBioportalException;

	public void updateSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> ontologySynonymSubmissions) throws UpdateSynonymSubmissionException, OntologyBioportalException;
	
	public void removeClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> ontologyClassSubmissions) throws RemoveClassSubmissionException, OntologyBioportalException;
	
	public void removeSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> ontologySynonymSubmissions) throws RemoveSynonymSubmissionException, OntologyBioportalException;
}
