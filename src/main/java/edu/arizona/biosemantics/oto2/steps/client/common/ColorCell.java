package edu.arizona.biosemantics.oto2.steps.client.common;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ColorCell extends TextCell {

	interface Template extends SafeHtmlTemplates {
		@Template("<div style=\"background-color:#{0}; width:30px; height:30px;\"></div>")
		SafeHtml div(String color);
	}

	private static Template template;

	public ColorCell() {
		if (template == null) {
			template = GWT.create(Template.class);
		}
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		if (value != null) {
			// The template will sanitize the URI.
			sb.append(template.div(value));
		}
	}
}