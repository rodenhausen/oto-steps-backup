package edu.arizona.biosemantics.oto2.steps.server.rpc;

import java.util.List;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.Configuration;
import edu.arizona.biosemantics.oto2.steps.server.persist.DAOManager;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.ClassExistsException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.CreateOntologyException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.CreateSynonymSubmissionException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyBioportalException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyExistsException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.OntologyFileException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.CreateClassSubmissionException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.RemoveClassSubmissionException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.RemoveSynonymSubmissionException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.UpdateClassSubmissionException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.UpdateSynonymSubmissionException;

/**
 * Note: Not trivial to guarantee consistency between all three data stores (DB, OWL File, Bioportal), e.g. could end up in a situation where 
 * where DB throws exception, want to revert bioportal changes, bioportal fails aswell.
 * Thus, only a best effort.
 * @author rodenhausen
 *
 */
public class ToOntologyService extends RemoteServiceServlet implements IToOntologyService {

	private DAOManager daoManager = new DAOManager();
	
	@Override
	public List<Ontology> getOntologies(Collection collection) throws Exception {
		return daoManager.getOntologyDBDAO().getOntologiesForCollection(collection);
	}
	
	@Override
	public List<OntologyClassSubmission> getClassSubmissions(Collection collection) throws Exception {
		return daoManager.getOntologyClassSubmissionDAO().get(collection);
	}

	@Override
	public List<OntologySynonymSubmission> getSynonymSubmissions(Collection collection) throws Exception {
		return daoManager.getOntologySynonymSubmissionDAO().get(collection);
	}
	
	@Override
	public Ontology createOntology(Collection collection, Ontology ontology) throws CreateOntologyException {
		ontology.setIri(Configuration.etcOntologyBaseIRI +collection.getId() + "/" + ontology.getAcronym());
		try {
			daoManager.getOntologyFileDAO().insertOntology(collection, ontology);
		} catch (OntologyFileException e) {
			daoManager.getOntologyFileDAO().removeOntology(collection, ontology);
			throw new CreateOntologyException(e);
		}
		
		try {
			ontology = daoManager.getOntologyDBDAO().insert(ontology);
		} catch (QueryException e) {
			try {
				daoManager.getOntologyDBDAO().remove(ontology);
			} catch (QueryException re) {
				log(LogLevel.ERROR, "Couldn't remove ontology from DB where creation of ontology overall failed!", e);
			}
			daoManager.getOntologyFileDAO().removeOntology(collection, ontology);
			throw new CreateOntologyException(e);
		}
		return ontology;
	}

	@Override
	public OntologyClassSubmission createClassSubmission(OntologyClassSubmission submission) throws CreateClassSubmissionException, OntologyBioportalException, 
			OntologyFileException, ClassExistsException {
		Collection collection;
		try {
			collection = daoManager.getCollectionDAO().get(submission.getTerm().getCollectionId());
		} catch (QueryException | IOException e) {
			throw new CreateClassSubmissionException(e);
		}
		
		String classIRI = null;
		if(submission.getOntology().hasCollectionId()) {
			classIRI = daoManager.getOntologyFileDAO().insertClassSubmission(collection, submission);
		} else {
			try {
				daoManager.getOntologyBioportalDAO().insertClassSubmission(collection, submission);
			} catch (InterruptedException | ExecutionException e) {
				try {
					daoManager.getOntologyBioportalDAO().removeClassSubmission(submission);
				} catch (InterruptedException | ExecutionException e1) {
					log(LogLevel.ERROR, "Couldn't remove class submission from bioportal where creation of submission at bioportal failed!", e1);
				}
				log(LogLevel.ERROR, "Couldn't insert at bioportal", e);
				throw new OntologyBioportalException(e);
			}
		}
		try {
			submission = daoManager.getOntologyClassSubmissionDAO().insert(submission);
			if(classIRI != null)	
				setAccepted(submission, classIRI);
			else 
				setPending(submission);
		} catch (QueryException e) {
			if(submission.getOntology().hasCollectionId()) {
				daoManager.getOntologyFileDAO().removeClassSubmission(collection, submission);
			} else {
				try {
					daoManager.getOntologyBioportalDAO().removeClassSubmission(submission);
				} catch (InterruptedException | ExecutionException e1) {
					log(LogLevel.ERROR, "Couldn't remove class submission from bioportal where creation of submission in db failed!", e1);
				}
			}
			throw new CreateClassSubmissionException(e);
		}
		
		return submission;
	}

