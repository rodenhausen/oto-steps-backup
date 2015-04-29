package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.SetColorEvent.SetColorEventHandler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;

public class SetColorEvent extends GwtEvent<SetColorEventHandler> {

	public interface SetColorEventHandler extends EventHandler {
		void onSet(SetColorEvent event);
	}
	
	public static Type<SetColorEventHandler> TYPE = new Type<SetColorEventHandler>();
	private Collection<Object> objects;
	private Color color;
	
	public SetColorEvent(Object object, Color color, boolean collection) {
		if(collection) {
			this.objects = (java.util.Collection)object;
		} else {
			this.objects = new LinkedList<Object>();
			this.objects.add(objects);
		}
		this.color = color;
	}
	
	@Override
	public GwtEvent.Type<SetColorEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SetColorEventHandler handler) {
		handler.onSet(this);
	}

	public static Type<SetColorEventHandler> getTYPE() {
		return TYPE;
	}

	public Collection<Object> getObjects() {
		return objects;
	}

	public Color getColor() {
		return color;
	}

}
