package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status;

public class OntologySynonymSubmissionStatusDAO {
	
	private StatusDAO statusDAO;
	
	public OntologySynonymSubmissionStatusDAO() {} 
	
	public OntologySynonymSubmissionStatus get(int id) throws QueryException  {
		OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus = null;
		try(Query query = new Query("SELECT * FROM otosteps_ontologysynonymsubmission_status WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				ontologySynonymSubmissionStatus = createOntologySynonymSubmissionStatus(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologySynonymSubmissionStatus;
	}
	
	private OntologySynonymSubmissionStatus createOntologySynonymSubmissionStatus(ResultSet result) throws SQLException, QueryException {
		int id = result.getInt("id");
		int ontologysynonymsubmissionId = result.getInt("ontologysynonymsubmission");
		Status status = statusDAO.get(result.getInt("status"));
		String externalId= result.getString("iri");
		return new OntologySynonymSubmissionStatus(id, ontologysynonymsubmissionId, status, externalId);
	}

	public OntologySynonymSubmissionStatus insert(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus) throws QueryException  {
		if(!ontologySynonymSubmissionStatus.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_ontologysynonymsubmission_status` "
					+ "(`ontologysynonymsubmission`, `status`, `iri`) VALUES(?, ?, ?)")) {
				insert.setParameter(1, ontologySynonymSubmissionStatus.getOntologySynonymSubmissionId());
				insert.setParameter(2, ontologySynonymSubmissionStatus.getStatus().getId());
				insert.setParameter(3, ontologySynonymSubmissionStatus.getIri());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				ontologySynonymSubmissionStatus.setId(id);
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		}
		return ontologySynonymSubmissionStatus;
	}
	
	public void update(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus) throws QueryException  {		
		try(Query query = new Query("UPDATE otosteps_ontologysynonymsubmission_status SET ontologysynonymsubmission = ?, "
				+ "status = ?, iri = ? WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmissionStatus.getOntologySynonymSubmissionId());
			query.setParameter(2, ontologySynonymSubmissionStatus.getStatus().getId());
			query.setParameter(3, ontologySynonymSubmissionStatus.getIri());
			query.setParameter(4, ontologySynonymSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	public void remove(OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus) throws QueryException  {
		try(Query query = new Query("DELETE FROM otosteps_ontologysynonymsubmission_status WHERE id = ?")) {
			query.setParameter(1, ontologySynonymSubmissionStatus.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}

	public List<OntologySynonymSubmissionStatus> getStatusOfOntologySynonymSubmission(int ontologySynonymSubmissionId) throws QueryException {
		List<OntologySynonymSubmissionStatus> ontologySynonymSubmissionStatuses = new LinkedList<OntologySynonymSubmissionStatus>();
		try(Query query = new Query("SELECT id FROM otosteps_ontologysynonymsubmission_status WHERE ontologysynonymsubmission = ?")) {
			query.setParameter(1, ontologySynonymSubmissionId);
			ResultSet result = query.execute();
			while(result.next()) {
				int id = result.getInt(1);
				OntologySynonymSubmissionStatus ontologySynonymSubmissionStatus = get(id);
				if(ontologySynonymSubmissionStatus != null)
					ontologySynonymSubmissionStatuses.add(ontologySynonymSubmissionStatus);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return ontologySynonymSubmissionStatuses;
	}

	public void setStatusDAO(StatusDAO statusDAO) {
		this.statusDAO = statusDAO;
	}
	
}
