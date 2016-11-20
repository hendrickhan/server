/**
 */
package org.nasdanika.codegen;

import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.DiagnosticChain;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Generator</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.nasdanika.codegen.Generator#getIterator <em>Iterator</em>}</li>
 * </ul>
 *
 * @see org.nasdanika.codegen.CodegenPackage#getGenerator()
 * @model abstract="true" superTypes="org.nasdanika.codegen.WorkFactory<org.nasdanika.codegen.List<T>> org.nasdanika.codegen.Configurable"
 * @generated
 */
public interface Generator<T> extends WorkFactory<List<T>>, Configurable {

	/**
	 * Returns the value of the '<em><b>Iterator</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Iterator</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Iterator</em>' attribute.
	 * @see #setIterator(String)
	 * @see org.nasdanika.codegen.CodegenPackage#getGenerator_Iterator()
	 * @model
	 * @generated
	 */
	String getIterator();

	/**
	 * Sets the value of the '{@link org.nasdanika.codegen.Generator#getIterator <em>Iterator</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Iterator</em>' attribute.
	 * @see #getIterator()
	 * @generated
	 */
	void setIterator(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Validates element for execution/generation. Adds messages to diagnostics and 
	 * @param diagnostics Diagnostics to add validation messages to.
	 * @param context Validation context.
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	boolean validate(DiagnosticChain diagnostics, Map<Object, Object> context);
} // Generator
