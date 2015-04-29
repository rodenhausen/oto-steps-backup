package edu.arizona.biosemantics.oto2.steps.shared.rpc;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto2.steps.server.persist.db.Query.QueryException;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("collection")
public interface ICollectionService extends RemoteService {
	
	public Collection insert(Collection collection) throws Exception;;
	
	public Collection get(int id, String secret) throws Exception;
	
	public void update(Collection collection) throws Exception;;

}
