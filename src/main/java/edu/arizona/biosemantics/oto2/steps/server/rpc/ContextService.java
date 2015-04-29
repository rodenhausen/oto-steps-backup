package edu.arizona.biosemantics.oto2.steps.server.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.oto2.steps.server.persist.DAOManager;
import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Context;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TypedContext;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.IContextService;

public class ContextService extends RemoteServiceServlet implements IContextService {

	private DAOManager daoManager = new DAOManager();
	
	public Collection insert(Collection collection) throws QueryException, IOException {
		return daoManager.getCollectionDAO().insert(collection);
	}

	@Override
	public List<TypedContext> getContexts(Collection collection, Term term) {
		return daoManager.getContextDAO().get(collection, term);
	}
	
	@Override
	public List<Context> insert(int collectionId, String secret, List<Context> contexts) throws QueryException {
		if(daoManager.getCollectionDAO().isValidSecret(collectionId, secret)) {
			List<Context> result = new ArrayList<Context>(contexts.size());
			for(Context context : contexts) {
				context = daoManager.getContextDAO().insert(context);
				result.add(context);
			}
			return result;
		}
		return null;
	}


}
