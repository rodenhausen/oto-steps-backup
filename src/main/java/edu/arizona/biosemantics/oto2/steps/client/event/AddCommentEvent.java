package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.AddCommentEvent.CommentHandler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;

public class AddCommentEvent extends GwtEvent<CommentHandler> {

	public interface CommentHandler extends EventHandler {
		void onComment(AddCommentEvent event);
	}
	
    public static Type<CommentHandler> TYPE = new Type<CommentHandler>();
	private Comment comment;
	private Collection<Object> objects = new LinkedList<Object>();
	
    public AddCommentEvent(Collection<Object> objects, Comment comment) {
    	this.objects = objects;
    	this.comment = comment;
    }

	@Override
	public Type<CommentHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CommentHandler handler) {
		handler.onComment(this);
	}

	public Comment getComment() {
		return comment;
	}

	public Collection<Object> getObjects() {
		return objects;
	}

}
