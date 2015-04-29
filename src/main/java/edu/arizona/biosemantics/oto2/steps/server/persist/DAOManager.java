package edu.arizona.biosemantics.oto2.steps.server.persist;

import edu.arizona.biosemantics.oto2.steps.server.persist.bioportal.OntologyBioportalDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.CollectionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.ContextDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyClassSubmissionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyClassSubmissionStatusDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologyDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologySynonymSubmissionDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.OntologySynonymSubmissionStatusDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.StatusDAO;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.TermDAO;

public class DAOManager {

	private CollectionDAO collectionDAO;
	private TermDAO termDAO;
	private OntologyDAO ontologyDBDAO;
	private OntologyClassSubmissionDAO ontologyClassSubmissionDAO;
	private OntologySynonymSubmissionDAO ontologySynonymSubmissionDAO;
	private OntologyClassSubmissionStatusDAO ontologyClassSubmissionStatusDAO;
	private OntologySynonymSubmissionStatusDAO ontologySynonymSubmissionStatusDAO;
	private ContextDAO contextDAO;
	private StatusDAO statusDAO;
	private edu.arizona.biosemantics.oto2.steps.server.persist.file.OntologyDAO2 ontologyFileDAO;
	private OntologyBioportalDAO ontologyBioportalDAO;
	
	public DAOManager() {
		collectionDAO = new CollectionDAO();
		termDAO = new TermDAO();
		ontologyDBDAO = new OntologyDAO();
		ontologyClassSubmissionDAO = new OntologyClassSubmissionDAO();
		ontologySynonymSubmissionDAO = new OntologySynonymSubmissionDAO();
		ontologyClassSubmissionStatusDAO = new OntologyClassSubmissionStatusDAO();
		ontologySynonymSubmissionStatusDAO = new OntologySynonymSubmissionStatusDAO();
		contextDAO = new ContextDAO();
		statusDAO = new StatusDAO();
		ontologyFileDAO = new edu.arizona.biosemantics.oto2.steps.server.persist.file.OntologyDAO2();
		ontologyBioportalDAO = new OntologyBioportalDAO();
		
		collectionDAO.setTermDAO(termDAO);
		
		ontologyClassSubmissionDAO.setTermDAO(termDAO);
		ontologyClassSubmissionDAO.setOntologyDAO(ontologyDBDAO);
		ontologyClassSubmissionDAO.setOntologyClassSubmissionStatusDAO(ontologyClassSubmissionStatusDAO);
		ontologySynonymSubmissionDAO.setTermDAO(termDAO);
		ontologySynonymSubmissionDAO.setOntologyDAO(ontologyDBDAO);
		ontologySynonymSubmissionDAO.setOntologySynonymSubmissionStatusDAO(ontologySynonymSubmissionStatusDAO);
		ontologySynonymSubmissionStatusDAO.setStatusDAO(statusDAO);
		ontologyClassSubmissionStatusDAO.setStatusDAO(statusDAO);
		//ontologyFileDAO.setOntologyDAO(ontologyDBDAO);
		//ontologyFileDAO.setCollectionDAO(collectionDAO);
		ontologyBioportalDAO.setOntologyClassSubmissionDAO(ontologyClassSubmissionDAO);
		ontologyBioportalDAO.setOntologySynonymSubmissionDAO(ontologySynonymSubmissionDAO);
	}

	public CollectionDAO getCollectionDAO() {
		return collectionDAO;
	}

	public TermDAO getTermDAO() {
		return termDAO;
	}

	public OntologyDAO getOntologyDBDAO() {
		return ontologyDBDAO;
	}
	
	public edu.arizona.biosemantics.oto2.steps.server.persist.file.OntologyDAO2 getOntologyFileDAO() {
		return ontologyFileDAO;
	}

	public OntologyClassSubmissionDAO getOntologyClassSubmissionDAO() {
		return ontologyClassSubmissionDAO;
	}

	public OntologySynonymSubmissionDAO getOntologySynonymSubmissionDAO() {
		return ontologySynonymSubmissionDAO;
	}

	public OntologyClassSubmissionStatusDAO getOntologyClassSubmissionStatusDAO() {
		return ontologyClassSubmissionStatusDAO;
	}

	public OntologySynonymSubmissionStatusDAO getOntologySynonymSubmissionStatusDAO() {
		return ontologySynonymSubmissionStatusDAO;
	}

	public ContextDAO getContextDAO() {
		return contextDAO;
	}

	public StatusDAO getStatusDAO() {
		return statusDAO;
	}

	public OntologyBioportalDAO getOntologyBioportalDAO() {
		return ontologyBioportalDAO;
	}
	
	
}
