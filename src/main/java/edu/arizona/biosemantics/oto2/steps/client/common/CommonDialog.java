package edu.arizona.biosemantics.oto2.steps.client.common;

import com.sencha.gxt.widget.core.client.Dialog;

public class CommonDialog extends Dialog {

	public CommonDialog() {
		super();
		
		this.setBodyBorder(false);
		this.setModal(true);
		this.setResizable(true);
		this.setMaximizable(true);
		//this.setMinimizable(true);
		this.setBlinkModal(true);
		//this.setCollapsible(true);
	}
	
}
