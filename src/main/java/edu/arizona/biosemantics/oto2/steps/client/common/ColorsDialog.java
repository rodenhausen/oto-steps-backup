package edu.arizona.biosemantics.oto2.steps.client.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.theme.base.client.colorpalette.ColorPaletteBaseAppearance;
import com.sencha.gxt.widget.core.client.ColorPaletteCell;
import com.sencha.gxt.widget.core.client.ColorPaletteCell.ColorPaletteAppearance;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.event.CellSelectionEvent;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.steps.client.event.SetColorEvent;
import edu.arizona.biosemantics.oto2.steps.shared.model.Collection;
import edu.arizona.biosemantics.oto2.steps.shared.model.Color;
import edu.arizona.biosemantics.oto2.steps.shared.model.ColorProperties;
import edu.arizona.biosemantics.oto2.steps.shared.model.Colorable;
import edu.arizona.biosemantics.oto2.steps.shared.model.Term;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologyClassSubmission;
import edu.arizona.biosemantics.oto2.steps.shared.model.toontology.OntologySynonymSubmission;

public class ColorsDialog extends CommonDialog {

	public enum ColorEntryType {
		TERM("Term"),
		ONTOLOGY_CLASS_SUBMISSION("Ontology Class Submission"),
		ONTOLOGY_SYNONYM_SUBMISSION("Ontology Synonym Submission");
		
		private String readable;

		private ColorEntryType(String readable) {
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
	
	public class ColorEntry {

		private String id;
		private String source;
		private Color color;
		private Object object;
		private ColorEntryType type;

		public ColorEntry(String id, Object object, String source, Color color) {
			this.id = id;
			this.object = object;
			if (object instanceof Term)
				type = ColorEntryType.TERM;
			if (object instanceof OntologyClassSubmission)
				type = ColorEntryType.ONTOLOGY_CLASS_SUBMISSION;
			if (object instanceof OntologySynonymSubmission)
				type = ColorEntryType.ONTOLOGY_SYNONYM_SUBMISSION;
			this.source = source;
			this.color = color;
		}		
		
		public Object getObject() {
			return object;
		}

		public String getSource() {
			return source;
		}

		public Color getColor() {
			return color;
		}
		
		public ColorEntryType getType() {
			return type;
		}


		public void setColor(Color color) {
			this.color = color;
		}

		public void setObject(Object object) {
			this.object = object;
		}
		
		public String getId() {
			return id;
		}
	}

	public interface ColorEntryProperties extends PropertyAccess<ColorEntry> {

		@Path("id")
		ModelKeyProvider<ColorEntry> key();

		@Path("object")
		ValueProvider<ColorEntry, Object> object();
		
		@Path("source")
		ValueProvider<ColorEntry, String> source();
		
		@Path("color")
		ValueProvider<ColorEntry, Color> color();
		
		@Path("type")
		ValueProvider<ColorEntry, ColorEntryType> type();

	}

	private EventBus eventBus;
	private Collection collection;
	private ListStore<ColorEntry> colorEntriesStore;
	private Grid<ColorEntry> grid;
	private ColorEntryProperties colorEntryProperties = GWT.create(ColorEntryProperties.class);
	private ColorProperties colorProperties = GWT.create(ColorProperties.class);
	private ColorPaletteBaseAppearance appearance = GWT.create(ColorPaletteAppearance.class);

	public ColorsDialog(final EventBus eventBus, final Collection collection) {
		this.eventBus = eventBus;
		this.collection = collection;
		
		IdentityValueProvider<ColorEntry> identity = new IdentityValueProvider<ColorEntry>();
	    final CheckBoxSelectionModel<ColorEntry> checkBoxSelectionModel = new CheckBoxSelectionModel<ColorEntry>(identity);
	    		
	    ColumnConfig<ColorEntry, ColorEntryType> typeCol = new ColumnConfig<ColorEntry, ColorEntryType>(
				colorEntryProperties.type(), 0, "Type");
		ColumnConfig<ColorEntry, String> sourceCol = new ColumnConfig<ColorEntry, String>(
				colorEntryProperties.source(), 190, "Source");
		final ColumnConfig<ColorEntry, String> colorCol = new ColumnConfig<ColorEntry, String>(new ValueProvider<ColorEntry, String>() {
			@Override
			public String getValue(ColorEntry object) {
				return object.getColor().getHex();
			}
			@Override
			public void setValue(ColorEntry object, String value) {
				//don't implement ColorPalette will call this and overwrite the Color's correct hex value
			}
			@Override
			public String getPath() {
				return "color";
			}
		}, 210, "Color");
		/*colorCol.setCell(new AbstractCell<Color>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,
					Color value, SafeHtmlBuilder sb) {
				String colorHex = value.getHex();
				sb.append(SafeHtmlUtils.fromTrustedString("<div style='width:15px;height:15px; background-color:#" + colorHex + "'/>"));
			}
		});*/
		
		int numColors = collection.getColors().size();
		appearance.setColumnCount(8);
		List<Color> colors = collection.getColors();
		final Map<String, Color> hexColorsMap = new HashMap<String, Color>();
		String[] hexs = new String[colors.size()];
		String[] labels = new String[colors.size()];
		for(int i=0; i<colors.size(); i++) {
			Color color = colors.get(i);
			hexColorsMap.put(color.getHex(), color);
			hexs[i] = color.getHex();
			labels[i] = color.getUse();
		}
		//ColorPalette colorPalette = new ColorPalette();
		//colorPalette.get
		ColorPaletteCell colorPaletteCell = new ColorPaletteCell(appearance, hexs, labels) {
			@Override
			public boolean handlesSelection() {
				return true;
			}
		};
		colorPaletteCell.addSelectionHandler(new SelectionHandler<String>() {
			@Override
			public void onSelection(SelectionEvent<String> event) {
				String selectedHex = event.getSelectedItem();
				Color selectedColor = hexColorsMap.get(selectedHex);
				if(event instanceof CellSelectionEvent) {
					CellSelectionEvent cellEvent = (CellSelectionEvent)event;
					final ColorEntry colorEntry = grid.getStore().get(cellEvent.getContext().getIndex());
					colorEntry.setColor(selectedColor);
					eventBus.fireEvent(new SetColorEvent(colorEntry.getObject(), selectedColor, false));
					colorEntriesStore.update(colorEntry);
				}
			}
		});
		colorCol.setCell(colorPaletteCell);
		
		final ColumnConfig<ColorEntry, Color> colorUseCol = new ColumnConfig<ColorEntry, Color>(
				colorEntryProperties.color(), 400, "Color Use");
		colorUseCol.setCell(new AbstractCell<Color>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,
					Color value, SafeHtmlBuilder sb) {
				sb.append(SafeHtmlUtils.fromTrustedString(value.getUse()));
			}
		});
		List<ColumnConfig<ColorEntry, ?>> columns = new ArrayList<ColumnConfig<ColorEntry, ?>>();
		columns.add(checkBoxSelectionModel.getColumn());
		columns.add(typeCol);
		columns.add(sourceCol);
		columns.add(colorCol);
		columns.add(colorUseCol);
		ColumnModel<ColorEntry> cm = new ColumnModel<ColorEntry>(columns);
		