	@Override
	public OntologySynonymSubmission createSynonymSubmission(OntologySynonymSubmission submission) throws OntologyFileException, OntologyBioportalException, CreateSynonymSubmissionException {
		Collection collection;
		try {
			collection = daoManager.getCollectionDAO().get(submission.getTerm().getCollectionId());
		} catch (QueryException | IOException e) {
			throw new CreateSynonymSubmissionException(e);
		}
		String classIRI = null;
		if(submission.getOntology().hasCollectionId()) {
			classIRI = daoManager.getOntologyFileDAO().insertSynonymSubmission(collection, submission);
		} else {
			try {
				daoManager.getOntologyBioportalDAO().insertSynonymSubmission(collection, submission);
			} catch (InterruptedException | ExecutionException e) {
				try {
					daoManager.getOntologyBioportalDAO().removeSynonymSubmission(submission);
				} catch (InterruptedException | ExecutionException e1) {
					log(LogLevel.ERROR, "Couldn't remove synonym submission from bioportal where creation of submission at bioportal failed!", e1);
				}
				log(LogLevel.ERROR, "Couldn't insert at bioportal", e);
				throw new OntologyBioportalException(e);
			}
		}
		try {
			daoManager.getOntologySynonymSubmissionDAO().insert(submission);
			if(classIRI != null)
				setAccepted(submission, classIRI);
			else 
				setPending(submission);
		} catch (QueryException e) {
			if(submission.getOntology().hasCollectionId()) {
				daoManager.getOntologyFileDAO().removeSynonymSubmission(collection, submission);
			} else {
				try {
					daoManager.getOntologyBioportalDAO().removeSynonymSubmission(submission);
				} catch (InterruptedException | ExecutionException e1) {
					log(LogLevel.ERROR, "Couldn't remove synonym submission from bioportal where creation of submission in db failed!", e1);
				}
			}
			throw new CreateSynonymSubmissionException(e);
		}		
		return submission;
	}
	
