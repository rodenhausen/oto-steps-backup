package edu.arizona.biosemantics.oto2.steps.client.info;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

import edu.arizona.biosemantics.oto2.steps.client.common.Alerter;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.client.event.TermSelectEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.TypedContext;
import edu.arizona.biosemantics.oto2.steps.shared.model.TypedContext.Type;
import edu.arizona.biosemantics.oto2.steps.shared.model.TypedContextProperties;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.IContextService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.IContextServiceAsync;

public class ContextView implements IsWidget {

	private static final TypedContextProperties contextProperties = GWT.create(TypedContextProperties.class);
	private ListStore<TypedContext> store = new ListStore<TypedContext>(contextProperties.key());
	private IContextServiceAsync contextService = GWT.create(IContextService.class);
	
	private EventBus eventBus;
	private Collection collection;
	private Grid<TypedContext> grid;
	private Term currentTerm;
	private AutoProgressMessageBox searchingBox;
	
	public ContextView(EventBus eventBus) {
		this.eventBus = eventBus;
		store.setAutoCommit(true);
		RowNumberer<TypedContext> numberer = new RowNumberer<TypedContext>();
	    RowExpander<TypedContext> expander = new RowExpander<TypedContext>(new AbstractCell<TypedContext>() {
	        @Override
	        public void render(Context context, TypedContext value, SafeHtmlBuilder sb) {
	          sb.appendHtmlConstant("<p style='margin: 5px 5px 10px'><b>Full Text:&nbsp;</b></br>" + value.getFullText() + "</p>");
	          //sb.appendHtmlConstant("<p style='margin: 5px 5px 10px'><b>Summary:</b> " + desc);
	        }
	      });
		ColumnConfig<TypedContext, String> sourceColumn = new ColumnConfig<TypedContext, String>(contextProperties.source(), 50, SafeHtmlUtils.fromTrustedString("<b>Source</b>"));
		ColumnConfig<TypedContext, String> textColumn = new ColumnConfig<TypedContext, String>(contextProperties.text(), 100, SafeHtmlUtils.fromTrustedString("<b>Text</b>"));
		textColumn.setCell(new AbstractCell<String>() {
			@Override
		    public void render(Context context, String value, SafeHtmlBuilder sb) {
		      SafeHtml safeHtml = SafeHtmlUtils.fromTrustedString(value);
		      sb.append(safeHtml);
		    }
		});
		ColumnConfig<TypedContext, String> spellingColumn = new ColumnConfig<TypedContext, String>(contextProperties.typeString(), 100, SafeHtmlUtils.fromTrustedString("<b>Spelling</b>"));
		sourceColumn.setToolTip(SafeHtmlUtils.fromTrustedString("The source of the term"));
		textColumn.setToolTip(SafeHtmlUtils.fromTrustedString("The actual text phrase in which the term occurs in the source"));
		spellingColumn.setToolTip(SafeHtmlUtils.fromTrustedString("Indicateds whether the context is shown for a match of original or updated spelling."));
		spellingColumn.setMenuDisabled(false);
		textColumn.setMenuDisabled(false);
		sourceColumn.setMenuDisabled(false);
		List<ColumnConfig<TypedContext, ?>> columns = new ArrayList<ColumnConfig<TypedContext, ?>>();
		columns.add(numberer);
		columns.add(expander);
		columns.add(sourceColumn);
		columns.add(textColumn);
		//columns.add(spellingColumn);
		ColumnModel<TypedContext> columnModel = new ColumnModel<TypedContext>(columns);
		grid = new Grid<TypedContext>(store, columnModel);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		QuickTip quickTip = new QuickTip(grid);
		//sourceColumn.setWidth(200);
		grid.getView().setAutoExpandColumn(textColumn);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);
		grid.getView().setForceFit(true);
		grid.setBorders(false);
		grid.setAllowTextSelection(true);
		grid.setColumnReordering(true);
		/*grid.setStateful(true);
		grid.setStateId("contextsGrid");
		GridStateHandler<TypedContext> state = new GridStateHandler<TypedContext>(grid);
		state.loadState();*/
		
		StringFilter<TypedContext> sourceFilter = new StringFilter<TypedContext>(contextProperties.source());
		StringFilter<TypedContext> textFilter = new StringFilter<TypedContext>(contextProperties.text());
		ListStore<String> spellingStore = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		});
		for(TypedContext.Type type : TypedContext.Type.values()) 
			spellingStore.add(type.toString());
		ListFilter<TypedContext, String> spellingFilter = new ListFilter<TypedContext, String>(contextProperties.typeString(), spellingStore);
		GridFilters<TypedContext> filters = new GridFilters<TypedContext>();
	    filters.setLocal(true);
	    filters.addFilter(sourceFilter);
	    filters.addFilter(textFilter);
	    filters.addFilter(spellingFilter);
	    filters.initPlugin(grid);
	    expander.initPlugin(grid);
	    numberer.initPlugin(grid);
		
		bindEvents();
	}
	
	public void setContexts(List<TypedContext> contexts) {
		store.clear();
		if(contexts.isEmpty())
			store.add(new TypedContext("nothing-found", -1, "No match found", "", "", Type.original));
		else
			store.addAll(contexts);
		
		//bug: http://www.sencha.com/forum/showthread.php?285982-Grid-ColumnHeader-Menu-missing
		grid.getView().refresh(true);
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				setCollection(event.getCollection());
			}
		});
		eventBus.addHandler(TermSelectEvent.TYPE, new TermSelectEvent.Handler() {
			@Override
			public void onSelect(TermSelectEvent event) {
				currentTerm = event.getTerm();
				refresh();
			}
		});
		//show would show the box not relative to this widget yet, not ready in 
		//final location yet
		grid.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				showSearchingBox();
			}
		});
		grid.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				showSearchingBox();
			}
		});
	}
	
	protected void refresh() {
		setContexts(currentTerm);
	}

	private void setContexts(Term term) {
		createSearchingBox();
		 if(grid.isVisible()) {
        	showSearchingBox();
        }
		contextService.getContexts(collection, term, new AsyncCallback<List<TypedContext>>() {
			@Override
			public void onSuccess(List<TypedContext> contexts) {
				setContexts(contexts);
				destroySearchingBox();
			}
			@Override
			public void onFailure(Throwable caught) {
				Alerter.getContextsFailed(caught);
				destroySearchingBox();
			}
		});
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
		
		//bug: http://www.sencha.com/forum/showthread.php?285982-Grid-ColumnHeader-Menu-missing
		grid.getView().refresh(true);
	}
	
	private void createSearchingBox() {
		if(searchingBox == null) {
			searchingBox = new AutoProgressMessageBox("Progress", 
					"Searching contexts, please wait...");
			searchingBox.setProgressText("Searching...");
			searchingBox.auto();
			searchingBox.setClosable(true); // in case user figures search takes too long / some technical problem
			searchingBox.setModal(false);
		}
	}

	protected void destroySearchingBox() {
		if(searchingBox != null) {
			searchingBox.hide();
			searchingBox = null;
		}
	}

	private void showSearchingBox() {
		if(searchingBox != null) {
			searchingBox.getElement().alignTo(grid.getElement(), 
	        		 new AnchorAlignment(Anchor.CENTER, Anchor.CENTER), 0, 0);
			searchingBox.show();
		}
	}

	@Override
	public Widget asWidget() {
		return grid;
	}
}
