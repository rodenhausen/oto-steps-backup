package edu.arizona.biosemantics.oto2.steps.client.common;

import java.util.HashSet;
import java.util.Set;

import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;

import edu.arizona.biosemantics.common.biology.TaxonGroup;

public class CreateOntologyDialog extends Dialog {
	
	final TextField ontologyNameField = new TextField();
    final TextField ontologyAcronymField = new TextField();
	private ListView<TaxonGroup, String> taxonGroupList;
	
	public CreateOntologyDialog() {
		this.setHeadingText("Create Ontology");
		
		ListStore<TaxonGroup> taxonGroupsStore = new ListStore<TaxonGroup>(new ModelKeyProvider<TaxonGroup>() {
			@Override
			public String getKey(TaxonGroup taxonGroup) {
				return taxonGroup.toString();
			}
		});
		for(TaxonGroup taxonGroup : TaxonGroup.values()) 
			taxonGroupsStore.add(taxonGroup);
		taxonGroupList = new ListView<TaxonGroup, String>(taxonGroupsStore, new ValueProvider<TaxonGroup, String>() {
			@Override
			public String getValue(TaxonGroup object) {
				return object.getDisplayName();
			}
			@Override
			public void setValue(TaxonGroup object, String value) { }
			@Override
			public String getPath() {
				return "taxongroup";
			}
		});
		taxonGroupList.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		
	    VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
	    formContainer.add(new FieldLabel(ontologyNameField, "Name"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(ontologyAcronymField, "Prefix"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(taxonGroupList, "Taxon Groups"), new VerticalLayoutData(1, -1));
	    
		this.add(formContainer);
		this.setPredefinedButtons(PredefinedButton.OK, PredefinedButton.CANCEL);
		this.setHideOnButtonClick(true);
	}

	public String getName() {
		return ontologyNameField.getValue();
	}

	public String getAcronym() {
		return ontologyAcronymField.getValue();
	}
	
	public Set<TaxonGroup> getTaxonGroups() {
		return new HashSet<TaxonGroup>(taxonGroupList.getSelectionModel().getSelectedItems());
	}


}
