package org.nasdanika.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.Platform;
import org.nasdanika.core.AdapterManager;
import org.nasdanika.core.AuthorizationProvider.AccessDecision;
import org.nasdanika.core.CoreUtil;
import org.nasdanika.core.InstanceMethodCommand;
import org.nasdanika.core.NasdanikaException;
import org.nasdanika.html.FactoryProducer;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.Producer;
import org.nasdanika.html.impl.DefaultHTMLFactory;
import org.nasdanika.web.RouteDescriptor.RouteType;
import org.nasdanika.web.html.UIPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Helper class for resolving and caching extensions and references.
 * @author Pavel
 *
 */
public class ExtensionManager extends AdapterManager {
	
	private static final String BUNDLE_ID_TOKEN = "${bundleId}";
	private ServiceTracker<Route, Route> routeServiceTracker;
	private ServiceTracker<UIPart<?,?>, UIPart<?,?>> uiPartServiceTracker;	
	private HTMLFactory htmlFactory = HTMLFactory.INSTANCE;
	
	// TODO - Converter and ConverterProvider service tracker.
	
	private static class UIPartEntry implements Comparable<UIPartEntry> {
		UIPart<?,?> uiPart;
		int priority;
		String category;
		Class<?> target;
		
		@Override
		public int compareTo(UIPartEntry o) {
			return o.priority - priority;
		}
	}
	
	private List<UIPartEntry> uiPartEntries = new ArrayList<>();
	private AccessDecision defaultAccessDecision;
	private BundleContext bundleContext;
	
	public AccessDecision getDefaultAccessDecision() {
		return defaultAccessDecision;
	}
	
