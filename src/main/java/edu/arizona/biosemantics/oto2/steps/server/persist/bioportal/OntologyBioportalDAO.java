package edu.arizona.biosemantics.oto2.steps.server.persist.bioportal;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import edu.arizona.biosemantics.bioportal.client.BioPortalClient;
import edu.arizona.biosemantics.bioportal.model.ProvisionalClass;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyClassSubmissionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyClassSubmissionStatusDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologySynonymSubmissionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologySynonymSubmissionStatusDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.StatusDAO;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum;

public class OntologyBioportalDAO {

	private StatusDAO statusDAO;
	private OntologyClassSubmissionDAO ontologyClassSubmissionDAO;
	private OntologySynonymSubmissionDAO ontologySynonymSubmissionDAO;
	private OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO;
	private OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO;
	
	public void refreshStatuses(Collection collection) throws QueryException {
		List<OntologyClassSubmission> ontologyClassSubmissions = ontologyClassSubmissionDAO.get(collection, StatusEnum.PENDING);
		List<OntologySynonymSubmission> ontologySynonymSubmissions = ontologySynonymSubmissionDAO.get(collection, StatusEnum.PENDING);
		
		for(OntologyClassSubmission ontologyClassSubmission : ontologyClassSubmissions) {
			refresh(ontologyClassSubmission);
		}
		for(OntologySynonymSubmission ontologySynonymSubmission : ontologySynonymSubmissions) {
			refresh(ontologySynonymSubmission);
		}
	}
	
