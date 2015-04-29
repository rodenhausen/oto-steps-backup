package edu.arizona.biosemantics.oto2.steps.shared.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;


public interface TermProperties extends PropertyAccess<Term> {

	  @Path("id")
	  ModelKeyProvider<Term> key();
	   
	  @Path("term")
	  LabelProvider<Term> nameLabel();
	 
	  ValueProvider<Term, String> term();
	  
	  ValueProvider<Term, String> category();
	  
	  ValueProvider<Term, Boolean> removed();
	
}
