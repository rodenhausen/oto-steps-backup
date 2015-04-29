package edu.arizona.biosemantics.oto2.steps.client.common;

import com.google.gwt.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent.BeforeSelectHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.steps.client.event.TermRenameEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;

public class Alerter {

	public static class InfoMessageBox extends MessageBox {
		public InfoMessageBox(String title, String message) {
			super(title, message);
			setIcon(ICONS.info());
		}
	}
	
	public static MessageBox startLoading() {
		AutoProgressMessageBox box = new AutoProgressMessageBox("Loading", "Loading your data, please wait...");
        box.setProgressText("Loading...");
        box.auto();
        box.show();
        return box;
	}
	
	public static void stopLoading(MessageBox box) {
		box.hide();
	}
	
	public static MessageBox alertTermWithNameExists(String newName) {
		return showInfo("Term with name exists", "Failed to rename term. " +
				"Another term with the same spelling <b>" + newName + "</b> exists already.");
	}
	
	public static MessageBox failedToSetColors(Throwable caught) {
		return showAlert("Set Colors failed", "Failed to set colors.");
	}
	
	public static MessageBox addCommentFailed(Throwable caught) {
		return showAlert("Add Comment", "Adding of comment failed.");
	}
	
	public static MessageBox failedToRefreshSubmissions() {
		return showAlert("Refresh Submissions", "Refresh of submissions failed");
	}

	public static MessageBox succesfulCreatedOntology() {
		return showInfo("Ontology Creation successful", "Ontology creation successful.");
	}

	public static MessageBox failedToCreateOntology() {
		return showAlert("Ontology Creation failed", "Ontology creation failed");
	}
	
	public static MessageBox failedToBrowseOntology() {
		return showAlert("Ontology not browsable", "No location known to browse ontology");
	}
	
	public static MessageBox noOntologySelected() {
		return showAlert("No Target Ontology", "No target ontology selected");
	}

	public static MessageBox getContextsFailed(Throwable caught) {
		return showAlert("Context failed", "Could not get contexts", caught);
	}

	public static MessageBox getOntologyEntriesFailed(Throwable caught) {
		return showAlert("Ontology Entries failed", "Could not get ontology entries", caught);
	}

	public static MessageBox getOntologiesFailed(Throwable caught) {
		return showAlert("Ontologies failed", "Could not get ontologies", caught);
	}

	public static MessageBox alertNoOntoloygySelected() {
		return showAlert("Ontology selection", "No Ontologies Selected.");
	}

	public static MessageBox failedToSubmitSynonym(Throwable caught) {
		return showAlert("Synonym submission failed", "Failed to submit synonym.");
	}
	
	public static MessageBox failedToSubmitClassExists(Throwable caught) {
		return showAlert("Class submission failed", "Failed to submit class. " + caught.getMessage());
	}

	public static MessageBox failedToSubmitClass(Throwable caught) {
		return showAlert("Class submission failed", "Failed to submit class.");
	}
	
	private static MessageBox showAlert(String title, String message, Throwable caught) {
		if(caught != null)
			caught.printStackTrace();
		return showAlert(title, message);
	}
	
	private static MessageBox showAlert(String title, String message) {
		AlertMessageBox alert = new AlertMessageBox(title, message);
		alert.show();
		return alert;
	}

	private static MessageBox showInfo(String title, String message) {
		InfoMessageBox info = new InfoMessageBox(title, message);
		info.show();
		return info;
	}
	
	private static MessageBox showConfirm(String title, String message) {
		 ConfirmMessageBox confirm = new ConfirmMessageBox(title, message);
		 confirm.show();
         return confirm;
	}

	private static MessageBox showYesNoCancelConfirm(String title, String message) {
		MessageBox box = new MessageBox(title, message);
        box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
        box.setIcon(MessageBox.ICONS.question());
        box.show();
        return box;
	}

	public static void failedToRemoveOntologyClassSubmission() {
		// TODO Auto-generated method stub
		
	}

	public static void dialogRename(final EventBus eventBus, final Term term, final Collection collection) {
		final PromptMessageBox box = new PromptMessageBox(
				"Correct Spelling", "Please input new spelling");
		box.getButton(PredefinedButton.OK).addBeforeSelectHandler(new BeforeSelectHandler() {
			@Override
			public void onBeforeSelect(BeforeSelectEvent event) {
				if(box.getTextField().getValue().trim().isEmpty()) {
					event.setCancelled(true);
					AlertMessageBox alert = new AlertMessageBox("Empty", "Empty not allowed");
					alert.show();
				}
			}
		});
		box.getTextField().setValue(term.getTerm());
		box.getTextField().setAllowBlank(false);
		box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				String newName = box.getValue();
				eventBus.fireEvent(new TermRenameEvent(term, term.getTerm(), newName, collection));
			}
		});
		box.show();
	}

	public static void failedToSetColor() {
		// TODO Auto-generated method stub
		
	}

	public static void failedToEditClass(Throwable caught) {
		// TODO Auto-generated method stub
		
	}


}
