/**
 */
package org.nasdanika.cdo.security;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Protected</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Classes extending this interface maintain a list of permissions referencing principals.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.nasdanika.cdo.security.Protected#getPermissions <em>Permissions</em>}</li>
 * </ul>
 *
 * @see org.nasdanika.cdo.security.SecurityPackage#getProtected()
 * @model interface="true" abstract="true"
 * @extends CDOObject
 * @generated
 */
public interface Protected extends CDOObject {

	/**
	 * Returns the value of the '<em><b>Permissions</b></em>' containment reference list.
	 * The list contents are of type {@link org.nasdanika.cdo.security.ProtectedPermission}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Permissions on this object.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Permissions</em>' containment reference list.
	 * @see org.nasdanika.cdo.security.SecurityPackage#getProtected_Permissions()
	 * @model containment="true"
	 * @generated
	 */
	EList<ProtectedPermission> getPermissions();

} // Protected
