package edu.arizona.biosemantics.oto2.steps.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell.CheckBoxAppearance;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell.CheckBoxCellOptions;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.base.client.field.CheckBoxDefaultAppearance.CheckBoxResources;
import com.sencha.gxt.theme.base.client.field.CheckBoxDefaultAppearance.CheckBoxStyle;
import com.sencha.gxt.theme.base.client.field.ValueBaseFieldDefaultAppearance;

import edu.arizona.biosemantics.oto2.steps.client.common.ColorableCheckBoxCell.ColorableCheckBoxCellOptions;

public class ColorableCheckBoxAppearance extends ValueBaseFieldDefaultAppearance implements CheckBoxAppearance {

  protected final CheckBoxResources resources;
  protected final CheckBoxStyle style;
  protected String type = "checkbox";

  public ColorableCheckBoxAppearance() {
    this(GWT.<CheckBoxResources> create(CheckBoxResources.class));
  }

  public ColorableCheckBoxAppearance(CheckBoxResources resources) {
    super(resources);
    this.resources = resources;
    this.style = resources.css();
  }

  @Override
  public XElement getInputElement(Element parent) {
    return parent.<XElement> cast().selectNode("input");
  }

  @Override
  public void onEmpty(Element parent, boolean empty) {

  }

  @Override
  public void onFocus(Element parent, boolean focus) {
    // Override method to prevent outline from being applied to check boxes on
    // focus
  }

  @Override
  public void onValid(Element parent, boolean valid) {
    // no-op, cb is true or false...
  }

  @Override
  public void render(SafeHtmlBuilder sb, Boolean value, CheckBoxCellOptions options) {
    String checkBoxId = XDOM.getUniqueId();

    String nameParam = options.getName() != null ? " name='" + options.getName() + "' " : "";
    String disabledParam = options.isDisabled() ? " disabled=true" : "";
    String readOnlyParam = options.isReadonly() ? " readonly" : "";
    String idParam = " id=" + checkBoxId;
    String typeParam = " type=" + type;
    String checkedParam = value ? " checked" : "";

    String backgroundHex = "";
    if(options instanceof ColorableCheckBoxCellOptions) {
    	ColorableCheckBoxCellOptions colorableCheckBoxCellOptions = (ColorableCheckBoxCellOptions)options;
    	backgroundHex = colorableCheckBoxCellOptions.getColorHex();
    }
   
    sb.appendHtmlConstant("<div class=" + style.wrap() + " style=background-color:" + backgroundHex + ";>");
    sb.appendHtmlConstant("<input " + typeParam + nameParam + disabledParam + readOnlyParam + idParam + checkedParam + " />");
    sb.appendHtmlConstant("<label for=" + checkBoxId + " class=" + style.checkBoxLabel() + ">");
    if (options.getBoxLabel() != null) {
      sb.appendHtmlConstant(options.getBoxLabel());
    }
    sb.appendHtmlConstant("</label></div>");

  }

  @Override
  public void setBoxLabel(String boxLabel, XElement parent) {
    parent.selectNode("." + resources.css().checkBoxLabel()).<LabelElement> cast().setInnerHTML(boxLabel);
  }

  @Override
  public void setReadOnly(Element parent, boolean readOnly) {
    getInputElement(parent).<InputElement> cast().setReadOnly(readOnly);
  }

}
