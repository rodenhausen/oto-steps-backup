package edu.arizona.biosemantics.oto2.steps.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.data.shared.ListStore;

import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.model.Colorable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Commentable;

public class ColorableCheckBoxCell extends CheckBoxCell {

	public interface CommentableColorableProvider {
		
		public Colorable provideColorable(Object source);
		public Commentable provideCommentable(Object source);
	}
	
	public static class ColorableCheckBoxCellOptions extends CheckBoxCellOptions {

	    private String boxLabel;
	    private String colorHex;
	    
	    public String getBoxLabel() {
	      return boxLabel;
	    }

	    public void setBoxLabel(String boxLabel) {
	      this.boxLabel = boxLabel;
	    }

		public String getColorHex() {
			return colorHex;
		}

		public void setColorHex(String colorHex) {
			this.colorHex = colorHex;
		}
	  }

	private ListStore commentableColorableStore;
	private CommentableColorableProvider commentableColorableProvider;
	private Collection collection;
	private EventBus eventBus;
	private ColorableCheckBoxAppearance appearance;
	
	
	public ColorableCheckBoxCell(EventBus eventBus, Collection collection) {
		this(GWT.<ColorableCheckBoxAppearance> create(ColorableCheckBoxAppearance.class), eventBus, collection);
	}
	
	public ColorableCheckBoxCell(ColorableCheckBoxAppearance appearance, EventBus eventBus, Collection collection) {	
		super(appearance);
		this.eventBus = eventBus;
		this.collection = collection;
		
		/*
		System.out.println(styles.headOver());
		System.out.println(styles.columnMoveBottom());
		System.out.println(styles.columnMoveTop());
		System.out.println(styles.head());
		System.out.println(styles.headButton());
		System.out.println(styles.header());
		System.out.println(styles.headInner());
		System.out.println(styles.headMenuOpen());
		System.out.println(styles.headOver());
		System.out.println(styles.headRow());
		System.out.println(styles.sortAsc());
		System.out.println(styles.sortDesc());
		System.out.println(styles.sortIcon());
		System.out.println(styles.headerInner());
		*/
		bindEvents();
	}
	
	
	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				collection = event.getCollection();
			}
		});
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
		ColorableCheckBoxCellOptions opts = new ColorableCheckBoxCellOptions();

		int rowIndex = context.getIndex();
		Object source = commentableColorableStore.get(rowIndex);
		Commentable commentable = commentableColorableProvider.provideCommentable(source);
		Colorable colorable = commentableColorableProvider.provideColorable(source);
		
		Color color = null;
		if(colorable != null) {
			color = collection.getColorization(colorable);
		}

		String colorHex = "";
		if(color != null) 
			colorHex = "#" + color.getHex();
		
		// radios must have a name for ie6 and ie7
		if (name == null && (GXT.isIE6() || GXT.isIE7())) {
			name = XDOM.getUniqueId();
		}

		opts.setName(name);
		opts.setColorHex(colorHex);

		opts.setReadonly(isReadOnly());
		opts.setDisabled(isDisabled());
		opts.setBoxLabel(getBoxLabel());

		getAppearance().render(sb, value == null ? false : value, opts);
	}
	
	public void setCommentColorizableObjectsStore(ListStore<Object> commentableColorableStore, CommentableColorableProvider commentableColorableProvider) {
		this.commentableColorableStore = commentableColorableStore;
		this.commentableColorableProvider = commentableColorableProvider;
	}
}
