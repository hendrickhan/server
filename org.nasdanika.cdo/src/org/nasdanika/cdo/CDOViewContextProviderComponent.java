package org.nasdanika.cdo;

import org.eclipse.emf.cdo.session.CDOSessionProvider;
import org.eclipse.emf.cdo.view.CDOView;
import org.nasdanika.cdo.security.ProtectionDomain;
import org.nasdanika.cdo.security.SecurityPolicyManager;
import org.nasdanika.core.NasdanikaException;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

public abstract class CDOViewContextProviderComponent<CR> implements CDOViewContextProvider<CR, CDOViewContext<CDOView, CR>> {
	
	private CDOSessionProvider sessionProvider;
	private SecurityPolicyManager securityPolicyManager;
	private Bundle bundle;
	private boolean deny;
	
	public void activate(ComponentContext componentContext) throws Exception {
		securityPolicyManager = new SecurityPolicyManager(
				componentContext.getBundleContext(), 
				(String) componentContext.getProperties().get("security-policy-filter"));
		this.bundle = componentContext.getBundleContext().getBundle();
		deny = "deny".equalsIgnoreCase((String) componentContext.getProperties().get("default-access-decision"));
	}
	
	public void deactivate() throws Exception {
		if (securityPolicyManager!=null) {
			securityPolicyManager.close();
		}
	}
	
	public void setSessionProvider(CDOSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	
	public void clearSessionProvider(CDOSessionProvider sessionProvider) {
		this.sessionProvider = null;
	}	

	@Override
	public <MC> CDOViewContext<CDOView, CR> createContext(MC masterContext) {
		if (sessionProvider!=null) {			
			try {
				return new CDOViewContextImpl<CDOView, CR, MC>(
						bundle, 
						securityPolicyManager,
						masterContext,
						deny) {
					
					@Override
					public ProtectionDomain<CR> getProtectionDomain() {
						return CDOViewContextProviderComponent.this.getProtectionDomain(getView());
					}

					@Override
					protected CDOView openView() {
						return sessionProvider.getSession().openView();
					}

					
				};
			} catch (Exception e) {
				throw new NasdanikaException("Cannot create CDO View context", e);
			}
		}
		return null;
	}
	
	protected abstract ProtectionDomain<CR> getProtectionDomain(CDOView view);

}
