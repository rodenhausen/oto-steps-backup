package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.SetCommentEvent.SetCommentEventHandler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;

public class SetCommentEvent extends GwtEvent<SetCommentEventHandler> {

	public interface SetCommentEventHandler extends EventHandler {
		void onSet(SetCommentEvent event);
	}
	
	public static Type<SetCommentEventHandler> TYPE = new Type<SetCommentEventHandler>();
	private Comment comment;
	private Collection<Object> objects;
	
	public SetCommentEvent(Object object, Comment comment, boolean collection) {
		if(collection) {
			this.objects = (java.util.Collection)object;
		} else {
			this.objects = new LinkedList<Object>();
			this.objects.add(objects);
		}
		this.comment = comment;
	}

	@Override
	public GwtEvent.Type<SetCommentEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SetCommentEventHandler handler) {
		handler.onSet(this);
	}

	public static Type<SetCommentEventHandler> getTYPE() {
		return TYPE;
	}

	public Collection<Object> getObjects() {
		return objects;
	}

	public Comment getComment() {
		return comment;
	}
	
}