	public String insertClassSubmission(Collection collection, OntologyClassSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey);) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = 
					bioPortalClient.postProvisionalClass(createProvisionalClass(collection, submission));
			String temporaryIRI = result.get().getId();
			return temporaryIRI;
		}
	}

	public String insertSynonymSubmission(Collection collection, OntologySynonymSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey);) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = 
					bioPortalClient.postProvisionalClass(createProvisionalClass(collection, submission));
			String temporaryIRI = result.get().getId();
			return temporaryIRI;
		}
	}
	
	public String updateClassSubmission(Collection collection, OntologyClassSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey)) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = bioPortalClient.patchProvisionalClass(createProvisionalClass(collection, submission));
			String temporaryIRI = result.get().getId();
			return temporaryIRI;
		}
	}

	public String updateSynonymSubmission(Collection collection, OntologySynonymSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey)) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = bioPortalClient.patchProvisionalClass(createProvisionalClass(collection, submission));
			String temporaryIRI = result.get().getId();
			return temporaryIRI;
		}
	}

	public void removeClassSubmission(OntologyClassSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey)) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = bioPortalClient.deleteProvisionalClass(getPendingStatus(submission).getIri());
			result.get();
		}
	}

	public void removeSynonymSubmission(OntologySynonymSubmission submission) throws InterruptedException, ExecutionException {
		try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey)) {
			bioPortalClient.open();
			Future<ProvisionalClass> result = bioPortalClient.deleteProvisionalClass(getPendingStatus(submission).getIri());
			result.get();
		}
	}

	public void setOntologyClassSubmissionDAO(
			OntologyClassSubmissionDAO ontologyClassSubmissionDAO) {
		this.ontologyClassSubmissionDAO = ontologyClassSubmissionDAO;
	}

	public void setOntologySynonymSubmissionDAO(
			OntologySynonymSubmissionDAO ontologySynonymSubmissionDAO) {
		this.ontologySynonymSubmissionDAO = ontologySynonymSubmissionDAO;
	}

	public void setOntologyClassSubmissionStatusDAO(
			OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO) {
		this.ontologyClassSubmissionStatusDAO = ontologyClassSubmissionStatusDAO;
	}

	public void setOntologySynonymSubmissionStatusDAO(
			OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO) {
		this.ontologySynonymSubmissionStatusDAO = ontologySynonymSubmissionStatusDAO;
	}

	private ProvisionalClass createProvisionalClass(Collection collection, OntologySynonymSubmission submission) {
		ProvisionalClass provisionalClass = new ProvisionalClass();
		provisionalClass.setLabel(submission.getSubmissionTerm());
		List<String> synonyms = new LinkedList<String>();
		synonyms.add(submission.getSynonyms());
		provisionalClass.setSynonym(synonyms);
		List<String> ontologies = new LinkedList<String>();
		ontologies.add(submission.getOntology().getIri());
		provisionalClass.setOntology(ontologies);
		provisionalClass.setCreator(OtoSteps.user);
		return provisionalClass;
	}
	
	private ProvisionalClass createProvisionalClass(Collection collection, OntologyClassSubmission submission) {
		String definitionToSubmit = submission.getDefinition() + " "
				+ "[this term has been used in sentence '"
				+ submission.getSampleSentence() + "' in source '"
				+ submission.getSource() + "']";
		
		ProvisionalClass provisionalClass = new ProvisionalClass();
		provisionalClass.setLabel(submission.getSubmissionTerm());
		List<String> definitions = new LinkedList<String>();
		definitions.add(definitionToSubmit);
		provisionalClass.setDefinition(definitions);
		provisionalClass.setSubclassOf(submission.getSuperclassIRI());
		List<String> synonyms = new LinkedList<String>();
		synonyms.add(submission.getSynonyms());
		provisionalClass.setSynonym(synonyms);
		List<String> ontologies = new LinkedList<String>();
		ontologies.add(submission.getOntology().getIri());
		provisionalClass.setOntology(ontologies);
		provisionalClass.setCreator(OtoSteps.user);
		return provisionalClass;
	}
	
	private void refresh(OntologySynonymSubmission ontologySynonymSubmission) throws QueryException {
		OntologySynonymSubmissionStatus pendingStatus = getPendingStatus(ontologySynonymSubmission);
		if(isAccepted(ontologySynonymSubmission)) {
			try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey);) {
				bioPortalClient.open();
				Future<ProvisionalClass> result = bioPortalClient.getProvisionalClass(pendingStatus.getIri());
				ProvisionalClass provisionalClass = result.get();
				String permanentId = provisionalClass.getPermanentId();
				if(permanentId != null && !permanentId.isEmpty()) {
					edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status status = statusDAO.get(StatusEnum.ACCEPTED.getDisplayName());
					OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus = new OntologySynonymSubmissionStatus(
							ontologySynonymSubmission.getId(), status, permanentId);
					ontologySynonymSubmissionStatusDAO.insert(ontologySynonymSubmissionStatus);
				}
			} catch (InterruptedException | ExecutionException e) {
				log(LogLevel.ERROR, "Problem refreshing status", e);
			}
		}
	}

	private boolean isAccepted(OntologySynonymSubmission ontologySynonymSubmission) {
		for(OntologySynonymSubmissionStatus status : ontologySynonymSubmission.getSubmissionStatuses()) {
			if(status.getStatus().equals(StatusEnum.ACCEPTED)) {
				return true;
			}
		}
		return false;
	}

	private OntologySynonymSubmissionStatus getPendingStatus(OntologySynonymSubmission ontologySynonymSubmission) {
		for(OntologySynonymSubmissionStatus status : ontologySynonymSubmission.getSubmissionStatuses()) {
			if(status.getStatus().equals(StatusEnum.PENDING)) {
				return status;
			}
		}
		return null;
	}

	private void refresh(OntologyClassSubmission ontologyClassSubmission) throws QueryException {
		OntologyClassSubmissionStatus pendingStatus = getPendingStatus(ontologyClassSubmission);
		if(!isAccepted(ontologyClassSubmission)) {
			try(BioPortalClient bioPortalClient = new BioPortalClient(Configuration.bioportalUrl, Configuration.bioportalApiKey);) {
				bioPortalClient.open();
				Future<ProvisionalClass> result = bioPortalClient.getProvisionalClass(pendingStatus.getIri());
				ProvisionalClass provisionalClass = result.get();
				String permanentId = provisionalClass.getPermanentId();
				if(permanentId != null && !permanentId.isEmpty()) {
					edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status status = statusDAO.get(StatusEnum.ACCEPTED.getDisplayName());
					OntologyClassSubmissionStatus ontologyClassSubmissionStatus = new OntologyClassSubmissionStatus(
							ontologyClassSubmission.getId(), status, permanentId);
					ontologyClassSubmissionStatusDAO.insert(ontologyClassSubmissionStatus);
				}
			} catch (InterruptedException | ExecutionException e) {
				log(LogLevel.ERROR, "Problem refreshing status", e);
			}
		}
	}

	private OntologyClassSubmissionStatus getPendingStatus(OntologyClassSubmission ontologyClassSubmission) {
		for(OntologyClassSubmissionStatus status : ontologyClassSubmission.getSubmissionStatuses()) {
			if(status.getStatus().equals(StatusEnum.PENDING)) {
				return status;
			}
		}
		return null;
	}

	private boolean isAccepted(OntologyClassSubmission ontologyClassSubmission) {
		for(OntologyClassSubmissionStatus status : ontologyClassSubmission.getSubmissionStatuses()) {
			if(status.getStatus().equals(StatusEnum.ACCEPTED)) {
				return true;
			}
		}
		return false;
	}
}
