package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status;

public class OntologyClassSubmissionStatusDAO {
	
	private StatusDAO statusDAO;
	
	public OntologyClassSubmissionStatusDAO() {} 
	
	public OntologyClassSubmissionStatus get(int id) throws QueryException  {
		OntologyClassSubmissionStatus ontologyClassSubmissionStatus = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologyclasssubmission_status WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologyClassSubmissionStatus = createOntologyClassSubmissionStatus(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologyClassSubmissionStatus;
	}
	
	private OntologyClassSubmissionStatus createOntologyClassSubmissionStatus(ResultSet result) throws SQLException, QueryException {
		int id = result.getInt("id");
		int ontologyclasssubmissionId = result.getInt("ontologyclasssubmission");
		Status status = statusDAO.get(result.getInt("status"));
		String iri = result.getString("iri");
		return new OntologyClassSubmissionStatus(id, ontologyclasssubmissionId, status, iri);
	}

	public OntologyClassSubmissionStatus insert(OntologyClassSubmissionStatus ontologyClassSubmissionStatus) throws QueryException  {
		if(!ontologyClassSubmissionStatus.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologyclasssubmission_status` "
					+ "(`ontologyclasssubmission`, `status`, `iri`) VALUES(?, ?, ?)")) {
				insert.setParameter(1, ontologyClassSubmissionStatus.getOntologyClassSubmissionId());
				insert.setParameter(2, ontologyClassSubmissionStatus.getStatus().getId());
				insert.setParameter(3, ontologyClassSubmissionStatus.getIri());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				ontologyClassSubmissionStatus.setId(id);
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		}
		return ontologyClassSubmissionStatus;
	}
	
	public void update(OntologyClassSubmissionStatus ontologyClassSubmissionStatus) throws QueryException  {		
		try(Query query = new Query("UPDATE otosteps_ontologyclasssubmission_status SET ontologyclasssubmission = ?, "
				+ "status = ?, iri = ? WHERE id = ?")) {
			query.setParameter(1, ontologyClassSubmissionStatus.getOntologyClassSubmissionId());
			query.setParameter(2, ontologyClassSubmissionStatus.getStatus().getId());
			query.setParameter(3, ontologyClassSubmissionStatus.getIri());
			query.setParameter(4, ontologyClassSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	public void remove(OntologyClassSubmissionStatus ontologyClassSubmissionStatus) throws QueryException  {
		try(Query query = new Query("DELETE FROM otosteps_ontologyclasssubmission_status WHERE id = ?")) {
			query.setParameter(1, ontologyClassSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	public List<OntologyClassSubmissionStatus> getStatusOfOntologyClassSubmission(int ontologyClassSubmissionId) throws QueryException {
		List<OntologyClassSubmissionStatus> ontologyClassSubmissionStatuses = new LinkedList<OntologyClassSubmissionStatus>();
		try(Query query = new Query("SELECT id FROM otosteps_ontologyclasssubmission_status WHERE ontologyclasssubmission = ?")) {
			query.setParameter(1, ontologyClassSubmissionId);
			ResultSet result = query.execute();
			while(result.next()) {
				int id = result.getInt(1);
				OntologyClassSubmissionStatus ontologyClassSubmissionStatus = get(id);
				if(ontologyClassSubmissionStatus != null)
					ontologyClassSubmissionStatuses.add(ontologyClassSubmissionStatus);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologyClassSubmissionStatuses;
	}

	public void setStatusDAO(StatusDAO statusDAO) {
		this.statusDAO = statusDAO;
	}
	
}
