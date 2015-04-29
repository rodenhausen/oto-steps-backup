package edu.arizona.biosemantics.oto2.steps.client.event;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.steps.client.event.SetColorsEvent.SetColorsEventHandler;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;

public class SetColorsEvent extends GwtEvent<SetColorsEventHandler> {

	public interface SetColorsEventHandler extends EventHandler {
		void onSet(SetColorsEvent event);
	}
	
	public static Type<SetColorsEventHandler> TYPE = new Type<SetColorsEventHandler>();
	private List<Color> colors;
	
	public SetColorsEvent(List<Color> colors) {
		this.colors = colors;
	}

	@Override
	public GwtEvent.Type<SetColorsEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(SetColorsEventHandler handler) {
		handler.onSet(this);
	}

	public List<Color> getColors() {
		return colors;
	}

	
}