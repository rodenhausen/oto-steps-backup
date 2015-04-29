package edu.arizona.biosemantics.oto2.steps.shared.rpc;

import java.io.IOException;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Context;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TypedContext;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("context")
public interface IContextService extends RemoteService {
		
	public List<TypedContext> getContexts(Collection collection, Term term) throws Exception;
	
	public List<Context> insert(int collectionId, String secret, List<Context> contexts) throws Exception;
	
}
