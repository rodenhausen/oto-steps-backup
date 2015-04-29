package edu.arizona.biosemantics.oto2.steps.server.rpc;

import java.io.IOException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.oto2.steps.server.persist.DAOManager;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;

public class CollectionService extends RemoteServiceServlet implements ICollectionService {

	private DAOManager daoManager = new DAOManager();
	
	public Collection insert(Collection collection) throws Exception {
		try {
			return daoManager.getCollectionDAO().insert(collection);
		} catch (QueryException e) {
			throw new Exception();
		}
	}

	public Collection get(int id, String secret) throws Exception {
		try {
			if(daoManager.getCollectionDAO().isValidSecret(id, secret))
				return daoManager.getCollectionDAO().get(id);
			return null;
		} catch (QueryException e) {
			throw new Exception();
		}
	}
	
	public void update(Collection collection) throws Exception {
		try {
			daoManager.getCollectionDAO().update(collection);
		} catch (QueryException e) {
			throw new Exception();
		}
	}

}