	@SuppressWarnings("unchecked")
	public ExtensionManager(
			Object target,
			BundleContext bundleContext, 
			String adapterServiceFilter,
			String routeServiceFilter,
			String uiPartServiceFilter,
			String htmlFactoryName,
			AccessDecision defaultAccessDecision,
			String contextPath) throws Exception {
		super(target, bundleContext, adapterServiceFilter);
		
		// TODO - converter profiles map: class name -> profile.
		if (bundleContext==null) {
			bundleContext = FrameworkUtil.getBundle(target.getClass()).getBundleContext();
		}
		if (bundleContext==null) {
			bundleContext = FrameworkUtil.getBundle(ExtensionManager.class).getBundleContext();
		}
		if (bundleContext==null) {
			throw new IllegalStateException("Bundle context is not available, make sure that bundle "+FrameworkUtil.getBundle(ExtensionManager.class).getSymbolicName()+" is activated");
		}
		this.bundleContext = bundleContext;
		// TODO - bundle is still null???
		if (routeServiceFilter==null || routeServiceFilter.trim().length()==0) {
			routeServiceTracker = new ServiceTracker<>(bundleContext, Route.class.getName(), null);
		} else {
			String rootRouteServiceFilter = "(&(" + Constants.OBJECTCLASS + "=" + Route.class.getName() + ")"+routeServiceFilter+")";
			routeServiceTracker = new ServiceTracker<>(bundleContext, bundleContext.createFilter(rootRouteServiceFilter), null);
		}
		routeServiceTracker.open();
		
		if (uiPartServiceFilter==null || uiPartServiceFilter.trim().length()==0) {
			uiPartServiceTracker = new ServiceTracker<>(bundleContext, UIPart.class.getName(), null);
		} else {
			String uiPartRouteServiceFilter = "(&(" + Constants.OBJECTCLASS + "=" + UIPart.class.getName() + ")"+uiPartServiceFilter+")";
			uiPartServiceTracker = new ServiceTracker<>(bundleContext, bundleContext.createFilter(uiPartRouteServiceFilter), null);
		}
		uiPartServiceTracker.open();
		
		for (IConfigurationElement ce: Platform.getExtensionRegistry().getConfigurationElementsFor(HTML_FACTORY_ID)) {
			if ("default_html_factory".equals(ce.getName())) {
				if (htmlFactoryName==null || htmlFactoryName.equals("default")) {
					DefaultHTMLFactory defaultHTMLFactory = new DefaultHTMLFactory();
					String bootstrapCssContainer = ce.getAttribute("bootstrapCssContainer");
					if (!"/".equals(contextPath)) {
						bootstrapCssContainer = contextPath+bootstrapCssContainer;
					}
					defaultHTMLFactory.setBootstrapCssContainer(bootstrapCssContainer);
					for (IConfigurationElement s: ce.getChildren("script")) {
						String scriptPath = s.getValue();
						if (!"/".equals(contextPath)) {
							scriptPath = contextPath+scriptPath;
						}
						defaultHTMLFactory.getScripts().add(scriptPath);
					}
					for (IConfigurationElement s: ce.getChildren("stylesheet")) {
						String stylesheetPath = s.getValue();
						if (!"/".equals(contextPath)) {
							stylesheetPath = contextPath+stylesheetPath;
						}
						defaultHTMLFactory.getStylesheets().add(stylesheetPath);
					}
					defaultHTMLFactory.setProducerAdapter(new Producer.Adapter() {
						
						@Override
						public Producer asProducer(Object obj) {							
							try {
								return adapt(Producer.class);
							} catch (Exception e) {
								throw new NasdanikaException(e);
							}
						}
						
					});
					defaultHTMLFactory.setFactoryProducerAdapter(new FactoryProducer.Adapter() {
						
						@Override
						public FactoryProducer asFactoryProducer(Object obj) {							
							try {
								return adapt(FactoryProducer.class);
							} catch (Exception e) {
								throw new NasdanikaException(e);
							}
						}
						
					});
					this.htmlFactory = defaultHTMLFactory;
					
					break;
				}
			} else if ("html_factory".equals(ce.getName())) {
				if (htmlFactoryName==null || htmlFactoryName.equals(ce.getAttribute("name"))) {
					this.htmlFactory = (HTMLFactory) ce.createExecutableExtension("class");
					CoreUtil.injectProperties(ce, htmlFactory);
					break;
				}
			}					
		}	
		
		for (IConfigurationElement ce: Platform.getExtensionRegistry().getConfigurationElementsFor(UI_PART_ID)) {
			if ("ui_part".equals(ce.getName())) {
				UIPartEntry uiPartEntry = new UIPartEntry();
				uiPartEntries.add(uiPartEntry);
				uiPartEntry.uiPart = (UIPart<?,?>) ce.createExecutableExtension("class");
				CoreUtil.injectProperties(ce, uiPartEntry.uiPart);
				String priorityStr = ce.getAttribute("priority");
				if (!CoreUtil.isBlank(priorityStr)) {
					uiPartEntry.priority = Integer.parseInt(priorityStr);
				}
				uiPartEntry.category = ce.getAttribute("category");
				
				IContributor contributor = ce.getContributor();		
				Bundle bundle = Platform.getBundle(contributor.getName());
				uiPartEntry.target = (Class<Object>) bundle.loadClass(ce.getAttribute("target").trim());
			}					
		}	
		
		objectPathResolver = new CompositeObjectPathResolver();
		for (IConfigurationElement ce: Platform.getExtensionRegistry().getConfigurationElementsFor(OBJECT_PATH_RESOLVER_ID)) {
			if ("resolver".equals(ce.getName())) {
				IContributor contributor = ce.getContributor();		
				Bundle bundle = Platform.getBundle(contributor.getName());
				objectPathResolver.addResolver(
						(Class<Object>) bundle.loadClass(ce.getAttribute("target").trim()),
						(ObjectPathResolver<Object>) ce.createExecutableExtension("class"));
			}					
		}	
		
		this.defaultAccessDecision = defaultAccessDecision;
	}
			
