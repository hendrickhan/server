package org.nasdanika.cdo.web.routes.app;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.nasdanika.html.HTMLFactory.TokenSource;

/**
 * Uses {@link EObject} {@link EAttribute}'s as source of tokens.
 * 
 * For {@link CDOObject}'s tokens starting with ``xpath:`` the suffix after ``xpath:`` prefix is evaluated by [JXPath](http://commons.apache.org/proper/commons-jxpath/). 
 * @author Pavel Vlasov
 *
 */
public class EObjectTokenSource implements TokenSource {
	
	public static final String XPATH_PREFIX = "xpath:";
	
	private EObject source;
	private TokenSource[] chain;
	private Object context;
	
	public EObjectTokenSource(Object context, EObject source, TokenSource... chain) {
		this.context = context;
		this.source = source;
		this.chain = chain;
	}

	@Override
	public Object get(String token) {		
		EStructuralFeature tokenFeature = source.eClass().getEStructuralFeature(token);
		if (tokenFeature instanceof EAttribute) {
			Object tokenValue = source.eGet(tokenFeature);
			if (tokenValue != null) {
				return tokenValue;
			}
		}
		
		if (source instanceof CDOObject && token.startsWith(XPATH_PREFIX)) {
			Object ret = RenderUtil.newJXPathContext(context, (CDOObject) source).getValue(token.substring(XPATH_PREFIX.length()));
			if (ret != null) {
				return ret;
			}
		}
		
		for (TokenSource ch: chain) {
			Object tokenValue = ch.get(token);
			if (tokenValue != null) {
				return tokenValue;
			}
		}
		
		return null;
	}

}