	/**
	 * There's still open questions about this: What to do with a bioportal submission that has been accepted.
	 * Should it still be possible to edit that submission? Would that be a resubmissoin then since the original
	 * submisison is already permanently accepted?
	 */
	@Override
	public void updateClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> submissions) throws OntologyBioportalException, UpdateClassSubmissionException {
		for(OntologyClassSubmission submission : submissions) {
			if(submission.getOntology().hasCollectionId()) {
				try {
					daoManager.getOntologyFileDAO().updateClassSubmission(collection, submission);
				} catch (ClassExistsException | OntologyFileException e) {
					log(LogLevel.ERROR, "Couldn't update ontology file", e);
					throw new UpdateClassSubmissionException(e);
				}
			} else {
				try {
					daoManager.getOntologyBioportalDAO().updateClassSubmission(collection, submission);
				} catch (InterruptedException | ExecutionException e) {
					log(LogLevel.ERROR, "Couldn't update at bioportal", e);
					throw new OntologyBioportalException(e);
				}
			}
			try {
				daoManager.getOntologyClassSubmissionDAO().update(submission);
			} catch (QueryException e) {
				throw new UpdateClassSubmissionException(e);
			}
		}
	}

	@Override
	public void updateSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> submissions) throws OntologyBioportalException, UpdateSynonymSubmissionException {
		for(OntologySynonymSubmission submission : submissions) { 
			if(submission.getOntology().hasCollectionId()) {
				try {
					daoManager.getOntologyFileDAO().updateSynonymSubmission(collection, submission);
				} catch (OntologyFileException e) {
					log(LogLevel.ERROR, "Couldn't update ontology file", e);
					throw new UpdateSynonymSubmissionException(e);
				}
			} else {
				try {
					daoManager.getOntologyBioportalDAO().updateSynonymSubmission(collection, submission);
				} catch (InterruptedException | ExecutionException e) {
					log(LogLevel.ERROR, "Couldn't update at bioportal", e);
					throw new OntologyBioportalException(e);
				}
			}
			try {
				daoManager.getOntologySynonymSubmissionDAO().update(submission);
			} catch (QueryException e) {
				throw new UpdateSynonymSubmissionException(e);
			}
		}
	}

	@Override
	public void removeClassSubmissions(Collection collection, 
			java.util.Collection<OntologyClassSubmission> submissions) throws OntologyBioportalException, RemoveClassSubmissionException {
		for(OntologyClassSubmission submission : submissions) {
			if(submission.getOntology().hasCollectionId()) {
				daoManager.getOntologyFileDAO().removeClassSubmission(collection, submission);
			} else {
				try {
					daoManager.getOntologyBioportalDAO().removeClassSubmission(submission);
				} catch (InterruptedException | ExecutionException e) {
					log(LogLevel.ERROR, "Couldn't remove at bioportal", e);
					throw new OntologyBioportalException(e);
				}
			}
			try {
				daoManager.getOntologyClassSubmissionDAO().remove(submission);
			} catch (QueryException e) {
				throw new RemoveClassSubmissionException(e);
			}
		}
	}

	@Override
	public void removeSynonymSubmissions(Collection collection, 
			java.util.Collection<OntologySynonymSubmission> submissions) throws OntologyBioportalException, RemoveSynonymSubmissionException {
		for(OntologySynonymSubmission submission : submissions) {
			if(submission.getOntology().hasCollectionId()) {
				try {
					daoManager.getOntologyFileDAO().removeSynonymSubmission(collection, submission);
				} catch (OntologyFileException e) {
					log(LogLevel.ERROR, "Couldn't update ontology file", e);
					throw new RemoveSynonymSubmissionException(e);
				}
			} else {
				try {
					daoManager.getOntologyBioportalDAO().removeSynonymSubmission(submission);
				} catch (InterruptedException | ExecutionException e) {
					log(LogLevel.ERROR, "Couldn't remove at bioportal", e);
					throw new OntologyBioportalException(e);
				}
			}
			try {
				daoManager.getOntologySynonymSubmissionDAO().remove(submission);
			} catch (QueryException e) {
				throw new RemoveSynonymSubmissionException(e);
			}
		}
	}
	
	@Override
	public void refreshSubmissionStatuses(Collection collection) throws Exception {
		daoManager.getOntologyBioportalDAO().refreshStatuses(collection);
	}

	
	private void setAccepted(OntologySubmission submission, String classIRI) throws QueryException {
		this.setStatus(submission, classIRI, StatusEnum.ACCEPTED);
	}
	
	private void setPending(OntologySubmission submission) throws QueryException {
		this.setStatus(submission, StatusEnum.PENDING);
	}
	
	private void setRejected(OntologySubmission submission) throws QueryException {
		this.setStatus(submission, StatusEnum.REJECTED);
	}
	
	private void setStatus(OntologySubmission submission, String classIRI, StatusEnum status) throws QueryException {
		if(submission instanceof OntologyClassSubmission) {
			OntologyClassSubmission ontologyClassSubmission = (OntologyClassSubmission) submission;
			OntologyClassSubmissionStatus ontologyClassSubmissionStatus = new OntologyClassSubmissionStatus(ontologyClassSubmission.getId(), 
					daoManager.getStatusDAO().get(status.getDisplayName()), classIRI);
			daoManager.getOntologyClassSubmissionStatusDAO().insert(ontologyClassSubmissionStatus);
		}
		if(submission instanceof OntologySynonymSubmission) {
			OntologySynonymSubmission ontologySynonymSubmission = (OntologySynonymSubmission) submission;
			OntologySynonymSubmissionStatus ontologyClassSubmissionStatus = new OntologySynonymSubmissionStatus(ontologySynonymSubmission.getId(), 
					daoManager.getStatusDAO().get(status.getDisplayName()), classIRI);
			daoManager.getOntologySynonymSubmissionStatusDAO().insert(ontologyClassSubmissionStatus);
		}
	}
	
	private void setStatus(OntologySubmission submission, StatusEnum status) throws QueryException {
		this.setStatus(submission, "", status);
	}

}
