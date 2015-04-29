//package edu.arizona.biosemantics.oto2.steps.client.layout;
//
//import com.sencha.gxt.widget.core.client.menu.Item;
//import com.google.gwt.core.client.GWT;
//import com.google.gwt.event.logical.shared.SelectionEvent;
//import com.google.gwt.event.logical.shared.SelectionHandler;
//import com.google.gwt.event.shared.EventBus;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.gwt.user.client.ui.SimpleLayoutPanel;
//import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
//import com.sencha.gxt.core.client.resources.ThemeStyles;
//import com.sencha.gxt.core.client.util.Margins;
//import com.sencha.gxt.data.shared.ListStore;
//import com.sencha.gxt.data.shared.SortDir;
//import com.sencha.gxt.data.shared.Store.StoreSortInfo;
//import com.sencha.gxt.widget.core.client.ContentPanel;
//import com.sencha.gxt.widget.core.client.Dialog;
//import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
//import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
//import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
//import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
//import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
//import com.sencha.gxt.widget.core.client.event.DialogHideEvent.DialogHideHandler;
//import com.sencha.gxt.widget.core.client.form.ComboBox;
//import com.sencha.gxt.widget.core.client.menu.Menu;
//import com.sencha.gxt.widget.core.client.menu.MenuBar;
//import com.sencha.gxt.widget.core.client.menu.MenuBarItem;
//import com.sencha.gxt.widget.core.client.menu.MenuItem;
//
//import edu.arizona.biosemantics.common.log.LogLevel;
//import edu.arizona.biosemantics.oto2.steps.client.categorize.LabelsView;
//import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
//import edu.arizona.biosemantics.oto2.steps.client.common.CommentsDialog;
//import edu.arizona.biosemantics.oto2.steps.client.common.HelpView;
//import edu.arizona.biosemantics.oto2.steps.client.common.SelectOntologiesDialog;
//import edu.arizona.biosemantics.oto2.steps.client.event.ImportEvent;
//import edu.arizona.biosemantics.oto2.steps.client.event.LoadEvent;
//import edu.arizona.biosemantics.oto2.steps.client.event.OntologiesSelectEvent;
//import edu.arizona.biosemantics.oto2.steps.client.event.SaveEvent;
//import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent;
//import edu.arizona.biosemantics.oto2.steps.client.info.TermInfoView;
//import edu.arizona.biosemantics.oto2.steps.client.uncategorize.TermsView;
//import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
//import edu.arizona.biosemantics.oto2.steps.shared.model.Ontology;
//import edu.arizona.biosemantics.oto2.steps.shared.model.OntologyProperties;
//import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
//import edu.arizona.biosemantics.oto2.steps.shared.model.TermProperties;
//import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
//import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;
//import edu.arizona.biosemantics.oto2.steps.shared.rpc.IOntologyService;
//import edu.arizona.biosemantics.oto2.steps.shared.rpc.IOntologyServiceAsync;
//
//public class OtoView extends SimpleLayoutPanel {
//
//	public class MenuView extends MenuBar {
//
//		private final IOntologyServiceAsync ontologyService = GWT.create(IOntologyService.class);
//		private ListStore<Term> termStore = new ListStore<Term>(termProperties.key());
//		private EventBus eventBus;
//		private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
//
//		public MenuView(final EventBus eventBus) {
//			this.eventBus = eventBus;
//			addStyleName(ThemeStyles.get().style().borderBottom());
//
//			Menu sub = new Menu();
//			MenuBarItem item = new MenuBarItem("File", sub);
//			MenuItem resetItem = new MenuItem("Remove Categorizations");
//			Menu resetSub = new Menu();
//			resetItem.setSubMenu(resetSub);
//			MenuItem fullResetItem = new MenuItem("All");
//			MenuItem historyResetItem = new MenuItem("Users' Categorizations");
//			resetSub.add(fullResetItem);
//			resetSub.add(historyResetItem);
//			fullResetItem.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					ConfirmMessageBox box = new ConfirmMessageBox("Remove categorizations", "" +
//							"This will uncategorize all terms irreversibly. Are you sure you want to continue?");
//					box.addDialogHideHandler(new DialogHideHandler() {
//						@Override
//						public void onDialogHide(DialogHideEvent event) {
//							collectionService.reset(collection, new AsyncCallback<Collection>() {
//								@Override
//								public void onSuccess(Collection result) {
//									eventBus.fireEvent(new LoadEvent(collection, false));
//								}
//								@Override
//								public void onFailure(Throwable caught) {
//									Alerter.resetFailed(caught);
//								}
//							});
//						}
//			        });
//			        box.show();
//			        
//					
//				}
//			});
//			historyResetItem.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					ConfirmMessageBox box = new ConfirmMessageBox("Remove users' categorizations", "" +
//							"This will remove all user provided categorizations on all terms in this set. Are you sure you want to continue?");
//					box.addDialogHideHandler(new DialogHideHandler() {
//						@Override
//						public void onDialogHide(DialogHideEvent event) {
//							collectionService.reset(collection, new AsyncCallback<Collection>() {
//								@Override
//								public void onSuccess(Collection result) {
//									eventBus.fireEvent(new LoadEvent(result, true));
//								}
//
//								@Override
//								public void onFailure(Throwable caught) {
//									Alerter.resetFailed(caught);
//								}
//							});
//						}
//			        });
//					box.show();
//				}
//			});
//			MenuItem saveItem = new MenuItem("Download Categorization Results");
//			saveItem.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					eventBus.fireEvent(new SaveEvent(collection));
//				}
//			});
//			MenuItem importItem = new MenuItem("Import Categorizations");
//			importItem.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					eventBus.fireEvent(new ImportEvent(collection));
//				}
//			});
//			sub.add(resetItem);
//			sub.add(importItem);
//			sub.add(saveItem);
//			add(item);
//
//			sub = new Menu();
//			final ComboBox<Term> searchCombo = new ComboBox<Term>(termStore,
//					termProperties.nameLabel());
//			searchCombo.setForceSelection(true);
//			searchCombo.setTriggerAction(TriggerAction.ALL);
//			searchCombo.addSelectionHandler(new SelectionHandler<Term>() {
//				@Override
//				public void onSelection(SelectionEvent<Term> arg0) {
//					eventBus.fireEvent(new TermSelectEvent(arg0
//							.getSelectedItem()));
//				}
//			});
//			sub.add(searchCombo);
//			item = new MenuBarItem("Search", sub);
//			add(item);
//			
//			sub = new Menu();
//			MenuItem selectOntologies = new MenuItem("Select Ontologies for Term Information ");
//			selectOntologies.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					Dialog dialog = new SelectOntologiesDialog(eventBus);
//					dialog.show();
//				}
//			});
//			sub.add(selectOntologies);
//			item = new MenuBarItem("Ontologies", sub);
//			add(item);
//			
//			sub = new Menu();
//			MenuItem overview = new MenuItem("View All User Comments");
//			overview.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> event) {
//					CommentsDialog commentsDialog = new CommentsDialog(eventBus, collection);
//					commentsDialog.show();
//				}
//			});
//			sub.add(overview);
//			item = new MenuBarItem("Comments", sub);
//			add(item);
//
//			sub = new Menu();
//			MenuBarItem questionsItem = new MenuBarItem("Instructions", sub);
//			MenuItem helpItem = new MenuItem("Help");
//			helpItem.addSelectionHandler(new SelectionHandler<Item>() {
//				@Override
//				public void onSelection(SelectionEvent<Item> arg0) {
//					final Dialog dialog = new Dialog();
//					dialog.setBodyBorder(false);
//					dialog.setHeadingText("Help");
//					dialog.setHideOnButtonClick(true);
//					dialog.setWidget(new HelpView());
//					dialog.setWidth(600);
//					dialog.setMaximizable(true);
//					//dialog.setMinimizable(true);
//					dialog.setHeight(400);
//					dialog.setResizable(true);
//					dialog.setShadow(true);
//					dialog.show();
//				}
//			});
//			sub.add(helpItem);
//			add(questionsItem);
//			
//			
//
//		}
//
//		public void setCollection(Collection collection) {
//			termStore.clear();
//			termStore.addAll(collection.getTerms());
//			termStore.addSortInfo(new StoreSortInfo<Term>(
//					new Term.TermComparator(), SortDir.ASC));
//		}
//	}
//
//	public static class CategorizeView extends BorderLayoutContainer {
//
//		private int portalColumnCount = 9;
//		private TermsView termsView;
//		private LabelsView labelsView;
//		private TermInfoView termInfoView;
//
//		public CategorizeView(EventBus eventBus) {
//			termsView = new TermsView(eventBus);
//			labelsView = new LabelsView(eventBus, portalColumnCount);
//			termInfoView = new TermInfoView(eventBus);
//
//			ContentPanel cp = new ContentPanel();
//			cp.setHeadingText("Terms to be Categorized");
//			cp.add(termsView);
//			BorderLayoutData d = new BorderLayoutData(.20);
//			// d.setMargins(new Margins(0, 1, 1, 1));
//			d.setCollapsible(true);
//			d.setSplit(true);
//			d.setCollapseMini(true);
//			setWestWidget(cp, d);
//
//			cp = new ContentPanel();
//			cp.setHeadingText("Categories and Categorization Results");
//			cp.add(labelsView);
//			d = new BorderLayoutData();
//			d.setMargins(new Margins(0, 0, 0, 0));
//			setCenterWidget(cp, d);
//
//			cp = new ContentPanel();
//			cp.setHeadingText("Term Information");
//			cp.add(termInfoView);
//			d = new BorderLayoutData(.40);
//			d.setMargins(new Margins(0, 0, 20, 0));
//			d.setCollapsible(true);
//			d.setSplit(true);
//			d.setCollapseMini(true);
//			setSouthWidget(cp, d);
//
//			// cp = new ContentPanel();
//			/*
//			 * cp.setHeadingText("Search"); d = new BorderLayoutData(.20);
//			 * //d.setMargins(new Margins(1)); d.setCollapsible(true);
//			 * d.setSplit(true); d.setCollapseMini(true);
//			 * setNorthWidget(getMenu(), d);
//			 */
//		}
//
//		public void setCollection(Collection collection) {
//			termsView.setCollection(collection);
//			labelsView.setCollection(collection);
//			termInfoView.setCollection(collection);
//		}
//
//	}
//	
//	private EventBus eventBus;
//
//	private MenuView menuView;
//	private CategorizeView categorizeView;
//	private Collection collection;
//	private VerticalLayoutContainer verticalLayoutContainer = new VerticalLayoutContainer();
//	private static final TermProperties termProperties = GWT
//			.create(TermProperties.class);
//
//	public OtoView(EventBus eventBus) {
//		this.eventBus = eventBus;
//		categorizeView = new CategorizeView(eventBus);
//		menuView = new MenuView(eventBus);
//
//		verticalLayoutContainer.add(menuView,new VerticalLayoutData(1,-1));
//		verticalLayoutContainer.add(categorizeView,new VerticalLayoutData(1,1));
//		this.setWidget(verticalLayoutContainer);
//	}
//
//	public void setCollection(Collection collection) {
//		this.collection = collection;
//		categorizeView.setCollection(collection);
//		menuView.setCollection(collection);
//	}
//
//}
