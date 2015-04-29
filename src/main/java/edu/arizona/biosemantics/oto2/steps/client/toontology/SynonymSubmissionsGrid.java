package edu.arizona.biosemantics.oto2.steps.client.toontology;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.client.util.Params;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.HeaderMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.common.ColorableCell;
import edu.arizona.biosemantics.oto2.steps.client.event.AddCommentEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologySynonymSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RemoveOntologySynonymSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.SetColorEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.model.Colorable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;
import edu.arizona.biosemantics.oto2.steps.shared.model.Commentable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmissionStatusProperties;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class SynonymSubmissionsGrid implements IsWidget {

	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private static ColumnConfig<OntologySynonymSubmission, String> ontologyCol;
	private static OntologySynonymSubmissionProperties ontologySynonymSubmissionProperties = GWT.create(OntologySynonymSubmissionProperties.class);
	private static OntologySynonymSubmissionStatusProperties ontologySynonymSubmissionStatusProperties = GWT.create(OntologySynonymSubmissionStatusProperties.class);
	private static CheckBoxSelectionModel<OntologySynonymSubmission> checkBoxSelectionModel;
	
	private EventBus eventBus;
	protected Collection collection;
	private Grid<OntologySynonymSubmission> grid;
	
	public SynonymSubmissionsGrid(EventBus eventBus, ListStore<OntologySynonymSubmission> synonymSubmissionStore) {
		this.eventBus = eventBus;
		grid = new Grid<OntologySynonymSubmission>(synonymSubmissionStore, createColumnModel(synonymSubmissionStore));
		
		final GroupingView<OntologySynonymSubmission> groupingView = new GroupingView<OntologySynonymSubmission>();
		groupingView.setShowGroupedColumn(false);
		groupingView.setForceFit(true);
		groupingView.groupBy(ontologyCol);
		
		grid.setView(groupingView);
		grid.setContextMenu(createSynonymSubmissionsContextMenu());
	
		grid.setSelectionModel(checkBoxSelectionModel);
		//grid.getView().setAutoExpandColumn(taxonBCol);
		//grid.setBorders(false);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);
		
		bindEvents();
	}
	

	private Menu createSynonymSubmissionsContextMenu() {
		final Menu menu = new Menu();
		
		menu.addBeforeShowHandler(new BeforeShowHandler() {
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				menu.clear();
				final List<OntologySynonymSubmission> selected = checkBoxSelectionModel.getSelectedItems();
				if(!selected.isEmpty()) {
					
					MenuItem deleteItem = new MenuItem("Remove");
					menu.add(deleteItem);
					deleteItem.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							toOntologyService.removeSynonymSubmissions(collection, 
									grid.getSelectionModel().getSelectedItems(), new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
									Alerter.failedToRemoveOntologyClassSubmission();
								}
								@Override
								public void onSuccess(Void result) {
									eventBus.fireEvent(new RemoveOntologySynonymSubmissionsEvent(grid.getSelectionModel().getSelectedItems()));
								}
							});
						}
					});
					menu.add(deleteItem);

					menu.add(new HeaderMenuItem("Annotation"));
					MenuItem comment = new MenuItem("Comment");
					final OntologySynonymSubmission ontologySynonymSubmission = selected.get(0);
					comment.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							final MultiLinePromptMessageBox box = new MultiLinePromptMessageBox("Comment", "");
							box.getTextArea().setValue(getUsersComment(ontologySynonymSubmission));
							box.addHideHandler(new HideHandler() {
								@Override
								public void onHide(HideEvent event) {
									final Comment newComment = new Comment(OtoSteps.user, box.getValue());
									collection.addComments((java.util.Collection)selected, newComment);
									collectionService.update(collection, new AsyncCallback<Void>() {
										@Override
										public void onFailure(Throwable caught) {
											Alerter.addCommentFailed(caught);
										}
										@Override
										public void onSuccess(Void result) {
											eventBus.fireEvent(new AddCommentEvent(
													(java.util.Collection)selected, newComment));
											String comment = Format.ellipse(box.getValue(), 80);
											String message = Format.substitute("'{0}' saved", new Params(comment));
											Info.display("Comment", message);
										}
									});
								}
							});
							box.show();
						}
	
						private String getUsersComment(
								OntologySynonymSubmission ontologySynonymSubmission) {
							// TODO Auto-generated method stub
							return null;
						}
					});
					menu.add(comment);
					final MenuItem colorizeItem = new MenuItem("Colorize");
					if(!collection.getColors().isEmpty()) {
						menu.add(colorizeItem);
						colorizeItem.setSubMenu(createColorizeMenu(selected));
					} 
				}
			}

			protected Menu createColorizeMenu(final List<OntologySynonymSubmission> selected) {
				Menu colorMenu = new Menu();
				MenuItem offItem = new MenuItem("None");
				offItem.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						collection.setColorizations((java.util.Collection)selected, null);
						collectionService.update(collection, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.failedToSetColor();
							}
							@Override
							public void onSuccess(Void result) {
								eventBus.fireEvent(new SetColorEvent(selected, null, true));
							}
						});
					}
				});
				colorMenu.add(offItem);
				for(final Color color : collection.getColors()) {
					MenuItem colorItem = new MenuItem(color.getUse());
					colorItem.getElement().getStyle().setProperty("backgroundColor", "#" + color.getHex());
					colorItem.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							collection.setColorizations((java.util.Collection)selected, color);
							collectionService.update(collection, new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
									caught.printStackTrace();
									Alerter.failedToSetColor();
								}
								@Override
								public void onSuccess(Void result) {
									eventBus.fireEvent(new SetColorEvent(selected, color, true));
								}
							});
						}
					});
					colorMenu.add(colorItem);
				}
				return colorMenu;
			}
		});
		return menu;
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				SynonymSubmissionsGrid.this.collection = event.getCollection();
			}
		});
		getSelectionModel().addSelectionHandler(new SelectionHandler<OntologySynonymSubmission>() {
			@Override
			public void onSelection(SelectionEvent<OntologySynonymSubmission> event) {
				eventBus.fireEvent(new OntologySynonymSubmissionSelectEvent(event.getSelectedItem()));
			}
		});
		
		eventBus.addHandler(SetColorEvent.TYPE, new SetColorEvent.SetColorEventHandler() {
			@Override
			public void onSet(SetColorEvent event) {
				for(Object object : event.getObjects()) {
					if(object instanceof OntologySynonymSubmission) {
						OntologySynonymSubmission ontologySynonymSubmission = (OntologySynonymSubmission)object;
						if(grid.getStore().findModel(ontologySynonymSubmission) != null)
							grid.getStore().update(ontologySynonymSubmission);
					}
				}
			}
		});
	}

	private ColumnModel<OntologySynonymSubmission> createColumnModel(ListStore<OntologySynonymSubmission> synonymSubmissionStore) {
		IdentityValueProvider<OntologySynonymSubmission> identity = new IdentityValueProvider<OntologySynonymSubmission>();
		checkBoxSelectionModel = new CheckBoxSelectionModel<OntologySynonymSubmission>(
				identity);
		checkBoxSelectionModel.setSelectionMode(SelectionMode.MULTI);

		ColorableCell colorableCell = new ColorableCell(eventBus, collection);
		colorableCell.setCommentColorizableObjectsStore(synonymSubmissionStore, new ColorableCell.CommentableColorableProvider() {
			@Override
			public Colorable provideColorable(Object source) {
				return (OntologySynonymSubmission)source;
			}
			@Override
			public Commentable provideCommentable(Object source) {
				return (OntologySynonymSubmission)source;
			}
		});
		ValueProvider<OntologySynonymSubmission, String> termValueProvider = new ValueProvider<OntologySynonymSubmission, String>() {
			@Override
			public String getValue(OntologySynonymSubmission object) {
				return object.getTerm().getTerm();
			}
			@Override
			public void setValue(OntologySynonymSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "term-term";
			}
		};
		final ColumnConfig<OntologySynonymSubmission, String> termCol = new ColumnConfig<OntologySynonymSubmission, String>(
				termValueProvider, 200, "Candidate Term");
		termCol.setCell(colorableCell);

		
		final ColumnConfig<OntologySynonymSubmission, String> submissionTermCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.submissionTerm(), 200, "Term");
		submissionTermCol.setCell(colorableCell);
		
		ValueProvider<OntologySynonymSubmission, String> categoryValueProvider = new ValueProvider<OntologySynonymSubmission, String>() {
			@Override
			public String getValue(OntologySynonymSubmission object) {
				return object.getTerm().getCategory();
			}
			@Override
			public void setValue(OntologySynonymSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "term-category";
			}
		};
		final ColumnConfig<OntologySynonymSubmission, String> categoryCol = new ColumnConfig<OntologySynonymSubmission, String>(
				categoryValueProvider, 200, "Category");
		categoryCol.setCell(colorableCell);
		ValueProvider<OntologySynonymSubmission, String> ontlogyAcronymValueProvider = new ValueProvider<OntologySynonymSubmission, String>() {
			@Override
			public String getValue(OntologySynonymSubmission object) {
				return object.getOntology().getAcronym();
			}
			@Override
			public void setValue(OntologySynonymSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "ontology-prefix";
			}
		};
		ontologyCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontlogyAcronymValueProvider, 200, "Ontology");
		ontologyCol.setCell(colorableCell);
		/*ontologyCol = new ColumnConfig<OntologyClassSubmission, Ontology>(
				ontologyClassSubmissionProperties.targetOntology(), 200, "Ontology");
		ontologyCol.setCell(new AbstractCell<Ontology>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,
					Ontology value, SafeHtmlBuilder sb) {
				sb.append(SafeHtmlUtils.fromSafeConstant(value.getName()));
			}
		}); */
		
		final ColumnConfig<OntologySynonymSubmission, String> classCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.classIRI(), 200, "Superclass");
		classCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> synonymsCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.synonyms(), 200, "Synonyms");
		synonymsCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> sourceCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.source(), 200, "Source");
		sourceCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> sampleCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.sampleSentence(), 200, "Sample Sentence");
		sampleCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> statusCol = new ColumnConfig<OntologySynonymSubmission, String>(
				new ValueProvider<OntologySynonymSubmission, String>() {
					@Override
					public String getValue(OntologySynonymSubmission object) {
						String status = "";
						for(OntologySynonymSubmissionStatus ontologyClassSubmissionStatus : object.getSubmissionStatuses()) {
							//if(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status.valueOf(ontologyClassSubmissionStatus.getStatus().getName().toUpperCase())
							//		.equals(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status.ACCEPTED))
								status += ontologyClassSubmissionStatus.getStatus().getName() + ", ";
						}
						return status.length() >= 2 ? status.substring(0, status.length() - 2) : "";
					}
					@Override
					public void setValue(OntologySynonymSubmission object, String value) {	}
					@Override
					public String getPath() {
						return "status";
					}
				}, 200, "Status");
		statusCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> iriCol = new ColumnConfig<OntologySynonymSubmission, String>(
				new ValueProvider<OntologySynonymSubmission, String>() {
					@Override
					public String getValue(OntologySynonymSubmission object) {
						for(OntologySynonymSubmissionStatus ontologyClassSubmissionStatus : object.getSubmissionStatuses()) {
							if(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum.valueOf(ontologyClassSubmissionStatus.getStatus().getName().toUpperCase())
									.equals(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum.ACCEPTED))
								return ontologyClassSubmissionStatus.getIri();
						}
						return "";
					}
					@Override
					public void setValue(OntologySynonymSubmission object, String value) {	}
					@Override
					public String getPath() {
						return "status";
					}
				}, 200, "IRI");
		iriCol.setCell(colorableCell);
		final ColumnConfig<OntologySynonymSubmission, String> userCol = new ColumnConfig<OntologySynonymSubmission, String>(
				ontologySynonymSubmissionProperties.user(), 200, "User");
		userCol.setCell(colorableCell);
		