	public static final String UI_PART_ID = "org.nasdanika.web.ui_part";			
	public static final String HTML_FACTORY_ID = "org.nasdanika.web.html_factory";			
	public static final String ROUTE_ID = "org.nasdanika.web.route";			
	public static final String OBJECT_PATH_RESOLVER_ID = "org.nasdanika.web.object_path_resolver";			
		
	/**
	 * Expands ${bundleId} token.
	 * @param property
	 * @param bundle
	 * @return
	 */
	private static String expandTokens(String str, Bundle bundle) {
		if (str==null) {
			return str;
		}
		StringBuilder ret = new StringBuilder(str);
		String id = String.valueOf(bundle.getBundleId());
		for (int i=ret.indexOf(BUNDLE_ID_TOKEN); i!=-1; i=ret.indexOf(BUNDLE_ID_TOKEN, i+id.length())) {
			ret.replace(i, i+BUNDLE_ID_TOKEN.length(), id);
		}
		return ret.toString();
	}
	
	public RouteRegistry getRouteRegistry() {
		return routeRegistry;
	}
	
	protected class MethodRoute extends InstanceMethodCommand<HttpServletRequestContext, Action> implements Route {
		
		private boolean isVoid;
		private String action;
		String qualifier;

		protected MethodRoute(Object target, Method routeMethod) throws Exception {
			super(target, new WebMethodCommand<HttpServletRequestContext, Action>(bundleContext, routeMethod));
			RouteMethod rma = routeMethod.getAnnotation(RouteMethod.class);
			this.action = rma.action();
			this.qualifier = rma.qualifier().length()==0 ? routeMethod.getName() : rma.qualifier();
			this.isVoid = void.class.equals(routeMethod.getReturnType());
		}
		
		@Override
		public Action execute(HttpServletRequestContext context, Object... args) throws Exception {
			// Action from request method?
			if (context.authorize(target, action.length()==0 ? context.getMethod().name() : action, qualifier, null)) {
				Object result = super.execute(context);
				if (result==null && isVoid) {
					return Action.NOP;				
				}
				return ValueAction.wrap(result);
			}
			
			return Action.FORBIDDEN;
		}
		
	}
	
