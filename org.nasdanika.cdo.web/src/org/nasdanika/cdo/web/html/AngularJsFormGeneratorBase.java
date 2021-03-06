package org.nasdanika.cdo.web.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ETypedElement;
import org.nasdanika.cdo.web.routes.CDOWebUtil;
import org.nasdanika.html.Form;
import org.nasdanika.html.FormGroup;
import org.nasdanika.html.FormInputGroup;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.InputBase;
import org.nasdanika.html.UIElement;

/**
 * Adds ng- attributes, uses help text to display validation errors, adds form validation error text on the top of the form. 
 * @author Pavel
 *
 * @param <T>
 */
public abstract class AngularJsFormGeneratorBase<S extends EModelElement, T extends ETypedElement> extends FormGeneratorBase<T> {

	private String model;
	private String handler;
	private S source;
	
	/**
	 * Source of generation metadata
	 * @return
	 */
	public S getSource() {
		return source;
	}
	
	@Override
	protected EAnnotation getFormAnnotation() {
		return source.getEAnnotation(FORM_ANNOTATION_SOURCE);
	}

	/**
	 * 
	 * @param model Model expression. The model object shall contain data object to which form controls are bound, validationResults object
	 * which holds validation messages for controls, and validationResult string with form validation result.
	 * @param handler
	 */
	protected AngularJsFormGeneratorBase(S source, String model, String handler) {
		this.source = source;
		this.model = model;
		this.handler = handler;
	}
	
	@Override
	public Form generateForm(HTMLFactory htmlFactory) throws Exception {		
		Form form = super.generateForm(htmlFactory);
		form.ngSubmit(handler);
		return form;
	}
	
	/**
	 * Adds DIV for form validation message.
	 */
	@Override
	protected void populateForm(HTMLFactory htmlFactory, Form form)	throws Exception {
		form.content(htmlFactory.div().style("color", "red").angular().bind(model+".validationResults['"+CDOWebUtil.getThisKey(getSource())+"']"));		
	}
	
	@Override
	protected Object generateHelpText(HTMLFactory htmlFactory, T element) {
		return htmlFactory.span().style("color", "red").angular().bind(model+".validationResults."+element.getName());
	}

	@Override
	protected void configureGroup(HTMLFactory htmlFactory, T element, Object group) {
		super.configureGroup(htmlFactory, element, group);
		if (group instanceof FormGroup) { 
			FormGroup<?> formGroup = (FormGroup<?>) group;
			if (!(group instanceof FormInputGroup)) {
				formGroup.feedback();
			}
			formGroup.angular().clazz("{ 'has-error' : "+model+".validationResults."+element.getName()+" }");
		}
	}
	
	@Override
	protected void configureControl(HTMLFactory htmlFactory, T element,	Object control) {		
		super.configureControl(htmlFactory, element, control);
		if (control instanceof InputBase) {
			((InputBase<?>) control).angular().model(model+".data."+element.getName());
		} else if (control instanceof UIElement) {
			((UIElement<?>) control).angular().bind(model+".data."+element.getName());
		}
	}
	
	/**
	 * Generates model object with asynchronous validation function.
	 * @return
	 * @throws Exception 
	 */
	public String generateModel() throws Exception {
		StringBuilder sb = new StringBuilder("{ ");
		Iterator<String> eit = generateModelEntries().iterator();
		while (eit.hasNext()) {
			sb.append(eit.next());
			if (eit.hasNext()) {
				sb.append(", ");
			}
		}		
		
		EAnnotation formAnnotation = source.getEAnnotation(FORM_ANNOTATION_SOURCE);
		if (formAnnotation!=null && formAnnotation.getDetails().containsKey(MODEL_KEY)) {
			sb.append(formAnnotation.getDetails().get(MODEL_KEY));
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	protected List<String> generateModelEntries() throws Exception {
		List<String> ret = new ArrayList<String>();
		ret.add("data: "+generateDataEntry());
		ret.add("createData: function() { return "+generateDataEntry()+"; }");
		ret.add("clear: function() { this.data = this.createData(); this.validationResults = {}; }");
		ret.add("validationResults: {}");
		StringBuilder sb = new StringBuilder("validate: function() {");
		sb.append("return Q.all([");
		List<String> vr = generateValidationEntries();
		for (int i=0, l=vr.size(); i<l; ++i) {
			if (i>0) {
				sb.append(",");
			}
			sb.append(vr.get(i));
		}
		sb.append("]).then(function(vResults) { return vResults.reduce(function(r1, r2) { return r1 && r2; }, true); });");		
		sb.append("}");
		ret.add(sb.toString());
		return ret;
	}
	
	protected abstract String generateDataEntry() throws Exception;

	protected abstract List<String> generateValidationEntries();
	
	protected String generateValidationEntry(String value, String validator, String target) {
		return "Q.when("+value+").then(function(value) { "+validator+"}.bind(this)).then(function(validationResult) { "+target+"=validationResult; return !validationResult; }.bind(this))";
	}
}