		colorEntriesStore = new ListStore<ColorEntry>(colorEntryProperties.key());
		colorEntriesStore.setAutoCommit(true);
		List<ColorEntry> colorEntries = createColorEntries();
		//final Map<Value, ColorEntry> valueColorEntries = new HashMap<Value, ColorEntry>();
		for (ColorEntry colorEntry : colorEntries) {
			//if(colorEntry.getType().equals(ColorEntryType.taxonCharacterValueType))
			//	valueColorEntries.put((Value)colorEntry.getObject(), colorEntry);
			colorEntriesStore.add(colorEntry);
		}
				
		final GroupingView<ColorEntry> groupingView = new GroupingView<ColorEntry>();
	    groupingView.setShowGroupedColumn(false);
	    groupingView.setForceFit(true);
	    groupingView.groupBy(typeCol);
		
		grid = new Grid<ColorEntry>(colorEntriesStore, cm);
		grid.setView(groupingView);
		grid.setContextMenu(createContextMenu());
		grid.setSelectionModel(checkBoxSelectionModel);
		grid.getView().setAutoExpandColumn(colorUseCol);
		grid.setBorders(false);
	    grid.getView().setStripeRows(true);
	    grid.getView().setColumnLines(true);
	    
	    StringFilter<ColorEntry> textFilter = new StringFilter<ColorEntry>(new ValueProvider<ColorEntry, String>() {
			@Override
			public String getValue(ColorEntry object) {
				return object.getColor().getUse();
			}
			@Override
			public void setValue(ColorEntry object, String value) { // should never update color	
			}
			@Override
			public String getPath() {
				return "color";
			}
	    }); 
	    StringFilter<ColorEntry> sourceFilter = new StringFilter<ColorEntry>(colorEntryProperties.source());
	    
