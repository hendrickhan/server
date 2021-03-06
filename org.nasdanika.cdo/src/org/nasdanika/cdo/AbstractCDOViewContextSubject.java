package org.nasdanika.cdo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.transaction.CDOCommitContext;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.transaction.CDOTransactionHandler2;
import org.eclipse.emf.cdo.util.ObjectNotFoundException;
import org.eclipse.emf.cdo.view.CDOView;
import org.nasdanika.cdo.security.LoginPasswordHashUser;
import org.nasdanika.cdo.security.LoginPasswordRealm;
import org.nasdanika.cdo.security.LoginUser;
import org.nasdanika.cdo.security.Principal;
import org.nasdanika.cdo.security.User;

public abstract class AbstractCDOViewContextSubject<V extends CDOView, CR> implements CDOViewContextSubject<V, CR> {
	
	protected abstract void setPrincipalIDs(List<CDOID> cdoID);

	/**
	 * List of principal ID's. Empty list if no principals.
	 * @return
	 */
	protected abstract List<CDOID> getPrincipalIDs();
	
	/**
	 * List of principal names. Empty list if no principals. 
	 * @return
	 */
	protected abstract List<String> getPrincipalNames();
	
	@Override
	public List<Principal> getPrincipals(CDOViewContext<V, CR> context) {
		List<Principal> principals = new ArrayList<>();
		List<CDOID> principalIDs = getPrincipalIDs();
		if (!principalIDs.isEmpty()) {
			for (CDOID principalID: principalIDs) {
				try {
					principals.add((Principal) context.getView().getObject(principalID));
				} catch (ObjectNotFoundException e) {
					System.err.println("Invalid principal ID: "+principalID);
				}
			}
			if (!principals.isEmpty()) {
				return principals;
			}
		}
		
		if (context.getSecurityRealm()!=null) {
			List<String> principalNames = getPrincipalNames();
			principalIDs = new ArrayList<>();
			if (principalNames.isEmpty()) {
				Principal guest = context.getSecurityRealm().getGuest();
				if (guest!=null) {
					principals.add(guest);
				}
			} else {
				for (String principalName: principalNames) {
					if (context.getSecurityRealm() instanceof LoginPasswordRealm) {
						LoginPasswordHashUser user = ((LoginPasswordRealm) context.getSecurityRealm()).getUser(principalName);
						if (user!= null && !user.isDisabled()) {
							principals.add(user);
						}
					} else {
						for (User<?> pdu : context.getSecurityRealm().getAllUsers()) { 
							// TODO - find(login) to optimize search in large user populations
							if (pdu instanceof LoginUser && ((LoginUser<?>) pdu).getLogin().equalsIgnoreCase(principalName)) {
								// Not clear why this check was here - (pdu instanceof LoginPasswordHashUser && ((LoginPasswordHashUser) pdu).getPasswordHash() != null)
								if (!((LoginUser<?>) pdu).isDisabled()) { 
									principals.add(pdu);
								}
								break;
							}
						}
					}
				}
			}
			setPrincipals(context, principals);
		}
		return principals;		
	}						

	@Override
	public void setPrincipals(CDOViewContext<V, CR> context, List<Principal> principals) {
		List<CDOID> principalIDs = new ArrayList<>();
		for (Principal principal: principals) {
			V view = context.getView();
			if (view instanceof CDOTransaction && principal.cdoID().isTemporary()) {
				((CDOTransaction) view).addTransactionHandler(new CDOTransactionHandler2() {

					@Override
					public void committedTransaction(CDOTransaction transaction, CDOCommitContext commitContext) {
						for (Principal principal: principals) {
							getPrincipalIDs().add(principal.cdoID());
						}		
					}

					@Override
					public void committingTransaction(CDOTransaction transaction, CDOCommitContext commitContext) {
						// NOP
						
					}

					@Override
					public void rolledBackTransaction(CDOTransaction transaction) {
						// NOP
						
					}
					
				});
			} else {
				principalIDs.add(principal.cdoID());
			}
		}		
		setPrincipalIDs(principalIDs);
	}
	
}

