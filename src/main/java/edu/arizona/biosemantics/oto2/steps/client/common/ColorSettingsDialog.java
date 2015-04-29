package edu.arizona.biosemantics.oto2.steps.client.common;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;

import edu.arizona.biosemantics.oto2.steps.client.event.SetColorsEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionService;
import edu.arizona.biosemantics.oto2.steps.shared.rpc.ICollectionServiceAsync;


public class ColorSettingsDialog extends CommonDialog {
	
	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);

	public ColorSettingsDialog(final EventBus eventBus, final Collection collection) {
		final CellTable<Color> colorsTable = new CellTable<Color>();
		final LinkedList<Color> colorsCopy = new LinkedList<Color>(collection.getColors());

		this.setBodyBorder(false);
		this.setHeadingText("Configure Color Usages");
		this.setWidth(600);
		this.setHeight(400);
		this.setModal(true);
		this.setHideOnButtonClick(true);
		this.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				collection.setColors(colorsCopy);
				collectionService.update(collection, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.failedToSetColors(caught);
					}
					@Override
					public void onSuccess(Void result) {
						eventBus.fireEvent(new SetColorsEvent(colorsCopy));
					}
				});
			}
		});

		BorderLayoutContainer layout = new BorderLayoutContainer();
		this.add(layout);

		// Layout - west
		ContentPanel westPanel = new ContentPanel();
		westPanel.setHeadingText("Select Color");
		BorderLayoutData data = new BorderLayoutData(302);
		data.setMargins(new Margins(0, 5, 0, 0));
		westPanel.setLayoutData(data);
		VerticalLayoutContainer colorChoosePanel = new VerticalLayoutContainer();
		
		ColorPalette colorPalette = new ColorPalette();
		colorChoosePanel.add(new FieldLabel(colorPalette, "Color Palette"));
		final TextField hexField = new TextField();
		final Label colorLabel = new Label();
		colorLabel.setWidth("30px");
		colorLabel.setHeight("30px");
		colorChoosePanel.add(new FieldLabel(hexField, "Hexadecimal Color-code"));
		colorChoosePanel.add(new FieldLabel(colorLabel, "Color"));
		colorPalette.addSelectionHandler(new SelectionHandler<String>() {
			@Override
			public void onSelection(SelectionEvent<String> event) {
				hexField.setValue(event.getSelectedItem());
				hexField.validate();
			}
		});
		hexField.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				hexField.validate();
			}
		});
		hexField.addValidator(new Validator<String>() {
			@Override
			public List<EditorError> validate(Editor<String> editor,
					String value) {
				List<EditorError> error = new LinkedList<EditorError>();
				RegExp hexPattern = RegExp.compile("^([A-Fa-f0-9]{6})$");
				if (!hexPattern.test(hexField.getValue())) {
					error.add(new DefaultEditorError(editor,
							"Not a valid color code", value));
				} else {
					String text = hexField.getText();
					if (text.length() == 6) {
						colorLabel.getElement().getStyle()
								.setBackgroundColor("#" + text);
					}
				}
				return error;
			}
		});
		westPanel.add(colorChoosePanel);
		Button addButton = new Button("add");
		westPanel.addButton(addButton);
		
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(hexField.validate()) {
					boolean exists = false;
					for(Color color : colorsCopy) {
						if(color.getHex().equals(hexField.getText())) {
							AlertMessageBox alert = new AlertMessageBox("Duplicate color", "This color is being used");
							alert.show();
							exists = true;
						}
					}
					if(!exists) {
						Color color = new Color(hexField.getText(), "");
						colorsCopy.add(color);
						colorsTable.setRowData(colorsCopy);
					}
				} else {
					AlertMessageBox alert = new AlertMessageBox("Not a valid color", "invalid color code");
					alert.show();
				}
			}
		});

		BorderLayoutData westLayoutData = new BorderLayoutData(280);
		westLayoutData.setCollapsible(false);
		westLayoutData.setSplit(false);
		westLayoutData.setCollapseMini(false);
		westLayoutData.setMargins(new Margins(0, 8, 0, 5));
		layout.setWestWidget(westPanel, westLayoutData);

		// Layout - center
		ContentPanel centerPanel = new ContentPanel();
		centerPanel.setHeadingText("Available color usages");
		layout.setCenterWidget(centerPanel);

		final CheckboxCell checkboxCell = new CheckboxCell();
		Column<Color, Boolean> checkColumn = new Column<Color, Boolean>(
				checkboxCell) {
			@Override
			public Boolean getValue(Color object) {
				return false;
			}
		};
		final ColorCell customCell = new ColorCell();
		Column<Color, String> colorColumn = new Column<Color, String>(
				customCell) {
			@Override
			public String getValue(Color object) {
				return object.getHex();
			}
		};
		final TextInputCell useCell = new TextInputCell();
		Column<Color, String> useColumn = new Column<Color, String>(useCell) {
			@Override
			public String getValue(Color object) {
				return object.getUse();
			}
		};
		useColumn.setFieldUpdater(new FieldUpdater<Color, String>() {
			@Override
			public void update(int index, Color object, String value) {
				object.setUse(value);
			}
		});

		//colorsTable.setTableLayoutFixed(true);
		colorsTable.addColumn(checkColumn, "");
		//colorsTable.setColumnWidth(checkColumn, "10%");
		colorsTable.addColumn(colorColumn, "Color");
		//colorsTable.setColumnWidth(colorColumn, "10%");
		colorsTable.addColumn(useColumn, "Usage");
		//colorsTable.setColumnWidth(useColumn, "80%");
		colorsTable.setRowData(colorsCopy);

		ScrollPanel scrollPanel = new ScrollPanel();
		Button removeButton = new Button("remove");

		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Set<Color> toRemove = new HashSet<Color>();
				for (Color color : colorsCopy) {
					Boolean checked = checkboxCell.getViewData(color);
					if (checked == null)
						continue;
					if (checked) {
						toRemove.add(color);
					}
				}
				for (Color color : toRemove) {
					colorsCopy.remove(color);
				}
				colorsTable.setRowData(colorsCopy);
			}
		});
		scrollPanel.setWidget(colorsTable);
		centerPanel.setWidget(scrollPanel);
		centerPanel.addButton(removeButton);
	}

}
