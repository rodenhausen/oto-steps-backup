package edu.arizona.biosemantics.oto2.steps.server.persist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status;

public class StatusDAO {
		
	public StatusDAO() {} 
	
	public Status get(int id) throws QueryException  {
		Status status = null;
		try(Query query = new Query("SELECT * FROM otosteps_status WHERE id = ?")) {
			query.setParameter(1, id);
			ResultSet result = query.execute();
			while(result.next()) {
				status = createStatus(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return status;
	}
	
	public Status get(String name) throws QueryException {
		Status status = null;
		try(Query query = new Query("SELECT * FROM otosteps_status WHERE name = ?")) {
			query.setParameter(1, name);
			ResultSet result = query.execute();
			while(result.next()) {
				status = createStatus(result);
			}
		} catch(QueryException | SQLException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw new QueryException(e);
		}
		return status;
	}
	
	private Status createStatus(ResultSet result) throws SQLException {
		int id = result.getInt("id");
		String name = result.getString("name");
		return new Status(id, name);
	}

	public Status insert(Status status) throws QueryException  {
		if(!status.hasId()) {
			try(Query insert = new Query("INSERT INTO `otosteps_status` (`name`) VALUES(?)")) {
				insert.setParameter(1, status.getName());
				insert.execute();
				ResultSet generatedKeys = insert.getGeneratedKeys();
				generatedKeys.next();
				int id = generatedKeys.getInt(1);
				
				status.setId(id);
			} catch(QueryException | SQLException e) {
				log(LogLevel.ERROR, "Query Exception", e);
				throw new QueryException(e);
			}
		}
		return status;
	}
	
	public void update(Status status) throws QueryException  {		
		try(Query query = new Query("UPDATE otosteps_status SET name = ? WHERE id = ?")) {
			query.setParameter(1, status.getName());
			query.setParameter(2, status.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
	
	public void remove(Status status) throws QueryException  {
		try(Query query = new Query("DELETE FROM otosteps_status WHERE id = ?")) {
			query.setParameter(1, status.getId());
			query.execute();
		} catch(QueryException e) {
			log(LogLevel.ERROR, "Query Exception", e);
			throw e;
		}
	}
}
