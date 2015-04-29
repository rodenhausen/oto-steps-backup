package edu.arizona.biosemantics.oto2.steps.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.resources.ThemeStyles;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.menu.HeaderMenuItem;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuBar;
import com.sencha.gxt.widget.core.client.menu.MenuBarItem;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.steps.client.common.ColorSettingsDialog;
import edu.arizona.biosemantics.oto2.steps.client.common.ColorsDialog;
import edu.arizona.biosemantics.oto2.steps.client.common.CommentsDialog;
import edu.arizona.biosemantics.oto2.steps.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;

public class MenuView extends MenuBar {

	private EventBus eventBus;
	private Collection collection;

	public MenuView(EventBus eventBus) {
		this.eventBus = eventBus;
		addStyleName(ThemeStyles.get().style().borderBottom());
		addItems();
		
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
	
	protected void addItems() {
		add(createFileItem());
		add(createSearchItem());
		add(createOntologiesItem());
		add(createAnnotationsItem());
		//add(createViewItem());
		add(createQuestionItem());
	}
	

	private Widget createOntologiesItem() {
		Menu sub = new Menu();
		/*MenuItem selectOntologies = new MenuItem("Select Ontologies for Term Information ");
		selectOntologies.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				//Dialog dialog = new SelectOntologiesDialog(eventBus);
				//dialog.show();
			}
		});
		sub.add(selectOntologies);*/
		MenuBarItem item = new MenuBarItem("Ontologies", sub);
		return item;
	}

	private Widget createQuestionItem() {
		Menu sub = new Menu();
		MenuBarItem questionsItem = new MenuBarItem("Instructions", sub);
		MenuItem helpItem = new MenuItem("Help");
		helpItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> arg0) {
				final Dialog dialog = new Dialog();
				dialog.setBodyBorder(false);
				dialog.setHeadingText("Help");
				dialog.setHideOnButtonClick(true);
				//dialog.setWidget(new HelpView());
				dialog.setWidth(600);
				dialog.setMaximizable(true);
				//dialog.setMinimizable(true);
				dialog.setHeight(400);
				dialog.setResizable(true);
				dialog.setShadow(true);
				dialog.show();
			}
		});
		sub.add(helpItem);
		return questionsItem;
	}

	private Widget createViewItem() {
		return null;
	}

	private Widget createAnnotationsItem() {
		Menu sub = new Menu();
		MenuBarItem annotationsItem = new MenuBarItem("Annotation", sub);
		sub.add(new HeaderMenuItem("Configure"));
		MenuItem colorSettingsItem = new MenuItem("Color Settings");
		colorSettingsItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> arg0) {
				ColorSettingsDialog dialog = new ColorSettingsDialog(eventBus, collection);
				dialog.show();
			}
		});
		sub.add(colorSettingsItem);
		sub.add(new HeaderMenuItem("Show"));
		MenuItem colorsItem = new MenuItem("Color Use");
		colorsItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> arg0) {
				ColorsDialog dialog = new ColorsDialog(eventBus, collection);
				dialog.show();
			}
		});
		sub.add(colorsItem);
		MenuItem commentsItem = new MenuItem("Comments");
		commentsItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> arg0) {
				CommentsDialog dialog = new CommentsDialog(eventBus, collection);
				dialog.show();
			}
		});
		sub.add(commentsItem);
		return annotationsItem;
	}

	private Widget createSearchItem() {
		Menu sub = new Menu();
		//final ComboBox<Term> searchCombo = new ComboBox<Term>(termStore,
		//		termProperties.nameLabel());
		//searchCombo.setForceSelection(true);
		//searchCombo.setTriggerAction(TriggerAction.ALL);
		/*searchCombo.addSelectionHandler(new SelectionHandler<Term>() {
			@Override
			public void onSelection(SelectionEvent<Term> arg0) {
				eventBus.fireEvent(new TermSelectEvent(arg0
						.getSelectedItem()));
			}
		});*/
		//sub.add(searchCombo);
		MenuBarItem item = new MenuBarItem("Search", sub);
		return item;
	}

	private Widget createFileItem() {
		Menu sub = new Menu();
		MenuBarItem item = new MenuBarItem("File", sub);
		return item;
	}
}