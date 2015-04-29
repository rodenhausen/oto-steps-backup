package edu.arizona.biosemantics.oto2.steps.client.toontology;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelAppearance;
import com.sencha.gxt.widget.core.client.TabPanel.TabPanelBottomAppearance;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologyClassSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologySynonymSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RefreshSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RemoveOntologyClassSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RemoveOntologySynonymSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.CreateOntologyClassSubmissionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.CreateOntologySynonymSubmissionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.UpdateOntologyClassSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.UpdateOntologySynonymsSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatusProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatusProperties;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class SubmissionsView implements IsWidget {

	private EventBus eventBus;
	private Collection collection;
	
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private OntologyClassSubmissionProperties ontologyClassSubmissionProperties = GWT.create(OntologyClassSubmissionProperties.class);
	private OntologySynonymSubmissionProperties ontologySynonymSubmissionProperties = GWT.create(OntologySynonymSubmissionProperties.class);
	private OntologySynonymSubmissionStatusProperties ontologySynonymSubmissionStatusProperties = GWT.create(OntologySynonymSubmissionStatusProperties.class);
	private OntologyClassSubmissionStatusProperties ontologyClassSubmissionStatusProperties = GWT.create(OntologyClassSubmissionStatusProperties.class);
	
	private ListStore<OntologyClassSubmission> classSubmissionStore =
			new ListStore<OntologyClassSubmission>(ontologyClassSubmissionProperties.key());
	private ListStore<OntologySynonymSubmission> synonymSubmissionStore =
			new ListStore<OntologySynonymSubmission>(ontologySynonymSubmissionProperties.key());
	private TabPanel tabPanel;
	private ClassSubmissionsGrid classSubmissionsGrid;
	private SynonymSubmissionsGrid synonymSubmissionsGrid;
	
	public SubmissionsView(EventBus eventBus) {
		this.eventBus = eventBus;
		
		//tabPanel = new TabPanel(GWT.<TabPanelAppearance> create(TabPanelBottomAppearance.class));
		tabPanel = new TabPanel();
		classSubmissionsGrid = createOntologyClassSubmissionGrid();
		synonymSubmissionsGrid = createOntologySynonymSubmissionGrid();
		tabPanel.add(createOntologyClassSubmissionGrid(), "Class");
		tabPanel.add(createOntologySynonymSubmissionGrid(), "Synonym");
		
		bindEvents();
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				collection = event.getCollection();
				refreshClassSubmissions();
				refreshSynonymSubmissions();
			}
		});
		eventBus.addHandler(CreateOntologyClassSubmissionEvent.TYPE, new CreateOntologyClassSubmissionEvent.Handler() {
			@Override
			public void onSubmission(CreateOntologyClassSubmissionEvent event) {
				refreshClassSubmissions();
			}
		});
		eventBus.addHandler(CreateOntologySynonymSubmissionEvent.TYPE, new CreateOntologySynonymSubmissionEvent.Handler() {
			@Override
			public void onSubmission(CreateOntologySynonymSubmissionEvent event) {
				refreshSynonymSubmissions();
			}
		});
		eventBus.addHandler(RefreshSubmissionsEvent.TYPE, new RefreshSubmissionsEvent.Handler() {
			@Override
			public void onSelect(RefreshSubmissionsEvent event) {
				refreshSynonymSubmissions();
				refreshClassSubmissions();
			}
		});
		
		eventBus.addHandler(RemoveOntologyClassSubmissionsEvent.TYPE, new RemoveOntologyClassSubmissionsEvent.Handler() {
			@Override
			public void onRemove(RemoveOntologyClassSubmissionsEvent event) {
				refreshClassSubmissions();
			}
		});
		eventBus.addHandler(RemoveOntologySynonymSubmissionsEvent.TYPE, new RemoveOntologySynonymSubmissionsEvent.Handler() {
			@Override
			public void onRemove(RemoveOntologySynonymSubmissionsEvent event) {
				refreshSynonymSubmissions();
			}
		});
		eventBus.addHandler(UpdateOntologyClassSubmissionsEvent.TYPE, new UpdateOntologyClassSubmissionsEvent.Handler() {
			@Override
			public void onRemove(UpdateOntologyClassSubmissionsEvent event) {
				refreshClassSubmissions();
			}
		});
		eventBus.addHandler(UpdateOntologySynonymsSubmissionsEvent.TYPE, new UpdateOntologySynonymsSubmissionsEvent.Handler() {
			@Override
			public void onRemove(UpdateOntologySynonymsSubmissionsEvent event) {
				refreshSynonymSubmissions();
			}
		});
	}

	protected void refreshClassSubmissions() {
		final MessageBox loadingBox = Alerter.startLoading();
		toOntologyService.getClassSubmissions(collection, new AsyncCallback<List<OntologyClassSubmission>>() {
			@Override
			public void onFailure(Throwable caught) {
				Alerter.stopLoading(loadingBox);
				Alerter.failedToRefreshSubmissions();
			}
			@Override
			public void onSuccess(List<OntologyClassSubmission> result) {
				classSubmissionStore.clear();
				classSubmissionStore.addAll(result);
				Alerter.stopLoading(loadingBox);
			}
		});
	}

	protected void refreshSynonymSubmissions() {
		final MessageBox loadingBox = Alerter.startLoading();
		toOntologyService.getSynonymSubmissions(collection, new AsyncCallback<List<OntologySynonymSubmission>>() {
			@Override
			public void onFailure(Throwable caught) {
				Alerter.stopLoading(loadingBox);
				Alerter.failedToRefreshSubmissions();
			}
			@Override
			public void onSuccess(List<OntologySynonymSubmission> result) {
				synonymSubmissionStore.clear();
				synonymSubmissionStore.addAll(result);
				Alerter.stopLoading(loadingBox);
			}
		});
	}

	private SynonymSubmissionsGrid createOntologySynonymSubmissionGrid() {
		return new SynonymSubmissionsGrid(eventBus, synonymSubmissionStore);
	}

	private ClassSubmissionsGrid createOntologyClassSubmissionGrid() {
		return new ClassSubmissionsGrid(eventBus, classSubmissionStore);
	}

	@Override
	public Widget asWidget() {
		return tabPanel;
	}

}
