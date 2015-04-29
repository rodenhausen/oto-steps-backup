package edu.arizona.biosemantics.oto2.steps.client.toontology;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.util.Format;
import com.sencha.gxt.core.client.util.Params;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent;
import com.sencha.gxt.widget.core.client.event.BeforeShowEvent.BeforeShowHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.menu.HeaderMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.tree.Tree;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.common.AllowSurpressSelectEventsTreeSelectionModel;
import edu.arizona.biosemantics.oto2.steps.client.event.AddCommentEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.RefreshSubmissionsEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.SetColorEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.TermMarkUselessEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TermProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.Bucket;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.BucketTreeNode;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.TermTreeNode;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.TextTreeNode;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.TextTreeNodeProperties;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.toontology.IToOntologyServiceAsync;

public class TermsView implements IsWidget {
	
	private class TermMenu extends Menu implements BeforeShowHandler {
		
		public TermMenu() {
			this.addBeforeShowHandler(this);
			this.setWidth(140);
		}

		@Override
		public void onBeforeShow(BeforeShowEvent event) {
			this.clear();
			
			List<Term> viewSelected = new LinkedList<Term>();
			List<TextTreeNode> nodes = termTreeSelectionModel.getSelectedItems();	
			for(TextTreeNode node : nodes)
				if(node instanceof TermTreeNode) {
					viewSelected.add(((TermTreeNode)node).getTerm());
				} else if(node instanceof BucketTreeNode) {
					for(TextTreeNode child : treeStore.getChildren(node)) {
						if(node instanceof TermTreeNode) {
							viewSelected.add(((TermTreeNode)node).getTerm());
						}
					}
				}
			
			final List<Term> selected = viewSelected;
			
			if(selected == null || selected.isEmpty()) {
				event.setCancelled(true);
				this.hide();
			} else {
				this.add(new HeaderMenuItem("Term"));
				MenuItem markUseless = new MenuItem("Mark");
				Menu subMenu = new Menu();
				markUseless.setSubMenu(subMenu);
				MenuItem useless = new MenuItem("Not Usefull");
				MenuItem useful = new MenuItem("Useful");
				subMenu.add(useless);
				subMenu.add(useful);
				useless.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						for(Term term : selected)
							term.setRemoved(true);
						eventBus.fireEvent(new TermMarkUselessEvent(selected, true));
					}
				});
				useful.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						for(Term term : selected)
							term.setRemoved(false);
						eventBus.fireEvent(new TermMarkUselessEvent(selected, false));
					}
				});
				this.add(markUseless);
				
				/*this.add(new HeaderMenuItem("Term"));
				MenuItem markUseless = new MenuItem("Mark");
				Menu subMenu = new Menu();
				markUseless.setSubMenu(subMenu);
				MenuItem useless = new MenuItem("Not Usefull");
				MenuItem useful = new MenuItem("Useful");
				subMenu.add(useless);
				subMenu.add(useful);
				useless.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						eventBus.fireEvent(new TermMarkUselessEvent(terms, true));
					}
				});
				useful.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						eventBus.fireEvent(new TermMarkUselessEvent(terms, false));
					}
				});
				this.add(markUseless);*/
				
				if(selected.size() == 1) {
					final Term term = selected.get(0);
					MenuItem rename = new MenuItem("Correct Spelling");
					rename.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							Alerter.dialogRename(eventBus, term, collection);
						}
					});
					this.add(rename);
					/*MenuItem split = new MenuItem("Split Term");
					split.addSelectionHandler(new SelectionHandler<Item>() {
						@Override
						public void onSelection(SelectionEvent<Item> event) {
							final PromptMessageBox box = new PromptMessageBox(
									"Split Term", "Please input splitted terms' separated by space.");
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
							box.addHideHandler(new HideHandler() {
								@Override
								public void onHide(HideEvent event) {
									String newName = box.getValue();
									eventBus.fireEvent(new TermSplitEvent(term, newName));
								}
							});
							box.show();
						}
					});
					this.add(split);*/
				}
					

				this.add(new HeaderMenuItem("Annotation"));
				MenuItem comment = new MenuItem("Comment");
				final Term term = selected.get(0);
				comment.addSelectionHandler(new SelectionHandler<Item>() {
					@Override
					public void onSelection(SelectionEvent<Item> event) {
						final MultiLinePromptMessageBox box = new MultiLinePromptMessageBox("Comment", "");
						box.getTextArea().setValue(getUsersComment(term));
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
				});
				this.add(comment);
				
				final MenuItem colorizeItem = new MenuItem("Colorize");
				if(!collection.getColors().isEmpty()) {
					this.add(colorizeItem);
					colorizeItem.setSubMenu(createColorizeMenu(selected));
				} 
			}
			
			if(this.getWidgetCount() == 0)
				event.setCancelled(true);
		}

		protected Menu createColorizeMenu(final List<Term> selected) {
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

		protected String getUsersComment(Term term) {
			//collection.getC
			return "";
		}
	}

	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private IToOntologyServiceAsync toOntologyService = GWT.create(IToOntologyService.class);
	private static final TermProperties termProperties = GWT.create(TermProperties.class);
	private static final TextTreeNodeProperties textTreeNodeProperties = GWT.create(TextTreeNodeProperties.class);
	
	private TreeStore<TextTreeNode> treeStore;
	private Map<Term, TermTreeNode> termTermTreeNodeMap = new HashMap<Term, TermTreeNode>();
	private Tree<TextTreeNode, TextTreeNode> termTree;
	private TextButton refreshButton = new TextButton("Refresh");
	private EventBus eventBus;
	private AllowSurpressSelectEventsTreeSelectionModel<TextTreeNode> termTreeSelectionModel = 
			new AllowSurpressSelectEventsTreeSelectionModel<TextTreeNode>();

	private BucketTreeNode availableTermsNode;
	private BucketTreeNode removedTermsNode;
	private BucketTreeNode removedStructureTermsNode;
	private BucketTreeNode availableStructureTermsNode;
	private BucketTreeNode availableCharacterTermsNode;
	private BucketTreeNode removedCharacterTermsNode;
	protected Collection collection;
	private VerticalLayoutContainer vertical;
	
	public TermsView(EventBus eventBus) {
		this.eventBus = eventBus;
		treeStore = new TreeStore<TextTreeNode>(textTreeNodeProperties.key());
		treeStore.setAutoCommit(true);
		treeStore.addSortInfo(new StoreSortInfo<TextTreeNode>(new Comparator<TextTreeNode>() {
			@Override
			public int compare(TextTreeNode o1, TextTreeNode o2) {
				return o1.getText().compareTo(o2.getText());
			}
		}, SortDir.ASC));
		termTree = new Tree<TextTreeNode, TextTreeNode>(treeStore, new IdentityValueProvider<TextTreeNode>());
		termTree.setCell(new AbstractCell<TextTreeNode>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,	TextTreeNode textTreeNode, SafeHtmlBuilder sb) {
					if(textTreeNode instanceof TermTreeNode) {
						TermTreeNode termTreeNode = (TermTreeNode)textTreeNode;
						Term term = termTreeNode.getTerm();
						if(term.isRemoved()) {
							sb.append(SafeHtmlUtils.fromTrustedString("<div style='padding-left:5px; padding-right:5px; background-color:gray; " +
									"color:white'>" + 
									textTreeNode.getText() + "</div>"));
						} else {
							if(collection.hasColorization(term)) {
								String colorHex = collection.getColorization(term).getHex();
								sb.append(SafeHtmlUtils.fromTrustedString("<div style='padding-left:5px; padding-right:5px; background-color:#" + colorHex + 
										"'>" + 
										textTreeNode.getText() + "</div>"));
							} else {
								sb.append(SafeHtmlUtils.fromTrustedString("<div style='padding-left:5px; padding-right:5px'>" + textTreeNode.getText() +
										"</div>"));
							}
						}
					} else {
						sb.append(SafeHtmlUtils.fromTrustedString("<div style='padding-left:5px; padding-right:5px'>" + textTreeNode.getText() +
								"</div>"));
					}
			}
		});
		termTree.setSelectionModel(termTreeSelectionModel);
		termTree.getElement().setAttribute("source", "termsview");
		termTree.setContextMenu(new TermMenu());
		
		addDefaultNodes();
		
		vertical = new VerticalLayoutContainer();
		vertical.add(termTree, new VerticalLayoutData(1, 1));
		vertical.add(refreshButton, new VerticalLayoutData(1, -1));
		
		bindEvents();
	}
	
	private void addDefaultNodes() {
		//availableTermsNode = new BucketTreeNode(new Bucket("Available Terms"));
		//removedTermsNode = new BucketTreeNode(new Bucket("Removed Terms"));
		availableStructureTermsNode = new BucketTreeNode(new Bucket("Structures"));
		//removedStructureTermsNode = new BucketTreeNode(new Bucket("Structures"));
		availableCharacterTermsNode = new BucketTreeNode(new Bucket("Characters"));
		//removedCharacterTermsNode = new BucketTreeNode(new Bucket("Characters"));
		
		//treeStore.add(availableTermsNode);
		//treeStore.add(availableTermsNode, availableStructureTermsNode);
		//treeStore.add(availableTermsNode, availableCharacterTermsNode);
		//treeStore.add(removedTermsNode);
		//treeStore.add(removedTermsNode, removedStructureTermsNode);
		//treeStore.add(removedTermsNode, removedCharacterTermsNode);
		treeStore.add(availableStructureTermsNode);
		treeStore.add(availableCharacterTermsNode);
	}

	private void bindEvents() {
		refreshButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				toOntologyService.refreshSubmissionStatuses(collection, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToRefreshSubmissions();
					}
					@Override
					public void onSuccess(Void result) {
						eventBus.fireEvent(new RefreshSubmissionsEvent());
					}
				});
			}
		});
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				TermsView.this.collection = event.getCollection();
				/*treeStore.removeChildren(availableStructureTermsNode);
				treeStore.removeChildren(availableCharacterTermsNode);
				treeStore.removeChildren(removedStructureTermsNode);
				treeStore.removeChildren(removedCharacterTermsNode);
				
				for(Term term : event.getCollection().getTerms()) {
					switch(term.getCategory().toLowerCase()) {
					case "character":
						if(term.isRemoved()) {
							addTermTreeNode(removedCharacterTermsNode, new TermTreeNode(term));
							
						} else {
							addTermTreeNode(availableCharacterTermsNode, new TermTreeNode(term));
						}
						break;
					case "structure":
						if(term.isRemoved()) {
							addTermTreeNode(removedStructureTermsNode, new TermTreeNode(term));
							treeStore.add(removedStructureTermsNode, new TermTreeNode(term));
						} else {
							addTermTreeNode(availableStructureTermsNode, new TermTreeNode(term));
						}
						break;
					}
				}*/
				for(Term term : event.getCollection().getTerms()) {
					switch(term.getCategory().toLowerCase()) {
						case "character":
							addTermTreeNode(availableCharacterTermsNode, new TermTreeNode(term));
							break;
						case "structure":
							addTermTreeNode(availableStructureTermsNode, new TermTreeNode(term));
							break;
					}
				}
				
				termTree.expandAll();
			}
		});
		
		termTreeSelectionModel.addSelectionHandler(new SelectionHandler<TextTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<TextTreeNode> event) {
				TextTreeNode node = event.getSelectedItem();
				if(node instanceof TermTreeNode) {
					TermTreeNode termTreeNode = (TermTreeNode)node;
					eventBus.fireEventFromSource(new TermSelectEvent(termTreeNode.getTerm()), TermsView.this);
				}
			}
		});
		
		eventBus.addHandler(TermSelectEvent.TYPE, new TermSelectEvent.Handler() {
			@Override
			public void onSelect(TermSelectEvent event) {
				Term term = event.getTerm();				
				TermTreeNode termTreeNode = termTermTreeNodeMap.get(term);
				if(termTreeNode != null && treeStore.findModel(termTreeNode) != null && !termTreeSelectionModel.isSelected(termTreeNode)) {
					List<TextTreeNode> selectionTree = new LinkedList<TextTreeNode>();
					selectionTree.add(termTreeNode);
					termTreeSelectionModel.setSelection(selectionTree, true);
				}
			}
		});
		
		eventBus.addHandler(SetColorEvent.TYPE, new SetColorEvent.SetColorEventHandler() {
			@Override
			public void onSet(SetColorEvent event) {
				for(Object object : event.getObjects()) {
					if(object instanceof Term) {
						Term term = (Term)object;
						List<TextTreeNode> treeStoreContent = treeStore.getAll();
						if(termTermTreeNodeMap.get(term) != null && treeStoreContent.contains(termTermTreeNodeMap.get(term))) 
							treeStore.update(termTermTreeNodeMap.get(term));
					}
				}
			}
		});
		
		eventBus.addHandler(TermMarkUselessEvent.TYPE, new TermMarkUselessEvent.Handler() {
			@Override
			public void onSelect(TermMarkUselessEvent event) {
				List<TextTreeNode> treeStoreContent = treeStore.getAll();
				for(Term term : event.getTerms()) {
					if(termTermTreeNodeMap.get(term) != null && treeStoreContent.contains(termTermTreeNodeMap.get(term))) 
						treeStore.update(termTermTreeNodeMap.get(term));
				}
			}
		});
	}	
	
	protected void addTermTreeNode(BucketTreeNode bucketNode, TermTreeNode termTreeNode) {
		this.termTermTreeNodeMap.put(termTreeNode.getTerm(), termTreeNode);
		this.treeStore.add(bucketNode, termTreeNode);
	}
	
	@Override
	public Widget asWidget() {
		return vertical;
	}
}