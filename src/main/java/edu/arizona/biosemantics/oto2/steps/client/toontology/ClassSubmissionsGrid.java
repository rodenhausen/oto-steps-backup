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
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.common.ColorableCell;
import edu.arizona.biosemantics.oto2.steps.client.common.ColorableCheckBoxCell;
import edu.arizona.biosemantics.oto2.steps.client.common.ColorableCheckBoxCell.CommentableColorableProvider;
import edu.arizona.biosemantics.oto2.steps.client.event.AddCommentEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologyClassSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.OntologySynonymSubmissionSelectEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RemoveOntologyClassSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.SetColorEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.model.Colorable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;
import edu.arizona.biosemantics.oto2.steps.shared.model.Commentable;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatus;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmissionStatusProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class ClassSubmissionsGrid implements IsWidget {

	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private static ColumnConfig<OntologyClassSubmission, String> ontologyCol;
	private static OntologyClassSubmissionProperties ontologyClassSubmissionProperties = GWT.create(OntologyClassSubmissionProperties.class);
	private static OntologyClassSubmissionStatusProperties ontologyClassSubmissionStatusProperties = GWT.create(OntologyClassSubmissionStatusProperties.class);
	private static CheckBoxSelectionModel<OntologyClassSubmission> checkBoxSelectionModel;
	
	private EventBus eventBus;
	protected Collection collection;
	private Grid<OntologyClassSubmission> grid;
	
	public ClassSubmissionsGrid(EventBus eventBus, ListStore<OntologyClassSubmission> classSubmissionStore) {		
		this.eventBus = eventBus;
		grid = new Grid<OntologyClassSubmission>(classSubmissionStore, createColumnModel(classSubmissionStore));
		
		final GroupingView<OntologyClassSubmission> groupingView = new GroupingView<OntologyClassSubmission>();
		groupingView.setShowGroupedColumn(false);
		groupingView.setForceFit(true);
		groupingView.groupBy(ontologyCol);
		
		grid.setView(groupingView);
		grid.setContextMenu(createClassSubmissionsContextMenu());
		
		grid.setSelectionModel(checkBoxSelectionModel);
		//grid.getView().setAutoExpandColumn(taxonBCol);
		//grid.setBorders(false);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);
		
		//classSubmissionStore.remove.
		
		bindEvents();
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				ClassSubmissionsGrid.this.collection = event.getCollection();
			}
		});
		
		eventBus.addHandler(SetColorEvent.TYPE, new SetColorEvent.SetColorEventHandler() {
			@Override
			public void onSet(SetColorEvent event) {
				for(Object object : event.getObjects()) {
					if(object instanceof OntologyClassSubmission) {
						OntologyClassSubmission ontologyClassSubmission = (OntologyClassSubmission)object;
						if(grid.getStore().findModel(ontologyClassSubmission) != null)
							grid.getStore().update(ontologyClassSubmission);
					}
				}
			}
		});
		getSelectionModel().addSelectionHandler(new SelectionHandler<OntologyClassSubmission>() {
			@Override
			public void onSelection(SelectionEvent<OntologyClassSubmission> event) {
				eventBus.fireEvent(new OntologyClassSubmissionSelectEvent(event.getSelectedItem()));
			}
		});

	}

	private Menu createClassSubmissionsContextMenu() {
		final Menu menu = new Menu();

		menu.addBeforeShowHandler(new BeforeShowHandler() {
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				menu.clear();
				final List<OntologyClassSubmission> selected = checkBoxSelectionModel.getSelectedItems();
				if(!selected.isEmpty()) {
					MenuItem deleteItem = new MenuItem("Remove");
					menu.add(deleteItem);
					deleteItem.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							toOntologyService.removeClassSubmissions(collection, 
									grid.getSelectionModel().getSelectedItems(), new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
									Alerter.failedToRemoveOntologyClassSubmission();
								}
								@Override
								public void onSuccess(Void result) {
									eventBus.fireEvent(new RemoveOntologyClassSubmissionsEvent(grid.getSelectionModel().getSelectedItems()));
								}
							});
						}
					});
					menu.add(deleteItem);
				
					menu.add(new HeaderMenuItem("Annotation"));
					MenuItem comment = new MenuItem("Comment");
					final OntologyClassSubmission ontologyClassSubmission = selected.get(0);
					comment.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							final MultiLinePromptMessageBox box = new MultiLinePromptMessageBox("Comment", "");
							box.getTextArea().setValue(getUsersComment(ontologyClassSubmission));
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
								OntologyClassSubmission ontologyClassSubmission) {
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

			protected Menu createColorizeMenu(final List<OntologyClassSubmission> selected) {
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

		
		/*final MenuItem commentItem = new MenuItem("Comment");
		commentItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				final List<Articulation> articulations = getSelectedArticulations();
				final MultiLinePromptMessageBox box = new MultiLinePromptMessageBox("Comment", "");

				if(articulations.size() == 1)
					box.getTextArea().setValue(model.hasComment(articulations.get(0)) ? model.getComment(articulations.get(0)) : "");
				else 
					box.getTextArea().setValue("");
				
				box.addHideHandler(new HideHandler() {

					@Override
					public void onHide(HideEvent event) {
						for(Articulation articulation : articulations) { 
							eventBus.fireEvent(new SetCommentEvent(articulation, box.getValue()));
							updateStore(articulation);
						}
						String comment = Format.ellipse(box.getValue(), 80);
						String message = Format.substitute("'{0}' saved", new Params(comment));
						Info.display("Comment", message);
					}
				});
				box.show();
			}
		});
		menu.add(commentItem);
		
		final MenuItem colorizeItem = new MenuItem("Colorize");
		menu.addBeforeShowHandler(new BeforeShowHandler() {
			@Override
			public void onBeforeShow(BeforeShowEvent event) {
				if(!model.getColors().isEmpty()) {
					menu.insert(colorizeItem, menu.getWidgetIndex(commentItem));
					//colors can change, refresh
					colorizeItem.setSubMenu(createColorizeMenu());
				} else {
					menu.remove(colorizeItem);	
				}
			}
		});*/
		
		return menu;
	}

	private ColumnModel<OntologyClassSubmission> createColumnModel(ListStore<OntologyClassSubmission> classSubmissionStore) {
		IdentityValueProvider<OntologyClassSubmission> identity = new IdentityValueProvider<OntologyClassSubmission>();
		checkBoxSelectionModel = new CheckBoxSelectionModel<OntologyClassSubmission>(
				identity);
		checkBoxSelectionModel.setSelectionMode(SelectionMode.MULTI);

		ColorableCell colorableCell = new ColorableCell(eventBus, collection);
		colorableCell.setCommentColorizableObjectsStore(classSubmissionStore, new ColorableCell.CommentableColorableProvider() {
			@Override
			public Colorable provideColorable(Object source) {
				return (OntologyClassSubmission)source;
			}
			@Override
			public Commentable provideCommentable(Object source) {
				return (OntologyClassSubmission)source;
			}
		});
		
		ValueProvider<OntologyClassSubmission, String> termValueProvider = new ValueProvider<OntologyClassSubmission, String>() {
			@Override
			public String getValue(OntologyClassSubmission object) {
				return object.getTerm().getTerm();
			}
			@Override
			public void setValue(OntologyClassSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "term-term";
			}
		};
		final ColumnConfig<OntologyClassSubmission, String> termCol = new ColumnConfig<OntologyClassSubmission, String>(
				termValueProvider, 200, "Candidate Term");
		termCol.setCell(colorableCell);

		
		final ColumnConfig<OntologyClassSubmission, String> submissionTermCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.submissionTerm(), 200, "Term");
		submissionTermCol.setCell(colorableCell);
		
		ValueProvider<OntologyClassSubmission, String> categoryValueProvider = new ValueProvider<OntologyClassSubmission, String>() {
			@Override
			public String getValue(OntologyClassSubmission object) {
				return object.getTerm().getCategory();
			}
			@Override
			public void setValue(OntologyClassSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "term-category";
			}
		};
		final ColumnConfig<OntologyClassSubmission, String> categoryCol = new ColumnConfig<OntologyClassSubmission, String>(
				categoryValueProvider, 200, "Category");
		categoryCol.setCell(colorableCell);

		//relationCol.setCell(colorableCell);
		ValueProvider<OntologyClassSubmission, String> ontlogyAcronymValueProvider = new ValueProvider<OntologyClassSubmission, String>() {
			@Override
			public String getValue(OntologyClassSubmission object) {
				return object.getOntology().getAcronym();
			}
			@Override
			public void setValue(OntologyClassSubmission object, String value) {	}
			@Override
			public String getPath() {
				return "ontology-prefix";
			}
		};
		ontologyCol = new ColumnConfig<OntologyClassSubmission, String>(ontlogyAcronymValueProvider, 200, "Ontology");
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
		final ColumnConfig<OntologyClassSubmission, String> superClassCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.superclassIRI(), 200, "Superclass");
		superClassCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> definitionCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.definition(), 200, "Defintion");
		definitionCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> synonymsCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.synonyms(), 200, "Synonyms");
		synonymsCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> sourceCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.source(), 200, "Source");
		sourceCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> sampleCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.sampleSentence(), 200, "Sample Sentence");
		sampleCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> partOfCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.partOfIRI(), 200, "Part Of");
		partOfCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, Boolean> entityCol = new ColumnConfig<OntologyClassSubmission, Boolean>(
				ontologyClassSubmissionProperties.entity(), 200, "Entity");
		ColorableCheckBoxCell colorableCheckBoxCell = new ColorableCheckBoxCell(eventBus, collection);
		colorableCheckBoxCell.setCommentColorizableObjectsStore((ListStore)classSubmissionStore, new CommentableColorableProvider() {
			@Override
			public Colorable provideColorable(Object source) {
				return (OntologyClassSubmission)source;
			}
			@Override
			public Commentable provideCommentable(Object source) {
				return (OntologyClassSubmission)source;
			}
		});
		entityCol.setCell(colorableCheckBoxCell);
		final ColumnConfig<OntologyClassSubmission, Boolean> qualityCol = new ColumnConfig<OntologyClassSubmission, Boolean>(
				ontologyClassSubmissionProperties.quality(), 200, "Quality");
		qualityCol.setCell(colorableCheckBoxCell);
		final ColumnConfig<OntologyClassSubmission, String> statusCol = new ColumnConfig<OntologyClassSubmission, String>(
				new ValueProvider<OntologyClassSubmission, String>() {
					@Override
					public String getValue(OntologyClassSubmission object) {
						String status = "";
						for(OntologyClassSubmissionStatus ontologyClassSubmissionStatus : object.getSubmissionStatuses()) {
							//if(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status.valueOf(ontologyClassSubmissionStatus.getStatus().getName().toUpperCase())
							//		.equals(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Status.ACCEPTED))
								status += ontologyClassSubmissionStatus.getStatus().getName() + ", ";
						}
						return status.length() >= 2 ? status.substring(0, status.length() - 2) : "";
					}
					@Override
					public void setValue(OntologyClassSubmission object, String value) {	}
					@Override
					public String getPath() {
						return "status";
					}
				}, 200, "Status");
		statusCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> iriCol = new ColumnConfig<OntologyClassSubmission, String>(
				new ValueProvider<OntologyClassSubmission, String>() {
					@Override
					public String getValue(OntologyClassSubmission object) {
						for(OntologyClassSubmissionStatus ontologyClassSubmissionStatus : object.getSubmissionStatuses()) {
							if(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum.valueOf(ontologyClassSubmissionStatus.getStatus().getName().toUpperCase())
									.equals(edu.arizona.biosemantics.oto2.steps.shared.model.toontology.StatusEnum.ACCEPTED))
								return ontologyClassSubmissionStatus.getIri();
						}
						return "";
					}
					@Override
					public void setValue(OntologyClassSubmission object, String value) {	}
					@Override
					public String getPath() {
						return "status";
					}
				}, 200, "IRI");
		iriCol.setCell(colorableCell);
		final ColumnConfig<OntologyClassSubmission, String> userCol = new ColumnConfig<OntologyClassSubmission, String>(
				ontologyClassSubmissionProperties.user(), 200, "User");
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
		
		List<ColumnConfig<OntologyClassSubmission, ?>> columns = new ArrayList<ColumnConfig<OntologyClassSubmission, ?>>();
		columns.add(checkBoxSelectionModel.getColumn());
		columns.add(termCol);
		columns.add(submissionTermCol);
		columns.add(categoryCol);
		columns.add(ontologyCol);
		columns.add(superClassCol);
		columns.add(definitionCol);
		columns.add(synonymsCol);
		columns.add(sourceCol);
		columns.add(sampleCol);
		columns.add(partOfCol);
		columns.add(entityCol);
		columns.add(qualityCol);
		columns.add(statusCol);
		columns.add(iriCol);
		columns.add(userCol);

		ColumnModel<OntologyClassSubmission> cm = new ColumnModel<OntologyClassSubmission>(columns);
		return cm;
	}

	@Override
	public Widget asWidget() {
		return grid;
	}

	public GridSelectionModel<OntologyClassSubmission> getSelectionModel() {
		return grid.getSelectionModel();
	}
	
}
