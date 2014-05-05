package org.nasdanika.html.impl;

import org.nasdanika.html.Button;
import org.nasdanika.html.FieldSet;
import org.nasdanika.html.FieldContainer;
import org.nasdanika.html.Form;
import org.nasdanika.html.FormFragment;
import org.nasdanika.html.FormGroup;
import org.nasdanika.html.FormInputGroup;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.InputGroup;

class FormImpl extends UIElementImpl<Form> implements Form {
	
	boolean horizontal;
	boolean inline;
	DeviceSize deviceSize;
	int labelWidth;
	private FieldContainer<Form> container;

	FormImpl(HTMLFactory factory, boolean nav, boolean navRight) {
		super(factory);
		container = new FieldContainerImpl<Form>(factory, this, this);
		if (nav) {
			addClass("navbar-form");
			if (navRight) {
				addClass("navbar-right");
			} else {
				addClass("navbar-left");
			}
		} 
		
		attribute("role", "form");
	}
	
	@Override
	public Form horizontal(DeviceSize deviceSize, int labelWidth) {
		horizontal = true;
		this.deviceSize = deviceSize;
		this.labelWidth = labelWidth;
		addClass("form-horizontal");
		return this;
	}

	@Override
	public Form inline(boolean inline) {
		this.inline = inline;
		addClass("form-inline");
		return this;
	}

	@Override
	public Form inline() {
		return inline(true);
	}

	@Override
	public Form content(Object... content) {
		return container.content(content);
	}

	@Override
	public FormGroup<?> formGroup(Object label, Object controlId, Object control, Object helpText) {
		return container.formGroup(label, controlId, control, helpText);
	}

	@Override
	public Form checkbox(Object label, Object checkboxControl, boolean inline) {
		return container.checkbox(label, checkboxControl, inline);
	}

	@Override
	public Form radio(Object label, Object radioControl, boolean inline) {
		return container.radio(label, radioControl, inline);
	}

	@Override
	public Button button(Object... content) {
		return container.button(content);
	}

	@Override
	public InputGroup<?> inputGroup(Object control) {
		return container.inputGroup(control);
	}

	@Override
	public FieldSet fieldset() {
		return container.fieldset();
	}
	
	@Override
	public FormFragment formFragment() {
		return container.formFragment();
	}

	@Override
	public FormInputGroup formInputGroup(Object label, Object controlId, Object control, Object helpText) {
		return container.formInputGroup(label, controlId, control, helpText);
	}
	
	@Override
	public String toString() {
		return new StringBuilder("<form")
			.append(attributes())
			.append(">")
			.append(container.toString())
			.append("</form>")
			.toString();
	}
	
	@Override
	public void close() throws Exception {
		close(container);		
	}

}