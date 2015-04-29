package edu.arizona.biosemantics.oto2.steps.client.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.steps.client.OtoSteps;
import edu.arizona.biosemantics.oto2.steps.client.event.SetCommentEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Comment;
import edu.arizona.biosemantics.oto2.steps.shared.model.Commentable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

public class CommentsDialog extends CommonDialog {

	public enum CommentType {
		TERM("Term"),
		ONTOLOGY_CLASS_SUBMISSION("Ontology Class Submission"),
		ONTOLOGY_SYNONYM_SUBMISSION("Ontology Synonym Submission");
		
		private String readable;

		private CommentType(String readable) {
			this.readable = readable;
		}
		
		public String getReadable() {
			return readable;
		}
		
		@Override
		public String toString() {
			return getReadable();
		}
	}
	
	public class CommentEntry {

		private String id;
		private Object object;
		private String source;
		private String user;
		private String text;
		private CommentType type;

		public CommentEntry(String id, Object object, String source, String user, String text) {
			this.id = id;
			this.object = object;
			if (object instanceof Term)
				type = CommentType.TERM;
			if (object instanceof OntologyClassSubmission)
				type = CommentType.ONTOLOGY_CLASS_SUBMISSION;
			if (object instanceof OntologySynonymSubmission)
				type = CommentType.ONTOLOGY_SYNONYM_SUBMISSION;
			this.source = source;
			this.user = user;
			this.text = text;
		}
	
		public String getUser() {
			return user;
		}

		public String getSource() {
			return source;
		}

		public String getText() {
			return text;
		}

		public Object getObject() {
			return object;
		}

		public CommentType getType() {
			return type;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setObject(Object object) {
			this.object = object;
		}
		
		public String getId() {
			return id;
		}
	}

	public interface CommentProperties extends PropertyAccess<CommentEntry> {

		@Path("id")
		ModelKeyProvider<CommentEntry> key();

		@Path("object")
		ValueProvider<CommentEntry, Object> object();

		@Path("source")
		ValueProvider<CommentEntry, String> source();
		
		@Path("user")
		ValueProvider<CommentEntry, String> user();

		@Path("text")
		ValueProvider<CommentEntry, String> text();

		@Path("type")
		ValueProvider<CommentEntry, CommentType> type();

	}

	private EventBus eventBus;
	private Collection collection;
	private ListStore<CommentEntry> commentStore;
	private Grid<CommentEntry> grid;

	public CommentsDialog(final EventBus eventBus, final Collection collection) {
		this.eventBus = eventBus;
		this.collection = collection;
		CommentProperties commentProperties = GWT
				.create(CommentProperties.class);

		IdentityValueProvider<CommentEntry> identity = new IdentityValueProvider<CommentEntry>();
		final CheckBoxSelectionModel<CommentEntry> checkBoxSelectionModel = new CheckBoxSelectionModel<CommentEntry>(
				identity);

		checkBoxSelectionModel.setSelectionMode(SelectionMode.MULTI);

		ColumnConfig<CommentEntry, CommentType> typeCol = new ColumnConfig<CommentEntry, CommentType>(
				commentProperties.type(), 0, "Type");
		ColumnConfig<CommentEntry, String> userCol = new ColumnConfig<CommentEntry, String>(
				commentProperties.user(), 0, "User");
		ColumnConfig<CommentEntry, String> sourceCol = new ColumnConfig<CommentEntry, String>(
				commentProperties.source(), 190, "Source");
		final ColumnConfig<CommentEntry, String> textCol = new ColumnConfig<CommentEntry, String>(
				commentProperties.text(), 400, "Comment");

		List<ColumnConfig<CommentEntry, ?>> columns = new ArrayList<ColumnConfig<CommentEntry, ?>>();
		columns.add(checkBoxSelectionModel.getColumn());
		columns.add(typeCol);
		columns.add(userCol);
		columns.add(sourceCol);
		columns.add(textCol);
		ColumnModel<CommentEntry> cm = new ColumnModel<CommentEntry>(columns);

		commentStore = new ListStore<CommentEntry>(commentProperties.key());
		commentStore.setAutoCommit(true);
		
		List<CommentEntry> commentEntries = createComments();
		for (CommentEntry comment : commentEntries)
			commentStore.add(comment);

		final GroupingView<CommentEntry> groupingView = new GroupingView<CommentEntry>();
		groupingView.setShowGroupedColumn(false);
		groupingView.setForceFit(true);
		groupingView.groupBy(typeCol);

		grid = new Grid<CommentEntry>(commentStore, cm);
		grid.setView(groupingView);
		grid.setContextMenu(createContextMenu());
		grid.setSelectionModel(checkBoxSelectionModel);
		grid.getView().setAutoExpandColumn(textCol);
		grid.setBorders(false);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);

