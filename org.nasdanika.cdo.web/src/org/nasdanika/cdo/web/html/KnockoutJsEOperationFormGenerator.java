package org.nasdanika.cdo.web.html;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.nasdanika.cdo.web.routes.CDOWebUtil;
import org.nasdanika.html.Form;
import org.nasdanika.html.HTMLFactory;

/**
 * Generates Bootstrap HTML form invoking EOperation through JavaScript API. Uses EOperation and EParameters metadata and annotations.
 * @author Pavel
 *
 */
public class KnockoutJsEOperationFormGenerator extends KnockoutJsFormGeneratorBase<EOperation, EParameter> {

	public KnockoutJsEOperationFormGenerator(EOperation eOperation, String model, String submitHandler, String cancelHandler) {
		super(eOperation, model, submitHandler, cancelHandler);
	}

	/**
	 * 
	 * @param htmlFactory
	 * @param form
	 * @throws Exception
	 */
	protected void populateForm(HTMLFactory htmlFactory, Form form) throws Exception {
		super.populateForm(htmlFactory, form);
		for (EParameter param: sortParameters(getSource().getEParameters())) {
			generateGroup(htmlFactory, form, param);
		}		
	}

	/**
	 * Subclasses can override this method to sort parameters. This implementation just returns <code>parameters</code> argument.
	 * @param parameters
	 * @return
	 */
	protected List<EParameter> sortParameters(List<EParameter> parameters) {
		return parameters;
	}
	
	@Override
	protected List<String[]> generateModelEntries() {
		List<String[]> ret = super.generateModelEntries();
		for (EParameter param: getSource().getEParameters()) {
			EAnnotation ann = param.getEAnnotation(FORM_CONTROL_ANNOTATION_SOURCE);
			ret.add(new String[] { 
					param.getName(), 
					ann==null ? null : ann.getDetails().get(DEFAULT_VALUE_KEY), 
					ann==null ? null : ann.getDetails().get(VALIDATOR_KEY)});
		}				
		return ret;
	}

	@Override
	protected String generateApply() throws Exception {
		StringBuilder applyBuilder = new StringBuilder("return (typeof applyTarget === 'object' && typeof applyTarget."+getSource().getName()+" === 'function' ? applyTarget."+getSource().getName()+" : applyTarget)(");
		Iterator<EParameter> pit = getSource().getEParameters().iterator();
		while (pit.hasNext()) {
			EParameter param = pit.next();
			if (param.getEAnnotation(CDOWebUtil.ANNOTATION_CONTEXT_PARAMETER)==null && param.getEAnnotation(CDOWebUtil.ANNOTATION_SERVICE_PARAMETER)==null) {			
				applyBuilder.append("this.observableData."+param.getName()+"()");
				if (pit.hasNext()) {
					applyBuilder.append(", ");
				}
			}
		}
		return applyBuilder.append(");").toString();
	}
	
	@Override
	protected Object generateControl(HTMLFactory htmlFactory, Form form, EParameter element) throws Exception {
		if (element.getEAnnotation(CDOWebUtil.ANNOTATION_CONTEXT_PARAMETER)!=null || element.getEAnnotation(CDOWebUtil.ANNOTATION_SERVICE_PARAMETER)!=null) {
			return null;
		}
		return super.generateControl(htmlFactory, form, element);
	}
}
