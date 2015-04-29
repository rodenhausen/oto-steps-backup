package edu.arizona.biosemantics.oto2.steps.client.toontology;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.common.CreateOntologyDialog;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologyClassSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.CreateOntologyClassSubmissionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.UpdateOntologyClassSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
import edu.arizona.biosemantics.oto2.steps.shared.model.OntologyProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TermProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.ClassExistsException;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class SubmitClassView implements IsWidget {
	
	private OntologyProperties ontologyProperties = GWT.create(OntologyProperties.class);
	private TermProperties termProperties = GWT.create(TermProperties.class);
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private EventBus eventBus;
	private Collection collection;
	
	private ListStore<Ontology> ontologiesStore = new ListStore<Ontology>(ontologyProperties.key());
	private ListStore<Term> termStore = new ListStore<Term>(termProperties.key());
	private TextButton editButton = new TextButton("Edit Submission");
	private TextButton submitButton = new TextButton("Save as New Submission");
	private TextField submissionTermField = new TextField();
	private TextField categoryField = new TextField();
	private TextButton browseOntologiesButton = new TextButton("Browse Selected Ontology");
	private ComboBox<Ontology> ontologyComboBox;
	private TextField classIRIField = new TextField();
	private TextField superclassIRIField = new TextField();
	private TextArea definitionArea = new TextArea();
	private TextField synonymsField = new TextField();
	private TextField sourceField = new TextField();
	private TextArea sampleArea = new TextArea();
	private TextField partOfField = new TextField();
	private CheckBox isEntityCheckBox = new CheckBox();
	private CheckBox isQualityCheckBox = new CheckBox();
	private ComboBox<Term> termComboBox;
	private VerticalLayoutContainer vlc;
	private TextButton createOntologyButton = new TextButton("Create New Ontology");
	private OntologyClassSubmission selectedSubmission;
	
	public SubmitClassView(EventBus eventBus) {
		this.eventBus = eventBus;
		
	    ontologyComboBox = new ComboBox<Ontology>(ontologiesStore, ontologyProperties.prefixLabel());
	    ontologyComboBox.setAllowBlank(true);
	    ontologyComboBox.setForceSelection(false);
	    ontologyComboBox.setTriggerAction(TriggerAction.ALL);

	    termComboBox = new ComboBox<Term>(termStore, termProperties.nameLabel());
	    categoryField.setEnabled(false);
	    
	    vlc = new VerticalLayoutContainer();
	    VerticalLayoutContainer formContainer = new VerticalLayoutContainer();
	    formContainer.add(new FieldLabel(termComboBox, "Candiate Term"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(submissionTermField, "Term"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(categoryField, "Category"), new VerticalLayoutData(1, -1));
	    VerticalLayoutContainer ontologyVlc = new VerticalLayoutContainer();
	    ontologyVlc.add(createOntologyButton, new VerticalLayoutData(1, -1));
	    ontologyVlc.add(ontologyComboBox, new VerticalLayoutData(1, -1));
	    ontologyVlc.add(browseOntologiesButton, new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(ontologyVlc, "Ontology"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(classIRIField, "Class IRI"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(superclassIRIField, "Superclass IRI"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(definitionArea, "Definition"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(synonymsField, "Synonyms"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(sourceField, "Source"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(sampleArea, "Sample Sentence"), new VerticalLayoutData(1, 100));
	    formContainer.add(new FieldLabel(partOfField, "Part of IRI"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(isEntityCheckBox, "Is Entity"), new VerticalLayoutData(1, -1));
	    formContainer.add(new FieldLabel(isQualityCheckBox, "Is Quality"), new VerticalLayoutData(1, -1));
	    formContainer.setScrollMode(ScrollMode.AUTOY);
	    formContainer.setAdjustForScroll(true);
	    vlc.add(formContainer, new VerticalLayoutData(1, 1));
	    vlc.add(editButton, new VerticalLayoutData(1,-1));
	    vlc.add(submitButton, new VerticalLayoutData(1,-1));
		bindEvents();		
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				SubmitClassView.this.collection = event.getCollection();
				
				initCollection();
			}
		});
		eventBus.addHandler(TermSelectEvent.TYPE, new TermSelectEvent.Handler() {
			@Override
			public void onSelect(TermSelectEvent event) {
				if(!event.getSource().equals(SubmitClassView.this)) {
					clearFields(false);
					setSelectedSubmission(null);
					setTerm(event.getTerm());
				}
			}
		});
		eventBus.addHandler(OntologyClassSubmissionSelectEvent.TYPE, new OntologyClassSubmissionSelectEvent.Handler() {
			@Override
			public void onSelect(OntologyClassSubmissionSelectEvent event) {
				setOntologyClassSubmission(event.getOntologyClassSubmission());
			}
		});
		termComboBox.addValueChangeHandler(new ValueChangeHandler<Term>() {
			@Override
			public void onValueChange(ValueChangeEvent<Term> event) {
				setTerm(event.getValue());
				eventBus.fireEventFromSource(new TermSelectEvent(event.getValue()), SubmitClassView.this);
			}
		});
		createOntologyButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final CreateOntologyDialog dialog = new CreateOntologyDialog();
				dialog.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						final Ontology ontology = new Ontology();
						ontology.setName(dialog.getName());
						ontology.setAcronym(dialog.getAcronym());
						ontology.setTaxonGroups(dialog.getTaxonGroups());
						ontology.setCollectionId(collection.getId());
						
						toOntologyService.createOntology(collection, ontology, new AsyncCallback<Ontology>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.failedToCreateOntology();
							}
							@Override
							public void onSuccess(Ontology result) {
								Alerter.succesfulCreatedOntology();
								refreshOntologies(result);
							}
						});
					}
				});
				dialog.getButton(PredefinedButton.CANCEL).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) { }
				});
				dialog.show();
			}
		});
		browseOntologiesButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				if(ontologyComboBox.getValue() != null && 
						ontologyComboBox.getValue().getBrowseURL() != null && !ontologyComboBox.getValue().getBrowseURL().isEmpty())
					Window.open(ontologyComboBox.getValue().getBrowseURL(), "_blank", "");
				else
					Alerter.failedToBrowseOntology();
			} 
		});
		
		submitButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				
				final OntologyClassSubmission submission = getClassSubmission();
				toOntologyService.createClassSubmission(submission, new AsyncCallback<OntologyClassSubmission>() {
					@Override
					public void onFailure(Throwable caught) {
						if(caught instanceof ClassExistsException) {
							Alerter.failedToSubmitClassExists(caught);
						} else
							Alerter.failedToSubmitClass(caught);
					}
					@Override
					public void onSuccess(OntologyClassSubmission result) {
						eventBus.fireEvent(new CreateOntologyClassSubmissionEvent(result));
					}
				});
			}
		});
		editButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final OntologyClassSubmission submission = getClassSubmission();
				submission.setId(selectedSubmission.getId());
				final List<OntologyClassSubmission> submissions = new LinkedList<OntologyClassSubmission>();
				submissions.add(submission);
				toOntologyService.updateClassSubmissions(collection, submissions, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToEditClass(caught);
					}
					@Override
					public void onSuccess(Void result) {
						eventBus.fireEvent(new UpdateOntologyClassSubmissionsEvent(submissions));
					}
				});
			}
		});
	}

	protected void setTerm(Term term) {
		termComboBox.setValue(term);
		categoryField.setValue(termComboBox.getValue().getCategory());
		submissionTermField.setValue(termComboBox.getValue().getTerm());
	}

	protected void setSelectedSubmission(OntologyClassSubmission ontologyClassSubmission) {
		this.selectedSubmission = ontologyClassSubmission;
		if(selectedSubmission == null)
			this.editButton.setEnabled(false);
		else
			this.editButton.setEnabled(true);
	}
	
	protected void clearFields(boolean fireEvents) {
		this.termComboBox.setValue(null, false);
		this.submissionTermField.setValue("", false); 
		this.ontologyComboBox.setValue(null, false);
		this.classIRIField.setValue("", false);
		this.superclassIRIField.setValue("", false);
		this.definitionArea.setValue("", false);
		this.synonymsField.setValue("", false);
		this.sourceField.setValue("", false);
		this.sampleArea.setValue("", false);
		this.partOfField.setValue("", false);
		this.isEntityCheckBox.setValue(false, false);
		this.isQualityCheckBox.setValue(false, false);
	}

	protected void setOntologyClassSubmission(OntologyClassSubmission ontologyClassSubmission) {
		this.setSelectedSubmission(ontologyClassSubmission);
		this.termComboBox.setValue(ontologyClassSubmission.getTerm());
		this.submissionTermField.setValue(ontologyClassSubmission.getSubmissionTerm()); 
		this.ontologyComboBox.setValue(ontologyClassSubmission.getOntology());
		this.classIRIField.setValue(ontologyClassSubmission.getClassIRI());
		this.superclassIRIField.setValue(ontologyClassSubmission.getSuperclassIRI());
		this.definitionArea.setValue(ontologyClassSubmission.getDefinition());
		this.synonymsField.setValue(ontologyClassSubmission.getSynonyms());
		this.sourceField.setValue(ontologyClassSubmission.getSource());
		this.sampleArea.setValue(ontologyClassSubmission.getSampleSentence());
		this.partOfField.setValue(ontologyClassSubmission.getPartOfIRI());
		this.isEntityCheckBox.setValue(ontologyClassSubmission.isEntity());
		this.isQualityCheckBox.setValue(ontologyClassSubmission.isQuality());
	}
	
	protected OntologyClassSubmission getClassSubmission() {
		boolean entity = isEntityCheckBox.getValue();
		boolean quality = isQualityCheckBox.getValue();
		String user = OtoSteps.user;
		return new OntologyClassSubmission(termComboBox.getValue(), submissionTermField.getValue(), 
				ontologyComboBox.getValue(), classIRIField.getValue(), superclassIRIField.getValue(),
				definitionArea.getValue(), synonymsField.getValue(), sourceField.getValue(), sampleArea.getValue(), partOfField.getValue(), 
				isEntityCheckBox.getValue(), isQualityCheckBox.getValue(), OtoSteps.user);
	}

	protected void initCollection() {
		refreshOntologies(null);
		refreshTerms();
	}

	private void refreshTerms() {
		termStore.clear();
		termStore.addAll(collection.getTerms());
	}

	private void refreshOntologies(final Ontology ontologyToSelect) {
		toOntologyService.getOntologies(collection, new AsyncCallback<List<Ontology>>() {
			@Override
			public void onFailure(Throwable caught) {
				Alerter.getOntologiesFailed(caught);
			}

			@Override
			public void onSuccess(List<Ontology> result) {
				ontologiesStore.clear();
				ontologiesStore.addAll(result);
				if(ontologyToSelect != null)
					ontologyComboBox.setValue(ontologyToSelect);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return vlc;
	}

}