		StringFilter<CommentEntry> textFilter = new StringFilter<CommentEntry>(
				commentProperties.text());
		StringFilter<CommentEntry> sourceFilter = new StringFilter<CommentEntry>(
				commentProperties.source());
		
		ListStore<CommentType> typeFilterStore = new ListStore<CommentType>(
				new ModelKeyProvider<CommentType>() {
					@Override
					public String getKey(CommentType item) {
						return item.toString();
					}
				});
		for(CommentType type : CommentType.values())
			typeFilterStore.add(type);
		ListFilter<CommentEntry, CommentType> typeFilter = new ListFilter<CommentEntry, CommentType>(
				commentProperties.type(), typeFilterStore);

		GridFilters<CommentEntry> filters = new GridFilters<CommentEntry>();
		filters.initPlugin(grid);
		filters.setLocal(true);

		filters.addFilter(textFilter);
		filters.addFilter(sourceFilter);
		filters.addFilter(typeFilter);

		GridInlineEditing<CommentEntry> editing = new GridInlineEditing<CommentEntry>(grid);
		editing.addEditor(textCol, new TextField());
		editing.addCompleteEditHandler(new CompleteEditHandler<CommentEntry>() {
			@Override
			public void onCompleteEdit(CompleteEditEvent<CommentEntry> event) {			
				GridCell cell = event.getEditCell();
				CommentEntry commentEntry = grid.getStore().get(cell.getRow());
				ColumnConfig<CommentEntry, String> config = grid.getColumnModel().getColumn(cell.getCol());
				if(config.equals(textCol)) 
					eventBus.fireEvent(new SetCommentEvent(commentEntry.getObject(), 
							new Comment(OtoSteps.user, commentEntry.getText()), false));
			}
		});

		setBodyBorder(false);
		setHeadingText("Comments");
		setWidth(800);
		setHeight(600);
		setHideOnButtonClick(true);
		setModal(true);

		ContentPanel panel = new ContentPanel();
		panel.add(grid);
		this.add(panel);
	}

	private Menu createContextMenu() {
		Menu menu = new Menu();
		MenuItem removeItem = new MenuItem("Remove");
		menu.add(removeItem);
		removeItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				for (CommentEntry commentEntry : grid.getSelectionModel().getSelectedItems()) {
					commentStore.remove(commentEntry);
					eventBus.fireEvent(new SetCommentEvent(commentEntry.getObject(), new Comment("", ""), false));
				}
			}
		});
		return menu;
	}

	private List<CommentEntry> createComments() {
		List<CommentEntry> commentEntries = new LinkedList<CommentEntry>();
		
		Map<Commentable, List<Comment>> commentsMap =  collection.getComments();
		for(Commentable commentable : commentsMap.keySet()) {
			if(commentable instanceof Term) {
				Term term = (Term)commentable;
				List<Comment> comments = commentsMap.get(term);
				for(int c=0; c<comments.size(); c++) {
					Comment comment = comments.get(c);
					commentEntries.add(new CommentEntry("term-" + term.getId() + "-" + c, term, term.getTerm(), 
							comment.getUser(), comment.getComment()));
				}
			}
			if(commentable instanceof OntologyClassSubmission) {
				OntologyClassSubmission ontologyClassSubmission = (OntologyClassSubmission)commentable;
				List<Comment> comments = commentsMap.get(ontologyClassSubmission);
				for(int c=0; c<comments.size(); c++) {
					Comment comment = comments.get(c);
					commentEntries.add(new CommentEntry("ontologyClassSubmission-" + ontologyClassSubmission.getId() + "-" + c, 
							ontologyClassSubmission, 
							ontologyClassSubmission.getSubmissionTerm() + " -> " + ontologyClassSubmission.getOntology().getAcronym(), 
							comment.getUser(), comment.getComment()));
				}
			}
			if(commentable instanceof OntologySynonymSubmission) {
				OntologySynonymSubmission ontologySynonymSubmission = (OntologySynonymSubmission)commentable;
				List<Comment> comments = commentsMap.get(ontologySynonymSubmission);
				for(int c=0; c<comments.size(); c++) {
					Comment comment = comments.get(c);
					commentEntries.add(new CommentEntry("ontologySynonymSubmission-" + ontologySynonymSubmission.getId() + "-" + c, 
							ontologySynonymSubmission, 
							ontologySynonymSubmission.getSubmissionTerm() + " -> " + ontologySynonymSubmission.getOntology().getAcronym(), 
							comment.getUser(), comment.getComment()));
				}
			}
		}
		
		return commentEntries;
	}

}
