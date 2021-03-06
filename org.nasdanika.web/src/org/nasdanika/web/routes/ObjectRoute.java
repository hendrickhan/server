package org.nasdanika.web.routes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nasdanika.core.CoreUtil;
import org.nasdanika.web.Action;
import org.nasdanika.web.HttpServletRequestContext;
import org.nasdanika.web.RequestMethod;
import org.nasdanika.web.Route;
import org.nasdanika.web.RouteMethod;
import org.nasdanika.web.WebSocketUpgradeInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Routes to object's methods, fields, and resources.
 * @author Pavel Vlasov
 *
 */
public class ObjectRoute implements Route {

	/**
	 * EObject route. Has feature, operation, resource, and code sub-routes.
	 */
	@Override
	public Action execute(final HttpServletRequestContext context, Object... arguments) throws Exception {
		final Object target = context.getTarget();

		if (target.getClass().isArray() && context.getPath().length>1) {
			return context.getAction(Array.get(target, Integer.parseInt(context.getPath()[1])), 1, null);
		}
		
		if (context.getPath().length==1) { 
			if (RequestMethod.GET.equals(context.getMethod())) {
				int dotIdx = context.getPath()[0].lastIndexOf(".");
				String extension = dotIdx==-1 ? "json" : context.getPath()[0].substring(dotIdx+1); // json is "default" extension
				if (context.authorize(target, "extension", extension, null)) {
					Action extensionAction = context.getExtensionAction(target, extension);
					if (extensionAction==null) {
						return new Action() {
							
							@Override
							public void close() throws Exception {
								// NOP								
							}
							
							@Override
							public Object execute() throws Exception {
								return target;
							}
						};
					}
					return extensionAction;
				} 
				return Action.FORBIDDEN;
			}
			
			return Action.NOT_FOUND;
		} 
		
		if (context.getPath().length==2 && ("self".equals(context.getPath()[1]) || context.getPath()[1].startsWith("self.")) ) {
			return context.getAction(target, 1, null);
		}				
		
		if (context.getPath().length>2) {
			switch (context.getPath()[1]) {			
			case "field":
				String fieldName = context.getPath()[2];
				int idx = fieldName.lastIndexOf('.');
				if (idx!=-1) {
					fieldName = fieldName.substring(0, idx);
				}
		
				try {
					Field field = target.getClass().getField(fieldName);
					// TODO - handle post - set field - if path length is 3.
					Object val = field.get(target);
					return val==null ? Action.NOT_FOUND : context.getAction(val, 2, null);
				} catch (NoSuchFieldError nsfe) {
					return Action.NOT_FOUND;
				}
			case "method":
				String methodName = context.getPath()[2];
				for (Method method: target.getClass().getMethods()) {
					// Route methods are matched by Extension Manager
					if (method.getAnnotation(RouteMethod.class)==null) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (methodName.equals(method.getName()) 
								&& parameterTypes.length<=context.getPath().length-3
								&& context.authorize(target, "invoke", methodName, Collections.singletonMap("method", (Object) method))) {
	
							Object[] args = new Object[parameterTypes.length];
							for (int pIdx=0; pIdx<parameterTypes.length; ++pIdx) {
								if (parameterTypes[pIdx].isAssignableFrom(String.class)) {
									args[pIdx] = context.getPath()[pIdx+3];
								} else {								
									Class<?> pType = CoreUtil.PRIMITIVES_TO_BOXES_MAP.get(parameterTypes[pIdx]);
									if (pType==null) {
										pType = parameterTypes[pIdx];
									}
									args[pIdx] = context.convert(context.getPath()[pIdx+3], pType);
								}
							}
							final Object result = method.invoke(target, args);
							if (result==null) {
								return void.class.equals(method.getReturnType()) ? Action.NOP : Action.NOT_FOUND;
							}
							return context.getAction(result, parameterTypes.length+2, null);
						}
					}
				}
				return Action.NOT_FOUND;
			case "resource":				
				String resourceName = StringUtils.join(context.getPath(), "/", 2, context.getPath().length); 
				if (RequestMethod.GET.equals(context.getMethod())) {
					if (context.authorize(target, "resource", resourceName, null)) {
						// TODO - MIME types.
						ResourceInfo resourceInfo = findResource(target.getClass(), resourceName);
						if (resourceInfo==null) {
							return Action.NOT_FOUND;
						}						
						
						HttpServletRequestContext httpContext = (HttpServletRequestContext) context;
						if (resourceInfo.lastModified!=-1) {
							httpContext.getResponse().setDateHeader("Last-Modified", resourceInfo.lastModified);
						}
						
						InputStream resourceStream = new BufferedInputStream(resourceInfo.inputStream);
						
						try (OutputStream out = new BufferedOutputStream(httpContext.getResponse().getOutputStream())) {
							for (int b = resourceStream.read(); b!=-1; b = resourceStream.read()) {
								out.write(b);
							}
						} finally {
							resourceStream.close();
						}
						
						return Action.NOP; 
					} 
					return Action.FORBIDDEN;
				}				
				return Action.BAD_REQUEST;
			case "code":				
				String codeName = StringUtils.join(context.getPath(), "/", 2, context.getPath().length); 
				if (RequestMethod.GET.equals(context.getMethod())) {
					if (context.authorize(target, "code", codeName, null)) {
						InputStream resourceStream = target.getClass().getClassLoader().getResourceAsStream(codeName);
						if (resourceStream==null) {
							return Action.NOT_FOUND;
						}
						resourceStream = new BufferedInputStream(resourceStream);
						
						try (OutputStream out = new BufferedOutputStream(((HttpServletRequestContext) context).getResponse().getOutputStream())) {
							for (int b = resourceStream.read(); b!=-1; b = resourceStream.read()) {
								out.write(b);
							}
						} finally {
							resourceStream.close();
						}
						
						return Action.NOP; 
					} 
				}				
				return Action.NOT_FOUND;
			}			
		}
		return Action.NOT_FOUND;
	}
	
	private class ResourceInfo {
		ResourceInfo(InputStream is) {
			this.inputStream = is;
		}
		InputStream inputStream;
		long lastModified = -1;
		
	}

	private ResourceInfo findResource(Class<?> clazz, String resourceName) {
		if (clazz==null || Object.class.equals(clazz)) {
			return null;
		}
		
		String baseName = clazz.getName();
		int idx = baseName.lastIndexOf('.');
		baseName = baseName.substring(0, idx+1).replace('.',  '/');
		InputStream is = clazz.getClassLoader().getResourceAsStream(baseName+resourceName);
		if (is!=null) {
			ResourceInfo ret = new ResourceInfo(is);
			Bundle bundle = FrameworkUtil.getBundle(clazz);
			if (bundle!=null) {
				ret.lastModified = bundle.getLastModified();
			}
			return ret;
		}
		ResourceInfo ret = findResource(clazz.getSuperclass(), resourceName);
		if (ret!=null) {
			return ret;
		}
		for (Class<?> i: clazz.getInterfaces()) {
			ret = findResource(i, resourceName);
			if (ret!=null) {
				return ret;
			}			
		}
		return null;
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public void close() throws Exception {
		// NOP
		
	}
	
	public static boolean matchConsumes(HttpServletRequestContext context, String[] consumes) throws Exception {
		
		if (RequestMethod.CREATE_WEB_SOCKET == context.getMethod()) {
			WebSocketUpgradeInfo upgradeInfo = context.adapt(WebSocketUpgradeInfo.class);
			List<String> subProtocols = upgradeInfo.getUpgradeRequest().getSubProtocols();
			if (subProtocols.isEmpty()) {
				return true;
			}
			for (String sp: subProtocols) {
				if (consumes == null || consumes.length == 0) {
					// Accept the first sub-protocol
					upgradeInfo.getUpgradeResponse().setAcceptedSubProtocol(sp);
					return true;
				}
				for (String ce: consumes) {
					if (ce.equals(sp)) {
						upgradeInfo.getUpgradeResponse().setAcceptedSubProtocol(sp);
						return true;
					}
				}
			}
		}
		
		if (consumes==null || consumes.length==0) {
			return true;
		}		
		
		String contentType = context.getRequest().getContentType();
		if (CoreUtil.isBlank(contentType)) {
			return false;
		}
		int idx = contentType.indexOf(";");
		if (idx != -1) {
			contentType = contentType.substring(0, idx).trim();
		}
		for (String consumesEntry: consumes) {
			String contentTypeLowerCase = contentType.trim().toLowerCase();
			String ceLowerCase = consumesEntry.trim().toLowerCase();
			if (ceLowerCase.equals("*/*") || ceLowerCase.equals(contentTypeLowerCase)) {
				return true;
			}
			if (consumesEntry.endsWith("/*") && contentTypeLowerCase.startsWith(ceLowerCase.substring(0, ceLowerCase.length()-1))) {
				return true;				 
			}
		}					
		
		return false;
	}

	public static boolean matchProduces(final HttpServletRequestContext context, String produces) {
		if (CoreUtil.isBlank(produces)) {
			return true;
		}
		String accept = context.getRequest().getHeader("Accept");
		if (CoreUtil.isBlank(accept)) {
			return true;
		}
		String plc = produces.toLowerCase().trim();
		for (String acceptEntry: accept.split(",")) {
			String acceptEntryLowerCase = acceptEntry.trim().toLowerCase();
			int idx = acceptEntryLowerCase.indexOf(";");
			if (idx!=-1) {
				acceptEntryLowerCase = acceptEntryLowerCase.substring(0, idx).trim();
			}
			// Ignoring q and level for now or forever
			if (acceptEntryLowerCase.equals("*/*") || acceptEntryLowerCase.equals(plc)) {
				return true;
			}
			
			if (acceptEntryLowerCase.endsWith("/*") && plc.startsWith(acceptEntryLowerCase.substring(0, acceptEntryLowerCase.length()-1))) {
				return true;
			}
		}					
			
		return false;
	}
	

}