	private RouteRegistry routeRegistry = new RouteRegistry() {
		
		@Override
		public List<Route> matchObjectRoutes(RequestMethod method, Object target, String[] path) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteType.OBJECT, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(target, path)) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				if ("object".equals(se.getKey().getProperty("type"))) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					RouteEntry re = new RouteEntry(
							RouteDescriptor.RouteType.OBJECT, 
							methods, 
							expandTokens((String) se.getKey().getProperty("path"), se.getKey().getBundle()), 
							expandTokens((String) se.getKey().getProperty("pattern"), se.getKey().getBundle()), 
							se.getKey().getBundle().loadClass((String) se.getKey().getProperty("targetType")), 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue());
					if (re.match(target, path)) {
						collector.add(re);
					}
				}
			}
			
			if (target!=null) {
				for (final Field routeField: target.getClass().getFields()) {
					RouteField rfAnnotation = routeField.getAnnotation(RouteField.class);
					if (rfAnnotation!=null) {
						RouteEntry re = new RouteEntry(
								RouteType.OBJECT, 
								rfAnnotation.value(), 
								rfAnnotation.path(),
								rfAnnotation.pattern(), 
								target.getClass(), 
								rfAnnotation.priority(), 
								(Route) routeField.get(target)) {
							
							protected boolean match(Object obj, String[] path) {
								if (CoreUtil.isBlank(this.path) && getPattern()==null) {
									return path.length>1 && routeField.getName().equals(path[1]);
								}
								return super.match(obj, path);
							};
						};
						if (re.match(target, path)) {
							collector.add(re);
						}
					}
				}
				
				for (final Method routeMethod: target.getClass().getMethods()) {
					RouteMethod amAnnotation = routeMethod.getAnnotation(RouteMethod.class);
					if (amAnnotation!=null) {
						RouteEntry re = new RouteEntry(
								RouteType.OBJECT, 
								amAnnotation.value(), 
								amAnnotation.path(),
								amAnnotation.pattern(), 
								target.getClass(), 
								amAnnotation.priority(), 
								new MethodRoute(target, routeMethod)) {
							
							protected boolean match(Object obj, String[] path) {
								if (getPattern()==null) {
									return path.length>1 && routeMethod.getName().equals(path[1]);
								}
								return super.match(obj, path);
							};
						};
						if (re.match(target, path)) {
							collector.add(re);
						}
					}
				}
			}

			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re:collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret;
		}

		@Override
		public List<Route> matchRootRoutes(RequestMethod method, String[] path) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteDescriptor.RouteType.ROOT, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(null, path)) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				Object routeType = se.getKey().getProperty("type");
				if (routeType==null || "root".equals(routeType)) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					RouteEntry re = new RouteEntry(
							RouteDescriptor.RouteType.ROOT, 
							methods, 
							expandTokens((String) se.getKey().getProperty("path"), se.getKey().getBundle()), 
							expandTokens((String) se.getKey().getProperty("pattern"), se.getKey().getBundle()), 
							null, 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue());
					if (re.match(null, path)) {
						collector.add(re);
					}
				}
			}
			
			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re: collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret;
		}

		@Override
		public Route getExtensionRoute(RequestMethod method, Object target, String extension) throws Exception {
			List<RouteEntry> collector = new ArrayList<RouteEntry>();
			List<RouteEntry> methodActions = getRoutes(RouteDescriptor.RouteType.EXTENSION, method);
			if (methodActions!=null) {
				for (RouteEntry ma: methodActions) {
					if (ma.match(target, new String[] {extension})) {
						collector.add(ma);
					}
				}
			}
			
			// Service routes			
			for (Entry<ServiceReference<Route>, Route> se: routeServiceTracker.getTracked().entrySet()) {	
				if ("extension".equals(se.getKey().getProperty("type"))) {
					Object methodsProperty = se.getKey().getProperty("methods");					
					RequestMethod[] methods;
					if (methodsProperty==null || (methodsProperty instanceof String && "*".equals(((String) methodsProperty).trim()))) {
						methods = RequestMethod.values(); 
					} else if (methodsProperty instanceof String) {
						methods = new RequestMethod[] { RequestMethod.valueOf((String) methodsProperty) };
					} else if (methodsProperty instanceof String[]) {
						String[] msa = (String[]) methodsProperty; 
						methods = new RequestMethod[msa.length];
						for (int i=0; i<msa.length; ++i) {
							methods[i] = RequestMethod.valueOf(msa[i]);
						}
					} else {
						throw new IllegalArgumentException("Unexpected methods property type: "+methodsProperty);
					}
					Object priorityProperty = se.getKey().getProperty("priority");
					collector.add(new RouteEntry(
							RouteDescriptor.RouteType.EXTENSION, 
							methods,
							null,
							(String) se.getKey().getProperty("extension"), 
							se.getKey().getBundle().loadClass((String) se.getKey().getProperty("targetType")), 
							priorityProperty instanceof Integer ? ((Integer) priorityProperty).intValue() : 0, 
							se.getValue()));
				}
			}

			Collections.sort(collector);
			List<Route> ret = new ArrayList<>();
			Z: for (RouteEntry re:collector) {
				for (RequestMethod rm: re.getMethods()) {
					if (rm.equals(method)) {
						ret.add(re.getRoute());
						continue Z;
					}
				}
			}
			return ret.isEmpty() ? null : ret.get(0);
		}

	};
			
	protected class RouteEntry implements Comparable<RouteEntry> {
		
		private Pattern pattern;
		private RouteDescriptor.RouteType type;
		private Class<?> targetType;
		private int priority;
		private Route route;
		private RequestMethod[] methods;
		protected String path;	

		public RouteEntry(
				RouteDescriptor.RouteType type,
				RequestMethod[] methods,
				String path,
				String patternStr, 
				Class<?> targetType, 
				int priority, 
				final Route route) {
			
			this.type = type;
			this.methods = methods;
			this.path = path;
			if (!CoreUtil.isBlank(patternStr)) {
				pattern = Pattern.compile(patternStr);
			}
			this.targetType = targetType;
			this.priority = priority;
			if (CoreUtil.isBlank(path)) {
				this.route = route;
			} else {
				final int offset = path.split("/").length;
				// Wrap route to shift context to path
				this.route = new Route() {
					
					@Override
					public void close() throws Exception {
						route.close();						
					}
					
					@Override
					public Action execute(HttpServletRequestContext context, Object... args) throws Exception {
						return route.execute(context.shift(offset), args);
					}
					
					@Override
					public boolean canExecute() {
						return route.canExecute();
					}
				};
			}
		}
		
		protected boolean match(Object obj, String[] path) {
			if (targetType!=null && !targetType.isInstance(obj)) {
				return false;
			}
			if (!CoreUtil.isBlank(this.path)) {
				String jrp = CoreUtil.join(path, "/");
				return jrp.equals(this.path) || (this.path.endsWith("/") && jrp.startsWith(this.path)); 
			}
			return pattern==null ? true : pattern.matcher(CoreUtil.join(path, "/")).matches();			
		}

		protected Pattern getPattern() {
			return pattern;
		}

		public RouteDescriptor.RouteType getType() {
			return type;
		}
		
		public RequestMethod[] getMethods() {
			return methods;
		}

		protected Class<?> getTargetType() {
			return targetType;
		}

		protected int getPriority() {
			return priority;
		}

		protected Route getRoute() {
			return route;
		}

		@Override
		public int compareTo(RouteEntry o) {
			if (targetType==null) {
				if (o.targetType!=null) {
					return 1; // o is more specific.
				}
			} else {
				if (o.targetType==null) {
					return -1; // this entry is more specific.
				}
				
				if (targetType.isAssignableFrom(o.getTargetType())) {
					if (!o.getTargetType().isAssignableFrom(targetType)) {
						return 1; // o is more specific.
					}
				} else if (o.getTargetType().isAssignableFrom(targetType)) {
					return -1; // this entry is more specific.
				}
			}
			
			return o.getPriority()-getPriority();
		}
		
	}	
	
	private Map<RouteType, Map<RequestMethod, List<RouteEntry>>> routeMap;

	/**
	 * Registered actions
	 * @param method
	 * @return
	 * @throws Exception
	 */
	protected synchronized List<RouteEntry> getRoutes(RouteType routeType, RequestMethod method) throws Exception {
		if (routeMap == null) {
			routeMap = new HashMap<>();
			for (RouteType rt: RouteType.values()) {
				routeMap.put(rt, new HashMap<RequestMethod, List<RouteEntry>>());
			}

			for (IConfigurationElement ce: Platform.getExtensionRegistry().getConfigurationElementsFor(ExtensionManager.ROUTE_ID)) {
				if ("object-route".equals(ce.getName())) {					
					Route route = (Route) ce.createExecutableExtension("class");		
					CoreUtil.injectProperties(ce, route);
					String priorityStr = ce.getAttribute("priority");
					int priority = CoreUtil.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String targetClassName = ce.getAttribute("target");
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());
					Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());
					String methodStr = ce.getAttribute("method");					
					RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
					RouteEntry routeEntry = new RouteEntry(
							RouteType.OBJECT, 
							routeMethods, 
							expandTokens(ce.getAttribute("path"), bundle),
							expandTokens(ce.getAttribute("pattern"), bundle),
							targetType, 
							priority, 
							route);
										
					for (RequestMethod routeMethod: routeMethods) {
						List<RouteEntry> methodRoutes = routeMap.get(RouteType.OBJECT).get(routeMethod);
						if (methodRoutes == null) {
							methodRoutes = new ArrayList<>();
							routeMap.get(RouteType.OBJECT).put(routeMethod, methodRoutes);
						}
						methodRoutes.add(routeEntry);
					}
				} else if ("extension-route".equals(ce.getName())) {					
						Route route = (Route) ce.createExecutableExtension("class");		
						CoreUtil.injectProperties(ce, route);
						String priorityStr = ce.getAttribute("priority");
						int priority = CoreUtil.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
						String targetClassName = ce.getAttribute("target");
						IContributor contributor = ce.getContributor();		
						Bundle bundle = Platform.getBundle(contributor.getName());
						Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());
						String methodStr = ce.getAttribute("method");					
						RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
						RouteEntry routeEntry = new RouteEntry(
								RouteType.EXTENSION, 
								routeMethods,
								null,
								ce.getAttribute("extension"), 
								targetType, 
								priority, 
								route);
											
						for (RequestMethod routeMethod: routeMethods) {
							List<RouteEntry> methodRoutes = routeMap.get(RouteType.EXTENSION).get(routeMethod);
							if (methodRoutes == null) {
								methodRoutes = new ArrayList<>();
								routeMap.get(RouteType.EXTENSION).put(routeMethod, methodRoutes);
							}
							methodRoutes.add(routeEntry);
						}
				} else if ("object-resource-route".equals(ce.getName())) {					
					String priorityStr = ce.getAttribute("priority");
					int priority = CoreUtil.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String targetClassName = ce.getAttribute("target");					
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());
					Class<?> targetType = (Class<?>) bundle.loadClass(targetClassName.trim());

					final String rName = ce.getAttribute("resource");			
					final URL baseURL = bundle.getEntry(rName);
					
					final String contentType = ce.getAttribute("content-type");					
					
					Route route = new Route() {
						
						@Override
						public Action execute(final HttpServletRequestContext context, Object... args) throws Exception {
							if (context.getPath().length==1) { // 0?
								return new Action() {
									
									@Override
									public Object execute() throws Exception {
										return baseURL;
									}

									@Override
									public void close() throws Exception {
										// NOP			
									}
								};
							}
							final String subPath = CoreUtil.join(Arrays.copyOfRange(context.getPath(), 1, context.getPath().length), "/");
							return new Action() {
								
								@Override
								public Object execute() throws Exception {
									if (!CoreUtil.isBlank(contentType) && context instanceof HttpServletRequestContext) {
										HttpServletResponse resp = ((HttpServletRequestContext) context).getResponse();
										if (CoreUtil.isBlank(resp.getContentType())) {
											resp.setContentType(contentType);
										}
									}
									return new URL(baseURL, subPath);
								}

								@Override
								public void close() throws Exception {
									// NOP			
								}
								
							};
						}

						@Override
						public boolean canExecute() {
							return true;
						}

						@Override
						public void close() throws Exception {
							// NOP							
						}
						
					};
					
					RouteEntry routeEntry = new RouteEntry(
							RouteType.OBJECT, 
							new RequestMethod[] {RequestMethod.GET}, 
							expandTokens(ce.getAttribute("path"), bundle), 
							expandTokens(ce.getAttribute("pattern"), bundle), 
							targetType, 
							priority, 
							route);
					
					List<RouteEntry> methodRoutes = routeMap.get(RouteType.OBJECT).get(RequestMethod.GET);
					if (methodRoutes == null) {
						methodRoutes = new ArrayList<>();
						routeMap.get(RouteType.OBJECT).put(RequestMethod.GET, methodRoutes);
					}
					methodRoutes.add(routeEntry);
				} else if ("root-route".equals(ce.getName())) {					
					Route route = (Route) ce.createExecutableExtension("class");			
					CoreUtil.injectProperties(ce, route);
					String priorityStr = ce.getAttribute("priority");
					int priority = CoreUtil.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					String methodStr = ce.getAttribute("method");					
					RequestMethod[] routeMethods = "*".equals(methodStr) ? RequestMethod.values() : new RequestMethod[] {RequestMethod.valueOf(methodStr)};
					RouteEntry routeEntry = new RouteEntry(
							RouteType.ROOT, 
							routeMethods, 
							expandTokens(ce.getAttribute("path"), Platform.getBundle(ce.getContributor().getName())), 
							expandTokens(ce.getAttribute("pattern"), Platform.getBundle(ce.getContributor().getName())), 
							null, 
							priority, 
							route);
					
					for (RequestMethod routeMethod: routeMethods) {
						List<RouteEntry> methodRoutes = routeMap.get(RouteType.ROOT).get(routeMethod);
						if (methodRoutes == null) {
							methodRoutes = new ArrayList<>();
							routeMap.get(RouteType.ROOT).put(routeMethod, methodRoutes);
						}
						methodRoutes.add(routeEntry);
					}
				} else if ("root-resource-route".equals(ce.getName())) {					
					String priorityStr = ce.getAttribute("priority");
					int priority = CoreUtil.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
					IContributor contributor = ce.getContributor();		
					Bundle bundle = Platform.getBundle(contributor.getName());

					final String rName = ce.getAttribute("resource");			
					final URL baseURL = bundle.getEntry(rName);
					final String contentType = ce.getAttribute("content-type");					
					
					final Route route = new Route() {
						
						@Override
						public Action execute(final HttpServletRequestContext context, Object... args) throws Exception {
							if (context.getPath().length==1) { // 0?
								return new Action() {
									
									@Override
									public Object execute() throws Exception {
										return baseURL;
									}

									@Override
									public void close() throws Exception {
										// NOP			
									}
								};
							}
							final String subPath = CoreUtil.join(Arrays.copyOfRange(context.getPath(), 1, context.getPath().length), "/");
							return new Action() {
								
								@Override
								public Object execute() throws Exception {
									if (!CoreUtil.isBlank(contentType) && context instanceof HttpServletRequestContext) {
										HttpServletResponse resp = ((HttpServletRequestContext) context).getResponse();
										if (CoreUtil.isBlank(resp.getContentType())) {
											resp.setContentType(contentType);
										}
									}
									return new URL(baseURL, subPath);
								}

								@Override
								public void close() throws Exception {
									// NOP			
								}
								
							};
						}

						@Override
						public boolean canExecute() {
							return true;
						}

						@Override
						public void close() throws Exception {
							// NOP							
						}
						
					};
					
					RouteEntry routeEntry = new RouteEntry(
							RouteType.ROOT, 
							new RequestMethod[] { RequestMethod.GET } , 
							expandTokens(ce.getAttribute("path"), Platform.getBundle(ce.getContributor().getName())), 
							expandTokens(ce.getAttribute("pattern"), Platform.getBundle(ce.getContributor().getName())), 
							null, 
							priority, 
							route);
					
					List<RouteEntry> methodRoutes = routeMap.get(RouteType.ROOT).get(RequestMethod.GET);
					if (methodRoutes == null) {
						methodRoutes = new ArrayList<>();
						routeMap.get(RouteType.ROOT).put(RequestMethod.GET, methodRoutes);
					}
					methodRoutes.add(routeEntry);
				} else if ("route-provider".equals(ce.getName())) {
					RouteProvider routeProvider = (RouteProvider) ce.createExecutableExtension("class");
					CoreUtil.injectProperties(ce, routeProvider);
					for (final RouteDescriptor routeDescriptor: routeProvider.getRouteDescriptors()) {
						RouteEntry routeEntry = new RouteEntry(
								routeDescriptor.getType(), 
								routeDescriptor.getMethods(),
								routeDescriptor.getPath(),
								routeDescriptor.getPattern(), 
								routeDescriptor.getTarget(), 
								routeDescriptor.getPriority(), 
								routeDescriptor.getRoute());
																
						for (RequestMethod routeMethod: routeDescriptor.getMethods()) {
							List<RouteEntry> methodRoutes = routeMap.get(routeDescriptor.getType()).get(routeMethod);
							if (methodRoutes == null) {
								methodRoutes = new ArrayList<>();
								routeMap.get(routeDescriptor.getType()).put(routeMethod, methodRoutes);
							}
							methodRoutes.add(routeEntry);
						}
						
					}
					
				}
			}

			for (Map<RequestMethod, List<RouteEntry>> rm: routeMap.values()) {
				for (List<RouteEntry> ame: rm.values()) {
					Collections.sort(ame);
				}
			}
		}
		
		List<RouteEntry> ret = routeMap.get(routeType).get(method);
		return ret == null ? Collections.<RouteEntry>emptyList() : ret;
	}
	@Override
	public void close() throws Exception {
		try {
			routeServiceTracker.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			uiPartServiceTracker.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Closing routes.
		if (routeMap!=null) {
			for (Map<RequestMethod, List<RouteEntry>> rm: routeMap.values()) {
				for (List<RouteEntry> rl: rm.values()) {
					for (RouteEntry r: rl) {
						if (r instanceof AutoCloseable) {
							try {
								((AutoCloseable) r).close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public HTMLFactory getHTMLFactory() {
		return htmlFactory;
	}	
	
	private CompositeObjectPathResolver objectPathResolver;
	
	public CompositeObjectPathResolver getObjectPathResolver() throws CloneNotSupportedException {
		return objectPathResolver.clone();
	}
	
	public List<UIPart<?,?>> getUIParts(Object target, String category) throws Exception {
		List<UIPartEntry> collector = new ArrayList<>();
		for (UIPartEntry ext: uiPartEntries) {
			if (ext.target.isInstance(target) && ext.category.equals(category)) {
				collector.add(ext);
			}
		}
		// Service ui parts			
		for (Entry<ServiceReference<UIPart<?,?>>, UIPart<?,?>> se: uiPartServiceTracker.getTracked().entrySet()) {	
			if (category.equals(se.getKey().getProperty("category"))) {
				Class<?> targetClass = se.getKey().getBundle().loadClass((String) se.getKey().getProperty("target"));
				if (targetClass.isInstance(target)) {
					UIPartEntry uiPartEntry = new UIPartEntry();
					collector.add(uiPartEntry);
					uiPartEntry.uiPart = se.getValue();
					Object priority = se.getKey().getProperty("priority");
					if (priority instanceof Number) {
						uiPartEntry.priority = ((Number) priority).intValue();
					}
				}
			}
		}
		
		Collections.sort(collector);
		
		List<UIPart<?,?>> ret = new ArrayList<>();
		for (UIPartEntry e: collector) {
			ret.add(e.uiPart);
		}
		
		return ret;
	}
	
//	/**
//	 * Traverses inheritance hierarchy and finds out whether obj is instance of className without loading
//	 * the class.
//	 * @param obj
//	 * @param className
//	 * @return
//	 */
//	public static boolean isInstance(Object obj, String className) {
//		if (obj==null) {
//			return false;
//		}
//		
//		if (isBlank(className)) {
//			return true;
//		}
//		
//		return isAssignableFrom(obj.getClass(), className);
//	}
//		
//	private static boolean isAssignableFrom(Class<?> subClass, String superClassName) {
//		if (subClass.getName().equals(superClassName)) {
//			return true;
//		}
//		
//		if (isAssignableFrom(subClass.getSuperclass(), superClassName)) {
//			return true;
//		}
//		
//		for (Class<?> i: subClass.getInterfaces()) {
//			if (isAssignableFrom(i, superClassName)) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
	
}
