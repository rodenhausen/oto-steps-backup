package edu.arizona.biosemantics.oto2.steps.shared.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface ICollectionServiceAsync {	

	public void insert(Collection collection, AsyncCallback<Collection> callback);
	
	public void get(int id, String secret, AsyncCallback<Collection> callback);
	
	public void update(Collection collection, AsyncCallback<Void> callback);
		
}