//		ValueProvider<Articulation, String> commentValueProvider = new ValueProvider<Articulation, String>() {
//			@Override
//			public String getValue(Articulation object) {
//				if(model.hasComment(object))
//					return model.getComment(object);
//				return "";
//			}
//			@Override
//			public void setValue(Articulation object, String value) {
//				model.setComment(object, value);
//			}
//			@Override
//			public String getPath() {
//				return "comment";
//			}
//		};
//		
//		final ColumnConfig<Articulation, String> commentCol = new ColumnConfig<Articulation, String>(
//				commentValueProvider, 400, "Comment");
//		commentCol.setCell(colorableCell);
		
//		StringFilter<Articulation> createdFilter = new StringFilter<Articulation>(new ArticulationProperties.CreatedStringValueProvder());
//		StringFilter<Articulation> taxonAFilter = new StringFilter<Articulation>(new ArticulationProperties.TaxonAStringValueProvider());
//		StringFilter<Articulation> taxonBFilter = new StringFilter<Articulation>(new ArticulationProperties.TaxonBStringValueProvider());
//		StringFilter<Articulation> commentFilter = new StringFilter<Articulation>(commentValueProvider);
//		
//		ListFilter<Articulation, ArticulationType> relationFilter = new ListFilter<Articulation, ArticulationType>(
//				articulationProperties.type(), this.allTypesStore);
//
//		GridFilters<Articulation> filters = new GridFilters<Articulation>();
//		filters.addFilter(createdFilter);
//		filters.addFilter(taxonAFilter);
//		filters.addFilter(taxonBFilter);
//		filters.addFilter(relationFilter);
//		filters.addFilter(commentFilter);
//		filters.setLocal(true);
//		filters.initPlugin(grid);
//
//		GridInlineEditing<Articulation> editing = new GridInlineEditing<Articulation>(grid);
//		
//		ComboBox<ArticulationType> relationCombo = createRelationCombo();
//		
//		if(this.relationEditEnabled)
//			editing.addEditor(relationCol, relationCombo);
//		editing.addEditor(commentCol, new TextField());
//		editing.addStartEditHandler(new StartEditHandler<Articulation>() {
//			@Override
//			public void onStartEdit(StartEditEvent<Articulation> event) {
//				Articulation articulation = grid.getStore().get(event.getEditCell().getRow());
//				List<ArticulationType> availableTypes = getAvailableTypes(articulation);
//				availableTypesStore.clear();
//				availableTypesStore.addAll(availableTypes);
//			}
//		});
//		/*editing.addBeforeStartEditHandler(new BeforeStartEditHandler<Articulation>() {
//
//			@Override
//			public void onBeforeStartEdit(
//					BeforeStartEditEvent<Articulation> event) {
//				event.get
//			}
//			
//		}); */
//		editing.addCompleteEditHandler(new CompleteEditHandler<Articulation>() {
//			@Override
//			public void onCompleteEdit(CompleteEditEvent<Articulation> event) {			
//				GridCell cell = event.getEditCell();
//				Articulation articulation = grid.getStore().get(cell.getRow());
//				ColumnConfig<Articulation, ?> config = grid.getColumnModel().getColumn(cell.getCol());
//				if(config.equals(relationCol)) {
//					ArticulationType type = (ArticulationType)config.getValueProvider().getValue(articulation);
//					eventBus.fireEvent(new ModifyArticulationEvent(articulation, type));
//				}
//				if(config.equals(commentCol)) {
//					String comment = (String)config.getValueProvider().getValue(articulation);
//					eventBus.fireEvent(new SetCommentEvent(articulation, comment));
//				}
//			}
//		});
//		
		
		List<ColumnConfig<OntologySynonymSubmission, ?>> columns = new ArrayList<ColumnConfig<OntologySynonymSubmission, ?>>();
		columns.add(checkBoxSelectionModel.getColumn());
		columns.add(termCol);
		columns.add(submissionTermCol);
		columns.add(categoryCol);
		columns.add(ontologyCol);
		columns.add(classCol);
		columns.add(synonymsCol);
		columns.add(sourceCol);
		columns.add(sampleCol);
		columns.add(statusCol);
		columns.add(iriCol);
		columns.add(userCol);
		
		ColumnModel<OntologySynonymSubmission> cm = new ColumnModel<OntologySynonymSubmission>(columns);
		return cm;
	}

	@Override
	public Widget asWidget() {
		return grid;
	}


	public GridSelectionModel<OntologySynonymSubmission> getSelectionModel() {
		return grid.getSelectionModel();
	}
	
}