	    ListStore<ColorEntryType> typeFilterStore = new ListStore<ColorEntryType>(new ModelKeyProvider<ColorEntryType>() {
			@Override
			public String getKey(ColorEntryType item) {
				return item.toString();
			}
	    });
		for(ColorEntryType type : ColorEntryType.values())
			typeFilterStore.add(type);
	    
	    ListFilter<ColorEntry, ColorEntryType> typeFilter = new ListFilter<ColorEntry, ColorEntryType>(colorEntryProperties.type(), typeFilterStore);
	    
	    /*ListStore<Color> hexFilterStore = new ListStore<Color>(colorProperties.key());
	    for(Color color : model.getColors()) {
	    	hexFilterStore.add(color);
	    }	    
	    ListFilter<ColorEntry, Color> hexFilter = new ListFilter<ColorEntry, Color>(colorEntryProperties.color(), hexFilterStore);
	    */
	    
	    /*ListStore<String> textFilterStore = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
	    });
	    for(Color color : model.getColors()) {
	    	textFilterStore.add(color.getUse());
	    }	    
	    ListFilter<ColorEntry, String> textFilter = new ListFilter<ColorEntry, String>(new ValueProvider<ColorEntry, String>() {
			@Override
			public String getValue(ColorEntry object) {
				return object.getColor().getUse();
			}
			@Override
			public void setValue(ColorEntry object, String value) {	}

			@Override
			public String getPath() {
				return "use";
			}
	    }, textFilterStore); */
	    
		GridFilters<ColorEntry> filters = new GridFilters<ColorEntry>();
		filters.initPlugin(grid);
		filters.setLocal(true);
		
		filters.addFilter(textFilter);
		filters.addFilter(sourceFilter);
		filters.addFilter(typeFilter);
		//filters.addFilter(hexFilter);
		
		GridInlineEditing<ColorEntry> editing = new GridInlineEditing<ColorEntry>(grid);
		/*editing.addEditor(textCol, new Converter<Color, String>() {
			@Override
			public Color convertFieldValue(String object) {	
				return new Color("FFFFFF", object);
			}
			@Override
			public String convertModelValue(Color object) {
				return object.getUse();
			}
		}, new TextField()); */

		setBodyBorder(false);
		setHeadingText("Colorations");
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
				for(ColorEntry colorEntry : grid.getSelectionModel().getSelectedItems()) {
					colorEntriesStore.remove(colorEntry);
					Object object = colorEntry.getObject();
					eventBus.fireEvent(new SetColorEvent(object, null, false));
				}
			}
		});
		return menu;
	}

	private List<ColorEntry> createColorEntries() {	
		List<ColorEntry> colorEntries = new LinkedList<ColorEntry>();
				
		Map<Colorable, Color> colorizationMap = collection.getColorizations();
		for(Colorable colorable : colorizationMap.keySet()) {
			if(colorable instanceof Term) {
				Term term = (Term)colorable;
				colorEntries.add(new ColorEntry("term-" + term.getId(), term, term.getTerm(), colorizationMap.get(colorable)));
			}
			if(colorable instanceof OntologyClassSubmission) {
				OntologyClassSubmission ontologyClassSubmission = (OntologyClassSubmission)colorable;
				colorEntries.add(new ColorEntry("ontologyClassSubmission-" + ontologyClassSubmission.getId(), 
						ontologyClassSubmission, 
						ontologyClassSubmission.getSubmissionTerm() + " -> " + ontologyClassSubmission.getOntology().getAcronym(), 
						colorizationMap.get(colorable)));
			}
			if(colorable instanceof OntologySynonymSubmission) {
				OntologySynonymSubmission ontologySynonymSubmission = (OntologySynonymSubmission)colorable;
				colorEntries.add(new ColorEntry("ontologySynonymSubmission-" + ontologySynonymSubmission.getId(), 
						ontologySynonymSubmission, 
						ontologySynonymSubmission.getSubmissionTerm() + " -> " + ontologySynonymSubmission.getOntology().getAcronym(), 
						colorizationMap.get(colorable)));
			}
		}
		return colorEntries;
	}

}
