package org.nasdanika.cdo.web.routes.app;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.nasdanika.cdo.CDOViewContext;
import org.nasdanika.cdo.EAttributeProtector;
import org.nasdanika.cdo.security.Action;
import org.nasdanika.cdo.security.Package;
import org.nasdanika.cdo.security.Principal;
import org.nasdanika.cdo.security.Protected;
import org.nasdanika.cdo.security.ProtectedPermission;
import org.nasdanika.cdo.security.Realm;
import org.nasdanika.cdo.security.SecurityPackage;
import org.nasdanika.cdo.validation.DelegatingDiagnostician;
import org.nasdanika.cdo.web.CDOIDCodec;
import org.nasdanika.cdo.web.routes.app.EOperationTargetInfo.Role;
import org.nasdanika.core.AuthorizationProvider.StandardAction;
import org.nasdanika.core.Context;
import org.nasdanika.core.CoreUtil;
import org.nasdanika.html.Bootstrap;
import org.nasdanika.html.Bootstrap.Color;
import org.nasdanika.html.Bootstrap.DeviceSize;
import org.nasdanika.html.Bootstrap.Glyphicon;
import org.nasdanika.html.Bootstrap.Style;
import org.nasdanika.html.Breadcrumbs;
import org.nasdanika.html.Button;
import org.nasdanika.html.Button.Type;
import org.nasdanika.html.Container;
import org.nasdanika.html.FieldContainer;
import org.nasdanika.html.FieldSet;
import org.nasdanika.html.FontAwesome;
import org.nasdanika.html.FontAwesome.Spinner;
import org.nasdanika.html.FontAwesome.WebApplication;
import org.nasdanika.html.Form;
import org.nasdanika.html.FormGroup;
import org.nasdanika.html.FormGroup.Status;
import org.nasdanika.html.FormInputGroup;
import org.nasdanika.html.Fragment;
import org.nasdanika.html.HTMLFactory;
import org.nasdanika.html.HTMLFactory.InputType;
import org.nasdanika.html.HTMLFactory.TokenSource;
import org.nasdanika.html.Input;
import org.nasdanika.html.InputBase;
import org.nasdanika.html.JsTreeContextMenuItem;
import org.nasdanika.html.JsTreeNode;
import org.nasdanika.html.LinkGroup;
import org.nasdanika.html.ListGroup;
import org.nasdanika.html.Modal;
import org.nasdanika.html.NamedItemsContainer;
import org.nasdanika.html.RowContainer.Row;
import org.nasdanika.html.RowContainer.Row.Cell;
import org.nasdanika.html.Select;
import org.nasdanika.html.Table;
import org.nasdanika.html.Tag;
import org.nasdanika.html.Tag.TagName;
import org.nasdanika.html.TextArea;
import org.nasdanika.html.UIElement;
import org.nasdanika.html.UIElement.Event;
import org.nasdanika.html.Well;
import org.nasdanika.web.HttpServletRequestContext;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.WikiLinkNode;
import org.yaml.snakeyaml.Yaml;

/**
 * Renders HTML elements for a target object such as form inputs, tables, e.t.c.
 * @author Pavel
 *
 * @param <T>
 */
public interface Renderer<C extends Context, T extends EObject> extends ResourceProvider<C> {
		
	public static final String PASSWORD_MASK = "********";
	String CONTENT_TYPE_TEXT_HTML = "text/html";
	String NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX = "nsd-jstree-context-menu-";
	String JSON_DATA_REQUEST_ATTRIBUTE_KEY = Renderer.class.getName()+":jsonData";
	String CONTENT_TYPE_APPLICATION_JSON = "application/json";
	String ORIGINAL_ELEMENT_VALUE_NAME_PREFIX = ".original.";
	String CONTEXT_ESTRUCTURAL_FEATURE_KEY = EStructuralFeature.class.getName()+":context";
	String TITLE_KEY = "title";
	String NAME_KEY = "name";
	String REFERRER_KEY = ".referrer";
	String REFERRER_HEADER = "referer";
	String OBJECT_VERSION_KEY = ".object-version";
	String INDEX_HTML = "index.html";
	String EXTENSION_HTML = ".html";
	String EXTENSION_JSON = ".json";
	String DIGEST_ALGORITHM = "SHA-256";
	String UTF_8 = "UTF-8";
	

	/**
	 * Rendering can be customized by annotating model element with
	 * annotations with this source.
	 * 
	 * Adding UI rendering annotations to the model mixes modeling and UI concerns.
	 * Also model annotations allow to define only one way of rendering a particular model element.
	 * 
	 * Other customization options include overriding <code>getRenderAnnotation()</code> method or rendering methods, and
	 * UI code generation, which leverages method overriding.  
	 */
	String RENDER_ANNOTATION_SOURCE = "org.nasdanika.cdo.web.render";
	
	/**
	 * Default pegdown options.
	 */
	int PEGDOWN_OPTIONS = 	Extensions.ALL ^ Extensions.HARDWRAPS ^ Extensions.SUPPRESS_HTML_BLOCKS ^ Extensions.SUPPRESS_ALL_HTML;

	/**
	 * Source for Ecore GenModel documentation.
	 */
	String ECORE_DOC_ANNOTATION_SOURCE = "http://www.eclipse.org/emf/2002/GenModel";	
		
	Pattern SENTENCE_PATTERN = Pattern.compile(".+?[\\.?!]+\\s+");	
	
	int MIN_FIRST_SENTENCE_LENGTH = 20;
	int MAX_FIRST_SENTENCE_LENGTH = 250;
		
	// multi-line
	// input type
	// select options	
	
	Renderer<Context, EObject> INSTANCE = new Renderer<Context, EObject>() {
		
	};
	
	/**
	 * Simple cache interface for caching, for example, expensive rendering results such as left panel object tree.
	 * Cache is responsible for recognizing change in the context and invalidating results.
	 * @author Pavel Vlasov
	 *
	 */
	interface Cache {
		
		Object createCompositeKey(Object... keys);
	
		Object get(Object key) throws Exception;
		
		void put(Object key, Object value) throws Exception;
		
		void remove(Object key) throws Exception;
	}
	
	Cache NO_CACHE = new Cache() {
		
		@Override
		public void remove(Object key) throws Exception {
			// NOP
		}
		
		@Override
		public void put(Object key, Object value) throws Exception {
			// NOP
			
		}
		
		@Override
		public Object get(Object key) throws Exception {
			return null;
		}

		@Override
		public Object createCompositeKey(Object... keys) {
			return null;
		}
		
	};
	
	/**
	 * Returns cache for given context.
	 * @param context
	 * @return
	 */
	default Cache getCache(C context) {
		return NO_CACHE;
	};
	
	/**
	 * Returns a renderer instance for a class. This implementation uses renderer registry 
	 * which loads renderers from extensions of ``org.nasdanika.cdo.web.renderer`` extension point.
	 * @param eClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Renderer<C, EObject> getRenderer(EClass eClass) {
		return (Renderer<C, EObject>) RendererRegistry.INSTANCE.getRenderer(eClass);
	}
	
	/**
	 * Returns an instance of renderer chained with the masterResourceProvider.
	 * Sub-interfaces and implementations must override this method to return a proper
	 * renderer implementation.
	 * @param masterResourceProvider
	 * @return
	 */
	default Renderer<C, T> chain(ResourceProvider<C> masterResourceProvider) throws Exception {
		return new Renderer<C, T>() {
			
			@Override
			public ResourceProvider<C> getMasterResourceProvider(C context) throws Exception {
				return masterResourceProvider;
			}
			
		};
	}
	
	/**
	 * Returns renderer for a reference. The renderer is chained with this renderer as its master
	 * resource provider with ``<feature class>.<feature name>.`` prefix. 
	 * 
	 * For example if a renderer is requested for {@link EAttribute} ``myAttribute`` then call to its
	 * ``getResource(context, "myResource")`` method will call this renderer with ``attribute.myAttribute.myResource`` key.
	 * 
	 * Such chaining allows contextual customization, a renderer for class A would behave differently when class A is a child 
	 * of B or C.
	 * @param reference
	 * @param featureValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <M extends EObject> Renderer<C, M> getReferenceRenderer(EReference reference, M featureValue) throws Exception {
		String className = reference.eClass().getName();
		if (className.startsWith("E")) {
			className = className.substring(1);
		}
		String prefix = StringUtils.uncapitalize(className)+"."+reference.getName()+".";
		
		ResourceProvider<C> master = new ResourceProvider<C>() {

			@Override
			public Object getResource(C context, String key) throws Exception {
				return Renderer.this.getResource(context, prefix+key);
			}

			@Override
			public String getResourceString(C context, String key) throws Exception {
				return Renderer.this.getResourceString(context, prefix+key);
			}
		};
		
		if (featureValue == null) {
			return (Renderer<C, M>) getRenderer(reference.getEReferenceType()).chain(master);
		}
		
		return getRenderer(featureValue).chain(master);
	}	

	/**
	 * Returns renderer for an object.
	 * @param modelObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default <M extends EObject> Renderer<C, M> getRenderer(M modelObject) {		
		return modelObject == null ? null : (Renderer<C, M>) getRenderer(modelObject.eClass());
	}
	
	/**
	 * Returns source values for model annotations to use as the source of rendering annotations.
	 * Annotation sources are read sequentially until the annotation is found.
	 * This implementation returns values of ``org.nasdanika.cdo.web.render:annotation-source`` system property split by semicolon plus RENDER_ANNOTATION_SOURCE constant value ``org.nasdanika.cdo.web.render``.
	 * This method can be overridden to "white-label" the model, i.e. to use rendering annotations with source
	 * specific to the development organization, e.g. ``com.mycompany.render``. 
	 * 
	 * It can also be overridden to use different annotations profiles in different situations, e.g. ``com.mycompany.lob-a.render`` for business A and ``com.mycompany.lob-b.render`` for business B. 
	 * 
	 * @param context
	 * @return
	 */
	default List<String> getRenderAnnotationSources(C context) {
		List<String> ret = new ArrayList<>();
		String annotationSourceProperty = System.getProperty("org.nasdanika.cdo.web.render:annotation-source");
		if (annotationSourceProperty != null) {
			for (String as: annotationSourceProperty.split(";")) {
				ret.add(as);
			}
		}
		ret.add(RENDER_ANNOTATION_SOURCE);
		return ret;
	}
		
	/**
	 * Retrieves render annotation. 
	 * 
	 * If the model element is {@link ENamedElement}, then annotation value is
	 * retrieved as a resource string with key ``<Named element EClass name without first E uncapitalized>.<named element name>.render.<key>``. 
	 * For example ``attribute.name.render.label`` or ``reference.guest.render.visible``. This approach keeps resource string keys simple enough, but
	 * may result in name clashes if used in a base renderer which serves two different model elements with different features with the same name. If it happens,
	 * define per-model element renderers and render annotations within their resource bundles - this is the approach which https://github.com/Nasdanika/codegen-ecore-web-ui takes.
	 * 
	 * If there is no resource string matching the annotation key, then annotation value is read from the details entry with the specified key of
	 * he model element annotation with source ``org.nasdanika.cdo.web.render``.
	 * 
	 * This method can be overridden to read annotations from another source,
	 * e.g. keeping render annotations associated with the current user would allow to customize UI on per-user basis.
	 * Along the same lines the UI may be customized based on the locale or geography. 
	 * All these and other options may be chained, e.g. if user profile does not cusomize rendering, then fall-back to 
	 * locale profile, and then to the model annotation (call super.getRenderAnnotation()).  
	 * @param context
	 * @param modelElement
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	default String getRenderAnnotation(C context, EModelElement modelElement, String key) throws Exception {
		if (modelElement instanceof ENamedElement) {
			String rs = getResourceString(context, ((ENamedElement) modelElement), "render."+key, false);
			if (rs != null) {
				return rs;
			}
		}
		
		for (String ras: getRenderAnnotationSources(context)) {
			EAnnotation ra = modelElement.getEAnnotation(ras);
			if (ra != null) {
				String value = ra.getDetails().get(key);
				if (value != null) {
					return value;
				}
			}
		}
		for (String ras: getRenderAnnotationSources(context)) {
			if (modelElement instanceof EClass) {
				String cra = RenderUtil.getRenderAnnotation(ras, (EClass) modelElement, key);
				if (cra != null) {
					return cra;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Parses result of getRenderAnnotation() as {@link Yaml}.
	 * @param context
	 * @param modelElement
	 * @param key
	 * @return
	 * @throws Exception
	 */
	default Object getYamlRenderAnnotation(C context, EModelElement modelElement, String key) throws Exception {
		String yamlStr = getRenderAnnotation(context, modelElement, key);
		if (yamlStr == null) {
			return null;
		}
		Yaml yaml = new Yaml();
		Object ret = yaml.load(yamlStr);		
		return ret == null ? Collections.emptyMap() : ret; // An empty document is different from the annotation not being present.
	}
	
	/**
	 * Retrieves render annotation using {@link RenderAnnotation} enum.
	 * @param context
	 * @param modelElement
	 * @param renderAnnotation
	 * @return
	 * @throws Exception
	 */
	default String getRenderAnnotation(C context, EModelElement modelElement, RenderAnnotation renderAnnotation) throws Exception {
		return getRenderAnnotation(context, modelElement, renderAnnotation.literal);
	}
	
	/**
	 * Returns true if ``visible`` annotation is absent, its value is ``true``, or its value is not ``false``
	 * , obj is and instance of {@link CDOObject} and the value is an XPath expression evaluating to ``true``.
	 * is evaluated as 
	 * @param context
	 * @param obj
	 * @param modelElement
	 * @return
	 * @throws Exception 
	 */
	default boolean isVisible(C context, T obj, EModelElement modelElement) throws Exception {
		String visibleRenderAnnotation = getRenderAnnotation(context, modelElement, RenderAnnotation.VISIBLE);
		if (CoreUtil.isBlank(visibleRenderAnnotation) || "true".equals(visibleRenderAnnotation)) {
			return true;
		} 
		
		if ("false".equals(visibleRenderAnnotation)) {
			return false;
		}
		
		if (obj instanceof CDOObject) {
			// XPath
			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
			return Boolean.TRUE.equals(jxPathContext.getValue(visibleRenderAnnotation, Boolean.class));
		}	
		
		return true;
	}

	/**
	 * Returns true if ``editable`` annotation is absent, its value is ``true``, or its value is not ``false``
	 * , obj is and instance of {@link CDOObject} and the value is an XPath expression evaluating to ``true``.
	 * is evaluated as 
	 * @param context
	 * @param obj
	 * @param modelElement
	 * @return
	 * @throws Exception 
	 */
	default boolean isEditable(C context, T obj, EModelElement modelElement) throws Exception {
		String editableRenderAnnotation = getRenderAnnotation(context, modelElement, RenderAnnotation.EDITABLE);
		if ("true".equals(editableRenderAnnotation)) {
			return true;
		} 
		
		if ("false".equals(editableRenderAnnotation)) {
			return false;
		}
		
		if (CoreUtil.isBlank(editableRenderAnnotation)) {
			if (modelElement instanceof EStructuralFeature) {
				return isEditable(context, obj, ((EStructuralFeature) modelElement).getEContainingClass());
			}
			return true;
		}
		
		if (obj instanceof CDOObject) {
			// XPath
			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
			return Boolean.TRUE.equals(jxPathContext.getValue(editableRenderAnnotation, Boolean.class));
		}	
		
		return true;		
	}

//	/**
//	 * Returns true if ``deletable`` annotation is absent, its value is ``true``, or its value is not ``false``
//	 * , obj is and instance of {@link CDOObject} and the value is an XPath expression evaluating to ``true``.
//	 * is evaluated as 
//	 * @param context
//	 * @param obj
//	 * @param modelElement
//	 * @return
//	 * @throws Exception 
//	 */
//	default boolean isDeletable(C context, T obj, EModelElement modelElement) throws Exception {
//		String editableRenderAnnotation = getRenderAnnotation(context, modelElement, RenderAnnotation.DELETABLE);
//		if (CoreUtil.isBlank(editableRenderAnnotation) || "true".equals(editableRenderAnnotation)) {
//			return true;
//		} 
//		
//		if ("false".equals(editableRenderAnnotation)) {
//			return false;
//		}
//		
//		if (obj instanceof CDOObject) {
//			// XPath
//			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
//			return Boolean.TRUE.equals(jxPathContext.getValue(editableRenderAnnotation, Boolean.class));
//		}	
//		
//		return true;		
//	}
	
	/**
	 * Parses result of getRenderAnnotation() as {@link Yaml}.
	 * @param context
	 * @param modelElement
	 * @param renderAnnotation
	 * @return
	 * @throws Exception
	 */
	default Object getYamlRenderAnnotation(C context, EModelElement modelElement, RenderAnnotation renderAnnotation) throws Exception {
		return getYamlRenderAnnotation(context, modelElement, renderAnnotation.literal);
	}	
	
	/**
	 * Derives label (display name) from a name. This implementation splits by camel case,
	 * lowercases 1+ segments and capitalizes the 0 segment. E.g. myCoolName becomes My cool name.
	 * @param name
	 * @return
	 */
	default String nameToLabel(String name) {
		String[] cca = StringUtils.splitByCharacterTypeCamelCase(name);
		cca[0] = StringUtils.capitalize(cca[0]);
		for (int i=1; i<cca.length; ++i) {
			cca[i] = cca[i].toLowerCase();
		}
		return StringUtils.join(cca, " ");
	}
	
	interface FeaturePredicate {
		
		boolean test(EStructuralFeature feature) throws Exception;
		
	}

	/**
	 * 
	 * @param obj
	 * @return A list of structural features to include into the object view. ``RenderAnnotation.VISIBLE`` defines feature visibility.
	 * @throws Exception 
	 */
	default List<EStructuralFeature> getVisibleFeatures(C context, T obj, FeaturePredicate predicate) throws Exception {
		List<EStructuralFeature> ret = new ArrayList<>();
		for (EStructuralFeature sf: obj.eClass().getEAllStructuralFeatures()) {
			if (context.authorizeRead(obj, sf.getName(), null) && (predicate == null || predicate.test(sf))) {
				if (sf instanceof EReference && !sf.isMany()) { 
					Object fv = obj.eGet(sf); 
					if (fv != null && !context.authorize(obj.eGet(sf), StandardAction.read, null, null)) {
						continue; // Single reference with unreadable value.
					}
				}
				if (isVisible(context, obj, sf)) {
					ret.add(sf);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Features to include in a (left-panel) tree representation. 
	 * This implementation returns visible features which are containment references.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default List<EStructuralFeature> getTreeFeatures(C context, T obj) throws Exception {
		FeaturePredicate treeFeaturePredicate = feature -> {
			String treeFeatureAnnotation = getRenderAnnotation(context, feature, RenderAnnotation.TREE_FEATURE);
			if ("true".equals(treeFeatureAnnotation)) {
				return true;
			}
			if ("false".equals(treeFeatureAnnotation)) {
				return false;
			}
			return feature instanceof EReference && ((EReference) feature).isContainment();
		};
		return getVisibleFeatures(context, obj, treeFeaturePredicate);
	}
	
	/**
	 * Returns true if this container argument shall be treated as the breadcrumbs path root.
	 * @param context
	 * @param obj
	 * @param container
	 * @return
	 * @throws Exception
	 */
	default boolean isObjectPathRoot(C context, T obj, EObject container) throws Exception {
		return container.eContainer() == null;
	}
	
	/**
	 * Renders object path to breadcrumbs. This implementation traverses the object containment path up to the top level object in the resource.
	 * @param target
	 * @param context
	 * @param action Action, e.g. Edit or Add reference.
	 * @param breadCrumbs
	 * @throws Exception
	 */
	default void renderObjectPath(C context, T obj, Object action, Breadcrumbs breadCrumbs) throws Exception {
		List<EObject> cPath = new ArrayList<EObject>();
		if (!isObjectPathRoot(context, obj, obj)) {
			for (EObject c = obj.eContainer(); c != null && context.authorize(c, StandardAction.read, null, null); c = c.eContainer()) {
				cPath.add(c);
				if (isObjectPathRoot(context, obj, c)) {
					break;
				}
			}
			Collections.reverse(cPath);
			for (EObject c: cPath) {
				Renderer<C, EObject> cRenderer = getRenderer(c);
				Object cIconAndLabel = cRenderer.renderIconAndLabel(context, c);  				
				if (cIconAndLabel != null) {
					EObject cContainer = c.eContainer();
					if (!breadCrumbs.isEmpty() && cPath.contains(cContainer)) { // Double-check to be on the safe side.
						EReference containmentFeature = c.eContainmentFeature();
						Renderer<C, EObject> containerRenderer = getRenderer(cContainer);
						if (containmentFeature != null && containerRenderer.isPathFeature(context, cContainer, containmentFeature)) {
							TypedElementLocation containmentFeatureLocation = containerRenderer.getTypedElementLocation(context, containmentFeature);
							if (containmentFeatureLocation  == TypedElementLocation.leftPanel || containmentFeatureLocation == TypedElementLocation.item) {
								List<EStructuralFeature> containerVisibleFeatures = containerRenderer.getVisibleFeatures(context, cContainer, null);
								Object categoryIconAndLabel = renderNamedElementCategoryIconAndLabel(context, containmentFeature, containerVisibleFeatures);
								if (categoryIconAndLabel != null) {
									breadCrumbs.item(null, TagName.i.create(categoryIconAndLabel).attribute("title", "Category"));
								}
								String containerURI = containerRenderer.getObjectURI(context, cContainer);
								String featureURI = containerURI;
								if (featureURI != null) {
									if (containmentFeatureLocation  == TypedElementLocation.leftPanel) {
										featureURI += "/feature/"+containmentFeature.getName()+"/view.html";
									} else {
										featureURI += "/"+INDEX_HTML+"?context-feature="+URLEncoder.encode(containmentFeature.getName(), StandardCharsets.UTF_8.name());										
									}
								}
								Tag featureIconAndLabel = TagName.i.create(containerRenderer.renderNamedElementIconAndLabel(context, containmentFeature, containerVisibleFeatures)).attribute("title", "Feature");
								if (c instanceof CDOObject) {
									featureIconAndLabel.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) c)+"-"+containmentFeature.getName());
								}								
								breadCrumbs.item(featureURI, featureIconAndLabel);
							}
						}
					}
					String objectURI = cRenderer.getObjectURI(context, c);
					Tag cIconAndLabelSpan = TagName.span.create(cIconAndLabel).attribute("title", nameToLabel(c.eClass().getName()));
					if (c instanceof CDOObject) {
						cIconAndLabelSpan.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) c));
					}					
					breadCrumbs.item(objectURI == null ? objectURI : objectURI+"/"+INDEX_HTML, cIconAndLabelSpan); // TODO - class label and then Jsoup to plain text
				}
			}
		}
		
		EObject objContainer = obj.eContainer();
		if (!breadCrumbs.isEmpty() && cPath.contains(objContainer)) { // Double-check to be on the safe side.
			EReference containmentFeature = obj.eContainmentFeature();
			Renderer<C, EObject> containerRenderer = getRenderer(objContainer);
			if (containmentFeature != null && containerRenderer.isPathFeature(context, objContainer, containmentFeature)) {
				TypedElementLocation containmentFeatureLocation = containerRenderer.getTypedElementLocation(context, containmentFeature);
				if (containmentFeatureLocation  == TypedElementLocation.leftPanel || containmentFeatureLocation == TypedElementLocation.item) {
					List<EStructuralFeature> containerVisibleFeatures = containerRenderer.getVisibleFeatures(context, objContainer, null);
					Object categoryIconAndLabel = renderNamedElementCategoryIconAndLabel(context, containmentFeature, containerVisibleFeatures);
					if (categoryIconAndLabel != null) {
						breadCrumbs.item(null, TagName.i.create(categoryIconAndLabel).attribute("title", "Category"));
					}
					String containerURI = containerRenderer.getObjectURI(context, objContainer);
					String featureURI = containerURI;
					if (featureURI != null) {
						if (containmentFeatureLocation  == TypedElementLocation.leftPanel) {
							featureURI += "/feature/"+containmentFeature.getName()+"/view.html";
						} else {
							featureURI += "/"+INDEX_HTML+"?context-feature="+URLEncoder.encode(containmentFeature.getName(), StandardCharsets.UTF_8.name());										
						}
					}
					Tag featureIconAndLabel = TagName.i.create(containerRenderer.renderNamedElementIconAndLabel(context, containmentFeature, containerVisibleFeatures)).attribute("title", "Feature");
					if (objContainer instanceof CDOObject) {
						featureIconAndLabel.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) objContainer)+"-"+containmentFeature.getName());
					}								
					breadCrumbs.item(featureURI, featureIconAndLabel);
				}
			}
		}
				
		String objTitle = nameToLabel(obj.eClass().getName()); // TODO - class label and to text.
		Tag objIconAndLabelSpan = TagName.span.create(renderIconAndLabel(context, obj)).attribute("title", objTitle);
		if (obj instanceof CDOObject) {
			objIconAndLabelSpan.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj));
		}		
		if (action == null) {
			breadCrumbs.item(null , objIconAndLabelSpan); 
		} else {
			String objectURI = getObjectURI(context, obj);
			breadCrumbs.item(objectURI == null ? objectURI : objectURI+"/"+INDEX_HTML, objIconAndLabelSpan);
			breadCrumbs.item(null, breadCrumbs.getFactory().tag(TagName.b, action).attribute("title", "Action"));
		}
	}
	
	/**
	 * @param eClass
	 * @param feature
	 * @return true if the feature shall be shown in breadcrumbs object path.
	 */
	default boolean isPathFeature(C context, T obj, EStructuralFeature feature) throws Exception {
		String pathFeatureAnnotation = getRenderAnnotation(context, feature, RenderAnnotation.PATH_FEATURE);
		if ("true".equals(pathFeatureAnnotation)) {
			return true;
		}
		if ("false".equals(pathFeatureAnnotation)) {
			return false;
		}
		return true;
	}
	
	// TODO - isPathFeature() method, also use in jsTree rendering. Rename getTreeFeatures to getPathFeatures()
	
	/**
	 * Renders object's feature path to breadcrumbs. This implementation traverses the object containment path up to the top level object in the resource.
	 * @param target
	 * @param context
	 * @param action Action, e.g. Edit or Add reference.
	 * @param breadCrumbs
	 * @throws Exception
	 */
	default void renderFeaturePath(C context, T obj, EStructuralFeature feature, Object action, Breadcrumbs breadCrumbs) throws Exception {
		List<EObject> cPath = new ArrayList<EObject>();
		if (!isObjectPathRoot(context, obj, obj)) {
			for (EObject c = obj.eContainer(); c != null && context.authorize(c, StandardAction.read, null, null); c = c.eContainer()) {
				cPath.add(c);
				if (isObjectPathRoot(context, obj, c)) {
					break;
				}
			}
			Collections.reverse(cPath);
			for (EObject c: cPath) {
				Renderer<C, EObject> cRenderer = getRenderer(c);
				Object cIconAndLabel = cRenderer.renderIconAndLabel(context, c);
				if (cIconAndLabel != null) {
					EObject cContainer = c.eContainer();
					if (!breadCrumbs.isEmpty() && cPath.contains(cContainer)) { // Double-check to be on the safe side.
						EReference containmentFeature = c.eContainmentFeature();
						Renderer<C, EObject> containerRenderer = getRenderer(cContainer);
						if (containmentFeature != null /* TODO - && containerRenderer.isPathFeature(containmentFeature) */) {
							TypedElementLocation containmentFeatureLocation = containerRenderer.getTypedElementLocation(context, containmentFeature);
							if (containmentFeatureLocation  == TypedElementLocation.leftPanel || containmentFeatureLocation == TypedElementLocation.item) {
								List<EStructuralFeature> containerVisibleFeatures = containerRenderer.getVisibleFeatures(context, cContainer, null);
								Object categoryIconAndLabel = renderNamedElementCategoryIconAndLabel(context, containmentFeature, containerVisibleFeatures);
								if (categoryIconAndLabel != null) {
									breadCrumbs.item(null, TagName.i.create(categoryIconAndLabel).attribute("title", "Category"));
								}
								String containerURI = containerRenderer.getObjectURI(context, cContainer);
								String featureURI = containerURI;
								if (featureURI != null) {
									if (containmentFeatureLocation  == TypedElementLocation.leftPanel) {
										featureURI += "/feature/"+containmentFeature.getName()+"/view.html";
									} else {
										featureURI += "/"+INDEX_HTML+"?context-feature="+URLEncoder.encode(containmentFeature.getName(), StandardCharsets.UTF_8.name());										
									}
								}
								Tag featureIconAndLabel = TagName.i.create(containerRenderer.renderNamedElementIconAndLabel(context, containmentFeature, containerVisibleFeatures)).attribute("title", "Feature");
								if (c instanceof CDOObject) {
									featureIconAndLabel.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) c)+"-"+containmentFeature.getName());
								}																
								breadCrumbs.item(featureURI, featureIconAndLabel);
							}
						}
					}
					String objectURI = cRenderer.getObjectURI(context, c);
					Tag cIconAndLabelSpan = TagName.span.create(cIconAndLabel).attribute("title", nameToLabel(c.eClass().getName()));
					if (c instanceof CDOObject) {
						cIconAndLabelSpan.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) c));
					}					
					breadCrumbs.item(objectURI == null ? objectURI : objectURI+"/"+INDEX_HTML, cIconAndLabelSpan); // TODO class label to text with JSoup
				}
			}
		}
		
		EObject objContainer = obj.eContainer();
		if (!breadCrumbs.isEmpty() && cPath.contains(objContainer)) { // Double-check to be on the safe side.
			EReference containmentFeature = obj.eContainmentFeature();
			Renderer<C, EObject> containerRenderer = getRenderer(objContainer);
			if (containmentFeature != null /* TODO - && containerRenderer.isPathFeature(containmentFeature) */) {
				TypedElementLocation containmentFeatureLocation = containerRenderer.getTypedElementLocation(context, containmentFeature);
				if (containmentFeatureLocation  == TypedElementLocation.leftPanel || containmentFeatureLocation == TypedElementLocation.item) {
					List<EStructuralFeature> containerVisibleFeatures = containerRenderer.getVisibleFeatures(context, objContainer, null);
					Object categoryIconAndLabel = renderNamedElementCategoryIconAndLabel(context, containmentFeature, containerVisibleFeatures);
					if (categoryIconAndLabel != null) {
						breadCrumbs.item(null, TagName.i.create(categoryIconAndLabel).attribute("title", "Category"));
					}
					String containerURI = containerRenderer.getObjectURI(context, objContainer);
					String featureURI = containerURI;
					if (featureURI != null) {
						if (containmentFeatureLocation  == TypedElementLocation.leftPanel) {
							featureURI += "/feature/"+containmentFeature.getName()+"/view.html";
						} else {
							featureURI += "/"+INDEX_HTML+"?context-feature="+URLEncoder.encode(containmentFeature.getName(), StandardCharsets.UTF_8.name());										
						}
					}
					Tag featureIconAndLabel = TagName.i.create(containerRenderer.renderNamedElementIconAndLabel(context, containmentFeature, containerVisibleFeatures)).attribute("title", "Feature");
					if (objContainer instanceof CDOObject) {
						featureIconAndLabel.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) objContainer)+"-"+containmentFeature.getName());
					}								
					breadCrumbs.item(featureURI, featureIconAndLabel);
				}
			}
		}
		
		String objectURI = getObjectURI(context, obj);
		Tag objIconAndLabelSpan = TagName.span.create(renderIconAndLabel(context, obj)).attribute("title", nameToLabel(obj.eClass().getName()));
		if (obj instanceof CDOObject) {
			objIconAndLabelSpan.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj));
		}		
		breadCrumbs.item(objectURI == null ? objectURI : objectURI+"/"+INDEX_HTML, objIconAndLabelSpan);	// TODO class label to text with JSoup	
		List<EStructuralFeature> visibleFeatures = getVisibleFeatures(context, obj, null);
		Object categoryIconAndLabel = renderNamedElementCategoryIconAndLabel(context, feature, visibleFeatures);
		if (categoryIconAndLabel != null) {
			breadCrumbs.item(null, TagName.i.create(categoryIconAndLabel).attribute("title", "Category"));
		}
		Tag featureCrumb = TagName.i.create(renderNamedElementIconAndLabel(context, feature, visibleFeatures)).attribute("title", "Feature");
		if (obj instanceof CDOObject) {
			featureCrumb.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj)+"-"+feature.getName());
		}										
		if (action == null) {
			breadCrumbs.item(null, featureCrumb);
		} else {
			breadCrumbs.item(objectURI == null ? objectURI : objectURI+"/feature/"+feature.getName()+"/view.html", featureCrumb);
			breadCrumbs.item(null, breadCrumbs.getFactory().tag(TagName.b, action).attribute("title", "Action"));
		}
	}

	/**
	 * Renders object path to a fragment with a given separator. This implementation traverses the object containment path up to the top level object in the resource.
	 * @param target
	 * @param context
	 * @param action Action, e.g. Edit or Add reference.
	 * @param breadCrumbs
	 * @throws Exception
	 */
	default Fragment renderObjectPath(C context, T obj, Object separator) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();
		List<EObject> cPath = new ArrayList<EObject>();
		for (EObject c = obj.eContainer(); c != null; c = c.eContainer()) {
			cPath.add(c);
		}
		Collections.reverse(cPath);
		for (EObject c: cPath) {
			Renderer<C, EObject> cRenderer = getRenderer(c);
			Object cLink = cRenderer.renderLink(context, c, false);
			if (cLink != null) {
				if (!ret.isEmpty()) {
					ret.content(separator);
				}
				ret.content(cLink);
			}
		}
		if (!ret.isEmpty()) {
			ret.content(separator);
		}
		ret.content(renderLink(context, obj, false));
		return ret;
	}

	/**
	 * Renders object label. This implementation interpolates the value of ``label`` annotation if it is found in 
	 * the object's EClass or any of its subclasses. The objects is used as the interpolation token source with 
	 * visible features names as token names and values as values. ``eclass-name`` token expands to object's EClass name and ``eclass-label`` to EClass label.
	 * 
	 * If ``label`` annotation is not found, then the value
	 * of the first feature is used as object label.  
	 *  
	 * Label value is HTML-escaped. 
	 * 
	 * @param context
	 * @param obj
	 * @return Object label or null if there are no visible features (e.g. the principal does not have permission to view the object.
	 * @throws Exception
	 */
	default Object renderLabel(C context, T obj) throws Exception {
		String labelAnnotation = getRenderAnnotation(context, obj.eClass(), RenderAnnotation.LABEL);
		
		if (labelAnnotation != null) {
			Map<String, EStructuralFeature> vsfm = new HashMap<>();
			for (EStructuralFeature vsf: getVisibleFeatures(context, obj, null)) {
				vsfm.put(vsf.getName(), vsf);
			}
			String label = getHTMLFactory(context).interpolate(labelAnnotation, token -> {
				if ("eclass-name".equals(token)) {
					return obj.eClass().getName();
				}
				
				if ("eclass-label".equals(token)) {
					try {
						return renderNamedElementLabel(context, obj.eClass());
					} catch (Exception e) {
						e.printStackTrace();
						return "*** ERROR ***";
					}
				}
				
				EStructuralFeature vsf = vsfm.get(token);
				return vsf == null ? null : obj.eGet(vsf);
			});
			return StringEscapeUtils.escapeHtml4(label);
		}
		
		for (EStructuralFeature vsf: getVisibleFeatures(context, obj, null)) {
			if (vsf instanceof EAttribute) {
				Object label = obj.eGet(vsf);
				if (label != null) {
					return StringEscapeUtils.escapeHtml4(String.valueOf(label));
				}
			}
		}
		
		if (obj instanceof CDOObject) {
			CDOID cdoID = ((CDOObject) obj).cdoID();
			if (cdoID != null) {
				return renderNamedElementLabel(context, obj.eClass())+"-"+CDOIDCodec.INSTANCE.encode(context, cdoID);
			}
		}
		
		return renderNamedElementLabel(context, obj.eClass());		
	}
	
	/**
	 * Renders icon and label.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Object renderIconAndLabel(C context, T obj) throws Exception {
		Object label = renderLabel(context, obj);
		if (label == null) {
			return renderIcon(context, obj);
		}
		
		Object icon = renderIcon(context, obj);
		if (icon == null) {
			return label;
		}
		return getHTMLFactory(context).fragment(icon, " ", label);		
	}
	
	/**
	 * Invokes getIcon(). If it returns null, this method also returns null.
	 * Otherwise, if the return value contains ``/``, then it returns ``img`` tag with ``src`` attribute set to icon value.
	 * If there is no ``/``, then it returns ``span`` with ``class`` attribute set to icon value (for glyphs, such as {@link Bootstrap.Glyphicon} or {@link FontAwesome}).
	 * @param context
	 * @param modelElement
	 * @return
	 * @throws Exception
	 */
	default Object renderIcon(C context, T obj) throws Exception {
		String icon = getIcon(context, obj);
		if (icon == null) {
			return null;			
		}
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (icon.indexOf("/") == -1) {
			return htmlFactory.span().addClass(icon);
		}
		return htmlFactory.tag(TagName.img).attribute("src", icon);
	}
	
	/**
	 * Icon "location" for a given object. If object element has {@link RenderAnnotation}.ICON annotation, then it is interpolated with object features as tokens and
	 * ``context-path`` token set to request context path. Otherwise this implementation returns icon of the object's {@link EClass}.
	 * 
	 * If icon contains ``/`` it is treated as URL, otherwise it is treated as css class, e.g. Bootstrap's ``glyphicon glyphicon-close``.
	 * @param context
	 * @param modelElement
	 * @return
	 * @throws Exception 
	 */
	default String getIcon(C context, T obj) throws Exception {
		if (obj == null) {
			return null;
		}
		String ra = getRenderAnnotation(context, obj.eClass(), RenderAnnotation.ICON);
		if (ra != null) {
			boolean[] hasTokenExpansionFailures = { false };
			String objectURI = getObjectURI(context, obj);
			TokenSource contextPathTokenSource = token -> {
				if ("context-path".equals(token) && context instanceof HttpServletRequestContext) {
					return ((HttpServletRequestContext) context).getRequest().getContextPath();
				}
				if ("object-uri".equals(token)) {
					return objectURI;
				}
				return null;
			};
			TokenSource eObjectTokenSource = new EObjectTokenSource(context, obj, contextPathTokenSource) {
				@Override
				public Object get(String token) {
					Object ret = super.get(token);
					if (ret == null) {
						hasTokenExpansionFailures[0] = true;
					}
					return ret;
				}
			};
			String icon = getHTMLFactory(context).interpolate(ra, eObjectTokenSource);
			if (!hasTokenExpansionFailures[0]) {
				return icon;
			}
		}
		
		return getModelElementIcon(context, obj.eClass());
	}
	
	/**
	 * @param context
	 * @param obj
	 * @return Object URI. This implementation returns object path if context is instanceof {@link HttpServletRequestContext} 
	 * and ``null`` otherwise. Subtypes can override this method to, say, return a URL fragment for Single-Page Applications.
	 * @throws Exception
	 */
	default String getObjectURI(C context, T obj) throws Exception {
		if (context instanceof HttpServletRequestContext) {
			return ((HttpServletRequestContext) context).getObjectPath(obj);		
		}
		
		return null;
	}
	
	/**
	 * Renders object link using object label and path.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Object renderLink(C context, T obj, boolean withPathTooltip) throws Exception {
		String objectURI = getObjectURI(context, obj);
		Tag ret = getHTMLFactory(context).link(objectURI == null ? "#" : objectURI+"/"+INDEX_HTML, renderIconAndLabel(context, obj));
		if (withPathTooltip) {
			String pathTxt = Jsoup.parse(renderObjectPath(context, obj, " > ").toString() + " ["+renderNamedElementLabel(context, obj.eClass())+"]").text();			
			ret.attribute("title", pathTxt);
		}
		if (obj instanceof CDOObject) {
			ret.addClass(NSD_JSTREE_CONTEXT_MENU_CLASS_PREFIX+CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj));
		}
		ret.setData(obj);
		return ret;
	}
		
	/**
	 * Detect common prefix in named element and uses it as a category. E.g. ``miscKey`` and miscValue`` will 
	 * get an auto-category ``Misc``. Names are tokenized by camel case. Category contains at least two
	 * named elements. If an element belongs to two categories, e.g. ``miscFeatureA`` would belong to ``misc` and to ``miscFeature`` categories, 
	 * the category with larger number of features in it wins. If the number of features in two categories is equal, then the longest category wins.
	 * @param namedElement
	 * @param eClass
	 * @return
	 */
	default <NE extends ENamedElement> String getAutoCategory(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		if (!namedElements.contains(namedElement)) {
			throw new IllegalArgumentException("Features do not contain the feature");
		}
		if (getRenderAnnotation(context, namedElement, RenderAnnotation.CATEGORY) != null) {
			return null;
		}
		Map<String, Set<NE>> categories = new HashMap<>();
		for (NE ene: namedElements) {
			String categoryAnnotation = getRenderAnnotation(context, ene, RenderAnnotation.CATEGORY);
			if (categoryAnnotation == null) {
				String[] esfn = StringUtils.splitByCharacterTypeCamelCase(ene.getName());
				for (int i = 1; i < esfn.length; ++i) {
					String category = StringUtils.join(esfn, null, 0, i);
					Set<NE> cf = categories.get(category);
					if (cf == null) {
						cf = new HashSet<>();
						categories.put(category, cf);
					}
					cf.add(ene);
				}
			} else {
				Set<NE> cf = categories.get(categoryAnnotation);
				if (cf == null) {
					cf = new HashSet<>();
					categories.put(categoryAnnotation, cf);
				}
				cf.add(ene);				
			}
		}
		
		// Remove irrelevant
		Iterator<Entry<String, Set<NE>>> eit = categories.entrySet().iterator();
		while (eit.hasNext()) {
			Entry<String, Set<NE>> entry = eit.next();
			if (entry.getValue().size()==1 || !entry.getValue().contains(namedElement)) {
				eit.remove();
			}
		}
		
		if (categories.isEmpty()) {
			return null;
		}
		
		if (categories.size() == 1) {
			return categories.keySet().iterator().next();
		}
		
		// Sort by size and then by length - largest/longest first.		
		List<String> cList = new ArrayList<>(categories.keySet());
		Collections.sort(cList, (c1, c2) -> {
			Set<NE> fs1 = categories.get(c1);
			Set<NE> fs2 = categories.get(c2);
			
			int cmp = fs2.size() - fs1.size();
			if (cmp != 0) {
				return cmp;
			}
			return c2.length() - c1.length();
		});
		
		return cList.get(0);
	}
	
	
	/**
	 * Renders {@link ENamedElement} label. Returns value of ``model-element-label`` render annotation if it is present.
	 * If it is not present, this implementation return element name suffix after the auto-category (if any) passed through nameToLabel() conversion.
	 * @param context
	 * @param namedElement
	 * @param eClass
	 * @throws Exception
	 */
	default <NE extends ENamedElement> String renderNamedElementLabel(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		String label = getRenderAnnotation(context, namedElement, RenderAnnotation.MODEL_ELEMENT_LABEL);
		if (label != null) {
			return label;
		}		
		String name = namedElement.getName();
		String autoCategory = (namedElement instanceof EStructuralFeature || namedElement instanceof EParameter) ? getAutoCategory(context, namedElement, namedElements) : null;
		return nameToLabel(autoCategory == null ? name : name.substring(autoCategory.length()));		
	}
	
	default <NE extends ENamedElement> Object renderNamedElementCategoryLabel(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		String category = getRenderAnnotation(context, namedElement, RenderAnnotation.CATEGORY);
		if (category == null) {
			category = getAutoCategory(context, namedElement, namedElements);
		}
		if (category == null) {
			return null;
		}
		
		String categoryLabelAnnotation = null;
		if (namedElement instanceof EStructuralFeature) {
			categoryLabelAnnotation = getRenderAnnotation(context, ((EStructuralFeature) namedElement).getEContainingClass(), "category."+category+".label");
		}
		if (namedElement instanceof EParameter) {
			categoryLabelAnnotation = getRenderAnnotation(context, ((EParameter) namedElement).getEOperation(), "category."+category+".label");
		}
		if (categoryLabelAnnotation != null) {
			return categoryLabelAnnotation;
		}
		
		String[] cca = StringUtils.splitByCharacterTypeCamelCase(category);
		cca[0] = StringUtils.capitalize(cca[0]);
		for (int i=1; i<cca.length; ++i) {
			cca[i] = cca[i].toLowerCase();
		}
		return StringUtils.join(cca, " ");		
	}
	
	default <NE extends ENamedElement> Object renderNamedElementCategoryIcon(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		String category = getRenderAnnotation(context, namedElement, RenderAnnotation.CATEGORY);
		if (category == null) {
			category = getAutoCategory(context, namedElement, namedElements);
		}
		if (category == null) {
			return null;
		}
		
		String iconAnnotation = null;
		if (namedElement instanceof EStructuralFeature) {
			iconAnnotation = getRenderAnnotation(context, ((EStructuralFeature) namedElement).getEContainingClass(), "category."+category+".icon");
		}
		if (namedElement instanceof EParameter) {
			iconAnnotation = getRenderAnnotation(context, ((EParameter) namedElement).getEOperation(), "category."+category+".icon");
		}
		if (iconAnnotation == null) {
			return null;
		}
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (iconAnnotation.indexOf("/") == -1) {
			return htmlFactory.span().addClass(iconAnnotation);
		}
		return htmlFactory.tag(TagName.img).attribute("src", iconAnnotation);
	}
	
	default <NE extends ENamedElement> Object renderNamedElementCategoryIconAndLabel(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		Object label = renderNamedElementCategoryLabel(context, namedElement, namedElements);
		if (label == null) {
			return renderNamedElementCategoryIcon(context, namedElement, namedElements);
		}
		
		Object icon = renderNamedElementCategoryIcon(context, namedElement, namedElements);
		if (icon == null) {
			return label;
		}
		return getHTMLFactory(context).fragment(icon, " ", label);				
	}
	
	/**
	 * 
	 * @param context
	 * @param namedElement
	 * @return Named element icon and label.
	 * @throws Exception
	 */
	default <NE extends ENamedElement> Object renderNamedElementIconAndLabel(C context, NE namedElement, Collection<NE> namedElements) throws Exception {		
		Object label = renderNamedElementLabel(context, namedElement, namedElements);
		if (label == null) {
			return renderModelElementIcon(context, namedElement);
		}
		
		Object icon = renderModelElementIcon(context, namedElement);
		if (icon == null) {
			return label;
		}
		return getHTMLFactory(context).fragment(icon, " ", label);		
	}
	
	/**
	 * Returns category from {@link RenderAnnotation}.CATEGORY or auto-category, if any.
	 * @param context
	 * @param featue
	 * @param eClass
	 * @return
	 */
	default <NE extends ENamedElement> String getNamedElementCategory(C context, NE namedElement, Collection<NE> namedElements) throws Exception {
		String category = getRenderAnnotation(context, namedElement, RenderAnnotation.CATEGORY);
		if (category == null) {
			category = getAutoCategory(context, namedElement, namedElements);
		}
		return category;
	}

	/**
	 * 
	 * @param context
	 * @param namedElement
	 * @return Value of ``model-element-label`` render annotation if it is present or element name passed through nameToLabel() conversion.
	 * 
	 * @throws Exception
	 */
	default Object renderNamedElementLabel(C context, ENamedElement namedElement) throws Exception {
		String label = getRenderAnnotation(context, namedElement, RenderAnnotation.MODEL_ELEMENT_LABEL);
		if (label != null) {
			return label;
		}	
		return nameToLabel(namedElement.getName());
	}
	
	/**
	 * 
	 * @param context
	 * @param namedElement
	 * @return Named element icon and label.
	 * @throws Exception
	 */
	default Object renderNamedElementIconAndLabel(C context, ENamedElement namedElement) throws Exception { 
		Object label = renderNamedElementLabel(context, namedElement);
		if (label == null) {
			return renderModelElementIcon(context, namedElement);
		}
		
		Object icon = renderModelElementIcon(context, namedElement);
		if (icon == null) {
			return label;
		}
		return getHTMLFactory(context).fragment(icon, " ", label);		
	}
	
	/**
	 * Invokes getModelElementIcon(). If it returns null, this method also returns null.
	 * Otherwise, if the return value contains ``/``, then it returns ``img`` tag with ``src`` attribute set to icon value.
	 * If there is no ``/``, then it returns ``span`` with ``class`` attribute set to icon value (for glyphs, such as {@link Bootstrap.Glyphicon} or {@link FontAwesome}).
	 * @param context
	 * @param modelElement
	 * @return
	 * @throws Exception
	 */
	default Object renderModelElementIcon(C context, EModelElement modelElement) throws Exception {
		String icon = getModelElementIcon(context, modelElement);
		if (icon == null) {
			return null;			
		}
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (icon.indexOf("/") == -1) {
			return htmlFactory.span().addClass(icon);
		}
		return htmlFactory.tag(TagName.img).attribute("src", icon);
	}
	
	/**
	 * Icon "location" for a given model element. This implementation returns ``icon`` render annotation.
	 * If icon contains ``/`` it is treated as URL, otherwise it is treated as css class, e.g. Bootstrap's ``glyphicon glyphicon-close``.
	 * 
	 * If annotation is not found and the model element is {@link EStructuralFeature}, then the icon of its type is returned.
	 * @param context
	 * @param modelElement
	 * @return
	 * @throws Exception 
	 */
	default String getModelElementIcon(C context, EModelElement modelElement) throws Exception {
		String ra = getRenderAnnotation(context, modelElement, RenderAnnotation.ICON);	
		if (ra != null) {
			boolean[] hasTokenExpansionFailures = { false };
			@SuppressWarnings("unchecked")
			String objectURI = context instanceof HttpServletRequestContext ? getObjectURI(context, (T) ((HttpServletRequestContext) context).getTarget()) : null;				
			TokenSource contextPathTokenSource = token -> {
				if ("context-path".equals(token) && context instanceof HttpServletRequestContext) {
					return ((HttpServletRequestContext) context).getRequest().getContextPath();
				}
				if ("object-uri".equals(token)) {
					return objectURI;
				}
				return null;
			};
			TokenSource eObjectTokenSource = new EObjectTokenSource(context, modelElement, contextPathTokenSource) {
				@Override
				public Object get(String token) {
					Object ret = super.get(token);
					if (ret == null) {
						hasTokenExpansionFailures[0] = true;
					}
					return ret;
				}
			};
			String icon = getHTMLFactory(context).interpolate(ra, eObjectTokenSource);
			if (!hasTokenExpansionFailures[0]) {
				return icon;
			}
		}
		
		if (modelElement instanceof ETypedElement) {
			EClassifier eType = ((ETypedElement) modelElement).getEType();
			if (eType != null) {
				return (eType instanceof EClass ? getRenderer((EClass) eType) : this).getModelElementIcon(context, eType);
			}
		}
		
		if (modelElement instanceof EClassifier) {
			String instanceClassName = ((EClassifier) modelElement).getInstanceClassName();
			String rs = getResourceString(context, "javaClass."+instanceClassName+".icon");
			if (rs != null) {
				return rs;
			}
			// TODO - super-classes and interfaces
		}
		
		if (modelElement instanceof EEnumLiteral) {
			EEnum eEnum = ((EEnumLiteral) modelElement).getEEnum();
			return getRenderer(eEnum).getModelElementIcon(context, eEnum);
		}
		
		return null;
	}
	
	/**
	 * Renders individual {@link ETypedElement} value. This implementation: 
	 * 
	 * * Unreadable targets of single references are treated as nulls.
	 * * Nulls are rendered as empty strings.
	 * * For booleans invokes renderTrue() or renderFalse();
	 * * For dates uses ``format`` annotation to format with {@link SimpleDateFormat}, if the annotation is present.
	 * * For numbers uses ``format`` annotation to format with {@link DecimalFormat}, if the annotation is present.
	 * * For {@link Diagnostic} renders status icon, message and children.
	 * * Otherwise converts value to string and then html-escapes it.
	 * @param context
	 * @param typedElement
	 * @param value
	 * @param forEditing
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	default Object renderTypedElementValue(C context, ETypedElement typedElement, Object value, Consumer<Object> appConsumer, boolean forEditing) throws Exception {
		if (typedElement instanceof EReference && !typedElement.isMany() && !context.authorize(value, StandardAction.read, null, null)) {
			value = null;
		}
		
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (value == null || (value instanceof String && ((String) value).length() == 0)) {
			String pra = getRenderAnnotation(context, typedElement, RenderAnnotation.PLACEHOLDER);
			if (!CoreUtil.isBlank(pra) && context instanceof HttpServletRequestContext) {
				Object target = ((HttpServletRequestContext) context).getTarget();
				if (target instanceof CDOObject) {
					JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) target);
					Object pv = jxPathContext.getValue(pra);
					if (pv != null) {
						return htmlFactory.well(renderTypedElementValue(context, typedElement, pv, appConsumer, false)).small();
					}
				}
			}
			return "";
		}
		if (value instanceof byte[]) {
			return Base64.getEncoder().encodeToString((byte[]) value);
		}
		if (value instanceof EObject) {
			EObject eObjectValue = (EObject) value;
			if (typedElement instanceof EReference) {
				return getReferenceRenderer((EReference) typedElement, eObjectValue).renderLink(context, eObjectValue, true);
			}			
			
			if (eObjectValue.eResource() == null) {
				// Not part of a resource, render as view, not as link.
				return getRenderer(eObjectValue).renderViewFeatures(context, eObjectValue, appConsumer);
			}
			return getRenderer(eObjectValue).renderLink(context, eObjectValue, true);
		}
		if (value instanceof Boolean) {
			return (Boolean) value ?  renderTrue(context) : renderFalse(context);
		}
		if (value instanceof Enumerator) {
			Enumerator enumeratorValue = (Enumerator) value;
			String ret = StringEscapeUtils.escapeHtml4(enumeratorValue.getLiteral());
			EClassifier featureType = typedElement.getEType();
			if (featureType instanceof EEnum) {
				EEnum featureEnum = (EEnum) featureType;
				EEnumLiteral enumLiteral = featureEnum.getEEnumLiteral(enumeratorValue.getName());
				Object literalIcon = renderModelElementIcon(context, enumLiteral);
				Tag literalDocumentationIcon = renderDocumentationIcon(context, enumLiteral, appConsumer, true);
				return htmlFactory.fragment(literalIcon, " ", ret, literalDocumentationIcon);
			}
			
			return ret;
		}
		
		if (value instanceof Diagnostic) {
			Diagnostic diagnostic = (Diagnostic) value;
			Fragment rdf = htmlFactory.fragment();
			switch (diagnostic.getSeverity()) {
			case Diagnostic.ERROR:
				rdf.content(htmlFactory.fontAwesome().webApplication(WebApplication.exclamation_circle).getTarget().bootstrap().text().color(Style.DANGER).style().margin().right("0.5em"));
				break;
			case Diagnostic.WARNING:
				rdf.content(htmlFactory.fontAwesome().webApplication(WebApplication.exclamation_triangle).getTarget().bootstrap().text().color(Style.WARNING).style().margin().right("0.5em"));
				break;
			case Diagnostic.OK:
				rdf.content(htmlFactory.fontAwesome().webApplication(WebApplication.check_square).getTarget().bootstrap().text().color(Style.SUCCESS).style().margin().right("0.5em"));
				break;
			case Diagnostic.INFO:
				rdf.content(htmlFactory.fontAwesome().webApplication(WebApplication.check_circle_o).getTarget().bootstrap().text().color(Style.INFO));
				break;				
			}
			if (!CoreUtil.isBlank(diagnostic.getMessage())) {
				rdf.content(StringEscapeUtils.escapeHtml4(diagnostic.getMessage()));
			}
			if (diagnostic.getChildren().isEmpty()) {
				return rdf;
			}
			if (diagnostic.getChildren().size() == 1 && CoreUtil.isBlank(diagnostic.getMessage())) {
				return renderTypedElementValue(context, typedElement, diagnostic.getChildren().iterator().next(), appConsumer, false);
			}
			Tag ul = htmlFactory.tag(TagName.ul);
			for (Diagnostic child: diagnostic.getChildren()) {
				ul.content(htmlFactory.tag(TagName.li, renderTypedElementValue(context, typedElement, child, appConsumer, false)));
			}
			if (CoreUtil.isBlank(diagnostic.getMessage())) {
				return ul;
			}
			rdf.content(ul);
			return rdf;
		}
		
		if (value instanceof Date) {
			Object formatAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.FORMAT);				
			String format = null;
			if (formatAnnotation instanceof String) {
				format = (String) formatAnnotation;
			} else if (formatAnnotation instanceof Map) {
				format = (String) ((Map<String, Object>) formatAnnotation).get(forEditing ? "edit" : "display");
			}
			if (format == null) {
				format = "yyyy-MM-dd"; // Default web format for dates.
			}
			SimpleDateFormat sdf = new SimpleDateFormat(format, getLocale(context));
			return sdf.format((Date) value);
		} else if (value instanceof Number) {
			Object formatAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.FORMAT);				
			String format = null;
			if (formatAnnotation instanceof String) {
				format = (String) formatAnnotation;
			} else if (formatAnnotation instanceof Map) {
				format = (String) ((Map<String, Object>) formatAnnotation).get(forEditing ? "edit" : "display");
			}
			if (format != null) {
				DecimalFormat df = new DecimalFormat(format,  DecimalFormatSymbols.getInstance(getLocale(context)));
				return df.format(value);
			}
		}	
		
		if (CONTENT_TYPE_TEXT_HTML.equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTENT_TYPE))) {
			return value;
		}
		
		String escapeRenderAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.ESCAPE);
		// Default "view"?
		boolean escape = "both".equals(escapeRenderAnnotation) || (forEditing ? "edit" : "view").equals(escapeRenderAnnotation);		
		return escape ? StringEscapeUtils.escapeHtml4(value.toString()) : value.toString();		
	}
	
	/**
	 * Parses/converts string value to be compatible with the typed element value type.
	 * 
	 * * Booleans - ``true`` and ``on`` are truthy values, ``false``, ``null``, ``off`` and empty string are falsey, all other values are illegal.
	 * * Date - uses ``format`` annotation, if present, to parse using {@link SimpleDateFormat}.
	 * * Number - uses ``format`` annotation, if present, to parse using {@link DecimalFormat}.
	 * * Otherwise uses context.convert() method.
	 * @param context
	 * @param typedElement
	 * @param value
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	default Object parseTypedElementValue(C context, ETypedElement typedElement, Object value) throws Exception {		
		Class<?> featureTypeInstanceClass = typedElement.getEType().getInstanceClass();
		if (featureTypeInstanceClass.isInstance(value)) {
			if (typedElement instanceof EAttribute && "true".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.PROTECTED))) {
				EAttributeProtector eAttributeProtector = context.adapt(EAttributeProtector.class);
				if (eAttributeProtector == null) {
					eAttributeProtector = EAttributeProtector.CRYPTO_PROTECTOR;
				}
				return eAttributeProtector.protect((EAttribute) typedElement, value);
			}
			
			return value;
		}
		
		if (Boolean.class == featureTypeInstanceClass || boolean.class == featureTypeInstanceClass) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			}
			
			if (value instanceof String) {
				if (CoreUtil.isBlank((String) value)) {
					return boolean.class == featureTypeInstanceClass ? false : null;
				}
				switch ((String) value) {
				case "true":
				case "on":
					return true;
				case "false":
				case "off":
					return false;
				default:
					Map<String,Object> env = new HashMap<>();
					env.put("value", value);
					env.put("type", "boolean");
					throw new IllegalArgumentException(getHTMLFactory(context).interpolate(getResourceString(context, "convertError"), env));
				}
			}
		}
		
		// Blank is treated as null for non-string values.
		if (value instanceof String) {
			String strValue = (String) value;
			if (strValue.trim().length() == 0) {
				return null;
			}
		
			if (byte[].class == featureTypeInstanceClass) {
				return Base64.getDecoder().decode(strValue);
			}
			
			if (featureTypeInstanceClass.isEnum()) {
				return featureTypeInstanceClass.getField(strValue).get(null);
			}
			
			if (CDOObject.class.isAssignableFrom(featureTypeInstanceClass) && context instanceof CDOViewContext<?, ?>) {
				return ((CDOViewContext<CDOView, ?>) context).getView().getObject(CDOIDCodec.INSTANCE.decode(context, strValue));
			}
			
			if (Date.class == featureTypeInstanceClass) {
				Object formatAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.FORMAT);				
				String format = null;
				if (formatAnnotation instanceof String) {
					format = (String) formatAnnotation;
				} else if (formatAnnotation instanceof Map) {
					format = (String) ((Map<String, Object>) formatAnnotation).get("edit");
				}
				if (format == null) {
					format = "yyyy-MM-dd"; // Default web format for dates.
				}
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				return sdf.parse(strValue);
			}
			
			if (Number.class.isAssignableFrom(featureTypeInstanceClass)) {
				Object formatAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.FORMAT);				
				String format = null;
				if (formatAnnotation instanceof String) {
					format = (String) formatAnnotation;
				} else if (formatAnnotation instanceof Map) {
					format = (String) ((Map<String, Object>) formatAnnotation).get("edit");
				}
				if (format == null) {
					if (Byte.class == featureTypeInstanceClass || byte.class == featureTypeInstanceClass) {
						return Byte.parseByte(strValue);
					}
					if (Double.class == featureTypeInstanceClass || double.class == featureTypeInstanceClass) {
						return Double.parseDouble(strValue);
					}
					if (Float.class == featureTypeInstanceClass || float.class == featureTypeInstanceClass) {
						return Float.parseFloat(strValue);
					}
					if (Integer.class == featureTypeInstanceClass || int.class == featureTypeInstanceClass) {
						return Integer.parseInt(strValue);
					}
					if (Long.class == featureTypeInstanceClass || long.class == featureTypeInstanceClass) {
						return Long.parseLong(strValue);
					}
					if (Short.class == featureTypeInstanceClass || short.class == featureTypeInstanceClass) {
						return Short.parseShort(strValue);
					}					
				} else {			
					DecimalFormat df = new DecimalFormat(format);
					if (BigDecimal.class == featureTypeInstanceClass) {
						df.setParseBigDecimal(true);
						return df.parse(strValue);
					}				
					Number parsed = df.parse(strValue);				
					if (Byte.class == featureTypeInstanceClass || byte.class == featureTypeInstanceClass) {
						return parsed.byteValue();
					}
					if (Double.class == featureTypeInstanceClass || double.class == featureTypeInstanceClass) {
						return parsed.doubleValue();
					}
					if (Float.class == featureTypeInstanceClass || float.class == featureTypeInstanceClass) {
						return parsed.floatValue();
					}
					if (Integer.class == featureTypeInstanceClass || int.class == featureTypeInstanceClass) {
						return parsed.intValue();
					}
					if (Long.class == featureTypeInstanceClass || long.class == featureTypeInstanceClass) {
						return parsed.longValue();
					}
					if (Short.class == featureTypeInstanceClass || short.class == featureTypeInstanceClass) {
						return parsed.shortValue();
					}				
					Object cp = context.convert(parsed, featureTypeInstanceClass);
					if (parsed != null && cp == null) {
						Map<String,Object> env = new HashMap<>();
						env.put("value", parsed);
						env.put("type", featureTypeInstanceClass.getName());
						throw new IllegalArgumentException(getHTMLFactory(context).interpolate(getResourceString(context, "convertError"), env));				
					}
					return cp;
				}
			}
		}
		
		Object ret = context.convert(value, featureTypeInstanceClass);
		if (value != null && ret == null) {
			Map<String,Object> env = new HashMap<>();
			env.put("value", value);
			env.put("type", featureTypeInstanceClass.getName());
			throw new IllegalArgumentException(getHTMLFactory(context).interpolate(getResourceString(context, "convertError"), env));
		}
		return ret;
	}
	
	/**
	 * Sets feature value from the context to the object. This implementation loads feature value(s) 
	 * from the {@link HttpServletRequest} parameters or from the request body parsed to json object if request content type is "application/json".
	 * @param context
	 * @param feature
	 * @throws Exception
	 */
	default void setFeatureValue(C context, T obj, EStructuralFeature feature) throws Exception {
		if (context instanceof HttpServletRequestContext) {
			HttpServletRequest request = ((HttpServletRequestContext) context).getRequest();
			JSONObject jsonData = null;
			if (CONTENT_TYPE_APPLICATION_JSON.contentEquals(request.getContentType())) {
				// Cache in request to avoid multiple parsings.
				jsonData = (JSONObject) request.getAttribute(JSON_DATA_REQUEST_ATTRIBUTE_KEY);
				if (jsonData == null) {
					try (InputStream in = request.getInputStream()) {
						jsonData = new JSONObject(new JSONTokener(in));
					}
					request.setAttribute(JSON_DATA_REQUEST_ATTRIBUTE_KEY, jsonData);
				}
			}
			String featureName = feature.getName();
			if (feature.isMany()) {
				@SuppressWarnings("unchecked")
				Collection<Object> fv = (Collection<Object>) obj.eGet(feature);
				fv.clear();
				if (jsonData == null) {
					String[] values = request.getParameterValues(featureName);
					if (values != null) {
						for (String val: values) {
							fv.add(parseTypedElementValue(context, feature, val));
						}
					}
				} else if (jsonData.has(featureName)) {
					JSONArray jva = jsonData.getJSONArray(featureName);
					for (int i=0; i < jva.length(); ++i) {
						fv.add(parseTypedElementValue(context, feature, jva.get(i)));
					}
				}
			} else {				
				Object value = null;
				if (jsonData == null) {
					value = request.getParameter(featureName);
				} else if (jsonData.has(featureName)) {
					value = jsonData.get(featureName);
				}
				if (value == null) {
					obj.eUnset(feature);
				} else {
					obj.eSet(feature, parseTypedElementValue(context, feature, value));
				}
			}
		}		
	}
	
	/**
	 * Renders true value. This implementation renders a checkmark of SUCCESS color.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Object renderTrue(C context) throws Exception {
		return getHTMLFactory(context).glyphicon(Glyphicon.ok).style().color().bootstrapColor(Color.SUCCESS);
	}
	
	/**
	 * Renders false value. This implementation renders empty string.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Object renderFalse(C context) throws Exception {
		return "";
	}

	/**
	 * Renders element documentation. Documentation is retrieved from "documentation" annotation key 
	 * and, if not found, from Ecore GenModel annotation.
	 * 
	 * For references, if documentation is not present, then the reference type documentation is returned.
	 * @param context
	 * @param modelElement
	 * @return gendoc annotation rendered as markdown to HTML or null if there is no documentation.
	 * @throws Exception
	 */
	default String renderDocumentation(C context, EModelElement modelElement) throws Exception {
		String markdown = getRenderAnnotation(context, modelElement, RenderAnnotation.DOCUMENTATION);
		
		if (markdown == null) {
			EAnnotation docAnn = modelElement.getEAnnotation(ECORE_DOC_ANNOTATION_SOURCE);
			if (docAnn==null) {
				if (modelElement instanceof EReference) {
					return renderDocumentation(context, ((EReference) modelElement).getEReferenceType());
				}
				return null;
			}
			markdown = docAnn.getDetails().get(RenderAnnotation.DOCUMENTATION.literal);
		}
		
		if (CoreUtil.isBlank(markdown)) {
			return null;
		}
		
		return markdownToHtml(context, markdown);				
	}
	
	default HTMLFactory getHTMLFactory(C context) throws Exception {
		HTMLFactory ret = context == null ? HTMLFactory.INSTANCE : context.adapt(HTMLFactory.class);
		return ret == null ? HTMLFactory.INSTANCE : ret;
	}
	
	/**
	 * @param context
	 * @param obj
	 * @return Documentation reference for EClass or null. If context is instance of {@link HttpServletRequestContext} then this implementation
	 * returns URL assuming that the documentation route path is ``/router/doc.html`` as generated by the workspace wizard by default. Override if 
	 * there is no doc route or it has a different path. 
	 * @throws Exception
	 */
	default String getEClassifierDocRef(C context, EClassifier eClassifier) throws Exception {
		if (context instanceof HttpServletRequestContext) {
			// Assuming that doc route path is /router/doc.html as generated by the workspace wizard. Override as needed.
			String contextPath = ((HttpServletRequestContext) context).getRequest().getContextPath();
			String prefix = contextPath+"/router/doc.html#router/doc-content/"+contextPath+"/router/doc/packages/session/";
			return prefix + Hex.encodeHexString(eClassifier.getEPackage().getNsURI().getBytes(/* UTF-8? */)) + "/" + eClassifier.getName() + ".html";			
		}
		return null;
	}
	
	/**
	 * Renders documentation modal if the element has documenation.
	 * @param context
	 * @param modelElement
	 * @return documentation modal or null if the element is not documented.
	 * @throws Exception 
	 */
	default Modal renderDocumentationModal(C context, EModelElement modelElement) throws Exception {
		String doc = renderDocumentation(context, modelElement);
		if (doc == null) {
			return null;
		}
		Modal docModal = getHTMLFactory(context).modal();
		if (doc.length() < 500) {
			docModal.small();				
		} else if (doc.length() > 2000) {
			docModal.large();
		}
		docModal.title(getHTMLFactory(context).tag(TagName.h4, modelElement instanceof ENamedElement ? renderNamedElementIconAndLabel(context, (ENamedElement) modelElement) : "Documentation"));
		docModal.body(getHTMLFactory(context).div(doc).addClass("markdown-body").style().background().color().value("white")); // Forcing white background to work with dark schemes - ugly but visible..
		EClass eClass = null;
		if (modelElement instanceof EClass) {
			eClass = (EClass) modelElement;
		} else if (modelElement.eContainer() instanceof EClass) {
			eClass = (EClass) modelElement.eContainer();
		}
		if (eClass != null) {
			String href = getEClassifierDocRef(context, eClass);
			if (href != null) {
				docModal.footer(getHTMLFactory(context).link(href, getResourceString(context, "informationCenter")).attribute("target", "_blank"));
			}
		}
		return docModal;
	}
	
	/**
	 * If element has documentation this method renders a question mark glyph icon with a tooltip containing the first sentence of documentation.
	 * If docModal is not null, then the cursor is set to pointer and click on the icon opens the doc modal.
	 * @param context
	 * @param modelElement
	 * @param docModal Doc modal to open on icon click. Can be null.
	 * @param superscript if true, the icon is wrapped into ``sup`` tag.
	 * @return
	 * @throws Exception
	 */
	default Tag renderDocumentationIcon(C context, EModelElement modelElement, Consumer<Object> appConsumer, boolean superscript) throws Exception {
		String doc = renderDocumentation(context, modelElement);
		if (doc == null) {
			return null;
		}
		String textDoc = Jsoup.parse(doc).text();
		String firstSentence = firstSentence(context, textDoc);					
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Tag helpTag = renderHelpIcon(context);
		if (superscript) {
			helpTag = htmlFactory.tag(TagName.sup, helpTag);
		}
		helpTag.attribute(TITLE_KEY, firstSentence);
		
		// More than one sentence - opens doc modal.
		if (!textDoc.equals(firstSentence)) {
			Modal docModal = renderDocumentationModal(context, modelElement);
			if (docModal != null) {
				appConsumer.accept(docModal);				
				helpTag.on(Event.click, "$('#"+docModal.getId()+"').modal('show')");
				helpTag.style("cursor", "pointer");
				return helpTag;
			}
		}
		
		// Opens EClass documentation, if configured.
		EClassifier eClassifier = null;
		if (modelElement instanceof EClassifier) {
			eClassifier = (EClassifier) modelElement;
		} else if (modelElement.eContainer() instanceof EClassifier) {
			eClassifier = (EClassifier) modelElement.eContainer();
		}
		if (eClassifier != null) {
			String href = getEClassifierDocRef(context, eClassifier);
			if (href != null) {
				helpTag.on(Event.click, "window.open('"+href+"', '_blank');return false;");
				helpTag.style("cursor", "pointer");
				return helpTag;
			}
		}
		
		// Shows help icon
		helpTag.style("cursor", "help");			
		return helpTag;
	}
	
	/**
	 * If EOperation has documentation this method renders a button with a question mark glyph icon with a tooltip containing the first sentence of documentation.
	 * If docModal is not null, then the cursor is set to pointer and click on the icon opens the doc modal.
	 * @param context
	 * @param modelElement
	 * @param docModal Doc modal to open on icon click. Can be null.
	 * @return
	 * @throws Exception
	 */
	default Button renderDocumentationButton(C context, EModelElement modelElement, Modal docModal) throws Exception {
		String doc = renderDocumentation(context, modelElement);
		if (doc == null) {
			return null;
		}
		String textDoc = Jsoup.parse(doc).text();
		String firstSentence = firstSentence(context, textDoc);					
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Tag helpTag = renderHelpIcon(context);
		Button button = htmlFactory.button(helpTag);
		button.attribute(TITLE_KEY, firstSentence);
		
		// More than one sentence - opens doc modal.
		if (!textDoc.equals(firstSentence) && docModal != null) {
			button.on(Event.click, "$('#"+docModal.getId()+"').modal('show')");
//			button.style("cursor", "pointer");
			return button;
		}
		
		// Opens EClass documentation, if configured.
		EClassifier eClassifier = null;
		if (modelElement instanceof EClassifier) {
			eClassifier = (EClassifier) modelElement;
		} else if (modelElement.eContainer() instanceof EClassifier) {
			eClassifier = (EClassifier) modelElement.eContainer();
		}
		if (eClassifier != null) {
			String href = getEClassifierDocRef(context, eClassifier);
			if (href != null) {
				button.on(Event.click, "window.open('"+href+"', '_blank');return false;");
//				button.style("cursor", "pointer");
				return button;
			}
		}
		
		// Shows help icon
		button.style("cursor", "help");			
		return button;
	}
		
	/**
	 * Converts markdown to HTML using {@link PegDownProcessor}.
	 * @param context
	 * @param markdown
	 * @return
	 * @throws Exception 
	 */
	default String markdownToHtml(C context, String markdown) throws Exception {		
		LinkRenderer pegDownLinkRenderer = createPegDownLinkRenderer(context);
		try {
			return new PegDownProcessor(PEGDOWN_OPTIONS).markdownToHtml(markdown, pegDownLinkRenderer);
		} catch (Exception e) {
			System.err.println("Error converting markdown to HTML: "+e);
			System.err.println("Problematic markdown:");
			System.err.println(markdown);
			System.err.println("=== End of problematic markdown ===");
			e.printStackTrace();
			return markdown;
		}
	}
	
	/**
	 * Creates link renderer. This implementation creates a renderer which opens links in new tabs
	 * @param context
	 * @return
	 * @throws Exception
	 */
	default LinkRenderer createPegDownLinkRenderer(C context) throws Exception {
		return new LinkRenderer() {
			
			@Override
			public Rendering render(AnchorLinkNode node) {
				return super.render(node).withAttribute("target", "_blank");
			}
			
			@Override
			public Rendering render(AutoLinkNode node) {
				return super.render(node).withAttribute("target", "_blank");
			}
			
			@Override
			public Rendering render(ExpLinkNode node, String text) {
				return super.render(node, text).withAttribute("target", "_blank");
			}
			
			@Override
			public Rendering render(RefLinkNode node, String url, String title, String text) {
				return super.render(node, url, title, text).withAttribute("target", "_blank");
			}
			
			@Override
			public Rendering render(WikiLinkNode wikiLinkNode) {
				return super.render(wikiLinkNode).withAttribute("target", "_blank");
			}
			
		};		
	}
	
	/**
	 * Extracts the first sentence from HTML as plain text.
	 * @param html
	 * @return
	 * @throws Exception 
	 */
	default String firstHtmlSentence(C context, String html) throws Exception {
		if (CoreUtil.isBlank(html)) {
			return "";
		}

		return firstSentence(context, Jsoup.parse(html).text());
	}

	default int getMinFirstSentenceLength() {
		return MIN_FIRST_SENTENCE_LENGTH;
	}
	
	default int getMaxFirstSentenceLength() {
		return MAX_FIRST_SENTENCE_LENGTH;
	}
	
	default String firstSentence(C context, String text) throws Exception {
		if (text == null || text.length() < getMinFirstSentenceLength()) {
			return text;
		}
		Matcher matcher = SENTENCE_PATTERN.matcher(text);		
		Z: while (matcher.find()) {
			String group = matcher.group();
			String abbreviations = getResourceString(context, "abbreviations");
			if (abbreviations != null) {
				String[] abbra = abbreviations.split("\\|");
				for (String abbr: abbra) {
					if (group.trim().endsWith(abbr)) {
						continue Z;
					}
				}
			}
			if (matcher.end() > getMinFirstSentenceLength() && matcher.end() < getMaxFirstSentenceLength()) {
				return text.substring(0, matcher.end());
			}
		}
		
		return text.length() < getMaxFirstSentenceLength() ? text : text.substring(0, getMaxFirstSentenceLength())+"...";
	}
	
//	getHTMLFactory().div(markdownToHtml(context, markdown)).addClass("markdown-body");
	
	default Object renderFirstDocumentationSentence(C context, EModelElement modelElement) throws Exception {
		Object doc = renderDocumentation(context, modelElement);
		return doc instanceof String ? firstHtmlSentence(context, (String) doc) : null;
	}
	
	/**
	 * @param context
	 * @param obj
	 * @return true if view shall be rendered in the item container. This implementation return true if <code>view-item</code> is set to true.
	 */
	default boolean isViewItem(C context, T obj) throws Exception {
		String viewItemAnnotation = getRenderAnnotation(context, obj.eClass(), RenderAnnotation.VIEW_ITEM);
		return viewItemAnnotation == null ? false : "true".equals(viewItemAnnotation);
	}
	
	/**
	 * @param context
	 * @param obj
	 * @return true if a new object view page shall be opened upon object creation. See {@link RenderAnnotation}.VIEW_ON_CREATE for details.
	 */
	default boolean isViewOnCreate(C context, T obj) throws Exception {
		String viewOnCreateAnnotation = getRenderAnnotation(context, obj.eClass(), RenderAnnotation.VIEW_ON_CREATE);
		if (viewOnCreateAnnotation != null) {
			if ("true".equals(viewOnCreateAnnotation)) {
				return true;
			}
			if ("false".equals(viewOnCreateAnnotation)) {
				return false;
			}
		}
		
		for (EStructuralFeature vf: getVisibleFeatures(context, obj, null)) {
			if (getTypedElementLocation(context, vf) != TypedElementLocation.view) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Defines where to display visible {@link EStructuralFeature} or feature link.
	 * @author Pavel Vlasov
	 *
	 */
	enum TypedElementLocation {
		/**
		 * Display as part of object view. Default for single features and operations
		 */
		view, 
		
		/**
		 * Display a link to feature view page or operation page in the left panel. 
		 */
		leftPanel,
		
		/**
		 * Display a feature or result of operation invocation in an item container below the object view. Default for many features.
		 */
		item,
		
		/**
		 * Applicable to single {@link EReference} and {@link EOperation} results. This location has the target object attributes "inlined" in the containing object 
		 * view and edit form. The inlined attributes will be categorized using the reference/operation label. For example if ``Customer`` can have one and only one ``Address`` set at ``Customer``
		 * object creation time, then the address may be inlined into the customer object. When a reference is inlined, it is not possible to change the reference target to another object 
		 * through the UI, but it is possible to view and edit it.  
		 */
		inline,
		
		/**
		 * Applicable to {@link EOperation}'s which shall be invocable, but does not need UI elements, e.g. API operations or operations invoked from custom code. 
		 */
		none
	}
		
	/**
	 * @param context
	 * @param obj
	 * @return typed element location. 
	 * This implementation returns value of {@link RenderAnnotation}.TYPED_ELEMENT_LOCATION render annotation. 
	 * If there is no annotation this method returns ``item`` for {@link EStructuralFeature}'s if <code>isMany()</code> returns true and ``view`` otherwise.
	 */
	default TypedElementLocation getTypedElementLocation(C context, ETypedElement typedElement) throws Exception {
		String typedElementLocationAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.TYPED_ELEMENT_LOCATION);
		if (typedElementLocationAnnotation == null) {
			return !(typedElement.getEType() instanceof EEnum) && typedElement.isMany() && typedElement instanceof EStructuralFeature ? TypedElementLocation.item : TypedElementLocation.view;
		}
		return TypedElementLocation.valueOf(typedElementLocationAnnotation);
	}
	
	/**
	 * Renders label for the view item, if view is rendered in an item container.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Object renderViewItemLabel(C context, T obj) throws Exception {
		return getResourceString(context, "viewItemLabel");
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @return Locale to use in resource strings. This implementation uses request locale if context is {@link HttpServletRequestContext} or default JVM locale.
	 * @throws Exception
	 */
	default Locale getLocale(C context) throws Exception {
		return context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getLocale() : Locale.getDefault(); 
	}
	
	/**
	 * Calls getResourceString(context, key, false)
	 * @throws Exception
	 */
	@Override
	default String getResourceString(C context, String key) throws Exception {
		return getResourceString(context, key, false);
	}
	
	/**
	 * If this method returns non-null value, then the master resource provider is used first to retrieve resources and 
	 * the renderer's own logic is used only if the provider doesn't contain requested resource. 
	 * 
	 * @param context
	 * @return
	 */
	default ResourceProvider<C> getMasterResourceProvider(C context) throws Exception {
		return null;
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @param key
	 * @param interpolate If true, the value of the key, if found, is interpolated using a context that resolves tokens to resource strings.
	 * @return Resource string for a given key. This implementation uses resource bundle. If property with given key is not found in the resource bundle, then
	 * this implementation reads ``<key>@`` property (property reference), e.g. ``documentation@`` for documentation. If such property is present, then a classloader
	 * resource with the name equal to the property value is loaded, if present, and stringified with {@link CoreUtil}.stringify() method. If resource reference property value ends with ``.md``,
	 * then its value is treated as markdown and is converted to HTML. Resource references and markdown conversion can be leveraged in localization of documentation
	 * resources. 
	 * @throws Exception
	 */
	default String getResourceString(C context, String key, boolean interpolate) throws Exception {
		ResourceProvider<C> master = getMasterResourceProvider(context);
		String rs = master == null ? null : master.getResourceString(context, key);
		
		if (rs == null) {
			LinkedList<Class<?>> resourceBundleClasses = getResourceBundleClasses(context);
			
			for (Class<?> rbc: resourceBundleClasses) {
				ResourceBundle rb = ResourceBundle.getBundle(rbc.getName(), getLocale(context), rbc.getClassLoader());
				if (rb.containsKey(key)) {
					rs = rb.getString(key);
					break;
				}
				
				String refKey = key + '@';
				if (rb.containsKey(refKey)) {
					String rsRef = rb.getString(refKey);
					URL rsRes = rbc.getResource(rsRef);
					if (rsRes != null) {
						rs = CoreUtil.stringify(rsRes);
						if (rsRef.endsWith(".md")) {
							rs = markdownToHtml(context, rs);
						}
						break;
					}
				}			
			}
		}
		
		if (rs != null && interpolate) {
			return getHTMLFactory(context).interpolate(rs, token -> {
				try {
					return getResourceString(context, token, true);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			});
		}
		
		return rs;
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @param key
	 * @return Resource for a given key. This implementation uses resource bundle. If property with given key is not found in the resource bundle, then
	 * this implementation reads ``<key>@`` property (property reference), e.g. ``documentation@`` for documentation. If such property is present, then a classloader
	 * resource ({@link URL}) with the name equal to the property value is returned, if present.  
	 * 
	 * Property references with ``.property``, ``.yml`` and ``.json`` extensions are handled in the following way:
	 * 
	 * * If there is ``#`` in the property value then the value after it (a fragment) is treated as a [jxpath](https://commons.apache.org/proper/commons-jxpath/) expression for yml and json and as a property name for properties and the value before the hash character is treated as resource path.
	 * * Resource is loaded and parsed by a respective parser.
	 * * If there is a fragment, then it is evaluated.
	 *  
	 * 
	 * @throws Exception
	 */
	@Override
	default Object getResource(C context, String key) throws Exception {
		ResourceProvider<C> master = getMasterResourceProvider(context);
		if (master != null) {
			Object res = master.getResource(context, key);
			if (res != null) {
				return res;
			}
		}
		
		LinkedList<Class<?>> resourceBundleClasses = getResourceBundleClasses(context);
		
		for (Class<?> rbc: resourceBundleClasses) {
			ResourceBundle rb = ResourceBundle.getBundle(rbc.getName(), getLocale(context), rbc.getClassLoader());
			if (rb.containsKey(key)) {
				return rb.getObject(key);
			}
			
			String refKey = key + '@';
			if (rb.containsKey(refKey)) {
				String rsRef = rb.getString(refKey);
				int hashIdx = rsRef.indexOf("#");				
				String cleanRsRef = hashIdx == -1 ? rsRef : rsRef.substring(0, hashIdx);
				String fragment = hashIdx == -1 ? null : rsRef.substring(hashIdx+1);
				int lastDotIdx = cleanRsRef.lastIndexOf('.');
				String extension = lastDotIdx == -1 ? null : cleanRsRef.substring(lastDotIdx + 1);
				boolean supportsFragment = "properties".equals(extension) || "yml".equals(extension) || "json".equals(extension);
				URL rsRes = rbc.getResource(supportsFragment ? cleanRsRef : rsRef);
				if (rsRes != null) {
					if (supportsFragment) {
						switch (extension) {
						case "json":
							try (InputStream is = rsRes.openStream()) {
								JSONTokener tokener = new JSONTokener(is);
								// Supports only JSON Objects
								JSONObject jsonObject = new JSONObject(tokener);
								if (fragment == null) {
									return jsonObject.toMap();
								}
								return JXPathContext.newContext(jsonObject.toMap()).getValue(fragment);
							}
						case "yml":
							try (InputStream is = rsRes.openStream()) {
								Yaml yaml = new Yaml();
								Object obj = yaml.load(is);
								if (fragment == null) {
									return obj;
								}
								return JXPathContext.newContext(obj).getValue(fragment);
							}														
						case "properties":
							try (InputStream is = rsRes.openStream()) {
								Properties properties = new Properties();
								properties.load(is);
								if (fragment == null) {
									return properties;
								}
								return properties.get(fragment);
							}							
						}
					}
					return rsRes;
				}
			}			
		}		
		
		return null;
	}
	
	/**
	 * Retrieves resource string for a named element. This method calls getResourceString() with ``<element type>.<element name>.<key>`` key. E.g. ``class.MyClass.myKey``.
	 */
	default String getResourceString(C context, ENamedElement namedElement, String key, boolean interpolate) throws Exception {
		String className = namedElement.eClass().getName();
		if (className.startsWith("E")) {
			className = className.substring(1);
		}
		return getResourceString(context, StringUtils.uncapitalize(className)+"."+namedElement.getName()+"."+key, interpolate);
	}
	
	/**
	 * Retrieves resource string for a named element. This method calls getResource() with ``<element type>.<element name>.<key>`` key. E.g. ``class.MyClass.myKey``.
	 */
	default Object getResource(C context, ENamedElement namedElement, String key) throws Exception {
		String className = namedElement.eClass().getName();
		if (className.startsWith("E")) {
			className = className.substring(1);
		}
		return getResource(context, StringUtils.uncapitalize(className)+"."+namedElement.getName()+"."+key);
	}
	
	/**
	 * @param context
	 * @return List of classes to load resource bundles from to search for resource strings. 
	 * This implementation returns list containing Renderer.class.
	 * 
	 * Subtypes may override this method to add additional bundles. 
	 * @throws Exception
	 */
	default LinkedList<Class<?>> getResourceBundleClasses(C context) throws Exception {
		LinkedList<Class<?>> ret = new LinkedList<>();
		ret.add(Renderer.class);
		return ret;
	}
	
	/**
	 * Renders object view.
	 * @param context
	 * @param obj
	 * @param featureDocModals
	 * @param appConsumer consumes create/edit/eoperation modal apps to be included on the top of the page.
	 * @return
	 * @throws Exception
	 */
	default Object renderView(C context, T obj, Consumer<Object> appConsumer) throws Exception {
		return getHTMLFactory(context).fragment(renderViewFeatures(context, obj, appConsumer), renderViewButtons(context, obj, false, appConsumer));
	}

	/**
	 * Renders view features with feature location set/defaulting to ``view``. This implementation renders them in a table or a group of tables.
	 * Features with ``category`` annotation are grouped into tables in panels by the annotation value.
	 * Panel header shows category icon if ``category.<category name>.icon`` annotation is present on the object's EClass. 
	 * Panel header text is set to the value of ``category.<category name>.label`` annotation on the object's EClass, or to the category name if this annotation is not present. 
	 * @param context
	 * @param obj
	 * @param featureDocModals
	 * @return
	 * @throws Exception
	 */
	default Object renderViewFeatures(C context, T obj, Consumer<Object> appConsumer) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Table featuresTable = htmlFactory.table();
		featuresTable.col().bootstrap().grid().col(1);
		featuresTable.col().bootstrap().grid().col(11);

		List<EStructuralFeature> viewFeatures = getVisibleFeatures(context, obj, vf -> getTypedElementLocation(context, vf) == TypedElementLocation.view);
		// TODO - add support of inlined features. 
		
		Map<String,List<EStructuralFeature>> categories = new TreeMap<>();
		Map<String,Object> categoriesIconsAndLabels = new HashMap<>();
		for (EStructuralFeature vf: viewFeatures) {
			String category = getNamedElementCategory(context, vf, viewFeatures);
			if (category == null) {
				Row fRow = featuresTable.body().row();
				Cell fLabelCell = fRow.header(renderNamedElementIconAndLabel(context, vf, viewFeatures)).style().whiteSpace().nowrap();
				Tag featureDocIcon = renderDocumentationIcon(context, vf, appConsumer, true);
				if (featureDocIcon != null) {
					fLabelCell.content(featureDocIcon);
				}
				boolean showActionButtons = false;
				if (vf instanceof EReference && ((EReference) vf).isContainment() && !vf.isMany()) {
					showActionButtons = true;
				}
				fRow.cell(renderTypedElementView(context, obj, vf, obj.eGet(vf), showActionButtons, null, null, null, appConsumer));
			} else {
				List<EStructuralFeature> categoryFeatures = categories.get(category);
				if (categoryFeatures == null) {
					categoryFeatures = new ArrayList<>();
					categories.put(category, categoryFeatures);
					categoriesIconsAndLabels.put(category, renderNamedElementCategoryIconAndLabel(context, vf, viewFeatures));
				}
				categoryFeatures.add(vf);
			}
		}
		
		if (categories.isEmpty()) {
			return featuresTable;
		}
		
		Fragment ret = htmlFactory.fragment(featuresTable);
		
		for (Entry<String, List<EStructuralFeature>> ce: categories.entrySet()) {
			Table categoryFeaturesTable = htmlFactory.table();
			categoryFeaturesTable.col().bootstrap().grid().col(1);
			categoryFeaturesTable.col().bootstrap().grid().col(11);
			for (EStructuralFeature vf: ce.getValue()) {
				Row fRow = categoryFeaturesTable.body().row();
				Cell fLabelCell = fRow.header(renderNamedElementIconAndLabel(context, vf, viewFeatures)).style().whiteSpace().nowrap();
				Tag featureDocIcon = renderDocumentationIcon(context, vf, appConsumer, true);
				if (featureDocIcon != null) {
					fLabelCell.content(featureDocIcon);
				}
				boolean showActionButtons = false;
				if (vf instanceof EReference && ((EReference) vf).isContainment() && !vf.isMany()) {
					showActionButtons = true;
				}
				fRow.cell(renderTypedElementView(context, obj, vf, obj.eGet(vf), showActionButtons, null, null, null, appConsumer));
			}
			ret.content(htmlFactory.panel(Style.DEFAULT, categoriesIconsAndLabels.get(ce.getKey()), categoryFeaturesTable, null));
		}
		return ret;
	}
	
	/**
	 * Renders view buttons. This implementation renders Edit and Delete buttons plus buttons to invoke web-operations with location ``view``
	 * and container web operations with ``feature-value`` spec equal to the containment feature of this object.
	 * @param context
	 * @param obj
	 * @param appConsumer accepts rendered apps like create/edit/eoperation to be added at the top of the page.
	 * @return
	 * @throws Exception
	 */
	default Object renderViewButtons(C context, T obj, boolean containerContext, Consumer<Object> appConsumer) throws Exception {
		Tag ret = getHTMLFactory(context).div().style().margin("5px"); 
		ret.content(renderEditButton(context, obj, true, containerContext, appConsumer));
		ret.content(renderDeleteButton(context, obj));
		for (EOperation eOperation: obj.eClass().getEAllOperations()) {
			EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
			if (eOperationTargetInfo != null 
					&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
					&& eOperationTargetInfo.getFeature() == null 
					&& eOperationTargetInfo.getFeatureValue() == null 
					&& eOperationTargetInfo.getRole() == Role.operation
					&& isVisible(context, obj, eOperation)) {
				ret.content(renderEOperationButton(context, obj, eOperation, null, null, appConsumer));
			}				
		}
		
		EObject eContainer = obj.eContainer();
		EStructuralFeature containmentFeature = obj.eContainmentFeature();
		if (eContainer != null && containmentFeature != null) {
			for (EOperation eOperation: eContainer.eClass().getEAllOperations()) {
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
				if (eOperationTargetInfo != null
						&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
						&& containmentFeature.getName().equals(eOperationTargetInfo.getFeatureValue())
						&& eOperationTargetInfo.getRole() == Role.operation
						&& isVisible(context, obj, eOperation)) {
					Map<String, String> queryParameters = new HashMap<>();
					queryParameters.put("feature", containmentFeature.getName());					
					if (obj instanceof CDOObject) {
						String formControlValue = getFormControlValue(context, obj, containmentFeature, obj, appConsumer);
						queryParameters.put("element", formControlValue);
						queryParameters.put("context-object", formControlValue);
					}
					
					Map<String, Object> vars = new HashMap<>();
					vars.put("element", obj);
					ret.content(getRenderer(eContainer).renderEOperationButton(context, eContainer, eOperation, queryParameters, vars, appConsumer));
				}				
			}
		}
		
		// Action buttons
		for (org.nasdanika.cdo.web.routes.app.Action<C, T> action: getActions(context, obj)) {
			if (action.getRole() == org.nasdanika.cdo.web.routes.app.Action.Role.action && action.getFeature() == null) {
				ret.content(renderActionButton(context, obj, action));
			}
		}			
		
		return ret;
	}
	
	/**
	 * Renders Web {@link EOperation} button
	 * @param context
	 * @param obj
	 * @param eOperation
	 * @param query
	 * @param jxPathContextVariables Variables for evaluating disabled state.
	 * @param eOperationAppConsumer is used to pass a rendered EOperation application to be added at the top of the page.
	 * @return
	 * @throws Exception
	 */
	default Fragment renderEOperationButton(
			C context, 
			T obj, 
			EOperation eOperation, 
			Map<String,String> queryParameters, 
			Map<String, Object> jxPathContextVariables,
			Consumer<Object> appConsumer) throws Exception {
		
		EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
		if (context.authorize(obj, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Fragment ret = htmlFactory.fragment(); 
			Modal docModal = renderDocumentationModal(context, eOperation);
			if (docModal != null) {
				appConsumer.accept(docModal);
			}
			Button eOperationButton = htmlFactory.button(renderNamedElementIconAndLabel(context, eOperation));
			eOperationButton.style(eOperationTargetInfo.getStyle());				
			Button documentationButton = renderDocumentationButton(context, eOperation, docModal);
			if (documentationButton == null) {
				ret.content(eOperationButton/*.style().margin().right("5px")*/);
			} else {
//				documentationButton.style().margin().right("5px");
				documentationButton.style(eOperationTargetInfo.getStyle());
				ret.content(htmlFactory.buttonGroup(eOperationButton, documentationButton));
			}
			
			String path = eOperationTargetInfo.getPath();
			
			if (queryParameters != null && !queryParameters.isEmpty()) {
				StringBuilder query = new StringBuilder();
				for (Entry<String, String> qp: queryParameters.entrySet()) {
					if (query.length()>0) {
						query.append("&");
					}
					query.append(qp.getKey()).append("=").append(URLEncoder.encode(qp.getValue(), StandardCharsets.UTF_8.name()));
				}
				path += "?" + query;
			}
			
			String guard = "";
			
			String confirm = eOperationTargetInfo.getConfirm();
			if (!CoreUtil.isBlank(confirm)) {
				Map<String, Object> env = new HashMap<>();
				env.put("object-label", renderLabel(context, obj));
				if (jxPathContextVariables != null) {
					Object element = jxPathContextVariables.get("element");
					if (element instanceof EObject) {
						env.put("element-label", getRenderer((EObject) element).renderLabel(context, (EObject) element));						
					}						
				}
				String confirmationMessage = StringEscapeUtils.escapeEcmaScript(htmlFactory.interpolate(confirm, env));			
				guard = "if (confirm('"+confirmationMessage+"')) ";
			}			

			if (getEOperationModalType(context, obj, eOperation) == ModalType.NONE) {					
				eOperationButton.on(Event.click, guard + "window.location='"+getObjectURI(context, obj)+"/"+path+"';");
			} else {
				String appId = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj)+"-eoperation-"+eOperation.getName()+"-"+htmlFactory.nextId();
				appConsumer.accept(renderEOperationModalDialogApplication(context, obj, eOperation, queryParameters, appId, appConsumer));
				eOperationButton.attribute("data-toggle", "modal").attribute("data-target", "#"+appId+"-modal");
			}
			
			// Disabled 
			boolean disabled;
			String disabledRenderAnnotation = getRenderAnnotation(context, eOperation, RenderAnnotation.DISABLED);
			if (CoreUtil.isBlank(disabledRenderAnnotation) || "false".equals(disabledRenderAnnotation)) {
				disabled = false;
			} else if ("true".equals(disabledRenderAnnotation)) {
				disabled = true;
			} else if (obj instanceof CDOObject) {
				// XPath
				JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
				if (jxPathContextVariables != null) {
					for (Entry<String, Object> ve: jxPathContextVariables.entrySet()) {
						jxPathContext.getVariables().declareVariable(ve.getKey(), ve.getValue());
					}
				}
				disabled = Boolean.TRUE.equals(jxPathContext.getValue(disabledRenderAnnotation, Boolean.class));
			} else {
				disabled = false;
			}
			eOperationButton.disabled(disabled);			
			
			return ret;
		}
		return null;		
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @param action
	 * @return
	 * @throws Exception
	 */
	default Button renderActionButton(C context, T obj, org.nasdanika.cdo.web.routes.app.Action<C, T> action) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Button ret = htmlFactory.button().style(Style.PRIMARY);
		String icon = action.getIcon();
		if (icon != null) {
			if (icon.indexOf("/") == -1) {
				ret.content(htmlFactory.span().addClass(icon).style().margin().right("0.3em"));
			} else {
				ret.content(htmlFactory.tag(TagName.img).attribute("src", icon).style().margin().right("0.3em"));
			}
		}
		
		String label = action.getLabel();
		if (label != null) {
			ret.content(label);
		}
		
		String path = action.getPath();
		if (path.endsWith("/")) {
			path += INDEX_HTML;
		}
		
		String guard = "";
		
		String confirm = action.getConfirmation();
		if (!CoreUtil.isBlank(confirm)) {
			guard = "if (confirm('"+confirm+"')) ";
		}			
		ret.on(Event.click, guard + "window.location='"+getObjectURI(context, obj)+"/"+path+"';");
		ret.disabled(!action.canExecute());	
		
		String comment = action.getComment();
		if (!CoreUtil.isBlank(comment)) {
			ret.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(comment));
		}
		
		return ret;
	}

	/**
	 * Renders edit button. 
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Button renderEditButton(C context, T obj, boolean showLabel, boolean containerContext, Consumer<Object> appConsumer) throws Exception {
		if (isEditable(context, obj, obj.eClass()) && context.authorizeUpdate(obj, null, null)) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Tag editIcon = renderEditIcon(context);
			if (showLabel) {
				editIcon.style().margin().right("5px");
			}
			Button editButton = htmlFactory.button(editIcon).style(Style.PRIMARY);
			if (showLabel) {
				editButton.content(getResourceString(context, "edit"));
			}
			wireEditButton(context, obj, editButton, containerContext, appConsumer);

			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "editTooltip"), env);
			editButton.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			return editButton;
		}
		return null;
	}
	
	/**
	 * Assigns an action to the edit button. This implementation adds onClick handler which navigates to edit page.
	 * @param feature
	 * @param idx
	 * @param editButton
	 */
	default void wireEditButton(C context, T obj, Button editButton, boolean containerContext, Consumer<Object> appConsumer) throws Exception {
		// Editor web operation
		for (EOperation eOperation: obj.eClass().getEAllOperations()) {
			EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
			if (eOperationTargetInfo != null && eOperationTargetInfo.getRole() == Role.editor) {
				if (getEOperationModalType(context, obj, eOperation) == ModalType.NONE) {					
					editButton.on(Event.click, "window.location='"+getObjectURI(context, obj)+"/"+eOperationTargetInfo.getPath()+"';");
				} else {
					String appId = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj)+"-eoperation-"+eOperation.getName()+"-"+getHTMLFactory(context).nextId();
					appConsumer.accept(renderEOperationModalDialogApplication(context, obj, eOperation, Collections.emptyMap(), appId, appConsumer));
					editButton.attribute("data-toggle", "modal").attribute("data-target", "#"+appId+"-modal");
				}					
				return;
			}				
		}
		
		if (getEditModalType(context, obj) == ModalType.NONE) {
			editButton.on(Event.click, "window.location='"+getObjectURI(context, obj)+"/edit.html';");		
		} else {
			String appId = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj)+"-edit-app-"+getHTMLFactory(context).nextId();
			appConsumer.accept(renderEditModalDialogApplication(context, obj, containerContext, appId, appConsumer));
			editButton.attribute("data-toggle", "modal").attribute("data-target", "#"+appId+"-modal");
		}
	}	
	
	/**
	 * Renders Save button.   
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Button renderSaveButton(C context, T obj) throws Exception {
		if (context.authorizeUpdate(obj, null, null)) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Button saveButton = htmlFactory.button(renderSaveIcon(context).style().margin().right("5px"), getResourceString(context, "save")).style(Style.PRIMARY);
			wireSaveButton(context, obj, saveButton);

			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "saveTooltip", false), env);
			saveButton.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			return saveButton;
		}
		return null;
	}	

	/**
	 * Assigns an action to the save button. This implementation set the button type to Submit.
	 * @param feature
	 * @param idx
	 * @param editButton
	 */
	default void wireSaveButton(C context, T obj, Button saveButton) throws Exception {
		saveButton.type(Type.SUBMIT);
	}	
	
	/**
	 * Renders Cancel button.   
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Button renderCancelButton(C context, T obj) throws Exception {
		if (context.authorizeUpdate(obj, null, null)) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Button cancelButton = htmlFactory.button(renderCancelIcon(context).style().margin().right("5px"), getResourceString(context, "cancel")).style(Style.DANGER);
			wireCancelButton(context, obj, cancelButton);

			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "cancelTooltip"), env);
			cancelButton.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			return cancelButton;
		}
		return null;
	}	

	/**
	 * Assigns an action to the cancel button. If there is "referrer" parameter, then this implementation sets onClick to navigate to the parameter name, 
	 * otherwise it sets button type to RESET.
	 *  the button type to Submit.
	 * @param feature
	 * @param idx
	 * @param editButton
	 */
	default void wireCancelButton(C context, T obj, Button cancelButton) throws Exception {
		if (context instanceof HttpServletRequestContext) {
			HttpServletRequest request = ((HttpServletRequestContext) context).getRequest();
			String referrer = request.getParameter(REFERRER_KEY);
			if (referrer == null) {
				referrer = request.getHeader("referer");
			}
			if (referrer == null) {
				referrer = getObjectURI(context, obj)+"/"+INDEX_HTML;
			}
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String cancelConfirmationMessage = StringEscapeUtils.escapeEcmaScript(htmlFactory.interpolate(getResourceString(context, "confirmCancel"), env));			
			cancelButton.on(Event.click, "if (confirm('"+cancelConfirmationMessage+"')) window.location='"+referrer+"';return false;");
			return;
		}
		cancelButton.type(Type.RESET);
	}	
	
	/**
	 * Renders edit icon. 
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderEditIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().custom("edit").getTarget();
	}

	/**
	 * Renders delete icon. Deletion means removal from containing reference and as such from the model.
	 * This implementation renders FontAwesome trash bin.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderDeleteIcon(C context) throws Exception {
		return getHTMLFactory(context).span().addClass("fas").addClass("fa-trash-alt");
	}

	/**
	 * Renders delete icon. Removal means removal from non-containing reference, so the object remains in the model.
	 * This implementation renders FontAwesome trash open bin.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderRemoveIcon(C context) throws Exception {
		return getHTMLFactory(context).span().addClass("fas").addClass("fa-trash-alt");
	}

	/**
	 * Renders clear icon. This implementation renders Bootstrap Glyphicon erase.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderClearIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.eraser).getTarget();
	}
	
	/**
	 * Renders "details" icon. This implementation renders Bootstrap Glyphicon option_horizontal.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderDetailsIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.ellipsis_h).getTarget();
	}

	/**
	 * Renders clear icon. This implementation renders Bootstrap Glyphicon erase.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderCreateIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.magic).getTarget();		
	}

	/**
	 * Renders clear icon. This implementation renders Bootstrap Glyphicon erase.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderAddIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.plus_circle).getTarget();
	}

	/**
	 * Renders clear icon. This implementation renders Bootstrap Glyphicon erase.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderCancelIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.times).getTarget();
	}
	

	/**
	 * Renders clear icon. This implementation renders Bootstrap Glyphicon erase.
	 * @param context
	 * @return
	 * @throws Exception 
	 */
	default Tag renderSaveIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().custom("save").getTarget();
	}
			
	/**
	 * Renders delete button. 
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Button renderDeleteButton(C context, T obj) throws Exception {
		if (obj.eContainer() != null && context.authorizeDelete(obj, null, null) && isEditable(context, obj, obj.eContainmentFeature())) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Button deleteButton = htmlFactory.button(renderDeleteIcon(context).style().margin().right("5px"), getResourceString(context, "delete")).style(Style.DANGER);
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "deleteTooltip"), env);
			deleteButton.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			wireDeleteButton(context, obj, deleteButton);
			
			return deleteButton;
		}
		return null;
	}

	/**
	 * Assigns action to the delete button. This implementation sets onClick handler which navigates to the delete page.
	 * @param context
	 * @param obj
	 * @param deleteButton
	 * @throws Exception
	 */
	default void wireDeleteButton(C context, T obj, Button deleteButton) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Map<String, Object> env = new HashMap<>();
		env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
		String deleteConfirmationMessage = StringEscapeUtils.escapeEcmaScript(htmlFactory.interpolate(getResourceString(context, "confirmDelete"), env));			
		// Delete through GET, not REST-compliant, but works with simple JavaScript. 
		deleteButton.on(Event.click, "if (confirm('"+deleteConfirmationMessage+"')) window.location='"+getObjectURI(context, obj)+"/delete.html';"); 
	}
	
	/**
	 * Renders feature items.
	 * @param context
	 * @param obj
	 * @param itemContainer
	 * @param featureDocModals
	 * @param appConsumer accepts create/edit/eoperation apps to be included on the top of the page.
	 * @throws Exception
	 */
	default NamedItemsContainer<?, ?> renderFeatureItemsContainer(
			C context, 
			T obj, 
			Consumer<Object> appConsumer) throws Exception {
		
		NamedItemsContainer<?, ?> ret = null;
		Object spec = getYamlRenderAnnotation(context, obj.eClass(), RenderAnnotation.FEATURE_ITEMS_CONTAINER);
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (spec instanceof String) {
			switch (((String) spec).trim()) {
			case "accordion":
				ret = htmlFactory.accordion();
				break;
			case "pills":
				ret = htmlFactory.pills();				
				break;
			case "tabs":
				ret = htmlFactory.tabs();
				break;
			}			
		} else if (spec instanceof Map) {
			for (Entry<?, ?> se: ((Map<?,?>) spec).entrySet()) {
				if (se.getKey() instanceof String) {
					switch (((String) se.getKey()).trim()) {
					case "accordion":
						ret = htmlFactory.accordion();
						// TODO - style
						break;
					case "pills":
						ret = htmlFactory.pills();
						// TODO - stacked, justified, size(s)
						break;
					case "tabs":
						ret = htmlFactory.tabs();
						// TODO - justified
						break;
					}								
				}
			}
		}
		
		boolean itemHeader = false;
		List<EStructuralFeature> itemFeatures = getVisibleFeatures(context, obj, vf -> getTypedElementLocation(context, vf) == TypedElementLocation.item);
		if (ret == null) {
			if (itemFeatures.size() > 7) {
				 ret = htmlFactory.pills()
				 	.stacked()
				 	.pillsWidth(DeviceSize.LARGE, 2)
				 	.pillsWidth(DeviceSize.MEDIUM, 3)
				 	.pillsWidth(DeviceSize.SMALL, 4)
				 	.pillsWidth(DeviceSize.EXTRA_SMALL, 5);
				 itemHeader = true;
			} else {
				ret = htmlFactory.tabs();
			}
		}

		if (isViewItem(context, obj)) {
			ret.item(renderViewItemLabel(context, obj), renderView(context, obj, appConsumer));
		}
		String contextFeatureName = null;
		if (context instanceof HttpServletRequestContext) {
			contextFeatureName = ((HttpServletRequestContext) context).getRequest().getParameter("context-feature");
		}
		for (EStructuralFeature vf: itemFeatures) {
			Tag featureDocIcon = renderDocumentationIcon(context, vf, appConsumer, true);
			Tag nameSpan = htmlFactory.span(renderNamedElementIconAndLabel(context, vf));
			if (featureDocIcon != null) {
				nameSpan.content(featureDocIcon);
			}
			
			// Applies filter-<view feature name>-<column feature name>=control value filters		
			FeatureTableFilterManager<C, T> featureTableFilterManager = vf.getEType() instanceof EClass & context instanceof HttpServletRequestContext ? new FeatureTableFilterManager<C, T>(context, vf, this, null, appConsumer) : null; 
						
			Tag itemContent = htmlFactory.div().style().margin("3px");
			if (itemHeader) {
				itemContent.content(TagName.h4.create(renderNamedElementIconAndLabel(context, vf)));
			}
			itemContent.content(renderTypedElementView(context, obj, vf, obj.eGet(vf), true, featureTableFilterManager, null, featureTableFilterManager, appConsumer));
			ret.item(
					nameSpan, 
					itemContent, 
					contextFeatureName == null ? ret.isEmpty() : vf.getName().equals(contextFeatureName));
		}	
		
		// TODO - add support of inlined features.
		
		return ret;
	}
			
	/**
	 * Renders left panel. This implementation renders jsTree and link groups for visible features with location set to ``leftPanel``.
	 * @param context
	 * @return
	 * @throws Exception
	 */
	default Object renderLeftPanel(C context, T obj) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();
		
		if (context instanceof CDOViewContext<?, ?>) {
			for (Principal principal: ((CDOViewContext<?,?>) context).getPrincipals()) {
				// User
				if (principal.getRealm().getAllUsers().contains(principal)) {
					Renderer<C, Principal> principalRenderer = getRenderer(principal);
					Object principalLink = principalRenderer.renderLink(context, principal, false);
					Tag logOutLink = htmlFactory.link(principalRenderer.getObjectURI(context, principal)+"/logout.html", principalRenderer.getResourceString(context, "logOut"));
					logOutLink.on(Event.click, "return confirm('"+getResourceString(context, "logOutConfirmation")+"');");
					ret.content(htmlFactory.tag(TagName.div, principalLink, ": ", logOutLink).style().padding("3px"));
					break;
				}
			}
		}
		
		// Tree
		EObject root = EcoreUtil.getRootContainer(obj);
		Cache cache = getCache(context);
		Object treeKey = cache.createCompositeKey("left-panel-tree", root, obj);
		Object leftPanelTree = cache.get(treeKey);
		if (leftPanelTree == null) {
			leftPanelTree = "";
			Renderer<C, EObject> rootRenderer = getRenderer(root);
			if (rootRenderer != null) {
				JsTreeNode rootNode = rootRenderer.renderJsTreeNode(context, root, obj, false);
				// Mark nodes as has-readable
				rootNode.<Boolean>accept((node, childResults) -> {
					
					if (Boolean.TRUE.equals(node.getData("readable"))) {
						return true;
					}
					for (Boolean cr: childResults) {
						if (cr) {
							node.setData("has-readable", true);
							return true;
						}
					}
					return false;
				});
				
				Predicate<JsTreeNode> filter = node -> Boolean.TRUE.equals(node.getData("readable")) || Boolean.TRUE.equals(node.getData("has-readable"));
				
				// Remove non-readable
				rootNode.<Void>accept((node, childResult) -> {
					Iterator<JsTreeNode> cit = node.children().iterator();
					while (cit.hasNext()) {
						JsTreeNode child = cit.next();
						if (!filter.test(child)) {
							cit.remove();
						}
					}
					
					return null;
				});
				
				// Find common root
				while (!Boolean.TRUE.equals(rootNode.getData("readable")) && rootNode.children().size() == 1) {
					rootNode = rootNode.children().get(0);
				}
				
				JSONArray data = new JSONArray();
				
				if (Boolean.TRUE.equals(rootNode.getData("readable"))) {
					data.put(rootNode.toJSON(filter));
				} else {
					for (JsTreeNode child: rootNode.children()) {
						data.put(child.toJSON(filter));					
					}
				}
					
				if (data.length() > 0) {
					Tag treeContainer = htmlFactory.div().id("left-panel-tree");
					Tag treeSearch = htmlFactory.div(htmlFactory.input(InputType.text).id(treeContainer.getId()+"-search").style().width("100%").placeholder(getResourceString(context, "search")));
					Map<String, Object> env = new HashMap<>();
					env.put("container-id", treeContainer.getId());
					env.put("data", data);
					
					// Collecting context menus.
					String contextMenuItems = rootNode.accept((node, childMenus) -> {
						StringBuilder cmb = new StringBuilder();
						Object ncm = node.getData("context-menu");
						if (ncm != null) {
							if (cmb.length() > 0) {
								cmb.append(",").append(System.lineSeparator());
							}
							cmb.append("\"").append(node.getId()).append("\": ").append(ncm);
						}
						for (String ccm: childMenus) {
							if (!CoreUtil.isBlank(ccm)) {
								if (cmb.length() > 0) {
									cmb.append(",").append(System.lineSeparator());
								}
								cmb.append(ccm);
							}					
						}
						return cmb.toString();
					});								
					env.put("context-menu-items", contextMenuItems);
					
					Tag treeScript = htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("jstree-initializer.js"), env));
					leftPanelTree = htmlFactory.fragment(treeSearch, treeContainer, treeScript).toString();
				}
			}
			cache.put(treeKey, leftPanelTree);
		}
		ret.content(leftPanelTree);
		
		// Left panel features
		LinkGroup linkGroup = htmlFactory.linkGroup();
		Map<String,List<EStructuralFeature>> categories = new TreeMap<>();
		Map<String,Object> categoriesIconsAndLabels = new HashMap<>();
		List<EStructuralFeature> leftPanelFeatures = getVisibleFeatures(context, obj, vf -> getTypedElementLocation(context, vf) == TypedElementLocation.leftPanel);
		if (leftPanelFeatures.isEmpty()) {
			return ret;
		}
		
		Object feature = context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getAttribute(CONTEXT_ESTRUCTURAL_FEATURE_KEY) : null;
		for (EStructuralFeature vf: leftPanelFeatures) {
			String category = getNamedElementCategory(context, vf, leftPanelFeatures);
			if (category == null) {
				linkGroup.item(renderNamedElementIconAndLabel(context, vf, leftPanelFeatures), getObjectURI(context, obj)+"/feature/"+vf.getName()+"/view.html", Style.DEFAULT, vf == feature);
			} else {
				List<EStructuralFeature> categoryFeatures = categories.get(category);
				if (categoryFeatures == null) {
					categoryFeatures = new ArrayList<>();
					categories.put(category, categoryFeatures);
					categoriesIconsAndLabels.put(category, renderNamedElementCategoryIconAndLabel(context, vf, leftPanelFeatures));
				}
				categoryFeatures.add(vf);
			}					
		}		
		
		ret.content(linkGroup);
		
		for (Entry<String, List<EStructuralFeature>> ce: categories.entrySet()) {
			LinkGroup categoryFeaturesLinkGroup = htmlFactory.linkGroup();
			for (EStructuralFeature vf: ce.getValue()) {
				categoryFeaturesLinkGroup.item(renderNamedElementIconAndLabel(context, vf, leftPanelFeatures), getObjectURI(context, obj)+"/feature/"+vf.getName()+"/view.html", Style.DEFAULT, vf == feature);
			}
			ret.content(htmlFactory.panel(Style.DEFAULT, categoriesIconsAndLabels.get(ce.getKey()), categoryFeaturesLinkGroup, null));
		}		
		
		return ret;
	}	
	
	/**
	 * Returns object to use for value sorting. This implementation uses {@link RenderAnnotation}.SORT annotation
	 * to compute the value. It returns null if there is not annotation. 
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param value
	 * @return
	 * @throws Exception
	 */
	default Object getTypedElementSortKey(C context, T obj, ETypedElement typedElement, Object value) throws Exception {
		String sortRenderAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.SORT);
		if (sortRenderAnnotation == null) {
			sortRenderAnnotation = getRenderAnnotation(context, typedElement.getEType(), RenderAnnotation.SORT);
		}			
		if (sortRenderAnnotation != null && value instanceof CDOObject) {
			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) value);
			jxPathContext.getVariables().declareVariable("owner", obj);
			return jxPathContext.getValue(sortRenderAnnotation);
		}
		return null;
	}

	/**
	 * Returns true if feature values shall be sorted. This implementation returns true if {@link RenderAnnotation}.SORT annotation
	 * is present on the feature or the feature type. 
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param featureValue
	 * @return
	 * @throws Exception
	 */
	default boolean isSortTypedElementValues(C context, T obj, ETypedElement typedElement) throws Exception {		
		String sortRenderAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.SORT);
		if (sortRenderAnnotation == null) {
			sortRenderAnnotation = getRenderAnnotation(context, typedElement.getEType(), RenderAnnotation.SORT);
		}			
		return sortRenderAnnotation != null;
	}	
	
	/**
	 * Filters table renderer listener. This implementation returns the passed listener.
	 * @return 
	 */
	default TypedElementTableRenderListener<C,T> filterTypedElementTableRendererListener(
			C context, 
			T obj, 
			ETypedElement typedElement, 
			Object typedElementValue, 
			TypedElementTableRenderListener<C,T> typedElementTableRenderListener) {

		return typedElementTableRenderListener;
	}
	
	/**
	 * Renders a view of the typed element value. 
	 * A value is rendered as a list if <code>view</code> annotation value is <code>list</code> or 
	 * if it is not present and the value is rendered in the view.
	 * <P/>
	 * If <code>view</code> annotation value is <code>table</code> or 
	 * if it is not present and the value is rendered in an item container, 
	 * then the value is rendered as a table. Object features to show and their order in the
	 * table can be defined using <code>view-features</code> annotation. Annotation value shall list 
	 * the features in the order in appearance, whitespace separated. 
	 * If this annotation is not present, all visible single-value features are shown in the order of their declaration.
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param showButtons if true, action buttons such as edit/delete/add/create/clear/select are shown if user is authorized to perform action.
	 * @param filter Used for many-value features to filter, if not null.
	 * @param comparator If not null it is used for sorting, overrides annotation-defined sorting.
	 * @param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	default Object renderTypedElementView(
			C context, 
			T obj, 
			ETypedElement typedElement, 
			Object typedElementValue, 
			boolean showActionButtons, 
			Predicate<Object> filter, 
			Comparator<Object> comparator,
			TypedElementTableRenderListener<C,T> typedElementTableRenderListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);
				
		Fragment ret = htmlFactory.fragment();
		Map<String, Object> env = new HashMap<>();
		env.put(NAME_KEY, typedElement.getName());
		if (typedElement.isMany()) {
			String viewAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.VIEW);
			boolean asTable = false;
			if (EObject.class.isAssignableFrom(typedElement.getEType().getInstanceClass())) {
				boolean isView = getTypedElementLocation(context, typedElement) == TypedElementLocation.view;
				if (viewAnnotation == null) {
					asTable = typedElement instanceof EOperation ? true : !isView;  
				} else {
					if (!isView) {
						asTable = !"list".equals(viewAnnotation);
					} else {
						asTable = "table".equals(viewAnnotation);
					}
				}
			}
			
			boolean isSort = comparator == null && isSortTypedElementValues(context, obj, typedElement);					
									
			if (asTable) {
				EClass refType = (EClass) typedElement.getEType();
				List<EStructuralFeature> tableFeatures = new ArrayList<EStructuralFeature>();
				// TODO - add support of inlined references.
				Object viewFeaturesAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.VIEW_FEATURES);
				Map<EStructuralFeature, Object> featureSpecs = new HashMap<>();
				if (viewFeaturesAnnotation == null) {
					for (EStructuralFeature sf: refType.getEAllStructuralFeatures()) {
						if (!sf.isMany() && context.authorizeRead(obj, typedElement.getName()+"/"+sf.getName(), null)) {
							tableFeatures.add(sf);
						}
					}
				} else {
					if (viewFeaturesAnnotation instanceof String) {
						for (String vf: ((String) viewFeaturesAnnotation).split("\\s+")) {
							if (!CoreUtil.isBlank(vf)) {
								EStructuralFeature sf = refType.getEStructuralFeature(vf.trim());
								if (sf != null && context.authorizeRead(obj, typedElement.getName()+"/"+sf.getName(), null)) {
									tableFeatures.add(sf);
								}
							}
						}
					} else if (viewFeaturesAnnotation instanceof List) {
						// List containing either feature names or mappings of names to feature specs
						for (Object fe: (List<Object>) viewFeaturesAnnotation) {
							if (fe instanceof String) {
								EStructuralFeature sf = refType.getEStructuralFeature(((String) fe).trim());
								if (sf != null && context.authorizeRead(obj, typedElement.getName()+"/"+sf.getName(), null)) {
									tableFeatures.add(sf);
								}								
							} else if (fe instanceof Map) {
								// Should be a single-entry map
								for (Entry<String, Object> fme: ((Map<String, Object>) fe).entrySet()) {
									EStructuralFeature sf = refType.getEStructuralFeature(fme.getKey().trim());
									if (sf != null && context.authorizeRead(obj, typedElement.getName()+"/"+sf.getName(), null)) {
										boolean visible = true;
										if (fme.getValue() instanceof Map) {
											Object vspec = ((Map<?,?>) fme.getValue()).get(RenderAnnotation.VISIBLE.literal);
											if (vspec instanceof String) {
												if ("true".equals(((String) vspec).trim())) {
													visible = true;
												} else if ("false".equals(((String) vspec).trim())) {
													visible = false;
												} else if (obj instanceof CDOObject) {
													JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
													visible = Boolean.TRUE.equals(jxPathContext.getValue((String) vspec, Boolean.class));
												}
											}
										}
										if (visible) {
											tableFeatures.add(sf);
											featureSpecs.put(sf, fme.getValue());
										}
									}																	
								}
							}
						}						
					} else if (viewFeaturesAnnotation instanceof Map) {
						for (Entry<String, Object> fme: ((Map<String, Object>) viewFeaturesAnnotation).entrySet()) {
							EStructuralFeature sf = refType.getEStructuralFeature(fme.getKey().trim());
							if (sf != null && context.authorizeRead(obj, typedElement.getName()+"/"+sf.getName(), null)) {
								boolean visible = true;
								if (fme.getValue() instanceof Map) {
									Object vspec = ((Map<?,?>) fme.getValue()).get(RenderAnnotation.VISIBLE.literal);
									if (vspec instanceof String) {
										if ("true".equals(((String) vspec).trim())) {
											visible = true;
										} else if ("false".equals(((String) vspec).trim())) {
											visible = false;
										} else if (obj instanceof CDOObject) {
											JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
											visible = Boolean.TRUE.equals(jxPathContext.getValue((String) vspec, Boolean.class));
										}
									}
								}
								if (visible) {
									tableFeatures.add(sf);
									featureSpecs.put(sf, fme.getValue());
								}
							}																	
						}						
					}
				}
								
				Map<EStructuralFeature, Modal> featureDocModals = new HashMap<>();
				for (EStructuralFeature sf: tableFeatures) {
					Modal fdm = renderDocumentationModal(context, sf);
					if (fdm != null) {
						featureDocModals.put(sf, fdm);
					}
					ret.content(fdm);
				}		
				
				Table typedElementTable = ret.getFactory().table().bordered().style().margin().bottom("5px");				
				Map<String,List<EStructuralFeature>> categories = new TreeMap<>();
				Map<String,Object> categoriesIconsAndLabels = new HashMap<>();
				List<EStructuralFeature> uncategorizedTableFeatures = new ArrayList<>();
				for (EStructuralFeature tf: tableFeatures) {
					String category = getNamedElementCategory(context, tf, tableFeatures);
					if (category == null) {
						uncategorizedTableFeatures.add(tf);
					} else {
						List<EStructuralFeature> categoryFeatures = categories.get(category);
						if (categoryFeatures == null) {
							categoryFeatures = new ArrayList<>();
							categories.put(category, categoryFeatures);
							categoriesIconsAndLabels.put(category, renderNamedElementCategoryIconAndLabel(context, tf, tableFeatures));
						}
						categoryFeatures.add(tf);
					}
				}
				
				Row headerRow = typedElementTable.header().row().style(Style.INFO);
				String typeColumnAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.TYPE_COLUMN);
				if (!CoreUtil.isBlank(typeColumnAnnotation)) {
					headerRow.header(getResourceString(context, "type"));					
				}				
				
				TypedElementTableRenderListener<C , T> filteredTableRendererListener = filterTypedElementTableRendererListener(context, obj, typedElement, typedElementValue, typedElementTableRenderListener);
				if (filteredTableRendererListener == null) {
					filteredTableRendererListener = new TypedElementTableRenderListener<>();
				}
				
				for (EStructuralFeature sf: uncategorizedTableFeatures) {					
					// TODO - colgroups, alignments, widths.
					Tag featureDocIcon = renderDocumentationIcon(context, sf, appConsumer, true);
					Cell headerCell = headerRow.header(renderNamedElementIconAndLabel(context, sf, tableFeatures));
					if (featureDocIcon != null) {
						headerCell.content(featureDocIcon);
					}
					if (!categories.isEmpty()) {
						headerCell.rowspan(2);
					}
					filteredTableRendererListener.onFeatureHeader(context, obj, typedElement, typedElementValue, sf, featureSpecs.get(sf), headerCell);
				}
					
				for (Entry<String, List<EStructuralFeature>> ce: categories.entrySet()) {
					headerRow.header(categoriesIconsAndLabels.get(ce.getKey())).colspan(ce.getValue().size()).style().text().align().center();
				}
								
				Cell actionsCell = headerRow.header(getResourceString(context, "actions")).style().text().align().center();
				if (!categories.isEmpty()) {
					actionsCell.rowspan(2);
				}
				
				if (!categories.isEmpty()) { // second row
					Row cfhr = typedElementTable.header().row().style(Style.INFO);
					for (Entry<String, List<EStructuralFeature>> ce: categories.entrySet()) {
						for (EStructuralFeature sf: ce.getValue()) {
							Tag featureDocIcon = renderDocumentationIcon(context, sf, appConsumer, true);
							Cell featureHeader = cfhr.header(renderNamedElementIconAndLabel(context, sf, tableFeatures));
							if (featureDocIcon != null) {
								featureHeader.content(featureDocIcon);
							}
							filteredTableRendererListener.onFeatureHeader(context, obj, typedElement, typedElementValue, sf, featureSpecs.get(sf), featureHeader);
							if (!categories.isEmpty()) {
								featureHeader.rowspan(2);
							}							
						}
					}
					
				}				
				
				int pos = 0;
				List<ValueEntry<EObject>> typedElementValueEntries = new ArrayList<>();
				FV: for (EObject fv: (Collection<EObject>) typedElementValue) {
					if (context.authorize(fv, StandardAction.read, null, null)) {
						// Testing readability of all single table features
						for (EStructuralFeature tf: tableFeatures) {
							if (tf instanceof EReference && !((EReference) tf).isMany()) {
								Object tfv = fv.eGet(tf);
								if (tfv != null && !context.authorize(tfv, StandardAction.read, null, null)) {
									continue FV;
								}
							}
						}
						if (filter == null || filter.test(fv)) {
							typedElementValueEntries.add(new ValueEntry<EObject>(fv, pos++, isSort ? getTypedElementSortKey(context, obj, typedElement, fv) : null));
						}
					}
				}
				
				if (comparator != null) {
					Collections.sort(typedElementValueEntries, (e1, e2) -> comparator.compare(e1.value, e2.value));
				} else if (isSort) {
					Collections.sort(typedElementValueEntries);
				}
				
				int rowCounter = 0;
				for (ValueEntry<EObject> teve: typedElementValueEntries) {
					Row vRow = typedElementTable.body().row();
					if (!CoreUtil.isBlank(typeColumnAnnotation)) {
						Map<String, Object> typeEnv = new HashMap<>();
						Renderer<C, EObject> tevr = getRenderer(teve.value);
						
						Object icon = tevr.renderIcon(context, teve.value);
						typeEnv.put("icon", icon == null ? "" : icon);
						
						EClass fvClass = teve.value.eClass();
						Object eClassIcon = tevr.renderModelElementIcon(context, fvClass);
						typeEnv.put("eclass-icon", eClassIcon == null ? "" : eClassIcon);
						
						Object eClassLabel = tevr.renderNamedElementLabel(context, fvClass);
						typeEnv.put("eclass-label", eClassLabel);
						
						Tag classDocIcon = tevr.renderDocumentationIcon(context, fvClass, null, true);		
						typeEnv.put("documentation-icon", classDocIcon == null ? "" : classDocIcon);
						
						vRow.cell(htmlFactory.interpolate(typeColumnAnnotation, typeEnv));
					}
					
					Renderer<C, EObject> renderer = typedElement instanceof EReference ? getReferenceRenderer((EReference) typedElement, teve.value) : getRenderer(teve.value);
					for (EStructuralFeature sf: uncategorizedTableFeatures) {
						Object eValue = teve.value.eGet(sf);
						Cell vCell = vRow.cell(renderer.renderTypedElementView(context, teve.value, sf, eValue, false, null, null, null, appConsumer));						
						filteredTableRendererListener.onElementFeatureCell(context, obj, typedElement, typedElementValue, teve.value, sf, featureSpecs.get(sf), eValue, vCell);					
					}
					for (List<EStructuralFeature> cv: categories.values()) {
						for (EStructuralFeature sf: cv) {
							Object eValue = teve.value.eGet(sf);
							Cell vCell = vRow.cell(renderer.renderTypedElementView(context, teve.value, sf, eValue, false, null, null, null, appConsumer));													
							filteredTableRendererListener.onElementFeatureCell(context, obj, typedElement, typedElementValue, teve.value, sf, featureSpecs.get(sf), eValue, vCell);					
						}						
					}
					Cell actionCell = vRow.cell().style().text().align().center();	
					List<EStructuralFeature> valueVisibleFeatures = getRenderer(teve.value).getVisibleFeatures(context, teve.value, null);
					actionCell.content(renderTypedElementValueButtons(context, obj, typedElement, teve.position, teve.value, tableFeatures.containsAll(valueVisibleFeatures), appConsumer));
					
					filteredTableRendererListener.onElementRow(context, obj, typedElement, typedElementValue, teve.value, rowCounter++, vRow);					
				}
				
				filteredTableRendererListener.onTable(context, obj, typedElement, typedElementValueEntries, typedElementTable);
				
				ret.content(typedElementTable);
				if (typedElement instanceof EStructuralFeature) {
//					if (typedElement instanceof EReference) {
//						for (EClass ec: getReferenceElementTypes(context, obj, (EReference) typedElement)) {
//							Object createApp = getRenderer(ec).renderCreateContainmentReferenceElementModalDialogApplication(context, obj, (EStructuralFeature) typedElement, ec);
//							ret.content(createApp);
//						}
//					}					
					ret.content(htmlFactory.div(renderFeatureViewButtons(context, obj, (EStructuralFeature) typedElement, appConsumer)).style().margin("5px"));
				}
			} else {
				Tag ul = htmlFactory.tag(TagName.ul);
				List<Object> typedElementValues = new ArrayList<>();
				for (Object fv: (Collection<Object>) typedElementValue) {
					if (context.authorize(fv, StandardAction.read, null, null)) {
						typedElementValues.add(fv);
					}
				}
				if (typedElementValues.size() == 1) {
					Object v = typedElementValues.iterator().next();
					Object renderedValue = renderTypedElementValue(context, typedElement, v, appConsumer, false);					
					
					// Non-html multi-line - wrap in PRE - use annotations to decide whether pre or not?
					if (v instanceof String) { 
						if ("textarea".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTROL))	&& !CONTENT_TYPE_TEXT_HTML.equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTENT_TYPE))) {
							renderedValue = htmlFactory.div(StringEscapeUtils.escapeHtml4((String) renderedValue)).style().whiteSpace().pre();
						} else if ("password".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.INPUT_TYPE))) {
							renderedValue = PASSWORD_MASK;
						}
					}						
					ret.content(renderedValue);

					if (typedElement instanceof EAttribute) {
						if (showActionButtons) {
							ret.content(renderFeatureValueEditButton(context, obj, (EAttribute) typedElement, 0, v));
						}												
					}
					if (showActionButtons) {
						ret.content(renderTypedElementValueButtons(context, obj, typedElement, 0, v, false, appConsumer));
					}
					
				} else if (!typedElementValues.isEmpty()) {
					int pos = 0;
					List<ValueEntry<Object>> featureValueEntries = new ArrayList<>();
					for (Object fv: typedElementValues) {
						if (filter == null || filter.test(fv)) {
							featureValueEntries.add(new ValueEntry<Object>(fv, pos++, isSort ? getTypedElementSortKey(context, obj, typedElement, fv) : null));
						}
					}
					
					if (comparator != null) {
						Collections.sort(featureValueEntries, (e1, e2) -> comparator.compare(e1.value, e2.value));
					} else if (isSort) {
						Collections.sort(featureValueEntries);
					}
					
					for (ValueEntry<Object> featureValueEntry: featureValueEntries) {
						Object renderedValue = renderTypedElementValue(context, typedElement, featureValueEntry.value, appConsumer, false);
						
						// Non-html multi-line - wrap in PRE
						if (featureValueEntry.value instanceof String) { 
							if ("textarea".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTROL))	&& !CONTENT_TYPE_TEXT_HTML.equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTENT_TYPE))) {
								renderedValue = htmlFactory.div(StringEscapeUtils.escapeHtml4((String) renderedValue)).style().whiteSpace().pre();
							} else if ("password".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.INPUT_TYPE))) {
								renderedValue = PASSWORD_MASK;
							}
						}						
						
						Fragment liFragment = ret.getFactory().fragment(renderedValue);
						if (typedElement instanceof EAttribute) {
							if (showActionButtons) {
								liFragment.content(renderFeatureValueEditButton(context, obj, (EAttribute) typedElement, featureValueEntry.position, featureValueEntry.value));
							}												
						}
						if (showActionButtons) {
							liFragment.content(renderTypedElementValueButtons(context, obj, typedElement, featureValueEntry.position, featureValueEntry.value, false, appConsumer));
						}
						ul.content(htmlFactory.tag(TagName.li, liFragment).style().margin().bottom("3px"));
					}
					ret.content(ul);
				}
				if (showActionButtons && typedElement instanceof EStructuralFeature) {
					Object featureViewButtons = renderFeatureViewButtons(context, obj, (EStructuralFeature) typedElement, appConsumer);
					if (typedElement.isMany()) {
						featureViewButtons = TagName.div.create(featureViewButtons).style().margin().top("5px"); // Many feature - display buttons below.
					}
					ret.content(featureViewButtons);							
				}
			}
		} else {
			Object renderedValue = renderTypedElementValue(context, typedElement, typedElementValue, appConsumer, false);
			
			// Non-html multi-line - wrap in PRE
			if (typedElementValue instanceof String) { 
				if ("textarea".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTROL))	&& !CONTENT_TYPE_TEXT_HTML.equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTENT_TYPE))) {
					renderedValue = htmlFactory.div(StringEscapeUtils.escapeHtml4((String) renderedValue)).style().whiteSpace().pre();
				} else if ("password".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.INPUT_TYPE))) {
					renderedValue = PASSWORD_MASK;
				}
			}						
			
			ret.content(renderedValue);
			if (showActionButtons) {
				ret.content(renderTypedElementValueButtons(context, obj, typedElement, -1, typedElementValue, false, appConsumer));
			}						
		}
		return ret;
	}

	/**
	 * Renders a button to add a new value to the feature, maybe by creating one. 
	 * @param context
	 * @param obj
	 * @param feature
	 * @return
	 * @throws Exception
	 */
	default Object renderFeatureViewButtons(C context, T obj, EStructuralFeature feature, Consumer<Object> appConsumer)	throws Exception {
		Fragment ret = getHTMLFactory(context).fragment(); 
		ret.content(renderFeatureAddButton(context, obj, feature, appConsumer));
		for (EOperation eOperation: obj.eClass().getEAllOperations()) {
			EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
			if (eOperationTargetInfo != null
					&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
					&& feature.getName().equals(eOperationTargetInfo.getFeature())
					&& eOperationTargetInfo.getRole() == Role.operation
					&& isVisible(context, obj, eOperation)) {
				ret.content(renderEOperationButton(context, obj, eOperation, Collections.singletonMap("feature", feature.getName()), null, appConsumer));
			}				
		}
		
		// Action buttons
		for (org.nasdanika.cdo.web.routes.app.Action<C, T> action: getActions(context, obj)) {
			if (action.getRole() == org.nasdanika.cdo.web.routes.app.Action.Role.action && action.getFeature() == feature) {
				ret.content(renderActionButton(context, obj, action));
			}
		}			
		
		return ret;
	}
	
	/**
	 * Renders a button to add a new value to the feature, maybe by creating one. 
	 * @param context
	 * @param obj
	 * @param feature
	 * @return
	 * @throws Exception
	 */
	default Button renderFeatureAddButton(C context, T obj, EStructuralFeature feature, Consumer<Object> appConsumer) throws Exception {
		if (feature.isChangeable() && isEditable(context, obj, feature) && context.authorizeCreate(obj, feature.getName(), null)) { // Adding to a reference is considered create.
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, feature.getName());
			
			Button addButton;
			if (feature instanceof EReference && ((EReference) feature).isContainment()) {
				String tooltip = htmlFactory.interpolate(getResourceString(context, "createTooltip"), env);
				Tag icon = renderCreateIcon(context).style().margin().right("5px");
				addButton = htmlFactory.button(icon, getResourceString(context, "create"))
						.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
				
			} else if (feature instanceof EAttribute) {
				Tag icon = renderAddIcon(context).style().margin().right("5px");
				addButton = htmlFactory.button(icon, getResourceString(context, "add"));
			} else {
				String tooltip = htmlFactory.interpolate(getResourceString(context, "selectTooltip"), env);
				Tag icon = renderAddIcon(context).style().margin().right("5px");
				addButton = htmlFactory.button(icon, getResourceString(context, "select"))
						.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			}

			addButton.style(Style.PRIMARY).style().margin().left("5px");
			wireFeatureAddButton(context, obj, feature, addButton, appConsumer);
			return addButton;
		}
		return null;
	}	

	/**
	 * Assigns an action to the button. For containment references this feature invokes getFeatureElementTypes() and creates a drop-down button if there is more than one type. 
	 * For other features it adds onClick handler which navigates to add page.
	 * If the feature supports multiple object types which can be added to it, use {@link Button}.item() method to
	 * create a drop-down button with multiple add handlers.
	 * @param context
	 * @param obj
	 * @param feature
	 * @return
	 * @throws Exception
	 */
	default void wireFeatureAddButton(C context, T obj, EStructuralFeature feature, Button addButton, Consumer<Object> appConsumer) throws Exception {
		// TODO - four types of items
		// - classes
		// - template objects
		// - builder eoperations
		// - dividers
		// collect them all as Consumer<Button> and then apply to the button itself if single, or to items is multiple - check size from the consumers.
		
		String objectURI = getObjectURI(context, obj);	
		addButton.type(Type.BUTTON); // No submitting.
		
		List<Task> wirerers = new ArrayList<>();
		
		if (feature instanceof EReference && ((EReference) feature).isContainment()) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			List<EClass> featureElementTypes = new ArrayList<>();
			
			// Classes			
			for (EClass ec: getReferenceElementTypes(context, obj, (EReference) feature)) {
				String qualifier = feature.getName()+"/"+ec.getName();
				if (feature.getEContainingClass().getEPackage() != ec.getEPackage()) {
					qualifier += "@"+ec.getEPackage().getNsURI();
				}
				if (context.authorizeCreate(obj, qualifier, null)) {
					featureElementTypes.add(ec);
				}
			}
			
			for (EClass featureElementType: featureElementTypes) {
				wirerers.add(() -> {
					String encodedPackageNsURI = Hex.encodeHexString(featureElementType.getEPackage().getNsURI().getBytes(/* StandardCharsets.UTF_8.name()? */));
					Renderer<C, EObject> fetr = getRenderer(featureElementType);
					String createURL = objectURI+"/reference/"+feature.getName()+"/create/"+encodedPackageNsURI+"/"+featureElementType.getName()+EXTENSION_HTML;
					if (fetr.getCreateModalType(context) == ModalType.NONE) {
						if (wirerers.size() == 1) {
							addButton.on(Event.click, "window.location='"+createURL+"';");
						} else {
							addButton.item(htmlFactory.link(createURL, fetr.renderNamedElementIconAndLabel(context, featureElementType)));
						}
					} else {
						String appId = CDOIDCodec.INSTANCE.encode(context, ((CDOObject) obj)) + "-" + feature.getName() + "-create-" + featureElementType.getName()+ "-" + htmlFactory.nextId(); 
						appConsumer.accept(fetr.renderCreateContainmentReferenceElementModalDialogApplication(context, obj, feature, featureElementType, appId, appConsumer));
						UIElement<?> modalToggle;
						if (wirerers.size() == 1) {
							modalToggle = addButton;
						} else {
							modalToggle = htmlFactory.link("#", fetr.renderNamedElementIconAndLabel(context, featureElementType));
							addButton.item(modalToggle);
						}						
						modalToggle.attribute("data-toggle", "modal").attribute("data-target", "#"+appId+"-modal");
					}					
				});
				
			}
			
			// Template objects
			
			// Factory operations
			boolean createFactoryDivider = !wirerers.isEmpty();
			for (EOperation eOperation: obj.eClass().getEAllOperations()) {				
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
				if (eOperationTargetInfo != null
						&& feature.getName().equals(eOperationTargetInfo.getFeature())
						&& eOperationTargetInfo.getRole() == Role.factory
						&& isVisible(context, obj, eOperation)
						&& context.authorize(obj, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
					
					if (createFactoryDivider) {
						wirerers.add(() -> addButton.divider());
						createFactoryDivider = false;
					}
					
					wirerers.add(() -> {
						if (getEOperationModalType(context, obj, eOperation) == ModalType.NONE) {
							String webOperationURL = getObjectURI(context, obj)+"/"+eOperationTargetInfo.getPath();
							if (wirerers.size() == 1) {
								addButton.on(Event.click, "window.location='"+webOperationURL+"';");
							} else {
								addButton.item(htmlFactory.link(webOperationURL, renderNamedElementIconAndLabel(context, eOperation)));
							}
						} else {
							String appId = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj)+"-eoperation-"+eOperation.getName()+"-"+htmlFactory.nextId();
							appConsumer.accept(renderEOperationModalDialogApplication(context, obj, eOperation, Collections.emptyMap(), appId, appConsumer));
							UIElement<?> modalToggle;
							if (wirerers.size() == 1) {
								modalToggle = addButton;
							} else {
								modalToggle = htmlFactory.link("#", renderNamedElementIconAndLabel(context, eOperation));
								addButton.item(modalToggle);
							}						
							modalToggle.attribute("data-toggle", "modal").attribute("data-target", "#"+appId+"-modal");
						}					
					});
				}				
			}						
		} else if (feature instanceof EAttribute) {
			wirerers.add(() -> {
				addButton.on(Event.click, "window.location='"+objectURI+"/attribute/"+feature.getName()+"/add.html';"); // TODO - modal support.
			});
		} else {
			wirerers.add(() -> {
				addButton.on(Event.click, "window.location='"+objectURI+"/feature/"+feature.getName()+"/select.html';"); // TODO - modal support
			});
		}
		
		for (Task wirerer: wirerers) {
			wirerer.execute();
		}
		if (wirerers.isEmpty()) {
			addButton.disabled();
		}
	}	
	
	/**
	 * Returns a list of {@link EClass}'es which can be instantiated and instances can be added as elements to the specified feature.
	 * This implementation reads element types from ``element-types`` annotation. The list of element types shall be space-separated. Elements shall be in
	 * the following format: ``<eclass name>[@<epackage ns uri>]``. EPackage namespace URI part can be omitted if the class is in the same package with the 
	 * feature's declaring EClass. If the annotation starts with ``#`` then the rest of it is considered as a comment. This can be used to clearly
	 * specify an empty list of element types, e.g. if ``factory`` EOperations or template objects are used to populate the references.
	 *   
	 * If there is no ``element-types`` annotation, this implementation returns a list of all concrete classes from the session package registry which are compatible with the feature type.
	 * @param context
	 * @param obj
	 * @param feature
	 * @return
	 * @throws Exception
	 */
	default List<EClass> getReferenceElementTypes(C context, T obj, EReference reference) throws Exception {
		List<EClass> ret = new ArrayList<>();
		String elementTypesAnnotation = getRenderAnnotation(context, reference, RenderAnnotation.ELEMENT_TYPES);
		if (elementTypesAnnotation == null) {
			if (context instanceof CDOViewContext) {
				@SuppressWarnings("unchecked")
				Registry ePackageRegistry = ((CDOViewContext<CDOView, ?>) context).getView().getSession().getPackageRegistry();
				for (String nsURI: ePackageRegistry.keySet()) {			
					EPackage ePackage = ePackageRegistry.getEPackage(nsURI);					
					if (ePackage!=null) {
						for (EClassifier ec: ePackage.getEClassifiers()) {
							if (ec instanceof EClass) {
								EClass eClass = (EClass) ec;
								EClass eReferenceType = reference.getEReferenceType();
								if (!eClass.isAbstract() && !eClass.isInterface() && eReferenceType.isSuperTypeOf(eClass)) {
									// Testing for generic types compatibility
									if (eReferenceType.isInstance(eClass.getEPackage().getEFactoryInstance().create(eClass))) {
										ret.add(eClass);
									}
								}
							}
						}
					}
				}
			}
		} else if (!elementTypesAnnotation.startsWith("#")) {
			for (String etSpec: elementTypesAnnotation.split("\\s+")) {
				if (!CoreUtil.isBlank(etSpec)) {
					int atIdx = etSpec.indexOf("@");
					if (atIdx == -1) {
						EClassifier eClassifier = reference.getEContainingClass().getEPackage().getEClassifier(etSpec.trim());
						if (eClassifier instanceof EClass) {
							ret.add((EClass) eClassifier);
						}
					} else if (context instanceof CDOViewContext) {
						@SuppressWarnings("unchecked")
						EPackage ePackage = ((CDOViewContext<CDOView, ?>) context).getView().getSession().getPackageRegistry().getEPackage(etSpec.substring(atIdx+1).trim());
						if (ePackage != null) {
							EClassifier eClassifier = ePackage.getEClassifier(etSpec.trim());
							if (eClassifier instanceof EClass) {
								ret.add((EClass) eClassifier);
							}							
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Renders delete button for feature value.
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param idx
	 * @param value
	 * @return
	 * @throws Exception
	 */
	default Button renderTypedElementValueDeleteButton(C context, T obj, ETypedElement typedElement, int idx, Object value) throws Exception {
		boolean authorized;
		if (typedElement instanceof EStructuralFeature && !(((EStructuralFeature) typedElement).isChangeable() && isEditable(context, obj, typedElement))) {
			return null;
		}
		boolean isDelete = typedElement instanceof EOperation || typedElement instanceof EReference && ((EReference) typedElement).isContainment();
		if (value instanceof EObject && isDelete) {
			// Deletion from the repository.
			authorized = context.authorizeDelete(value, null, null); 
		} else {
			// Removal from the feature.
			authorized = context.authorizeDelete(obj, typedElement.getName(), null);
		}
		if (authorized) {
			HTMLFactory htmlFactory = getHTMLFactory(context);
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, Jsoup.parse(renderNamedElementLabel(context, typedElement).toString()).text());
			env.put("element", value);
			if (value instanceof EObject) {
				Renderer<C, EObject> vr = getRenderer((EObject) value);
				if (vr != null) {
					Object vLabel = vr.renderLabel(context, (EObject) value);
					if (vLabel != null) {
						env.put("element", Jsoup.parse(vLabel.toString()).text());
					}
				}
			}
			String tooltipResourceString;
			if (idx == -1) {
				tooltipResourceString = getResourceString(context, "clearTooltip");
			} else {
				tooltipResourceString = getResourceString(context, isDelete ? "deleteTooltip" : "removeTooltip");
			}
			String tooltip = htmlFactory.interpolate(tooltipResourceString, env);
	
			// Again, deletion through GET, not REST-compliant, but JavaScript part is kept simple.
			Button deleteButton = htmlFactory.button(idx == -1 ? renderClearIcon(context) : (isDelete ? renderDeleteIcon(context) : renderRemoveIcon(context)))
					.style(Style.DANGER)
//					.style().margin().left("5px")
					.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			wireTypedElementValueDeleteButton(context, obj, typedElement, idx, value, deleteButton);
			return deleteButton;
		}
		return null;
	}

	/**
	 * Assigns an action to the button. This implementation adds onClick handler which navigates to delete page.
	 * @param typedElement
	 * @param idx
	 * @param editButton
	 */
	default void wireTypedElementValueDeleteButton(C context, T obj, ETypedElement typedElement, int idx, Object value, Button deleteButton) throws Exception {
		Map<String, Object> env = new HashMap<>();
		env.put(NAME_KEY, Jsoup.parse(renderNamedElementLabel(context, typedElement).toString()).text());
		env.put("element", value);
		if (value instanceof EObject) {
			Renderer<C, EObject> vr = getRenderer((EObject) value);
			if (vr != null) {
				Object vLabel = vr.renderLabel(context, (EObject) value);
				if (vLabel != null) {
					env.put("element", Jsoup.parse(vLabel.toString()).text());
				}
			}
		}
		boolean isDelete = typedElement instanceof EOperation || typedElement instanceof EReference && ((EReference) typedElement).isContainment();
		String confirmationResourceString;
		if (idx == -1) {
			confirmationResourceString = getResourceString(context, "confirmClear");
		} else {
			confirmationResourceString = getResourceString(context, isDelete ? "confirmDelete" : "confirmRemove");
		}
		String deleteConfirmationMessage = StringEscapeUtils.escapeEcmaScript(getHTMLFactory(context).interpolate(confirmationResourceString, env));
		String deleteLocation;
		if (value instanceof EObject && isDelete) {
			Renderer<C, EObject> renderer = typedElement instanceof EReference ? getReferenceRenderer((EReference) typedElement, (EObject) value) : getRenderer((EObject) value);
			String objectURI = renderer.getObjectURI(context, (EObject) value);
			deleteLocation = objectURI+"/delete.html";
		} else if (idx == -1) {
			deleteLocation = getObjectURI(context, obj)+"/feature/"+typedElement.getName()+"/delete.html";
		} else {
			deleteLocation = getObjectURI(context, obj)+"/feature/"+typedElement.getName()+"/"+idx+"/delete.html";			
		}
		deleteButton.on(Event.click, "if (confirm('"+deleteConfirmationMessage+"')) window.location='"+deleteLocation+"';");
	}

	/**
	 * Renders edit button for feature value
	 * @param context
	 * @param feature
	 * @param idx Value index, shall be -1 for single-value features.
	 * @return
	 * @throws Exception
	 */
	default Button renderFeatureValueEditButton(C context, T obj, EStructuralFeature feature, int idx, Object value) throws Exception {		
		if (context.authorizeUpdate(obj, feature.getName(), null)) {
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, feature.getName());
			HTMLFactory htmlFactory = getHTMLFactory(context);
			String tooltip = htmlFactory.interpolate(getResourceString(context, idx == -1 ? "selectTooltip" : "editTooltip"), env);
			Button editButton = htmlFactory.button(renderEditIcon(context))
				.style(Style.PRIMARY)
				.style().margin().left("5px")
				.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			wireFeatureValueEditButton(context, obj, feature, idx, value, editButton); 
			return editButton;
		}
		return null;
	}

	/**
	 * Assigns an action to the button. This implementation adds onClick handler which navigates to edit page.
	 * @param feature
	 * @param idx
	 * @param editButton
	 */
	default void wireFeatureValueEditButton(C context, T obj, EStructuralFeature feature, int idx, Object value, Button editButton) throws Exception {
		String objURI = getObjectURI(context, obj);
		if (idx == -1) {
			editButton.on(Event.click, "window.location='"+objURI+"/feature/"+feature.getName()+"/edit.html'");
		} else {
			editButton.on(Event.click, "window.location='"+objURI+"/feature/"+feature.getName()+"/"+idx+"/edit.html'");			
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param idx
	 * @param value
	 * @param fullView true when invoked for table row and all visible features of the object are already present in the table, i.e. value view page doesn't add any information
	 * and doesn't have to be rendered.
	 * @return
	 * @throws Exception
	 */
	default Object renderTypedElementValueButtons(C context, T obj, ETypedElement typedElement, int idx, Object value, boolean fullView, Consumer<Object> appConsumer) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();

		if (value instanceof EObject) {
			EObject eObjectValue = (EObject) value;
			boolean isContained = typedElement instanceof EReference && ((EReference) typedElement).isContainment();
			// View (...) button is shown only if there is no edit button (not contained) or not all the features are shown (e.g. an object with "many" references).
			if (!isContained || !fullView) {
				ret.content(renderTypedElementValueViewButton(context, obj, typedElement, idx, eObjectValue));
			}
			// Avoiding locking issues by staying within the object containment tree. 
			if (isContained) {
				ret.content(getRenderer(eObjectValue).renderEditButton(context, eObjectValue, false, true, appConsumer));
			}
		}
		
		if (value != null) {
			ret.content(renderTypedElementValueDeleteButton(context, obj, typedElement, idx, value));
		}
		
		if (value instanceof EObject) {
			EObject eObjectValue = (EObject) value;
			Renderer<C, EObject> valueRenderer = getRenderer(eObjectValue);
			for (EOperation eOperation: eObjectValue.eClass().getEAllOperations()) {
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
				if (eOperationTargetInfo != null
						&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
						&& eOperationTargetInfo.getFeature() == null 
						&& eOperationTargetInfo.getFeatureValue() == null
						&& eOperationTargetInfo.getRole() == Role.operation
						&& isVisible(context, obj, eOperation)) {
					ret.content(valueRenderer.renderEOperationButton(context, eObjectValue, eOperation, null, null, appConsumer));
				}				
			}
		}
		
		if (typedElement instanceof EStructuralFeature) {
			// ??? - ret.content(renderFeatureViewButtons(context, obj, (EReference) typedElement));
			for (EOperation eOperation: obj.eClass().getEAllOperations()) {
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
				if (eOperationTargetInfo != null
						&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
						&& typedElement.getName().equals(eOperationTargetInfo.getFeatureValue())
						&& eOperationTargetInfo.getRole() == Role.operation
						&& isVisible(context, obj, eOperation)) {
					Map<String,String> queryParameters = new HashMap<>();
					queryParameters.put("feature", typedElement.getName());					
					if (idx != -1) {
						queryParameters.put("position", String.valueOf(idx));
					}
					if (value instanceof CDOObject) {
						queryParameters.put("element", getFormControlValue(context, obj, typedElement, value, appConsumer));
					}
					
					Map<String, Object> vars = new HashMap<>();
					vars.put("element", value);
					vars.put("position", idx);
					ret.content(renderEOperationButton(context, obj, eOperation, queryParameters, vars, appConsumer));
				}				
			}
		}
				
//		if (typedElement instanceof EReference) {
//			if (((EReference) typedElement).isContainment()) {
//				ret.content(renderFeatureViewButtons(context, obj, (EReference) typedElement));
//				if (value != null) {
//					ret.content(renderTypedElementValueDeleteButton(context, obj, typedElement, -1, value));
//				}
//			} else {
//				ret.content(renderFeatureValueEditButton(context, obj, (EReference) typedElement, -1, value));
//				ret.content(renderTypedElementValueDeleteButton(context, obj, typedElement, -1, value));
//			}
//		} else {
//			if (value instanceof EObject) {
//				ret.content(renderTypedElementValueViewButton(context, obj, typedElement, idx, (EObject) value));
//			}
//			if (value != null) {
//				ret.content(renderTypedElementValueDeleteButton(context, obj, typedElement, idx, value));
//			}			
//		}		
				
		return ret;
	}
	
	
	/**
	 * Renders button which navigates to the value details page.
	 * @param context
	 * @param typedElement
	 * @param idx Value index, shall be -1 for single-value features.
	 * @return
	 * @throws Exception
	 */
	default Button renderTypedElementValueViewButton(C context, T obj, ETypedElement typedElement, int idx, EObject value) throws Exception {		
		if (context.authorizeRead(value, null, null)) {
			Map<String, Object> env = new HashMap<>();
			Renderer<C, EObject> renderer = typedElement instanceof EReference ? getReferenceRenderer((EReference) typedElement, value) : getRenderer(value);
			env.put(NAME_KEY, renderer.renderLabel(context, value));				
			HTMLFactory htmlFactory = getHTMLFactory(context);
			String tooltip = htmlFactory.interpolate(getResourceString(context, "viewTooltip"), env);
			Button viewButton = htmlFactory.button(renderDetailsIcon(context))
				.style(Style.PRIMARY)
				.style().margin().left("5px")
				.attribute(TITLE_KEY, StringEscapeUtils.escapeHtml4(tooltip));
			
			wireTypedElementValueViewButton(context, obj, typedElement, idx, value, viewButton); 
			return viewButton;
		}
		return null;
	}

	/**
	 * Assigns an action to the button. This implementation adds onClick handler which navigates to the value object page.
	 * @param typedElement
	 * @param idx
	 * @param editButton
	 * @throws Exception 
	 */
	default void wireTypedElementValueViewButton(C context, T obj, ETypedElement typedElement, int idx, EObject value, Button viewButton) throws Exception {
		Renderer<C, EObject> renderer = typedElement instanceof EReference ? getReferenceRenderer((EReference) typedElement, value) : getRenderer(value);
		viewButton.on(Event.click, "window.location='"+renderer.getObjectURI(context, value)+"/index.html'");
	}
	
	// Forms rendering 

	/**
	 * 
	 * @param obj
	 * @return A list of structural features to include into the object edit form. RenderAnnotation.EDITABLE annotation value
	 * defines feature editability.  
	 * @throws Exception 
	 */
	default List<EStructuralFeature> getEditableFeatures(C context, T obj) throws Exception {
		List<EStructuralFeature> ret = new ArrayList<>();
		for (EStructuralFeature vsf: getVisibleFeatures(context, obj, vf -> vf.isChangeable() && context.authorizeUpdate(obj, vf.getName(), null))) {
			String eav = getRenderAnnotation(context, vsf, RenderAnnotation.EDITABLE);
			if (CoreUtil.isBlank(eav)) {
				if (getTypedElementLocation(context, vsf) == TypedElementLocation.view && !(vsf instanceof EReference && ((EReference) vsf).isContainment())) {
					ret.add(vsf);
				}
			} else if (isEditable(context, obj, vsf)) {
				ret.add(vsf);
			}
		}
		return ret;
	}
	
	/**
	 * Returns typed element value to be used in form controls like input, select, e.t.c.
	 * This implementation returns hashed password for password fields, name for enums and {@link CDOID} encoded with {@link CDOIDCodec} for {@link CDOObject}'s.
	 * For all other values it returns HTML-escaped result of ``renderFeatureValue()``.
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	default String getFormControlValue(C context, T obj, ETypedElement typedElement, Object value, Consumer<Object> appConsumer) throws Exception {
		if ("password".equals(getRenderAnnotation(context, typedElement, RenderAnnotation.INPUT_TYPE)) && value instanceof String) {
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			md.update(typedElement.getName().getBytes(UTF_8)); // Seed			
			md.update((byte) 0); // Separator
			md.update(((String) value).getBytes(UTF_8));
			return Base64.getEncoder().encodeToString(md.digest()).substring(4);
		}
		
		if (value == null) {
			return "";
		}
		
		if (value.getClass().isEnum()) {
			return ((Enum<?>) value).name();
		} 
		
		if (value instanceof CDOObject) {
			return CDOIDCodec.INSTANCE.encode(context, ((CDOObject) value).cdoID());
		}
		
		if (value instanceof Boolean) {
			return Boolean.TRUE.equals(value) ? "true" : ""; // Is this the correct behavior?
		}
		
		if (value instanceof Enum) {
			return ((Enum<?>) value).name();
		}	
		
		Object rfv = renderTypedElementValue(context, typedElement, value, appConsumer, true);
		return rfv == null ? "" : rfv.toString();						
	}
	
	// TODO - placeholder - might be an implicit default, placeholder selector
	
	/**
	 * Renders control for {@link ETypedElement}, e.g. input, select, or text area.
	 * 
	 * Annotations:
	 * 
	 * * ``control`` - defaults to input for attributes and multi-value features and select for references.
	 *     * input (default),
	 *     * select
	 *     * textarea. If ``content-type`` annotation is set to ``text/html`` then the textarea is initialized with [TinyMCE](https://www.tinymce.com) editor.      
	 * * ``input-type`` - for ``input`` control - one of {@link HTMLFactory.InputType} values. Checkbox for booleans and multi-value features, text otherwise.
	 * * ``choice-tree`` - if value is ``true``, for radios and checkboxes choices are represented according to their containment hierarchy in the model. If value is ``reference-nodes``, then containing references are shown as nodes in the tree.
	 * 
	 * Control can be conditionally or unconditionally disabled - see RenderAnnotation.DISABLED for details.
	 *  
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @return Null for checkboxes and radios - they are added directly to the fieldContainer. Control to add to a field group otherwise.
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	default <TE extends ETypedElement> UIElement<?> renderTypedElementControl(
			C context, 
			T contextObject, 
			TE typedElement,
			Collection<TE> typedElements,
			Object value,
			FieldContainer<?> fieldContainer, 
			List<ValidationResult> validationResults,
			boolean helpTooltip,
			FormRenderingListener<C,T, TE> formRenderingListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		FormRenderingListener<C,T, TE> theFormRenderingListener = formRenderingListener == null ? FormRenderingListener.nopListener() : formRenderingListener; // so no need to check for null.

		String controlTypeStr = getRenderAnnotation(context, typedElement, RenderAnnotation.CONTROL);
		TagName controlType = controlTypeStr == null ? null : TagName.valueOf(controlTypeStr); 
		Class<?> instanceClass = typedElement.getEType().getInstanceClass();
		boolean isEObjectInstanceClass = EObject.class.isAssignableFrom(instanceClass) && !instanceClass.isEnum();
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (controlType == null) {
			if (typedElement.isMany()) {
				controlType = TagName.input;
			} else if (typedElement instanceof EAttribute || typedElement instanceof EParameter) {		
				controlType = instanceClass.isEnum() || isEObjectInstanceClass ? TagName.select : TagName.input;
			} else if (((EReference) typedElement).isContainment()) {				
				// Link and create button.
				Well well = htmlFactory.well(
						renderTypedElementValue(context, typedElement, value, appConsumer, false), 
						renderFeatureViewButtons(context, contextObject, (EStructuralFeature) typedElement, appConsumer)).small();
				return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, well);
			} else {
				controlType = TagName.select;
			}
		}
		
		Map<String, Object> controlConfiguration = new HashMap<>();
		Object controlConfigurationYaml = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.CONTROL_CONFIGURATION);
		if (controlConfigurationYaml instanceof Map) {
			for (Entry<String, Object> e: ((Map<String,Object>) controlConfigurationYaml).entrySet()) {
				Object v = e.getValue();
				if (v instanceof List) {
					StringBuilder sb = new StringBuilder();
					for (Object ve: (List<Object>) v) {
						if (sb.length() > 0) {
							sb.append(" ");
						}
						sb.append(ve);
					}
					controlConfiguration.put(e.getKey(), sb.toString());
				} else if (v instanceof Map) {
					StringBuilder sb = new StringBuilder();
					for (Entry<String, Object> ve: ((Map<String, Object>) v).entrySet()) {
						if (sb.length() > 0) {
							sb.append("; ");
						}
						sb.append(ve.getKey()).append(":").append(ve.getValue());
					}
					controlConfiguration.put(e.getKey(), sb.toString());					
				} else {
					controlConfiguration.put(e.getKey(), v);					
				}
			}
		}
		
		Object label = renderNamedElementIconAndLabel(context, typedElement, typedElements);
		String textLabel = Jsoup.parse(label.toString()).text();
		if (helpTooltip) {
			label = getHTMLFactory(context).fragment(label, renderDocumentationIcon(context, typedElement, appConsumer, true));			
		}

		Comparator<? super EObject> labelComparator = (e1, e2) -> {
			try {
				Object l1 = getRenderer(e1).renderLabel(context, e1);
				Object l2 = getRenderer(e2).renderLabel(context, e2);					
				return Jsoup.parse(String.valueOf(l1)).text().compareTo(Jsoup.parse(String.valueOf(l2)).text());
			} catch (Exception e) {
				return e1.hashCode() - e2.hashCode();
			}
		};		
		
		String choiceTreeAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.CHOICE_TREE);
		boolean isChoiceTreeReferenceNodes = "reference-nodes".equals(choiceTreeAnnotation);
		boolean isChoiceTree = isEObjectInstanceClass && ("true".equals(choiceTreeAnnotation) || isChoiceTreeReferenceNodes); 
		List<EObject> choices = new ArrayList<>();
		List<EObject> roots = new ArrayList<>();
		if (isChoiceTree) {
			choices.addAll(getEObjectTypedElementChoices(context, contextObject, typedElement));
			roots.addAll(choices);
			for (int i=0; i < roots.size() - 1; ++i) {
				for (EObject eObj = roots.get(i); eObj != null; eObj = eObj.eContainer()) {
					roots.set(i, eObj);
					if (i < roots.size() - 1) {
						ListIterator<EObject> nrit = roots.listIterator(i + 1);
						while (nrit.hasNext()) {
							if (EcoreUtil.isAncestor(eObj, nrit.next())) {
								nrit.remove();
							}
						}
					} 
					if (i == roots.size() - 1) {
						break;
					}
				}
			}
			if (roots.size() > 1) {
				Collections.sort(roots, labelComparator);				
			}
		}
		
		abstract class ChoiceTreeRenderer {
			
			void render(EObject obj, boolean includingThis, Container<?> container) throws Exception {
				if (includingThis) {
					Tag li = htmlFactory.tag(TagName.li);
					container.content(li);
					if (choices.contains(obj)) {
						UIElement<?> control = renderControl(obj);
						li.content(theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, obj, control), " ");
					}
					li.content(getRenderer(obj).renderIconAndLabel(context, obj));
					Tag ul = htmlFactory.tag(TagName.ul);
					for (EReference ref: obj.eClass().getEAllReferences()) {
						if (ref.isContainment()) {
							render(obj, ref, ul);
						}
					}
					if (!ul.isEmpty()) {
						li.content(ul);
					}
				} else {
					for (EReference ref: obj.eClass().getEAllReferences()) {
						if (ref.isContainment()) {
							render(obj, ref, container);
						}
					}
				}
			}
			
			void render(EObject obj, EReference ref, Container<?> container) throws Exception {
				Collection<EObject> refElements = new ArrayList<>();
				if (ref.isMany()) {
					refElements.addAll((Collection<EObject>) obj.eGet(ref));
				} else {
					refElements.add((EObject) obj.eGet(ref));
				}
				Iterator<EObject> rit = refElements.iterator();
				Z: while (rit.hasNext()) {
					EObject re = rit.next();
					for (EObject ch: choices) {
						if (EcoreUtil.isAncestor(re, ch)) {
							continue Z;
						}
					}
					rit.remove();
				}
				if (!refElements.isEmpty()) {
					if (refElements.size() > 1) {
						Collections.sort(roots, labelComparator);					
					}
					if (isChoiceTreeReferenceNodes) {
						Tag li = htmlFactory.tag(TagName.li);
						container.content(li);
						li.content(getRenderer(obj).renderNamedElementIconAndLabel(context, ref));
						Tag ul = htmlFactory.tag(TagName.ul);
						for (EObject re: refElements) {
							render(re, true, ul);
						}
						if (!ul.isEmpty()) {
							li.content(ul);
						}
					} else {					
						for (EObject re: refElements) {
							render(re, true, container);
						}
					}
				}
			}
			
			abstract UIElement<?> renderControl(EObject obj) throws Exception;
			
		}		
				
		boolean disabled;
		String disabledRenderAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.DISABLED);
		if (CoreUtil.isBlank(disabledRenderAnnotation) || "false".equals(disabledRenderAnnotation)) {
			disabled = false;
		} else if ("true".equals(disabledRenderAnnotation)) {
			disabled = true;
		} else if (contextObject instanceof CDOObject) {
			// XPath
			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) contextObject);
			disabled = Boolean.TRUE.equals(jxPathContext.getValue(disabledRenderAnnotation, Boolean.class));
		} else {
			disabled = false;
		}
		
		switch (controlType) {
		case input:
			String inputTypeStr = isChoiceTree ? "radio" : getRenderAnnotation(context, typedElement, RenderAnnotation.INPUT_TYPE);
			InputType inputType = inputTypeStr == null ? null : HTMLFactory.InputType.valueOf(inputTypeStr);
			if (inputType == null) {
				if (typedElement.isMany() && (isEObjectInstanceClass || instanceClass.isEnum())) {
					inputType = InputType.checkbox;
				} else {
					Class<?> featureTypeInstanceClass = typedElement.getEType().getInstanceClass();
					if (Boolean.class == featureTypeInstanceClass || boolean.class == featureTypeInstanceClass) {
						inputType = InputType.checkbox;
					} else if (Number.class.isAssignableFrom(featureTypeInstanceClass)) {
						inputType = InputType.number;
					} else if (Date.class == featureTypeInstanceClass) {
						inputType = InputType.date;
					} else {
						inputType = InputType.text;
					}
				}
			}
			
			// TODO - hidden inputs for disabled controls.
			Tag errorLabel = htmlFactory.label(Style.DANGER, getResourceString(context, "noChoices")).setData(FormGroup.Status.class.getName(), FormGroup.Status.ERROR);
			switch (inputType) {
			case checkbox:
				if (typedElement.isMany()) {
					// Render a checkbox per choice.
					Set<String> valuesToSelect = new HashSet<>();
					String[] requestValues = context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getParameterValues(typedElement.getName()) : null;
					if (requestValues == null) {
						for (Object fev: ((Collection<Object>) value)) {
							valuesToSelect.add(getFormControlValue(context, contextObject, typedElement, fev, appConsumer));
						}
					} else {
						valuesToSelect.addAll(Arrays.asList(requestValues));
					}
					if (isChoiceTree) {
						if (roots.isEmpty()) {
							if (isRequired(context, contextObject, typedElement)) {
								return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, errorLabel);
							}
							return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, null);
						} else {						
							Tag ul = htmlFactory.tag(TagName.ul);
							ChoiceTreeRenderer treeRenderer = new ChoiceTreeRenderer() {
								
								@Override
								UIElement<?> renderControl(EObject obj) throws Exception {
									Input checkbox = htmlFactory.input(InputType.checkbox).name(typedElement.getName());
									for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
										checkbox.attribute(ce.getKey(), ce.getValue());
									}
									if (obj instanceof CDOObject) {
										String value = CDOIDCodec.INSTANCE.encode(context, ((CDOObject) obj).cdoID());
										checkbox.value(value);
										if (valuesToSelect.contains(value)) {
											checkbox.attribute("checked", "true");
										}
									}
									return checkbox.disabled(disabled);
								}
								
							};
							for (EObject re: roots) {
								treeRenderer.render(re, roots.size() > 1, ul);
							}
							// TODO - listener onFieldSet
							FieldSet checkboxesFieldSet = fieldContainer.fieldset();
							checkboxesFieldSet
								.style().border().bottom("solid 1px "+Bootstrap.Color.GRAY_LIGHT.code)
								.style().margin().bottom("5px");
							checkboxesFieldSet.legend(label);
							checkboxesFieldSet.content(ul);
						}
					} else {					
						Collection<Entry<String, String>> featureChoices = getTypedElementChoices(context, contextObject, typedElement);
						if (featureChoices.isEmpty()) {
							if (isRequired(context, contextObject, typedElement)) {
								return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, errorLabel);
							}
							return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, null);
						} else {				
							// TODO - listener onFieldSet
							FieldSet checkboxesFieldSet = fieldContainer.fieldset();
							checkboxesFieldSet
								.style().border().bottom("solid 1px "+Bootstrap.Color.GRAY_LIGHT.code)
								.style().margin().bottom("5px");
							checkboxesFieldSet.legend(label);
							for (Entry<String, String> fc: featureChoices) {
								Input checkbox = htmlFactory.input(inputType).disabled(disabled);
								for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
									checkbox.attribute(ce.getKey(), ce.getValue());
								}
								checkbox.name(typedElement.getName());
								checkbox.value(StringEscapeUtils.escapeHtml4(fc.getKey()));
								if (valuesToSelect.contains(fc.getKey())) {
									checkbox.attribute("checked", "true");
								}
								checkboxesFieldSet.checkbox(fc.getValue(), theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, checkbox), false);
							}
						}
					}
					return null;
				}
				
				Input checkbox = htmlFactory.input(inputType).disabled(disabled);
				for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
					checkbox.attribute(ce.getKey(), ce.getValue());
				}
				checkbox.name(typedElement.getName());
				checkbox.value("true");
				if (Boolean.TRUE.equals(value)) {
					checkbox.attribute("checked", "true");					
				}

				fieldContainer.checkbox(
						renderNamedElementLabel(context, typedElement, typedElements), 
						theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, checkbox), 
						true);
				
				return null;
			case radio:
				// Radio - get values and labels from options.
				String requestValue = context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getParameter(typedElement.getName()) : null;				
				String valueToSelect = requestValue == null ? getFormControlValue(context, contextObject, typedElement, value, appConsumer) : requestValue;
				if (isChoiceTree) {
					if (roots.isEmpty()) {
						if (isRequired(context, contextObject, typedElement)) {
							return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, errorLabel);
						}
						return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, null);
					} else {						
						Tag ul = htmlFactory.tag(TagName.ul);
						ChoiceTreeRenderer treeRenderer = new ChoiceTreeRenderer() {
							
							@Override
							UIElement<?> renderControl(EObject obj) throws Exception {
								Input radio = htmlFactory.input(InputType.radio).name(typedElement.getName()).disabled(disabled);
								for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
									radio.attribute(ce.getKey(), ce.getValue());
								}
								if (obj instanceof CDOObject) {
									String value = CDOIDCodec.INSTANCE.encode(context, ((CDOObject) obj).cdoID());
									radio.value(value);
									if (valueToSelect != null && valueToSelect.equals(value)) {
										radio.attribute("checked", "true");
									}
								}
								return radio;
							}
							
						};
						for (EObject re: roots) {
							treeRenderer.render(re, roots.size() > 1, ul);
						}
						// TODO listener onFieldSet
						FieldSet radiosFieldSet = fieldContainer.fieldset();
						radiosFieldSet.style()
							.border().bottom("solid 1px "+Bootstrap.Color.GRAY_LIGHT.code)
							.style().margin().bottom("5px");
						radiosFieldSet.legend(label);
						radiosFieldSet.content(ul);
					}
				} else {										
					Collection<Entry<String, String>> featureChoices = getTypedElementChoices(context, contextObject, typedElement);
					if (featureChoices.isEmpty()) {
						if (isRequired(context, contextObject, typedElement)) {
							return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, errorLabel);
						}
						return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, null);
					} else {						
						FieldSet radiosFieldSet = fieldContainer.fieldset();
						radiosFieldSet.style()
							.border().bottom("solid 1px "+Bootstrap.Color.GRAY_LIGHT.code)
							.style().margin().bottom("5px");
						radiosFieldSet.legend(label);
						for (Entry<String, String> fc: featureChoices) {  
							Input radio = htmlFactory.input(inputType)
									.disabled(disabled)
									.name(typedElement.getName())
									.value(StringEscapeUtils.escapeHtml4(fc.getKey()))
									.placeholder(textLabel);
							for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
								radio.attribute(ce.getKey(), ce.getValue());
							}
							if (valueToSelect != null && valueToSelect.equals(fc.getKey())) {
								radio.attribute("checked", "true");
							}
							radiosFieldSet.radio(
									fc.getValue(), 
									theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, radio), 
									false);
						}
					}
				}
				return null;
			default:
				requestValue = context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getParameter(typedElement.getName()) : null;
				Input input = htmlFactory.input(inputType)
					.disabled(disabled)
					.name(typedElement.getName())
					.value(requestValue == null ? StringEscapeUtils.escapeHtml4(getFormControlValue(context, contextObject, typedElement, value, appConsumer)) : requestValue)
					.placeholder(textLabel)
					.required(isRequired(context, contextObject, typedElement));

				for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
					input.attribute(ce.getKey(), ce.getValue());
				}
				
				return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, input);
			}
		case select:
			Collection<Entry<String, String>> selectFeatureChoices = getTypedElementChoices(context, contextObject, typedElement);
			Select select = htmlFactory.select().required(isRequired(context, contextObject, typedElement));
			for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
				select.attribute(ce.getKey(), ce.getValue());
			}
			
			if (!isRequired(context, contextObject, typedElement)) {
				select.option("", "", false, false);
			}
			String requestValue = context instanceof HttpServletRequestContext ? ((HttpServletRequestContext) context).getRequest().getParameter(typedElement.getName()) : null;
			String valueToSelect = requestValue == null ? getFormControlValue(context, contextObject, typedElement, value, appConsumer) : requestValue;				
			if (disabled) {
				fieldContainer.content(htmlFactory.input(InputType.hidden).name(typedElement.getName()).value(valueToSelect));
				select.disabled();
			} else {
				select.name(typedElement.getName());
			}
			for (Entry<String, String> fc: selectFeatureChoices) {
				select.option(StringEscapeUtils.escapeHtml4(fc.getKey()), StringEscapeUtils.escapeHtml4(Jsoup.parse(fc.getValue()).text()), valueToSelect != null && valueToSelect.equals(fc.getKey()), false);
			}
			if (selectFeatureChoices.isEmpty()) {
				select.disabled();
				if (isRequired(context, contextObject, typedElement)) {
					select.setData(FormGroup.Status.class.getName(), FormGroup.Status.ERROR);
				}
			} 
			
			return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, select);
		case textarea:
			TextArea textArea = htmlFactory.textArea()
				.disabled(disabled)
				.name(typedElement.getName())
				.placeholder(textLabel)
				.required(isRequired(context, contextObject, typedElement));			
			for (Entry<String, Object> ce: controlConfiguration.entrySet()) {
				textArea.attribute(ce.getKey(), ce.getValue());
			}
			textArea.content(getFormControlValue(context, contextObject, typedElement, value, appConsumer));
			if (CONTENT_TYPE_TEXT_HTML.equals(getRenderAnnotation(context, typedElement, RenderAnnotation.CONTENT_TYPE))) {
				textArea.id(htmlFactory.nextId());
				fieldContainer.content(renderWysiwygJsInitializerScript(context, textArea)); 
//				fieldContainer.content(htmlFactory.tag(TagName.script, "$(document).ready(function() {  $('#"+textArea.getId()+"').wysiwyg({ toolbar: 'selection'|'top'|'top-focus'|'top-selection'|'top-focus-selection'|'bottom'|'bottom-focus'|'bottom-selection'|'bottom-focus-selection' }); });"));
			}
			return theFormRenderingListener.onFormControlRendering(context, contextObject, typedElement, value, textArea);
		default:
			throw new IllegalArgumentException("Unsupported control type: "+controlType);
		}
	}
	
	/**
	 * Renders TinyMCE initialization script for the text area. This implementation interpolates ``tinymce-init.js`` script with the ``#<text area id>`` as ``selector`` token.
	 * @param context
	 * @param textArea
	 * @return
	 * @throws Exception
	 */
	default Object renderTinymceInitScript(C context, TextArea textArea) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (textArea.getId() == null) {
			textArea.id(htmlFactory.nextId());
		}
		return htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("tinymce-init.js"), "selector", "#" + textArea.getId()));				
	}
	/**
	 * Renders TinyMCE initialization script for the text area. This implementation interpolates ``tinymce-init.js`` script with the ``#<text area id>`` as ``selector`` token.
	 * @param context
	 * @param textArea
	 * @return
	 * @throws Exception
	 */
	default Object renderWysiwygJsInitializerScript(C context, TextArea textArea) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		if (textArea.getId() == null) {
			textArea.id(htmlFactory.nextId());
		}
		return htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("wysiwyg-js-initializer.js"), "selector", "#" + textArea.getId()));				
	}


	/**
	 * Returns true if given feature is required. This implementation returns true if feature is not many and lower bound is not 0.
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @throws Exception
	 */
	default boolean isRequired(C context, T obj, ETypedElement typedElement) throws Exception {
		return !typedElement.isMany() && typedElement.getLowerBound() != 0;
	}
	
	/**
	 * Invoked for select, radio and checkbox on non-boolean types. 
	 * 
	 * This implementation evaluates selector read from ``choices-selector`` annotation, if it is present. The selector expression 
	 * is evaluated with [Apache Commons JXPath](https://commons.apache.org/proper/commons-jxpath/index.html). 
	 * 
	 * If ``choices-selector`` annotation is not present, then this implementation finds all objects compatible with the element's type in the object's containing resource set. 
	 * 
	 */
	default Collection<? extends EObject> getEObjectTypedElementChoices(C context, T obj, ETypedElement eObjectTypedElement) throws Exception {
		String choicesSelector = getRenderAnnotation(context, eObjectTypedElement, RenderAnnotation.CHOICES_SELECTOR);
		String choiceFilter = getRenderAnnotation(context, eObjectTypedElement, RenderAnnotation.CHOICE_FILTER);
		List<EObject> ret = new ArrayList<>(); 
		if (choicesSelector == null) {
			if (SecurityPackage.Literals.PERMISSION__ACTION == eObjectTypedElement  && obj instanceof ProtectedPermission && context instanceof CDOViewContext) {
				Realm<?> realm = ((CDOViewContext<?, ?>) context).getSecurityRealm();
				if (realm != null) {
					List<Action> grantableActions = new ArrayList<>();
					List<EClass> classesToMatch = new ArrayList<>();
					classesToMatch.add(obj.eContainer().eClass());
					classesToMatch.addAll(obj.eContainer().eClass().getEAllSuperTypes());
					for (Package pkg: realm.getPackages()) {
						for (org.nasdanika.cdo.security.Class cls: pkg.getClasses()) {
							for (EClass eClass: classesToMatch) {
								if (eClass.getName().equals(cls.getName()) && eClass.getEPackage().getNsURI().equals(pkg.getNsURI())) {
									for (Action action: cls.getActions()) {
										grantableActions.add(action);
									}
								}
							}
						}					
					}
					return grantableActions;
				}
			} 
			
			if (SecurityPackage.Literals.PROTECTED_PERMISSION__PRINCIPAL == eObjectTypedElement) {
				return ((Protected) obj.eContainer()).getGrantees(); 
			}			
			
			Resource eResource = obj.eResource();
			TreeIterator<? extends Notifier> tit = null;
			if (eResource == null && context instanceof HttpServletRequestContext) {
				Object target = ((HttpServletRequestContext) context).getTarget();
				if (target instanceof EObject) {
					eResource = ((EObject) target).eResource();
				}
			} 
			
			if (eResource != null) {
				ResourceSet resourceSet = eResource.getResourceSet();
				if (resourceSet == null) {
					tit = eResource.getAllContents();
				} else {
					tit = resourceSet.getAllContents();
				}
			}
			
			while (tit != null && tit.hasNext()) {
				Notifier next = tit.next();
				if (eObjectTypedElement.getEType().isInstance(next) && context.authorize(next, StandardAction.read, null, null)) {
					if (choiceFilter != null) {
						// XPath
						JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
						jxPathContext.getVariables().declareVariable("choice", next);
						if (!Boolean.TRUE.equals(jxPathContext.getValue(choiceFilter, Boolean.class))) {
							continue;
						}						
					}
					ret.add((EObject) next);
				}
			}
		} else if (obj instanceof CDOObject) {
			Iterator<?> cit = RenderUtil.newJXPathContext(context, (CDOObject) obj).iterate(choicesSelector);
			while (cit.hasNext()) {
				Object selection = cit.next();
				if (eObjectTypedElement.getEType().isInstance(selection) && context.authorize(selection, StandardAction.read, null, null)) {
					ret.add((EObject) selection);
				}
			}
		}
		return ret;
	}
	

	/**
	 * Invoked for select, radio and checkbox on non-boolean types. 
	 * 
	 * For references it calls getReferenceChoices, renders each object label as label, encodes CDOID as value and sorts choices by the label. 
	 * 
	 * For attributes choices are loaded from the ``choices`` annotation.
	 * Choices are defined each on a new line as a value - label pair <value>=<label>.  
	 * If there is no equal sign, then the line value is used for both value and label.   
	 * 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Collection<Map.Entry<String, String>> getTypedElementChoices(C context, T obj, ETypedElement typedElement) throws Exception {
		Map<String,String> collector = new LinkedHashMap<>();
		
		if (EObject.class.isAssignableFrom(typedElement.getEType().getInstanceClass())) {
			// Accumulates selections for sorting before adding to the collector.
			List<String[]> accumulator = new ArrayList<>(); 
			for (EObject choice: getEObjectTypedElementChoices(context, obj, typedElement)) {
				if (choice instanceof CDOObject) {
					CDOObject cdoNext = (CDOObject) choice;
					Renderer<C, CDOObject> choiceRenderer = typedElement instanceof EReference ? getReferenceRenderer((EReference) typedElement, cdoNext) : getRenderer(cdoNext);
					Object iconAndLabel = choiceRenderer.renderIconAndLabel(context, cdoNext);
					if (iconAndLabel != null) {
						accumulator.add(new String[] { CDOIDCodec.INSTANCE.encode(context, cdoNext.cdoID()), iconAndLabel.toString() });
					}
				}				
			}
			
			Collections.sort(accumulator, (e1, e2) -> {										
				return Jsoup.parse(e1[1]).text().compareTo(Jsoup.parse(e2[1]).text());
			});
			
			for (String[] e: accumulator) {
				collector.put(e[0], e[1]);
			}						
		} else {		
			Object choicesAnnotation = getYamlRenderAnnotation(context, typedElement, RenderAnnotation.CHOICES);
			if (choicesAnnotation instanceof Map) { // key-value pairs
				for (Entry<String, Object> e: ((Map<String,Object>) choicesAnnotation).entrySet()) {
					collector.put(e.getKey(), String.valueOf(e.getValue()));							
				}
			} else if (choicesAnnotation instanceof List) { // key and value are equal.
				for (Object e: ((List<Object>) choicesAnnotation)) {
					String strVal = String.valueOf(e);
					collector.put(strVal, strVal);							
				}				
			} else { // null or not supported
				Class<?> featureTypeInstanceClass = typedElement.getEType().getInstanceClass();
				if (featureTypeInstanceClass.isEnum()) {
					for (Field field: featureTypeInstanceClass.getFields()) {
						if (field.isEnumConstant()) {
							Object fieldValue = field.get(null);
							@SuppressWarnings("rawtypes")
							String name = ((Enum) fieldValue).name();
							if (fieldValue instanceof Enumerator) {
								collector.put(name, ((Enumerator) fieldValue).getLiteral());
							} else {
								collector.put(name, fieldValue.toString());							
							}
						}
					}
				}
			}
		}
		
		return Collections.unmodifiableCollection(collector.entrySet());
	}
	
	default Object renderModelElementFormGroupHelpText(C context, T obj, EModelElement modelElement, Consumer<Object> appConsumer) throws Exception {		
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();		
		String doc = renderDocumentation(context, modelElement);
		if (doc != null) {
			String textDoc = Jsoup.parse(doc).text();
			String firstSentence = firstSentence(context, textDoc);			
			ret.content(firstSentence);
			Modal docModal = renderDocumentationModal(context, modelElement);
			if (docModal != null) {
				appConsumer.accept(docModal);
			}
			if (!textDoc.equals(firstSentence) && docModal != null) {
				Tag helpGlyph = renderHelpIcon(context);
				helpGlyph.on(Event.click, "$('#"+docModal.getId()+"').modal('show')");
				helpGlyph.style("cursor", "pointer");
				ret.content(helpGlyph);
			}
		}
		return ret.isEmpty() ? null : ret;
	}	
	
	/**
	 * Renders help icon. This implementation uses FontAwesome WebApplication.question_circle_o.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Tag renderHelpIcon(C context) throws Exception {
		return getHTMLFactory(context).fontAwesome().webApplication(WebApplication.question_circle).getTarget();
	}
	
	/**
	 * Renders form group if renderTypedElementControl() returns non-null value.
	 * This implementation renders FormInputGroup if:
	 * 
	 * * ``form-input-group`` annotation is true.
	 * * ``form-input-group`` annotation is not present and:
	 *     * control is ``input`` tag.
	 *     * Feature has either icon (rendered on the left) or help icon (rendered on the right).  
	 * 
	 * @param context
	 * @param obj
	 * @param typedElement
	 * @param fieldContainer
	 * @param docModal
	 * @param errorMessage
	 * @param helpTooltip If true, help message is rendered as a tooltip over a help annotation, like in the view. Otherwise it is renders as form group help text
	 *  (not visible in some layouts). 
	 * @return FormGroup. 
	 * @throws Exception
	 */
	default <TE extends ETypedElement> FormGroup<?> renderTypedElementFormGroup(
			C context, 
			T obj, 
			TE typedElement, 
			Collection<TE> typedElements,
			Object value,
			FieldContainer<?> fieldContainer, 
			List<ValidationResult> validationResults,
			boolean helpTooltip,
			FormRenderingListener<C,T,TE> formRenderingListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		UIElement<?> control = renderTypedElementControl(
				context, 
				obj, 
				typedElement, 
				typedElements, 
				value, 
				fieldContainer, 
				validationResults, 
				helpTooltip, 
				formRenderingListener,
				appConsumer); 
		
		if (control == null) {
			return null;
		}
		
		Object icon = renderModelElementIcon(context, typedElement);
		Tag docIcon = renderDocumentationIcon(context, typedElement, appConsumer, false);
		
		boolean isFormInputGroup;
		String formInputGroupAnnotation = getRenderAnnotation(context, typedElement, RenderAnnotation.FORM_INPUT_GROUP);
		if (formInputGroupAnnotation == null) {
			isFormInputGroup = control instanceof Input && (icon != null || docIcon != null);	
		} else {
			isFormInputGroup = "true".equals(formInputGroupAnnotation);
		}
		
		Object helpText = helpTooltip ? null : renderModelElementFormGroupHelpText(context, obj, typedElement, appConsumer);

		HTMLFactory htmlFactory = getHTMLFactory(context);
				
		FormGroup.Status status = null;
		Object statusData = control.getData(FormGroup.Status.class.getName());
		if (statusData instanceof FormGroup.Status) {
			status = (Status) statusData;
		}
		if (validationResults != null) {
			Fragment htf = htmlFactory.fragment();
			for (ValidationResult validationResult: validationResults) {				
				htf.content(htmlFactory.label(validationResult.status.toStyle(), validationResult.message), " ");
				if (status == null || status.ordinal() < validationResult.status.ordinal()) {
					status = validationResult.status;
				}
			}
			if (!htf.isEmpty()) {
				helpText = htf.content(helpText);
			}
		}
		
		if (isFormInputGroup) {
			Object label = renderNamedElementLabel(context, typedElement, typedElements);
			if (isRequired(context, obj, typedElement)) {
				label = htmlFactory.fragment(label, "*");
			}
			FormInputGroup ret = fieldContainer.formInputGroup(label, control, helpText);
			if (icon != null) {
				ret.leftAddOn(icon);
			}
			if (docIcon != null) {
				ret.rightAddOn(docIcon);
			}
			if (status != null) {
				ret.status(status);
			}			
			
			if (formRenderingListener != null) {
				formRenderingListener.onFormGroupRendering(context, obj, typedElement, value, ret);
			}
			return ret;
		}
				
		Object label = renderNamedElementIconAndLabel(context, typedElement, typedElements);
		if (isRequired(context, obj, typedElement)) {
			label = htmlFactory.fragment(label, "*");
		}
		
		if (helpTooltip && docIcon != null) {
			label = htmlFactory.fragment(label, htmlFactory.tag(TagName.sup, docIcon));
		}
		FormGroup<?> ret = fieldContainer.formGroup(label, control, helpText);
		if (status != null) {
			ret.status(status);
		}	
		
		if (formRenderingListener != null) {
			formRenderingListener.onFormGroupRendering(context, obj, typedElement, value, ret);
		}
		
		return ret;
	}
	
	/**
	 * Helper class to pass validation results around.
	 * @author Pavel
	 *
	 */
	class ValidationResult {
		
		final FormGroup.Status status;
		final String message;
		
		ValidationResult(FormGroup.Status status, String message) {
			super();
			this.status = status;
			this.message = message;
		}
		
		JSONObject toJSON() {
			JSONObject ret = new JSONObject();
			ret.put("status", status.name());
			ret.put("statusStyle", status.toStyle().name());
			ret.put("message", message);
			return ret;
		}
		
	}
	
	/**
	 * Renders form groups for editable features.
	 * Features with ``category`` annotation are grouped into fieldsets by the annotation value.
	 * Field set legend shows category icon if ``category.<category name>.icon`` annotation is present on the object's EClass. 
	 * Legend's text is set to the value of ``category.<category name>.label`` annotation on the object's EClass, or to the category name if this annotation is not present. 
	 * @param context
	 * @param obj
	 * @param fieldContainer
	 * @param docModals
	 * @param validationResults
	 * @param helpTooltip
	 * @param formRenderingListener 
	 * @throws Exception
	 */
	default List<FormGroup<?>> renderEditableFeaturesFormGroups(
			C context, 
			T obj, 
			FieldContainer<?> fieldContainer, 
			Map<ENamedElement,List<ValidationResult>> validationResults,
			boolean helpTooltip, 
			FormRenderingListener<C, T, EStructuralFeature> formRenderingListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		Map<EStructuralFeature, Object> editableFeatures = new LinkedHashMap<>();
		for (EStructuralFeature ef: getEditableFeatures(context, obj)) {
			editableFeatures.put(ef, obj.eGet(ef));
		}
		
		// TODO - add support of inlined features.
		return renderTypedElementsFormGroups(context, obj, fieldContainer, validationResults, editableFeatures, helpTooltip, formRenderingListener, appConsumer);
	}

	/**
	 * 
	 * @param context
	 * @param obj
	 * @param fieldContainer
	 * @param docModals
	 * @param validationResults
	 * @param formElements
	 * @param helpTooltip
	 * @param categoryFieldSetConfigurator Optional configuration. May be used in dynamic forms to show/hide fieldsets depending on form state.
	 * @return
	 * @throws Exception
	 */
	default <TE extends ETypedElement> List<FormGroup<?>> renderTypedElementsFormGroups(
			C context, 
			T obj, 
			FieldContainer<?> fieldContainer,
			Map<ENamedElement, 
			List<ValidationResult>> validationResults,
			Map<TE, Object> formElements, 
			boolean helpTooltip,
			FormRenderingListener<C,T,TE> formRenderingListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		List<FormGroup<?>> ret = new ArrayList<>();
		Map<String,List<TE>> categories = new TreeMap<>();
		Map<String,Object> categoriesIconsAndLabels = new HashMap<>();
		for (TE fe: formElements.keySet()) {
			// Original value for features
			if (fe instanceof EStructuralFeature) {
				Object ov = formElements.get(fe);
				if (ov != null) {
					String originalName = ORIGINAL_ELEMENT_VALUE_NAME_PREFIX+fe.getName();
					String originalValue = StringEscapeUtils.escapeHtml4(getFormControlValue(context, obj, fe, ov, appConsumer));
					fieldContainer.content(InputType.hidden.create().name(originalName).value(originalValue));
				}
			}
			
			String category = getNamedElementCategory(context, fe, formElements.keySet());
			if (category == null) {
				FormGroup<?> fg = renderTypedElementFormGroup(
						context, 
						obj, 
						fe, 
						formElements.keySet(), 
						formElements.get(fe), 
						fieldContainer, 
						validationResults.get(fe), 
						helpTooltip,
						formRenderingListener,
						appConsumer);
				
				if (fg != null) {
					ret.add(fg);
				}
			} else {
				List<TE> categoryElements = categories.get(category);
				if (categoryElements == null) {
					categoryElements = new ArrayList<>();
					categories.put(category, categoryElements);
					categoriesIconsAndLabels.put(category, renderNamedElementCategoryIconAndLabel(context, fe, formElements.keySet()));
				}
				categoryElements.add(fe);
			}				
		}

		for (Entry<String, List<TE>> ce: categories.entrySet()) {
			FieldSet categoryFieldSet = fieldContainer.fieldset();
			categoryFieldSet.style().margin().bottom("5px");
			categoryFieldSet.legend(categoriesIconsAndLabels.get(ce.getKey()));
			
			for (TE cete: ce.getValue()) {
				FormGroup<?> fg = renderTypedElementFormGroup(
						context, 
						obj, 
						cete, 
						formElements.keySet(), 
						formElements.get(cete), 
						categoryFieldSet, 
						validationResults.get(cete), 
						helpTooltip, 
						formRenderingListener,
						appConsumer);
				
				if (fg != null) {
					ret.add(fg);
				}
			}
			if (formRenderingListener != null) {
				formRenderingListener.onCategoryFieldSet(context, obj, ce.getKey(), ce.getValue(), categoryFieldSet);
			}
		}
		
		return ret;
	}
	
	/** 
	 * Reads feature values for editable features from the request, parses them and sets feature values.
	 * Then validates the object. Invokes diagnostic consumer, if it is not null, for object-level results and results associated with one of editable features.
	 * @return true if there are no errors in object-level and editable features results.
	 */
	default boolean setEditableFeatures(C context, T obj, Consumer<Diagnostic> diagnosticConsumer) throws Exception {		
		// TODO - add support of inlined features.
		List<EStructuralFeature> editableFeatures = getEditableFeatures(context, obj);
		boolean noErrors = true;
		for (EStructuralFeature esf: editableFeatures) {
			try {
				setFeatureValue(context, obj, esf);
			} catch (Exception e) {
				Throwable rootCause = e;
				while (rootCause.getCause() != null) {
					rootCause = rootCause.getCause();
				}
				noErrors = false;
				if (diagnosticConsumer != null) {
					String rootCauseMessage = rootCause.getMessage() == null ? rootCause.toString() : rootCause.getMessage();
					diagnosticConsumer.accept(new BasicDiagnostic(Diagnostic.ERROR, getClass().getName(), 0, rootCauseMessage, new Object[] { obj, esf, e }));
				}
			}
		}
		Diagnostic vr = validate(context, obj);
		for (Diagnostic vc: vr.getChildren()) {
			List<?> vcData = vc.getData();
			if (!vcData.isEmpty() 
					&& vcData.get(0) == obj 
					&& (vcData.size() == 1 || editableFeatures.contains(vcData.get(1)))) {

				if (vc.getSeverity() == Diagnostic.ERROR) {
					noErrors = false;
				}
				
				if (diagnosticConsumer != null) {
					diagnosticConsumer.accept(vc);
				}
			}
		}
		return noErrors;
	}

	/** 
	 * Compares feature values from the object with the original values stored in hidden fields. 
	 * Creates error diagnostics for concurrently modified features.
	 * @return true if there are no differences in values.
	 */
	default boolean compareEditableFeatures(C context, T obj, Consumer<Diagnostic> diagnosticConsumer, Consumer<Object> appConsumer) throws Exception {		
		boolean noDiscrepancies = true;
		if (context instanceof HttpServletRequestContext) {
			HttpServletRequest request = ((HttpServletRequestContext) context).getRequest();
			List<EStructuralFeature> editableFeatures = getEditableFeatures(context, obj);
			for (EStructuralFeature feature: editableFeatures) {
				if (feature.isMany()) {
					String[] originalValues = request.getParameterValues(ORIGINAL_ELEMENT_VALUE_NAME_PREFIX+feature.getName());
					if (originalValues != null) {
						@SuppressWarnings("unchecked")
						Collection<Object> fv = (Collection<Object>) obj.eGet(feature);
						if (originalValues != null) {
							// TODO compare
						}
					}
				} else {
					String originalValue = request.getParameter(ORIGINAL_ELEMENT_VALUE_NAME_PREFIX+feature.getName());
					if (originalValue != null) {
						String currentValue = StringEscapeUtils.escapeHtml4(getFormControlValue(context, obj, feature, obj.eGet(feature), appConsumer));
						if (!originalValue.equals(currentValue)) {
							Map<String, Object> env = new HashMap<>();
							env.put("value", renderTypedElementValue(context, feature, obj.eGet(feature), appConsumer, false));
							String msg = getHTMLFactory(context).interpolate(getResourceString(context, "concurrentModification.feature"), env);
							diagnosticConsumer.accept(new BasicDiagnostic(Diagnostic.WARNING, getClass().getName(), 0, msg, new Object[] { obj, feature }));
							noDiscrepancies = false;
						}
					}
				}
			}
		}		
		return noDiscrepancies;
	}
	
	/**
	 * Validates object using Ecore validation and ``validate(C,T,EModelElement,DiagnosticChain)`` method.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Diagnostic validate(C context, T obj) throws Exception {
		DelegatingDiagnostician diagnostician = new DelegatingDiagnostician() {
			
			@Override
			public String getObjectLabel(EObject eObject) {
				try {
					Object label = getRenderer(eObject).renderLabel(context, eObject);
					String ret = label== null ? null : Jsoup.parse(label.toString()).text();
					return ret == null ? super.getObjectLabel(eObject) : ret;
				} catch (Exception e) {
					return super.getObjectLabel(eObject);
				}
			}
			
			@Override
			public Map<Object, Object> createDefaultContext() {
				Map<Object, Object> ret = super.createDefaultContext();
				ret.put(Context.class, context);
				ret.put(Renderer.class, this);
				return ret;
			}
			
		};
		
		BasicDiagnostic bd = new BasicDiagnostic();
		Diagnostic validationResult = diagnostician.validate(obj);
		if (!validationResult.getChildren().isEmpty()) {
			bd.merge(validationResult);
		}
		validate(context, obj, obj.eClass(), bd, null);
		for (EStructuralFeature sf: obj.eClass().getEAllStructuralFeatures()) {
			validate(context, obj, sf, bd, null);			
		}
		return bd;
	}

	/**
	 * Validates {@link EClass} or {@link ETypedElement} using {@link RenderAnnotation}.CONSTRAINT annotations.
	 * @param context
	 * @param obj
	 * @param modelElement
	 * @param diagnosticChain
	 * @param variables Additional variables for JXPath context, used for EOperation and EParameter validation.
	 */
	default void validate(C context, T obj, EModelElement modelElement, DiagnosticChain diagnosticChain, Map<String, Object> variables) throws Exception {
		if (obj instanceof CDOObject) {
			Object classConstraintSpec = getYamlRenderAnnotation(context, modelElement, RenderAnnotation.CONSTRAINT);
			if (classConstraintSpec != null) {
				List<?> classConstraints;
				if (classConstraintSpec instanceof List) {
					classConstraints = (List<?>) classConstraintSpec;
				} else {
					classConstraints = Collections.singletonList(classConstraintSpec);
				}
				for (Object cc: classConstraints) {
					String conditionStr = null;
					String errorMessageKey = null;
					String errorMessage = null; 
					if (cc instanceof String) {
						conditionStr = (String) cc;
					} else if (cc instanceof Map) {
						Map<?,?> ccm = (Map<?,?>) cc;
						Object condition = ccm.get("condition");
						if (condition instanceof String) {
							conditionStr = (String) condition;
						}
						Object emk = ccm.get("errorMessageKey");
						if (emk instanceof String) {
							errorMessageKey = (String) emk;
						}
						Object em = ccm.get("errorMessage");
						if (em instanceof String) {
							errorMessage = (String) em;
						}
					}
					if (!CoreUtil.isBlank(conditionStr)) {
						JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
						if (variables != null) {
							for (Entry<String, Object> ve: variables.entrySet()) {
								jxPathContext.getVariables().declareVariable(ve.getKey(), ve.getValue());
							}
						}
						if (!Boolean.TRUE.equals(jxPathContext.getValue(conditionStr, Boolean.TYPE))) {
							String errMsg = null;
							if (errorMessageKey != null) {
								errMsg = getResourceString(context, errorMessageKey);
								if (errMsg == null) {
									errMsg = errorMessage;
								}
								if (errMsg == null) {
									errMsg = "Constraint violation: "+conditionStr;
								} else {
									errMsg = getHTMLFactory(context).interpolate(errMsg, new EObjectTokenSource(context, obj));
								}
							}
							
							Object[] data;
							if (modelElement instanceof EStructuralFeature || modelElement instanceof EParameter) {
								data = new Object[] { obj, modelElement };
							} else {
								data = new Object[] { obj };
							}							
							diagnosticChain.add(new BasicDiagnostic(Diagnostic.ERROR, getClass().getName(), 0, errMsg, data));
						}						
					}
				}
			}
		}		
	}
	
	/**
	 * Validates EOperation input with ``validate(C,T,EModelElement,DiagnosticChain)`` method.
	 * EOperation constraint has access to ``$arguments`` variable of type ``Map<String, Object>``. 
	 * EParameter constraint also has access to ``$argument`` variable.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Diagnostic validate(C context, T obj, EOperation eOperation, Map<String, Object> arguments) throws Exception {
		BasicDiagnostic bd = new BasicDiagnostic();
		Map<String, Object> variables = new HashMap<>();
		variables.put("arguments", arguments);
		validate(context, obj, eOperation, bd, variables);
		for (EParameter ep: eOperation.getEParameters()) {
			Object argument = arguments.get(ep.getName());
			variables.put("argument", argument);
			validate(context, obj, ep, bd, variables);
			if (ep.isRequired()) {
				if (ep.isMany()) {
					if (argument == null || ((Collection<?>) argument).isEmpty()) {
						bd.add(new BasicDiagnostic(Diagnostic.ERROR, getClass().getName(), 0, "Required parameter", new Object[] { obj, ep }));
					}					
				} else {
					if (argument == null) {
						bd.add(new BasicDiagnostic(Diagnostic.ERROR, getClass().getName(), 0, "Required parameter", new Object[] { obj, ep }));
					}
				}
			}
		}
		return bd;
	}
		
//	/**
//	 * Renders a tree item for the object with the tree features under.
//	 * @param context Context
//	 * @param obj Object
//	 * @param depth tree depth, -1 - infinite depth.
//	 * @param itemFilter If not null, it is invoked when object list items are created. Filters can decorate or replace list items. Filter is invoked twice per item - first for the label and then for 
//	 * the entire ``li`` tag. In both cases data is set to the object. For the ``li`` invocation ``role`` property is set to ``item``
//	 * @param jsTree If true, list items are rendered for jsTree. It is responsibility of the caller code to create jsTree container and provide event handler for clicks.
//	 * @return
//	 */
//	default Object renderTreeItem(C context, T obj, int depth, Function<Object, Object> itemFilter, boolean jsTree) throws Exception {
//		HTMLFactory htmlFactory = getHTMLFactory(context);
//		Tag ret = htmlFactory.tag(TagName.li);
//		ret.setData(obj);
//		ret.setData("role", "item");
//		if (jsTree) {
//			JsTree jt = ret.jsTree();
//			jt.icon(getIcon(context, obj));
//			String objectURI = getObjectURI(context, obj);
//			Object link = htmlFactory.link(objectURI == null ? "#" : objectURI+"/"+INDEX_HTML, renderLabel(context, obj)).setData(obj);
//			if (itemFilter != null) {
//				link = itemFilter.apply(link);
//			}
//			ret.content(link);
//		} else {
//			Object link = renderLink(context, obj, true);
//			if (itemFilter != null) {
//				link = itemFilter.apply(link);
//			}
//			ret.content(link);
//		}
//		
//		ret.content(renderReferencesTree(context, obj, depth, itemFilter, jsTree));
//		return itemFilter == null ? ret : itemFilter.apply(ret);
//	}	

//	/**
//	 * Renders an object tree of tree references of the argument object. Tree features are those listed in the ``tree-references`` annotation separated by space.
//	 * If there is no annotation, then containing many features are considered as tree features. If ``tree-node`` annotation of the feature is set to false, then feature elements
//	 * appear directly under the container. Otherwise, a tree node with feature name and icon (if available) is created to hold feature elements. 
//	 * @param context Context
//	 * @param obj Object
//	 * @param depth tree depth, -1 - infinite depth.
//	 * @param itemFilter If not null, it is invoked when object list items are created. Filters can decorate or replace list items. 
//	 * @param jsTree If true, list items are rendered for jsTree.
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	default Object renderReferencesTree(C context, T obj, int depth, Function<Object, Object> itemFilter, boolean jsTree) throws Exception {
//		if (depth == 0) {
//			return null;
//		}
//		
//		List<EReference> treeReferences = new ArrayList<EReference>();
//		EClass eClass = obj.eClass();
//		String treeReferencesAnnotation = getRenderAnnotation(context, eClass, RenderAnnotation.TREE_REFERENCES);
//		if (treeReferencesAnnotation == null) {
//			for (EReference ref: eClass.getEAllReferences()) {
//				if (ref.isContainment() && ref.isMany() && context.authorizeRead(obj, ref.getName(), null)) {
//					treeReferences.add(ref);
//				}
//			}
//		} else {
//			for (String refName: treeReferencesAnnotation.split("\\s+")) {
//				if (!CoreUtil.isBlank(refName)) {
//					EReference sf = (EReference) eClass.getEStructuralFeature(refName.trim());
//					if (sf instanceof EReference && context.authorizeRead(obj, sf.getName(), null)) {
//						treeReferences.add(sf);
//					}
//				}
//			}
//		}
//		
//		// TODO - category nodes.
//		
//		HTMLFactory htmlFactory = getHTMLFactory(context);
//		Tag ret = htmlFactory.tag(TagName.ul);
//		for (EReference treeReference: treeReferences) {
//			String treeNodeAnnotation = getRenderAnnotation(context, treeReference, RenderAnnotation.TREE_NODE);
//			boolean isTreeNode = !"false".equals(treeNodeAnnotation);
//			Tag itemContainer = ret;
//			if (isTreeNode) {
//				Tag refNode = htmlFactory.tag(TagName.li);
//				refNode.setData(treeReference);
//				refNode.setData("role", "item");
//				if (jsTree) {
//					JsTree jt = refNode.jsTree();
//					jt.icon(getModelElementIcon(context, treeReference));
//					refNode.content(renderNamedElementLabel(context, treeReference));
//				} else {
//					refNode.content(renderNamedElementIconAndLabel(context, treeReference));
//				}
//				itemContainer = htmlFactory.tag(TagName.ul);
//				refNode.content(itemContainer);
//			} 
//			
//			if (treeReference.isMany()) {
//				for (EObject ref: (Collection<? extends EObject>) obj.eGet(treeReference)) {
//					itemContainer.content(getReferenceRenderer(treeReference, ref).renderTreeItem(context, ref, depth == -1 ? -1 : depth - 1, itemFilter, jsTree));
//				}
//			} else {
//				Object ref = obj.eGet(treeReference);
//				if (ref instanceof EObject) {
//					itemContainer.content(getReferenceRenderer(treeReference, (EObject) ref).renderTreeItem(context, (EObject) ref, depth == -1 ? -1 : depth - 1, itemFilter, jsTree));
//				}				
//			}
//		}
//		
//		return ret.isEmpty() ? null : ret;
//	}
	
	/**
	 * Creates JsTree node with containment references as children and contextObject selected if it is not null
	 * @param context
	 * @param obj
	 * @param contextObject object to select in the tree
	 * @param lazy If true take jstree-lazy annotation into account.  
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	default JsTreeNode renderJsTreeNode(C context, T obj, EObject contextObject, boolean lazy) throws Exception {
		if (obj instanceof CDOObject && ((CDOObject) obj).cdoID().isTemporary()) {
			return null; // Not addressable.
		}
		
		boolean readable = context.authorizeRead(obj, null, null);
		String objID = null;
		if (obj instanceof CDOObject && !((CDOObject) obj).cdoID().isTemporary()) {
			objID = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj);
		}
		
		HTMLFactory htmlFactory = getHTMLFactory(context);
		JsTreeNode ret = htmlFactory.jsTreeNode();
		ret.setData(obj);
		ret.setData("readable", readable);
		ret.text(renderLabel(context, obj));		
		ret.icon(getIcon(context, obj));
		ret.id(objID);		
		ret.anchorAttribute("title", renderNamedElementLabel(context, obj.eClass()));
		
		Object objContextMenu = renderJsTreeNodeContextMenu(context, obj);
		if (objContextMenu != null) {
			ret.setData("context-menu", objContextMenu);
		}
		
		String contextFeatureName = null;
		if (context instanceof HttpServletRequestContext) {
			HttpServletRequest request = ((HttpServletRequestContext) context).getRequest();
			contextFeatureName = request.getParameter("context-feature");
			if (contextFeatureName == null) {
				Object cfa = request.getAttribute(CONTEXT_ESTRUCTURAL_FEATURE_KEY);
				if (cfa instanceof EStructuralFeature) {
					contextFeatureName = ((EStructuralFeature) cfa).getName();
				}
			}
		}
		if (readable && obj == contextObject && contextFeatureName == null) {
			ret.selected();
		}

		String objectHome = getObjectURI(context, obj)+"/"+INDEX_HTML;
		if (readable) {
			ret.anchorAttribute("onclick", "window.location='"+objectHome+"';");
		} else {
			ret.anchorAttribute("style", "cursor:default");
		}
		
		boolean isLazy = false;
		if (lazy) {
			String jsTreeLazyAnnotation = getRenderAnnotation(context, obj.eClass(), RenderAnnotation.JSTREE_LAZY);
			isLazy = "true".equals(jsTreeLazyAnnotation);
		}
		
		if (isLazy) {
			if (hasJsTreeNodeChildren(context, obj)) {
				ret.hasChildren();
			}
		} else {
			ret.children().addAll(renderJsTreeNodeChildren(context, obj, contextObject, lazy));
		}
		
		if (readable && obj != contextObject && EcoreUtil.isAncestor(obj, contextObject)) {
			// Select ancestor of context object if context object is not in the tree.
			ret.selected(ret.<Boolean>accept((node, childResults) -> {
				// There is a closer ancestor
				if (node.getData() != obj && node.getData() instanceof EObject && EcoreUtil.isAncestor((EObject) node.getData(), contextObject)) {
					return false;
				}
				for (Boolean chr: childResults) {
					if (!chr) {
						return false;
					}
				}
				return true;
			}));
		}		
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	default List<JsTreeNode> renderJsTreeNodeChildren(C context, T obj, EObject contextObject, boolean lazy) throws Exception {
		List<JsTreeNode> ret = new ArrayList<>();

		boolean readable = context.authorizeRead(obj, null, null);
		String objID = null;
		if (obj instanceof CDOObject && !((CDOObject) obj).cdoID().isTemporary()) {
			objID = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj);
		}
		String objectHome = getObjectURI(context, obj)+"/"+INDEX_HTML;
		HTMLFactory htmlFactory = getHTMLFactory(context);
				
		String contextFeatureName = null;
		if (context instanceof HttpServletRequestContext) {
			HttpServletRequest request = ((HttpServletRequestContext) context).getRequest();
			contextFeatureName = request.getParameter("context-feature");
			if (contextFeatureName == null) {
				Object cfa = request.getAttribute(CONTEXT_ESTRUCTURAL_FEATURE_KEY);
				if (cfa instanceof EStructuralFeature) {
					contextFeatureName = ((EStructuralFeature) cfa).getName();
				}
			}
		}
				
		Map<String,JsTreeNode> categories = new TreeMap<>();
		List<EStructuralFeature> treeFeatures;
		if (readable) {
			treeFeatures = getTreeFeatures(context, obj);
		} else {
			treeFeatures = new ArrayList<>();
			for (EStructuralFeature feature: obj.eClass().getEAllStructuralFeatures()) {
				if (feature instanceof EReference && ((EReference) feature).isContainment()) {
					treeFeatures.add(feature);
				}
			}
		}
		
		List<EStructuralFeature> nodeFeatures = new ArrayList<>();
		List<EStructuralFeature> directFeatures = new ArrayList<>();
		for (EStructuralFeature treeFeature: treeFeatures) {
			String treeNodeAnnotation = getRenderAnnotation(context, treeFeature, RenderAnnotation.TREE_NODE);
			if ("false".equals(treeNodeAnnotation) || (treeNodeAnnotation == null && treeFeatures.size() == 1)) {
				directFeatures.add(treeFeature);
			} else {
				nodeFeatures.add(treeFeature);
			}			
		}
		
		// Node feature
		for (EStructuralFeature nodeFeature: nodeFeatures) {
			String category = getNamedElementCategory(context, nodeFeature, nodeFeatures);
			JsTreeNode featureNode = htmlFactory.jsTreeNode();
			featureNode.icon(getModelElementIcon(context, nodeFeature));
			featureNode.text(renderNamedElementLabel(context, nodeFeature, nodeFeatures));
			featureNode.anchorAttribute("title", "Feature");
			featureNode.setData(obj);
			featureNode.setData("readable", readable);
			featureNode.setData("feature", nodeFeature);
			featureNode.id(objID+"-"+nodeFeature.getName());
			if (readable) {
				featureNode.anchorAttribute("onclick", "window.location='"+objectHome+"?context-feature="+URLEncoder.encode(nodeFeature.getName(), StandardCharsets.UTF_8.name())+"';");
				Object featureContextMenu = renderJsTreeFeatureNodeContextMenu(context, obj, nodeFeature);
				if (featureContextMenu != null) {
					featureNode.setData("context-menu", featureContextMenu);
				}

			} else {
				featureNode.anchorAttribute("style", "cursor:default");
			}

			if (readable && obj == contextObject && nodeFeature.getName().equals(contextFeatureName)) {
				featureNode.selected();
			}			
			
			if (nodeFeature.isMany()) {
				// Grouping
				List<EStructuralFeature> groupByFeatures = new ArrayList<>();
				if (nodeFeature instanceof EReference) {
					EClass nodeFeatureType = ((EReference) nodeFeature).getEReferenceType();
					Object groupByAnnotation = getYamlRenderAnnotation(context, nodeFeature, RenderAnnotation.GROUP_BY);
					if (groupByAnnotation == null) {					
						groupByAnnotation = getYamlRenderAnnotation(context, nodeFeatureType, RenderAnnotation.GROUP_BY); 
					}
					if (groupByAnnotation instanceof String) {
						for (String gbfn: ((String) groupByAnnotation).split("\\s+")) {
							EStructuralFeature gbf = nodeFeatureType.getEStructuralFeature(gbfn);
							if (gbf != null) {
								groupByFeatures.add(gbf);
							}
						}
					} else if (groupByAnnotation instanceof List) {
						for (String gbfn: (List<String>) groupByAnnotation) {
							EStructuralFeature gbf = nodeFeatureType.getEStructuralFeature(gbfn);
							if (gbf != null) {
								groupByFeatures.add(gbf);
							}
						}						
					}
				}
				
				for (EObject element: (Collection<EObject>) obj.eGet(nodeFeature)) {
					JsTreeNode elementNode = getRenderer(element).renderJsTreeNode(context, element, contextObject, lazy);
				
					if (elementNode != null) {					
						JsTreeNode contextNode = featureNode;
						
						// Grouping
						for (EStructuralFeature groupByFeature: groupByFeatures) {
							Object groupByFeatureValue = element.eGet(groupByFeature);
							if (groupByFeatureValue == null) {
								break;
							} 
							
							final String groupByDataKey = "group-by";														
							Map<Object, JsTreeNode> contextGroupingNodes = (Map<Object, JsTreeNode>) contextNode.getData(groupByDataKey);
							if (contextGroupingNodes == null) {
								contextGroupingNodes = new HashMap<>();
								contextNode.setData(groupByDataKey, contextGroupingNodes);								
							}
							JsTreeNode groupByFeatureValueNode = contextGroupingNodes.get(groupByFeatureValue);
							if (groupByFeatureValueNode == null) {
								groupByFeatureValueNode = htmlFactory.jsTreeNode();
								if (groupByFeatureValue instanceof EObject) {
									EObject eGroupByFeatureValue = (EObject) groupByFeatureValue;
									Renderer<C, EObject> eGroupByFeatureValueRenderer = getRenderer(eGroupByFeatureValue);
									groupByFeatureValueNode.icon(eGroupByFeatureValueRenderer.getIcon(context, eGroupByFeatureValue));
									groupByFeatureValueNode.text(eGroupByFeatureValueRenderer.renderLabel(context, eGroupByFeatureValue));
								} else {
									groupByFeatureValueNode.icon(getModelElementIcon(context, groupByFeature));
									groupByFeatureValueNode.text(groupByFeatureValue.toString());										
								}
								groupByFeatureValueNode.anchorAttribute("title", "Group");
								groupByFeatureValueNode.anchorAttribute("style", "cursor:default");
								groupByFeatureValueNode.setData(groupByFeatureValue);
								groupByFeatureValueNode.setData("readable", false);
								groupByFeatureValueNode.setData("feature", groupByFeature);
								groupByFeatureValueNode.id(objID+"-"+nodeFeature.getName()+"-group-"+htmlFactory.nextId()); 

								contextNode.children().add(groupByFeatureValueNode);
								contextGroupingNodes.put(groupByFeatureValue, groupByFeatureValueNode);
							}
							contextNode = groupByFeatureValueNode;
						}
						
						contextNode.children().add(elementNode);
					}					
				}
			} else {
				Object val = obj.eGet(nodeFeature);
				if (val instanceof EObject) {
					JsTreeNode valNode = getRenderer((EObject) val).renderJsTreeNode(context, (EObject) val, contextObject, lazy);
					if (valNode != null) {
						featureNode.children().add(valNode);
					}
				}
			}
			
			if (!featureNode.children().isEmpty()) {
				if (category == null) {
					ret.add(featureNode);
				} else {
					JsTreeNode categoryNode = categories.get(category);
					if (categoryNode == null) {
						categoryNode = htmlFactory.jsTreeNode();
						categories.put(category, categoryNode);
						ret.add(categoryNode);
						categoryNode.icon(getRenderAnnotation(context, obj.eClass(), "category."+category+".icon"));
						
						String categoryLabel = getRenderAnnotation(context, obj.eClass(), "category."+category+".label"); 
						if (categoryLabel == null) {
							String[] cca = StringUtils.splitByCharacterTypeCamelCase(category);
							cca[0] = StringUtils.capitalize(cca[0]);
							for (int i=1; i<cca.length; ++i) {
								cca[i] = cca[i].toLowerCase();
							}
							categoryLabel = StringUtils.join(cca, " ");
						}
						categoryNode.text(categoryLabel);
						categoryNode.anchorAttribute("title", "Category");								
						categoryNode.setData(obj);
						categoryNode.setData("readable", false);
						categoryNode.setData("category", true);
						categoryNode.anchorAttribute("style", "cursor:default");
					}
					categoryNode.children().add(featureNode);
				}
			}			
		}
		
		// Direct features
		for (EStructuralFeature directFeature: directFeatures) {
			if (directFeature.isMany()) {
				for (EObject element: (Collection<EObject>) obj.eGet(directFeature)) {
					JsTreeNode elementNode = getRenderer(element).renderJsTreeNode(context, element, contextObject, lazy);
					if (elementNode != null) {
						ret.add(elementNode);
					}					
				}
			} else {
				Object val = obj.eGet(directFeature);
				if (val instanceof EObject) {
					JsTreeNode valNode = getRenderer((EObject) val).renderJsTreeNode(context, (EObject) val, contextObject, lazy);
					if (valNode != null) {
						ret.add(valNode);
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns true if the node has children. Makes a good enough guess, which may be false sometimes. As usual - override as needed.
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default boolean hasJsTreeNodeChildren(C context, T obj) throws Exception {
		if (context.authorizeRead(obj, null, null)) {
			for (EStructuralFeature treeFeature: getTreeFeatures(context, obj)) {
				if (obj.eIsSet(treeFeature)) {
					return true;
				}
			}				
		} else {
			for (EStructuralFeature feature: obj.eClass().getEAllStructuralFeatures()) {
				if (feature instanceof EReference && ((EReference) feature).isContainment()) {
					if (obj.eIsSet(feature)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * Renders jsTree context menu items for the object node. See https://www.jstree.com/api/#/?q=$.jstree.defaults.contextmenu&f=$.jstree.defaults.contextmenu.items for details
	 * @param context
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	default Object renderJsTreeNodeContextMenu(C context, T obj) throws Exception {
		Map<String, JsTreeContextMenuItem> menuItems = new LinkedHashMap<>();
		HTMLFactory htmlFactory = getHTMLFactory(context);
		
		// Edit context menu item
		if (isEditable(context, obj, obj.eClass()) && context.authorizeUpdate(obj, null, null)) {
			JsTreeContextMenuItem editMenuItem = htmlFactory.jsTreeContextMenuItem();
			menuItems.put("edit", editMenuItem);
			editMenuItem.icon("fas fa-edit").label(getResourceString(context, "edit"));

			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "editTooltip"), env);
			editMenuItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(tooltip)));
			
			// Wire
			boolean hasEditOperation = false;
			// Editor web operation
			for (EOperation eOperation: obj.eClass().getEAllOperations()) {
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
				if (eOperationTargetInfo != null && eOperationTargetInfo.getRole() == Role.editor) {
					hasEditOperation = true;
					editMenuItem.action("function() { window.location='"+getObjectURI(context, obj)+"/"+eOperationTargetInfo.getPath()+"'; }");
					break;
				}				
			}
			
			if (!hasEditOperation) {
				editMenuItem.action("function() { window.location='"+getObjectURI(context, obj)+"/edit.html'; }");		
			}						
		}

		// Delete context menu item
		if (obj.eContainer() != null && context.authorizeDelete(obj, null, null) && isEditable(context, obj, obj.eContainmentFeature())) {
			JsTreeContextMenuItem deleteMenuItem = htmlFactory.jsTreeContextMenuItem();
			menuItems.put("delete", deleteMenuItem);
			deleteMenuItem.icon("far fa-trash").label(getResourceString(context, "delete"));
			
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, renderNamedElementLabel(context, obj.eClass())+" '"+renderLabel(context, obj)+"'");
			String tooltip = htmlFactory.interpolate(getResourceString(context, "deleteTooltip"), env);
			deleteMenuItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(tooltip)));
			String deleteConfirmationMessage = StringEscapeUtils.escapeEcmaScript(htmlFactory.interpolate(getResourceString(context, "confirmDelete"), env));
			deleteMenuItem.action("function() { if (confirm('"+deleteConfirmationMessage+"')) window.location='"+getObjectURI(context, obj)+"/delete.html'; }"); 
		}
		
		// Web operations
		for (EOperation eOperation: obj.eClass().getEAllOperations()) {
			EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
			if (eOperationTargetInfo != null 
					&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
					&& eOperationTargetInfo.getFeature() == null 
					&& eOperationTargetInfo.getFeatureValue() == null 
					&& eOperationTargetInfo.getRole() == Role.operation
					&& isVisible(context, obj, eOperation)
					&& context.authorize(obj, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
				
				menuItems.put("webOperation_"+htmlFactory.nextId(), createEOperationJsTreeContextMenuItem(context, obj, eOperationTargetInfo, null, null));
			}				
		}
		
		EObject eContainer = obj.eContainer();
		EStructuralFeature containmentFeature = obj.eContainmentFeature();
		if (eContainer != null && containmentFeature != null) {
			for (EOperation eOperation: eContainer.eClass().getEAllOperations()) {
				EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
				if (eOperationTargetInfo != null
						&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
						&& containmentFeature.getName().equals(eOperationTargetInfo.getFeatureValue())
						&& eOperationTargetInfo.getRole() == Role.operation
						&& isVisible(context, obj, eOperation)
						&& context.authorize(eContainer, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
					Map<String, String> queryParameters = new HashMap<>();
					queryParameters.put("feature", containmentFeature.getName());					
					if (obj instanceof CDOObject) {
						String formControlValue = CDOIDCodec.INSTANCE.encode(context, (CDOObject) obj);
						queryParameters.put("element", formControlValue);
						queryParameters.put("context-object", formControlValue);
					}
					
					Map<String, Object> vars = new HashMap<>();
					vars.put("element", obj);
					menuItems.put("featureValueWebOperation_"+htmlFactory.nextId(), getRenderer(eContainer).createEOperationJsTreeContextMenuItem(context, eContainer, eOperationTargetInfo, queryParameters, vars));
				}				
			}
		}
		
		for (org.nasdanika.cdo.web.routes.app.Action<C, T> action: getActions(context, obj)) {
			if (action.getRole() == org.nasdanika.cdo.web.routes.app.Action.Role.action && action.getFeature() == null) {
				menuItems.put("action_"+htmlFactory.nextId(), createActionJsTreeContextMenuItem(context, obj, action));
			}
		}					
		
		boolean needFeatureSeparator = !menuItems.isEmpty();
		
		for (EStructuralFeature sf: getTreeFeatures(context, obj)) {
			Object fcm = renderJsTreeFeatureNodeContextMenu(context, obj, sf);
			if (fcm != null) {
				JsTreeContextMenuItem featureContextMenuItem = htmlFactory.jsTreeContextMenuItem()
					.icon(getModelElementIcon(context, sf))
					.label(StringEscapeUtils.escapeEcmaScript(String.valueOf(renderNamedElementLabel(context, sf))));
				
				Object firstDocSentence = renderFirstDocumentationSentence(context, sf);
				if (firstDocSentence != null) {
					featureContextMenuItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(firstDocSentence.toString())));
				}
				featureContextMenuItem.subMenu(fcm);
				if (needFeatureSeparator) {
					featureContextMenuItem.separatorBefore();
					needFeatureSeparator = false;
				}
				menuItems.put("feature_"+sf.getName(), featureContextMenuItem);
			}
		}
				
		if (menuItems.isEmpty()) {
			return null;
		}
		
		StringBuilder ret = new StringBuilder("{").append(System.lineSeparator());		
		int initialLength = ret.length();
		for (Entry<String, JsTreeContextMenuItem> mie: menuItems.entrySet()) {
			if (ret.length() > initialLength) {
				ret.append(",").append(System.lineSeparator());
			}
			ret.append(mie.getKey()).append(": ").append(mie.getValue());
		}
		ret.append(System.lineSeparator()).append("}");		
		return ret.toString();
	}
	
	default JsTreeContextMenuItem createEOperationJsTreeContextMenuItem(
			C context, 
			T obj, 
			EOperationTargetInfo eOperationTargetInfo, 
			Map<String,String> queryParameters, 
			Map<String, Object> jxPathContextVariables) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);
		JsTreeContextMenuItem ret = htmlFactory.jsTreeContextMenuItem()
				.icon(getModelElementIcon(context, eOperationTargetInfo.getEOperation()))
				.label(StringEscapeUtils.escapeEcmaScript(String.valueOf(renderNamedElementLabel(context, eOperationTargetInfo.getEOperation()))));
		
		String path = eOperationTargetInfo.getPath();
		
		if (queryParameters != null && !queryParameters.isEmpty()) {
			StringBuilder query = new StringBuilder();
			for (Entry<String, String> qp: queryParameters.entrySet()) {
				if (query.length()>0) {
					query.append("&");
				}
				query.append(qp.getKey()).append("=").append(URLEncoder.encode(qp.getValue(), StandardCharsets.UTF_8.name()));
			}
			path += "?" + query;
		}
		
		String guard = "";
		
		String confirm = eOperationTargetInfo.getConfirm();
		if (!CoreUtil.isBlank(confirm)) {
			Map<String, Object> env = new HashMap<>();
			env.put("object-label", renderLabel(context, obj));
			if (jxPathContextVariables != null) {
				Object element = jxPathContextVariables.get("element");
				if (element instanceof EObject) {
					env.put("element-label", getRenderer((EObject) element).renderLabel(context, (EObject) element));						
				}						
			}
			String confirmationMessage = StringEscapeUtils.escapeEcmaScript(htmlFactory.interpolate(confirm, env));			
			guard = "if (confirm('"+confirmationMessage+"')) ";
		}			
	
		ret.action("function() { "+ guard + "window.location='"+getObjectURI(context, obj)+"/"+path+"'; }");
		
		// Disabled 
		boolean disabled;
		String disabledRenderAnnotation = getRenderAnnotation(context, eOperationTargetInfo.getEOperation(), RenderAnnotation.DISABLED);
		if (CoreUtil.isBlank(disabledRenderAnnotation) || "false".equals(disabledRenderAnnotation)) {
			disabled = false;
		} else if ("true".equals(disabledRenderAnnotation)) {
			disabled = true;
		} else if (obj instanceof CDOObject) {
			// XPath
			JXPathContext jxPathContext = RenderUtil.newJXPathContext(context, (CDOObject) obj);
			if (jxPathContextVariables != null) {
				for (Entry<String, Object> ve: jxPathContextVariables.entrySet()) {
					jxPathContext.getVariables().declareVariable(ve.getKey(), ve.getValue());
				}
			}
			disabled = Boolean.TRUE.equals(jxPathContext.getValue(disabledRenderAnnotation, Boolean.class));
		} else {
			disabled = false;
		}
		ret.disabled(disabled);

		Object firstDocSentence = renderFirstDocumentationSentence(context, eOperationTargetInfo.getEOperation());
		if (firstDocSentence != null) {
			ret.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(firstDocSentence.toString())));
		}
		
		return ret;
	}
	
	default JsTreeContextMenuItem createActionJsTreeContextMenuItem(C context, T obj, org.nasdanika.cdo.web.routes.app.Action<C, T> action) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		JsTreeContextMenuItem ret = htmlFactory.jsTreeContextMenuItem()
				.icon(action.getIcon())
				.label(StringEscapeUtils.escapeEcmaScript(action.getLabel()));
		
		String path = action.getPath();
		if (path.endsWith("/")) {
			path += INDEX_HTML;
		}
				
		String guard = "";
		
		String confirm = action.getConfirmation();
		if (!CoreUtil.isBlank(confirm)) {
			guard = "if (confirm('"+confirm+"')) ";
		}			
	
		ret.action("function() { "+ guard + "window.location='"+getObjectURI(context, obj)+"/"+path+"'; }");
		
		ret.disabled(!action.canExecute());
		
		String comment = action.getComment();
		if (!CoreUtil.isBlank(comment)) {
			ret.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(comment)));
		}
		
		return ret;
	}	
	
	/**
	 * Renders jsTree context menu items for the feature node. See https://www.jstree.com/api/#/?q=$.jstree.defaults.contextmenu&f=$.jstree.defaults.contextmenu.items for details
	 * @param context
	 * @param obj
	 * @param eReference
	 * @return
	 * @throws Exception
	 */
	default Object renderJsTreeFeatureNodeContextMenu(C context, T obj, EStructuralFeature feature) throws Exception {
		Map<String, JsTreeContextMenuItem> menuItems = new LinkedHashMap<>();
		HTMLFactory htmlFactory = getHTMLFactory(context);
		
		// Add
		if (feature.isChangeable() && isEditable(context, obj, feature) && context.authorizeCreate(obj, feature.getName(), null)) { // Adding to a reference is considered create.
			Map<String, Object> env = new HashMap<>();
			env.put(NAME_KEY, feature.getName());
			
			JsTreeContextMenuItem addItem = htmlFactory.jsTreeContextMenuItem();
			menuItems.put("add", addItem);
			if (feature instanceof EReference && ((EReference) feature).isContainment()) {
				addItem.icon("fa fa-magic");
				addItem.label(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(getResourceString(context, "create"))));
				String tooltip = htmlFactory.interpolate(getResourceString(context, "createTooltip"), env);
				if (tooltip != null) {
					addItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(tooltip)));					
				}
			} else if (feature instanceof EAttribute) {
				addItem.icon("fa fa-plus-circle");
				addItem.label(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(getResourceString(context, "add"))));
			} else {
				addItem.icon("fa fa-plus-circle");
				addItem.label(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(getResourceString(context, "select"))));
				String tooltip = htmlFactory.interpolate(getResourceString(context, "selectTooltip"), env);
				if (tooltip != null) {
					addItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(tooltip)));					
				}
			}

			// Four types of items
			// - classes
			// - template objects
			// - factory eoperations
			// - dividers
			// collect them all as Consumer<Button> and then apply to the button itself if single, or to items is multiple - check size from the consumers.
			
			String objectURI = getObjectURI(context, obj);	
			
			List<Task> wirerers = new ArrayList<>();
			
			if (feature instanceof EReference && ((EReference) feature).isContainment()) {
				List<EClass> featureElementTypes = new ArrayList<>();
				
				// Classes			
				for (EClass ec: getReferenceElementTypes(context, obj, (EReference) feature)) {
					String qualifier = feature.getName()+"/"+ec.getName();
					if (feature.getEContainingClass().getEPackage() != ec.getEPackage()) {
						qualifier += "@"+ec.getEPackage().getNsURI();
					}
					if (context.authorizeCreate(obj, qualifier, null)) {
						featureElementTypes.add(ec);
					}
				}
				
				for (EClass featureElementType: featureElementTypes) {
					wirerers.add(() -> {
						String encodedPackageNsURI = Hex.encodeHexString(featureElementType.getEPackage().getNsURI().getBytes(/* StandardCharsets.UTF_8.name()? */));
						Renderer<C, EObject> fetr = getRenderer(featureElementType);
						String createURL = objectURI+"/reference/"+feature.getName()+"/create/"+encodedPackageNsURI+"/"+featureElementType.getName()+EXTENSION_HTML;
						if (wirerers.size() == 1) {
							addItem.action("function() { window.location='"+createURL+"'; }");
						} else {
							JsTreeContextMenuItem subItem = addItem.createSubMenuItem("createEClass_"+htmlFactory.nextId());
							subItem.action("function() { window.location='"+createURL+"'; }");
							subItem.icon(fetr.getModelElementIcon(context, featureElementType));
							subItem.label(StringEscapeUtils.escapeEcmaScript(String.valueOf(fetr.renderNamedElementLabel(context, featureElementType))));
						}
					});
					
				}
				
				// Template objects
				
				// Factory operations
				for (EOperation eOperation: obj.eClass().getEAllOperations()) {				
					EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
					if (eOperationTargetInfo != null
							&& feature.getName().equals(eOperationTargetInfo.getFeature())
							&& eOperationTargetInfo.getRole() == Role.factory
							&& isVisible(context, obj, eOperation)
							&& context.authorize(obj, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
						
						wirerers.add(() -> {
							String webOperationURL = getObjectURI(context, obj)+"/"+eOperationTargetInfo.getPath();
							if (wirerers.size() == 1) {
								addItem.action("function() { window.location='"+webOperationURL+"'; }");
							} else {
								JsTreeContextMenuItem subItem = addItem.createSubMenuItem("eOperation_"+htmlFactory.nextId());
								subItem.action("function() { window.location='"+webOperationURL+"'; }");
								subItem.icon(getModelElementIcon(context, eOperation));
								subItem.label(StringEscapeUtils.escapeEcmaScript(String.valueOf(renderNamedElementLabel(context, eOperation))));
								Object tooltip = renderFirstDocumentationSentence(context, eOperation);
								if (tooltip != null) {
									addItem.title(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeXml11(tooltip.toString())));					
								}
							}
						});
					}				
				}						
			} else if (feature instanceof EAttribute) {
				wirerers.add(() -> {
					addItem.action("function() { window.location='"+objectURI+"/attribute/"+feature.getName()+"/add.html'; }"); 
				});
			} else {
				wirerers.add(() -> {
					addItem.action("function() { window.location='"+objectURI+"/feature/"+feature.getName()+"/select.html'; }");
				});
			}
			
			for (Task wirerer: wirerers) {
				wirerer.execute();
			}
			if (wirerers.isEmpty()) {
				addItem.disabled();
			}
		}
		
		// Web Operations
		for (EOperation eOperation: obj.eClass().getEAllOperations()) {
			EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);
			if (eOperationTargetInfo != null 
					&& getTypedElementLocation(context, eOperation) == TypedElementLocation.view 
					&& feature.getName().equals(eOperationTargetInfo.getFeature())
					&& eOperationTargetInfo.getRole() == Role.operation
					&& isVisible(context, obj, eOperation)
					&& context.authorize(obj, eOperationTargetInfo.getAction(), eOperationTargetInfo.getQualifier(), null)) {
				
				menuItems.put("webOperation_"+htmlFactory.nextId(), createEOperationJsTreeContextMenuItem(context, obj, eOperationTargetInfo, Collections.singletonMap("feature", feature.getName()), null));
			}				
		}		
		
		// Action buttons
		for (org.nasdanika.cdo.web.routes.app.Action<C, T> action: getActions(context, obj)) {
			if (action.getRole() == org.nasdanika.cdo.web.routes.app.Action.Role.action && action.getFeature() == feature) {
				menuItems.put("action_"+htmlFactory.nextId(), createActionJsTreeContextMenuItem(context, obj, action));
			}
		}			
										
		if (menuItems.isEmpty()) {
			return null;
		}
		
		StringBuilder ret = new StringBuilder("{").append(System.lineSeparator());		
		int initialLength = ret.length();
		for (Entry<String, JsTreeContextMenuItem> mie: menuItems.entrySet()) {
			if (ret.length() > initialLength) {
				ret.append(",").append(System.lineSeparator());
			}
			ret.append(mie.getKey()).append(": ").append(mie.getValue());
		}
		ret.append(System.lineSeparator()).append("}");		
		return ret.toString();
	}
		
	/**
	 * Renders object header. This implementation interpolates ``object.header`` resource string with the following tokens:
	 * 
	 * * ``icon``
	 * * ``label``
	 * * ``eclass-icon``
	 * * ``eclass-label``
	 * * ``documentation-icon``
	 * 
	 * @param context
	 * @param obj
	 * @param classDocModal 
	 * @return
	 * @throws Exception 
	 */
	default Object renderObjectHeader(C context, T obj, Consumer<Object> appConsumer) throws Exception {
		Map<String, Object> env = new HashMap<>();
		
		Object icon = renderIcon(context, obj);
		env.put("icon", icon == null ? "" : icon);
		
		Object label = renderLabel(context, obj);
		env.put("label", label == null ? "" : label);
		
		Object eClassIcon = renderModelElementIcon(context, obj.eClass());
		env.put("eclass-icon", eClassIcon == null ? "" : eClassIcon);
		
		Object eClassLabel = renderNamedElementLabel(context, obj.eClass());
		env.put("eclass-label", eClassLabel == null || eClassLabel.equals(label) ? "" : eClassLabel);
		
		Tag classDocIcon = renderDocumentationIcon(context, obj.eClass(), appConsumer, true);		
		env.put("documentation-icon", classDocIcon == null ? "" : classDocIcon);
		
		return getHTMLFactory(context).interpolate(getResourceString(context, "object.header"), env);
	}

	/**
	 * Renders object edit form with feature documentation modals and error messages if any. Action buttons are not rendered.
	 * @param context
	 * @param obj
	 * @param validationResults
	 * @param namedElementValidationResults
	 * @param horizontalForm
	 * @param docModalsContainer If form is rendered in a modal, then doc modals shall be rendered in a different container. 
	 * @return
	 * @throws Exception
	 */
	default Form renderEditForm(
			C context, 
			T obj, 
			List<ValidationResult> validationResults, 
			Map<ENamedElement, List<ValidationResult>> namedElementValidationResults, 
			boolean horizontalForm,
			FormRenderingListener<C, T, EStructuralFeature> formRenderingListener,			
			Consumer<Object> appConsumer) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);		
		Form editForm = htmlFactory.form();
		
		if (formRenderingListener != null) {
			formRenderingListener.onBeforeFormRendering(context, obj, editForm);
		}

		if (appConsumer == null) {
			appConsumer = editForm;
		}
		
		ListGroup errorList = htmlFactory.listGroup();
		for (ValidationResult vr: validationResults) {
			errorList.item(vr.message, vr.status.toStyle());			
		}
		
		if (horizontalForm) {
			for (Entry<ENamedElement, List<ValidationResult>> fe: namedElementValidationResults.entrySet()) {
				for (ValidationResult fvr: fe.getValue()) {
					Object featureNameLabel = renderNamedElementIconAndLabel(context, fe.getKey());
					errorList.item(htmlFactory.label(fvr.status.toStyle(), featureNameLabel) + " " + fvr.message, fvr.status.toStyle());											
				}
			}
		}
		
		if (!errorList.isEmpty()) {
			editForm.content(errorList);
		}
				
		renderEditableFeaturesFormGroups(
				context, 
				obj, 
				editForm, 
				namedElementValidationResults, 
				horizontalForm,
				formRenderingListener,
				appConsumer).forEach((fg) -> fg.feedback(!horizontalForm));
		
		return editForm;
	}
	
	/**
	 * Renders operation input form with parameter documentation modals and error messages if any. Action buttons are not rendered.
	 * @param context
	 * @param obj
	 * @param validationResults
	 * @param namedElementValidationResults
	 * @param horizontalForm
	 * @return
	 * @throws Exception
	 */
	default Form renderInputForm(
			C context, 
			T obj, 
			Map<EParameter, Object> formParameters, 
			List<ValidationResult> validationResults, 
			Map<ENamedElement, List<ValidationResult>> namedElementValidationResults, 
			boolean horizontalForm, 
			FormRenderingListener<C,T,EParameter> formRenderingListener,
			Consumer<Object> appConsumer) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);		
		Form editForm = htmlFactory.form();
				
		if (formRenderingListener != null) {
			formRenderingListener.onBeforeFormRendering(context, obj, editForm);
		}
		
		ListGroup errorList = htmlFactory.listGroup();
		for (ValidationResult vr: validationResults) {
			errorList.item(vr.message, vr.status.toStyle());			
		}
		
		if (horizontalForm) {
			for (Entry<ENamedElement, List<ValidationResult>> nee: namedElementValidationResults.entrySet()) {
				for (ValidationResult nevr: nee.getValue()) {
					Object featureNameLabel = renderNamedElementIconAndLabel(context, nee.getKey());
					errorList.item(htmlFactory.label(nevr.status.toStyle(), featureNameLabel) + " " + nevr.message, nevr.status.toStyle());											
				}
			}
		}
		
		if (!errorList.isEmpty()) {
			editForm.content(errorList);
		}
				
		renderTypedElementsFormGroups(
				context, 
				obj, 
				editForm, 
				namedElementValidationResults, 
				formParameters, 
				horizontalForm, 
				formRenderingListener,
				appConsumer).forEach((fg) -> fg.feedback(!horizontalForm));
		
		return editForm;
	}
	
	/**
	 * Returns default/initial values to use in EOperation dialogs. This implementation returns a map with
	 * values from ``default-value`` {@link EParameter} annotations. If there is no such annotation and EOperation is in ``editor`` role, 
	 * then parameter value is the value of the feature matching parameter name.
	 * @param context
	 * @param obj
	 * @param eOperation
	 * @return
	 */
	default Map<EParameter, Object> getParameterValues(C context, T obj, EOperation eOperation) throws Exception {
		Map<EParameter, Object> ret = new LinkedHashMap<>();
		EOperationTargetInfo eOperationTargetInfo = EOperationTargetInfo.create(context, this, eOperation);				
		for (EParameter eParameter: eOperation.getEParameters()) {
			if (eOperationTargetInfo.isFormParameter(eParameter)) {
				Object defaultValue = getRenderAnnotation(context, eParameter, RenderAnnotation.DEFAULT_VALUE);
				if (defaultValue == null && eOperationTargetInfo.getRole() == Role.editor) {
					EStructuralFeature msf = obj.eClass().getEStructuralFeature(eParameter.getName());
					if (msf != null 
							&& msf.isMany() == eParameter.isMany() 
							&& eParameter.getEType().getInstanceClass().isAssignableFrom(msf.getEType().getInstanceClass())) {
						
						defaultValue = obj.eGet(msf);
					}
				}
				ret.put(eParameter, defaultValue);
			}
		}
		return ret;
	}	
	
	/**
	 * Renders an edit form for a single feature, e.g. a reference with checkboxes for selecting multiple values and radios or select for selecting a single value.
	 * @param context
	 * @param obj
	 * @param validationResults
	 * @param featureValidationResults
	 * @param horizontalForm
	 * @return
	 * @throws Exception
	 */
	default Form renderFeatureEditForm(
			C context, 
			T obj, 
			EStructuralFeature feature,
			List<ValidationResult> featureValidationResults, 
			boolean horizontalForm,
			FormRenderingListener<C,T,ETypedElement> formRenderingListener, 
			Consumer<Object> appConsumer) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);		
		Form selectForm = htmlFactory.form();
		
		ListGroup errorList = htmlFactory.listGroup();
		
		if (horizontalForm && featureValidationResults != null) {
			for (ValidationResult fvr: featureValidationResults) {
				errorList.item(fvr.message, fvr.status.toStyle());											
			}
		}
		
		if (!errorList.isEmpty()) {
			selectForm.content(errorList);
		}
		
		FormGroup<?> fg = renderTypedElementFormGroup(
				context, 
				obj, 
				feature, 
				Collections.singletonList(feature), 
				obj.eGet(feature), 
				selectForm, 
				featureValidationResults, 
				horizontalForm,
				formRenderingListener,
				appConsumer);
		
		if (fg != null) {
			fg.feedback(!horizontalForm);
		}
		
		return selectForm;
	}
	
	/**
	 * 
	 * @param context
	 * @param obj
	 * @param feature
	 * @return
	 */
	default Object getPlaceholder(C context, T obj, EStructuralFeature feature) throws Exception {
		String ra = getRenderAnnotation(context, feature, RenderAnnotation.PLACEHOLDER);
		return ra != null && obj instanceof CDOObject ? RenderUtil.newJXPathContext(context, (CDOObject) obj).getValue(ra) : null;		
	}
	
	// --- Modal dialogs ---
	
	/**
	 * Type of a modal dialog to render for a particular action - create, edit, invoke EOperation.
	 * @author Pavel Vlasov.
	 *
	 */
	enum ModalType {
		
		NONE,		
		SMALL,
		MEDIUM,
		LARGE
		
	}
	
	// --- Create ---
	
	/**
	 * Modal dialog type to use to display the create form. 
	 * @param context
	 * @return
	 */
	default ModalType getCreateModalType(C context) throws Exception {
		return ModalType.LARGE;
	}
	
	/**
	 * Renders create application which includes a modal dialog with an overlay and a form, documentation modals, and a view model script. 
	 * @param context
	 * @param container
	 * @param containmentFeature
	 * @param nsURI
	 * @param eClassName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Object renderCreateContainmentReferenceElementModalDialogApplication(
			C context, 
			EObject container, 
			EStructuralFeature containmentFeature, 
			EClass featureElementType, 
			String appId,
			Consumer<Object> appConsumer) throws Exception {
		
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();
		ModalType modalType = getCreateModalType(context);
		if (modalType != ModalType.NONE) {		
			String qualifier = containmentFeature.getName()+"/"+featureElementType.getName();
			if (containmentFeature.getEContainingClass().getEPackage() != featureElementType.getEPackage()) {
				qualifier += "@"+featureElementType.getEPackage().getNsURI();
			}
			if (context.authorizeCreate(container, qualifier, null)) {
				Modal formModal = htmlFactory.modal().id(appId+"-modal");
				switch (modalType) {
				case LARGE:
					formModal.large();
					break;
				case SMALL:
					formModal.small();
					break;
				default:
					break;		
				}
				Renderer<C, EObject> renderer = getRenderer(featureElementType);
				
				Tag classDocIcon = renderer.renderDocumentationIcon(context, featureElementType, appConsumer, true);						
				formModal.title(getResourceString(context, "create"), " ", renderer.renderNamedElementIconAndLabel(context, featureElementType), classDocIcon);
				
				Tag overlay = htmlFactory.spinnerOverlay(Spinner.spinner).id(appId+"-overlay").style("display", "none").addClass("nsd-form-overlay");

				// Form elements
				boolean horizontalForm = !"false".equals(renderer.getRenderAnnotation(context, featureElementType, RenderAnnotation.HORIZONTAL_FORM));
				boolean noValidate = "true".equals(renderer.getRenderAnnotation(context, featureElementType, RenderAnnotation.NO_VALIDATE));

				Object oldValue = null;
				EObject instance = featureElementType.getEPackage().getEFactoryInstance().create(featureElementType);
				// Adding the new instance to the object graph for selectors to work. 
				if (containmentFeature.isMany()) {
					((Collection<Object>) container.eGet(containmentFeature)).add(instance);
				} else {
					oldValue = container.eGet(containmentFeature);
					container.eSet(containmentFeature, instance);
				}		
				
				StringBuilder koDataBindings = new StringBuilder();
				StringBuilder koStatusBindings = new StringBuilder();
				Set<ETypedElement> boundElements = new HashSet<>();
				JSONObject labels = new JSONObject();			
								
				try {
					FormRenderingListener<C, EObject, EStructuralFeature> koBinder = new FormRenderingListener<C, EObject, EStructuralFeature>() {
						
						@Override
						public UIElement<?> onFormControlRendering(C context, EObject obj, EStructuralFeature typedElement, Object value, UIElement<?> control) throws Exception {
							if (control instanceof InputBase) {
								if (control instanceof Input && (((Input) control).getType() == InputType.checkbox || ((Input) control).getType() == InputType.radio)) {
									control.knockout().checked("data."+typedElement.getName());							
								} else {
									control.knockout().value("data."+typedElement.getName());
								}
								if (boundElements.add(typedElement)) {
									if (koDataBindings.length() > 0) {
										koDataBindings.append(",").append(System.lineSeparator());
									}
									if (typedElement.isMany()) {
										// TODO - values
										koDataBindings.append(typedElement.getName()+": ko.observableArray()");
									} else {
										// TODO - initial/default values
										koDataBindings.append(typedElement.getName()+": ko.observable('"+StringEscapeUtils.escapeEcmaScript(renderer.getFormControlValue(context, instance, typedElement, value, appConsumer))+"')");									
									}
									
									if (koStatusBindings.length() > 0) {
										koStatusBindings.append(",").append(System.lineSeparator());
									}								
									koStatusBindings.append(typedElement.getName()+": ko.observable()");
									labels.put(typedElement.getName(), renderNamedElementLabel(context, typedElement));									
								}
							}
							
							return super.onFormControlRendering(context, obj, typedElement, value, control);
						}
						
						@Override
						public void onFormGroupRendering(C context, EObject obj, EStructuralFeature typedElement, Object value, FormGroup<?> formGroup) throws Exception {
							formGroup.knockout().css("status."+typedElement.getName());
							super.onFormGroupRendering(context, obj, typedElement, value, formGroup);
						}
						
						@Override
						public void onBeforeFormRendering(C context, EObject obj, Form form) throws Exception {
							// Validation messages.
							ListGroup messages = htmlFactory.listGroup().knockout().foreach("messages");
							Tag labelText = htmlFactory.span().knockout().text("name");
							Tag messageLabel = htmlFactory.label(Style.DEFAULT, labelText).style().margin().right("1em").knockout().visible("name");
							Tag messageText = htmlFactory.span().knockout().text("message");
							messages.item(htmlFactory.fragment(messageLabel, messageText), Style.DEFAULT).knockout().css("style");
							form.content(messages);
							
							super.onBeforeFormRendering(context, obj, form);
						}
						
					};
					
					Form form = renderer.renderEditForm(
							context, 
							instance, 
							Collections.emptyList(), 
							Collections.emptyMap(), 
							horizontalForm,
							koBinder,
							appConsumer)
								.knockout().submit("submit")
								.novalidate(noValidate);
					
					configureForm(form, horizontalForm, modalType);
	
					form.content(htmlFactory.tag(TagName.hr));
					form.button(getResourceString(context, "submit")).type(Button.Type.SUBMIT).style(Style.PRIMARY);
					form.button(getResourceString(context, "cancel")).type(Button.Type.BUTTON).style(Style.DEFAULT).attribute("data-dismiss", "modal");
					
					formModal.body(overlay, form);
					ret.content(formModal);
					
					StringBuilder declarationsBuilder = new StringBuilder();
					declarationsBuilder.append("this.data = {").append(koDataBindings).append("};").append(System.lineSeparator());
					declarationsBuilder.append("this.status = {").append(koStatusBindings).append("};").append(System.lineSeparator());
					declarationsBuilder.append("this.messages = ko.observableArray();").append(System.lineSeparator());

					declarationsBuilder.append("var labels = ").append(labels.toString(4)).append(";").append(System.lineSeparator());
					
					StringBuilder ajaxConfigBuilder = new StringBuilder();
					ajaxConfigBuilder.append("type: 'POST',").append(System.lineSeparator());
					ajaxConfigBuilder.append("contentType: '"+CONTENT_TYPE_APPLICATION_JSON+"',").append(System.lineSeparator());
					ajaxConfigBuilder.append("dataType: 'json',").append(System.lineSeparator());
					ajaxConfigBuilder.append("data: ko.toJSON(this.data),").append(System.lineSeparator());
					
					String encodedPackageNsURI = Hex.encodeHexString(featureElementType.getEPackage().getNsURI().getBytes(/* StandardCharsets.UTF_8.name()? */));		
					String createURL = getRenderer(container).getObjectURI(context, container)+"/reference/"+containmentFeature.getName()+"/create/"+encodedPackageNsURI+"/"+featureElementType.getName()+EXTENSION_JSON;
					
					Map<String, Object> scriptConfig = new HashMap<>();
					scriptConfig.put("app-id", appId);
					scriptConfig.put("url", createURL); 
					scriptConfig.put("declarations", declarationsBuilder.toString()); 
					
					// Success handler
					Map<String,Object> successHandlerConfig = new HashMap<>();
					successHandlerConfig.put("app-id", appId);
					scriptConfig.put("success-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-success-handler.js"), successHandlerConfig));
					
					// Error handler
					Map<String,Object> errorHandlerConfig = new HashMap<>();
					errorHandlerConfig.put("app-id", appId);
					scriptConfig.put("error-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-error-handler.js"), successHandlerConfig));
					
					
					scriptConfig.put("ajax-config", ajaxConfigBuilder.toString());
					ret.content(htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("form-view-model.js"), scriptConfig)));										
				} finally {				
					// Removing the new instance from the object graph. 
					if (containmentFeature.isMany()) {
						((Collection<Object>) container.eGet(containmentFeature)).remove(instance);
					} else {
						container.eSet(containmentFeature, oldValue);
					}					
				}				
			}
		}
		
		return ret;
	}
	
	// --- Edit ---
	
	/**
	 * Modal dialog type to use to display the edit form. 
	 * @param context
	 * @return
	 */
	default ModalType getEditModalType(C context, T obj) throws Exception {
		return ModalType.LARGE;
	}
		
	/**
	 * Renders edit application which includes a modal dialog with an overlay and a form, documentation modals, and a view model script.
	 * @param containerContext if true, the submitted data shall have "container-context" data element set to true, indicating that 
	 * upon update the location shall have context-feature parameter. 
	 * @return
	 */
	default Object renderEditModalDialogApplication(C context, T obj, boolean containerContext, String appId, Consumer<Object> appConsumer) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();
		ModalType modalType = getEditModalType(context, obj);
		if (obj instanceof CDOObject && modalType != ModalType.NONE && context.authorizeUpdate(obj, null, null)) {		
											
			Modal formModal = htmlFactory.modal().id(appId+"-modal");
			switch (modalType) {
			case LARGE:
				formModal.large();
				break;
			case SMALL:
				formModal.small();
				break;
			default:
				break;		
			}
			
			// Object header
			formModal.title(getResourceString(context, "edit"), " ", renderObjectHeader(context, obj, appConsumer));
			
			Tag overlay = htmlFactory.spinnerOverlay(Spinner.spinner).id(appId+"-overlay").style("display", "none").addClass("nsd-form-overlay");

			// Form elements
			EClass eClass = obj.eClass();			
			boolean horizontalForm = !"false".equals(getRenderAnnotation(context, eClass, RenderAnnotation.HORIZONTAL_FORM));
			boolean noValidate = "true".equals(getRenderAnnotation(context, eClass, RenderAnnotation.NO_VALIDATE));
			
			StringBuilder koDataBindings = new StringBuilder();
			StringBuilder koStatusBindings = new StringBuilder();
			
			Set<ETypedElement> boundElements = new HashSet<>();
			JSONObject labels = new JSONObject();			
							
			FormRenderingListener<C, T, EStructuralFeature> koBinder = new FormRenderingListener<C, T, EStructuralFeature>() {
				
				@Override
				public UIElement<?> onFormControlRendering(C context, T obj, EStructuralFeature typedElement, Object value, UIElement<?> control) throws Exception {
					if (control instanceof InputBase) {
						if (control instanceof Input && (((Input) control).getType() == InputType.checkbox || ((Input) control).getType() == InputType.radio)) {
							control.knockout().checked("data."+typedElement.getName());							
						} else {
							control.knockout().value("data."+typedElement.getName());
						}
						if (boundElements.add(typedElement)) {
							if (koDataBindings.length() > 0) {
								koDataBindings.append(",").append(System.lineSeparator());
							}
							if (typedElement.isMany()) {
								JSONArray jsonValue = new JSONArray();
								if (value instanceof Iterable) {
									for (Object ve: (Iterable<?>) value) {
										jsonValue.put(getFormControlValue(context, obj, typedElement, ve, appConsumer));
									}
								}
								koDataBindings.append(typedElement.getName()+": ko.observableArray("+jsonValue+")");
							} else {
								koDataBindings.append(typedElement.getName()+": ko.observable('"+StringEscapeUtils.escapeEcmaScript(getFormControlValue(context, obj, typedElement, value, appConsumer))+"')");									
							}
							
							if (koStatusBindings.length() > 0) {
								koStatusBindings.append(",").append(System.lineSeparator());
							}								
							koStatusBindings.append(typedElement.getName()+": ko.observable()");
							labels.put(typedElement.getName(), renderNamedElementLabel(context, typedElement));
						}
					}
					
					return super.onFormControlRendering(context, obj, typedElement, value, control);
				}
				
				@Override
				public void onFormGroupRendering(C context, T obj, EStructuralFeature typedElement, Object value, FormGroup<?> formGroup) throws Exception {
					formGroup.knockout().css("status."+typedElement.getName());
					super.onFormGroupRendering(context, obj, typedElement, value, formGroup);
				}
				
				@Override
				public void onBeforeFormRendering(C context, T obj, Form form) throws Exception {
					// Validation messages.
					ListGroup messages = htmlFactory.listGroup().knockout().foreach("messages");
					Tag labelText = htmlFactory.span().knockout().text("name");
					Tag messageLabel = htmlFactory.label(Style.DEFAULT, labelText).style().margin().right("1em").knockout().visible("name");
					Tag messageText = htmlFactory.span().knockout().text("message");
					messages.item(htmlFactory.fragment(messageLabel, messageText), Style.DEFAULT).knockout().css("style");
					form.content(messages);
					
					super.onBeforeFormRendering(context, obj, form);
				}
				
			};
			
			Form form = renderEditForm(
					context, 
					obj, 
					Collections.emptyList(), 
					Collections.emptyMap(), 
					horizontalForm,
					koBinder,
					appConsumer)
						.knockout().submit("submit")
						.novalidate(noValidate);
			
			// Optimistic locking
			CDORevision revision = ((CDOObject) obj).cdoRevision();
			if (revision != null) {
				if (koDataBindings.length() > 0) {
					koDataBindings.append(",").append(System.lineSeparator());
				}
				koDataBindings.append("'"+OBJECT_VERSION_KEY+"': ko.observable("+revision.getVersion()+")");
			}
			
			if (containerContext) {
				if (koDataBindings.length() > 0) {
					koDataBindings.append(",").append(System.lineSeparator());
				}
				koDataBindings.append("'.container-context': ko.observable(true)");				
			}
			
			configureForm(form, horizontalForm, modalType);

			form.content(htmlFactory.tag(TagName.hr));
			form.button(getResourceString(context, "submit")).type(Button.Type.SUBMIT).style(Style.PRIMARY);
			form.button(getResourceString(context, "cancel")).type(Button.Type.BUTTON).style(Style.DEFAULT).attribute("data-dismiss", "modal");
			
			formModal.body(overlay, form);
			ret.content(formModal);
			
			StringBuilder declarationsBuilder = new StringBuilder();
			declarationsBuilder.append("this.data = {").append(koDataBindings).append("};").append(System.lineSeparator());
			declarationsBuilder.append("this.status = {").append(koStatusBindings).append("};").append(System.lineSeparator());
			declarationsBuilder.append("this.messages = ko.observableArray();").append(System.lineSeparator());
			
			declarationsBuilder.append("var labels = ").append(labels.toString(4)).append(";").append(System.lineSeparator());
			
			StringBuilder ajaxConfigBuilder = new StringBuilder();
			ajaxConfigBuilder.append("type: 'PUT',").append(System.lineSeparator());
			ajaxConfigBuilder.append("contentType: '"+CONTENT_TYPE_APPLICATION_JSON+"',").append(System.lineSeparator());
			ajaxConfigBuilder.append("dataType: 'json',").append(System.lineSeparator());
			ajaxConfigBuilder.append("data: ko.toJSON(this.data),").append(System.lineSeparator());
			
			String updateURL = getObjectURI(context, obj)+"/update";
			
			Map<String, Object> scriptConfig = new HashMap<>();
			scriptConfig.put("app-id", appId);
			scriptConfig.put("url", updateURL);
			scriptConfig.put("declarations", declarationsBuilder.toString());
			
			// Success handler
			Map<String,Object> successHandlerConfig = new HashMap<>();
			successHandlerConfig.put("app-id", appId);
			scriptConfig.put("success-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-success-handler.js"), successHandlerConfig));
			
			// Error handler
			Map<String,Object> errorHandlerConfig = new HashMap<>();
			errorHandlerConfig.put("app-id", appId);
			scriptConfig.put("error-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-error-handler.js"), successHandlerConfig));
			
			scriptConfig.put("ajax-config", ajaxConfigBuilder.toString());
			ret.content(htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("form-view-model.js"), scriptConfig)));										
		}
		
		return ret;
	}
			
	// --- Select ---
	
	/**
	 * Modal dialog type to use to display the select form. 
	 * @param context
	 * @return
	 */
	default ModalType getSelectModalType(C context, T obj) {
		return ModalType.LARGE;
	}
		
	// --- Add attribute ---
	
	/**
	 * Modal dialog type to use to display the add attribute form. 
	 * @param context
	 * @return
	 */
	default ModalType getAddAttributeModalType(C context, T obj) {
		return ModalType.MEDIUM;
	}
		
	// --- Edit attribute ---
	
	/**
	 * Modal dialog type to use to display the edit form for attribute value. 
	 * @param context
	 * @return
	 */
	default ModalType getEditAttributeModalType(C context, T obj) {
		return ModalType.MEDIUM;
	}
	
	// --- EOperation ---
	
	/**
	 * Modal dialog type to use to display the {@link EOperation} input form.  
	 * @param context
	 * @return NONE for EOperations with confirmation as they are typically input-less or EOperations with "part" parameter bindings (file uploads), and MEDIUM otherwise.
	 */
	default ModalType getEOperationModalType(C context, T obj, EOperation eOperation) throws Exception {
		EOperationTargetInfo info = EOperationTargetInfo.create(context, this, eOperation);
		return info == null || info.getConfirm() instanceof String || info.hasPartParameters() || !info.hasFormParameters() ? ModalType.NONE : ModalType.MEDIUM;
	}		
	
	/**
	 * Renders EOperation application which includes a modal dialog with an overlay, an input form, results div with a close button refreshing the referrer view, 
	 * documentation modals, and a view model script.
	 * @param containerContext if true, the submitted data shall have "container-context" data element set to true, indicating that 
	 * upon update the location shall have context-feature parameter. 
	 * @return
	 */
	default Object renderEOperationModalDialogApplication(C context, T obj, EOperation eOperation, Map<String,String> queryParameters, String appId, Consumer<Object> appConsumer) throws Exception {
		HTMLFactory htmlFactory = getHTMLFactory(context);
		Fragment ret = htmlFactory.fragment();
		ModalType modalType = getEditModalType(context, obj);
		// TODO - proper authorization...
		EOperationTargetInfo info = EOperationTargetInfo.create(context, this, eOperation);
		if (obj instanceof CDOObject && modalType != ModalType.NONE && context.authorize(obj, info.getAction(), info.getQualifier(), null)) {		
			Modal formModal = htmlFactory.modal().id(appId+"-modal");
			switch (modalType) {
			case LARGE:
				formModal.large();
				break;
			case SMALL:
				formModal.small();
				break;
			default:
				break;		
			}
			
			// Dialog title
			formModal.title(getResourceString(context, "execute"), " ", renderNamedElementIconAndLabel(context, eOperation), renderDocumentationIcon(context, eOperation, appConsumer, true));
			
			Tag overlay = htmlFactory.spinnerOverlay(Spinner.spinner).id(appId+"-overlay").style("display", "none").addClass("nsd-form-overlay");

			// Form elements
			boolean horizontalForm = !"false".equals(getRenderAnnotation(context, eOperation, RenderAnnotation.HORIZONTAL_FORM));
			boolean noValidate = "true".equals(getRenderAnnotation(context, eOperation, RenderAnnotation.NO_VALIDATE));
			
			StringBuilder koDataBindings = new StringBuilder();
			StringBuilder koStatusBindings = new StringBuilder();
			JSONObject labels = new JSONObject();			
			
			Set<ETypedElement> boundElements = new HashSet<>();
							
			FormRenderingListener<C, T, EParameter> koBinder = new FormRenderingListener<C, T, EParameter>() {
				
				@Override
				public UIElement<?> onFormControlRendering(C context, T obj, EParameter typedElement, Object value, UIElement<?> control) throws Exception {
					if (control instanceof InputBase) {
						if (control instanceof Input && (((Input) control).getType() == InputType.checkbox || ((Input) control).getType() == InputType.radio)) {
							control.knockout().checked("data."+typedElement.getName());							
						} else {
							control.knockout().value("data."+typedElement.getName());
						}
						if (boundElements.add(typedElement)) {
							if (koDataBindings.length() > 0) {
								koDataBindings.append(",").append(System.lineSeparator());
							}
							if (typedElement.isMany()) {
								JSONArray jsonValue = new JSONArray();
								if (value instanceof Iterable) {
									for (Object ve: (Iterable<?>) value) {
										jsonValue.put(getFormControlValue(context, obj, typedElement, ve, appConsumer));
									}
								}
								koDataBindings.append(typedElement.getName()+": ko.observableArray("+jsonValue+")");
							} else {
								koDataBindings.append(typedElement.getName()+": ko.observable('"+StringEscapeUtils.escapeEcmaScript(getFormControlValue(context, obj, typedElement, value, appConsumer))+"')");									
							}
							
							if (koStatusBindings.length() > 0) {
								koStatusBindings.append(",").append(System.lineSeparator());
							}								
							koStatusBindings.append(typedElement.getName()+": ko.observable()");									
							labels.put(typedElement.getName(), renderNamedElementLabel(context, typedElement));
						}
					}
					
					return super.onFormControlRendering(context, obj, typedElement, value, control);
				}
				
				@Override
				public void onFormGroupRendering(C context, T obj, EParameter typedElement, Object value, FormGroup<?> formGroup) throws Exception {
					formGroup.knockout().css("status."+typedElement.getName());
					super.onFormGroupRendering(context, obj, typedElement, value, formGroup);
				}
				
				@Override
				public void onBeforeFormRendering(C context, T obj, Form form) throws Exception {
					// Validation messages.
					ListGroup messages = htmlFactory.listGroup().knockout().foreach("messages");
					Tag labelText = htmlFactory.span().knockout().text("name");
					Tag messageLabel = htmlFactory.label(Style.DEFAULT, labelText).style().margin().right("1em").knockout().visible("name");
					Tag messageText = htmlFactory.span().knockout().text("message");
					messages.item(htmlFactory.fragment(messageLabel, messageText), Style.DEFAULT).knockout().css("style");
					form.content(messages);
					
					super.onBeforeFormRendering(context, obj, form);
				}
				
			};
			
			Form form = renderInputForm(context, obj, getParameterValues(context, obj, eOperation), Collections.emptyList(), Collections.emptyMap(), horizontalForm, koBinder, appConsumer)
				.novalidate(noValidate)
				.knockout().submit("submit")
				.knockout().visible("result() == undefined");
			
			for (EParameter eParameter: eOperation.getEParameters()) {
				String queryParameterName = info.getQueryParameterName(eParameter);
				if (queryParameterName != null) {					
					String queryParameterValue = queryParameters.get(queryParameterName);
					if (queryParameterValue != null) {
						if (koDataBindings.length() > 0) {
							koDataBindings.append(",").append(System.lineSeparator());
						}
						koDataBindings.append("'"+queryParameterName+"': ko.observable('"+StringEscapeUtils.escapeEcmaScript(queryParameterValue)+"')");				
					}
				}
			}
						
			configureForm(form, horizontalForm, modalType);
			
			form.content(htmlFactory.tag(TagName.hr));
			
			Object buttonContent = info.getRole() == Role.operation ? renderNamedElementIconAndLabel(context, eOperation) : htmlFactory.fragment(renderSaveIcon(context).style().margin().right("5px"), getResourceString(context, "save"));
			form.button(buttonContent).type(Button.Type.SUBMIT).style(Style.PRIMARY);
			form.button(getResourceString(context, "cancel")).type(Button.Type.BUTTON).style(Style.DEFAULT).attribute("data-dismiss", "modal");
						
			Tag resultDiv = htmlFactory.div(
					htmlFactory.div().knockout().html("result"), // Displays result
					htmlFactory.tag(TagName.hr),
					htmlFactory.button(getResourceString(context, "close")).type(Button.Type.BUTTON).style(Style.INFO).knockout().click("close")
			).knockout().visible("result").id(appId+"-result");
			
			formModal.body(overlay, form, resultDiv);
			ret.content(formModal);
			
			StringBuilder declarationsBuilder = new StringBuilder();
			declarationsBuilder.append("this.data = {").append(koDataBindings).append("};").append(System.lineSeparator());
			declarationsBuilder.append("this.status = {").append(koStatusBindings).append("};").append(System.lineSeparator());
			declarationsBuilder.append("this.messages = ko.observableArray();").append(System.lineSeparator());
			declarationsBuilder.append("this.result = ko.observable();").append(System.lineSeparator());
			declarationsBuilder.append("this.location = ko.observable();").append(System.lineSeparator());
			
			declarationsBuilder.append("var labels = ").append(labels.toString(4)).append(";").append(System.lineSeparator());			
			
			declarationsBuilder.append("this.close = function() { $('#"+appId+"-modal').modal('hide'); window.location = this.location(); };").append(System.lineSeparator());
			
			StringBuilder ajaxConfigBuilder = new StringBuilder();
			ajaxConfigBuilder.append("type: 'POST',").append(System.lineSeparator());
			ajaxConfigBuilder.append("contentType: '"+CONTENT_TYPE_APPLICATION_JSON+"',").append(System.lineSeparator());
			ajaxConfigBuilder.append("dataType: 'json',").append(System.lineSeparator());
			ajaxConfigBuilder.append("data: ko.toJSON(this.data),").append(System.lineSeparator());
			
			String executeURL = getObjectURI(context, obj)+"/"+info.getPath();
			
			Map<String, Object> scriptConfig = new HashMap<>();
			scriptConfig.put("app-id", appId);
			scriptConfig.put("url", executeURL);
			scriptConfig.put("declarations", declarationsBuilder.toString());
			
			// Success handler
			Map<String,Object> successHandlerConfig = new HashMap<>();
			successHandlerConfig.put("app-id", appId);
			scriptConfig.put("success-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-success-handler.js"), successHandlerConfig));
			
			// Error handler
			Map<String,Object> errorHandlerConfig = new HashMap<>();
			errorHandlerConfig.put("app-id", appId);
			scriptConfig.put("error-handler", htmlFactory.interpolate(Renderer.class.getResource("form-view-modal-error-handler.js"), successHandlerConfig));
			
			scriptConfig.put("ajax-config", ajaxConfigBuilder.toString());
			ret.content(htmlFactory.tag(TagName.script, htmlFactory.interpolate(Renderer.class.getResource("form-view-model.js"), scriptConfig)));										
		}
		
		return ret;
	}	
	
	/**
	 * Configures form appearance
	 * @param form
	 * @param horizontalForm
	 * @param modalType
	 */
	default void configureForm(Form form, boolean horizontalForm, ModalType modalType) {
		form
			.bootstrap().grid().col(Bootstrap.DeviceSize.EXTRA_SMALL, 12)
			.bootstrap().grid().col(Bootstrap.DeviceSize.SMALL, 12)
			.bootstrap().grid().col(Bootstrap.DeviceSize.MEDIUM, 9)
			.bootstrap().grid().col(Bootstrap.DeviceSize.LARGE, 7);
	
		if (horizontalForm) {
			form
				.horizontal(Bootstrap.DeviceSize.EXTRA_SMALL, 6)
				.horizontal(Bootstrap.DeviceSize.SMALL, 5)
				.horizontal(Bootstrap.DeviceSize.MEDIUM, 4)
				.horizontal(Bootstrap.DeviceSize.LARGE, 3);						
		}		
	}

	default List<org.nasdanika.cdo.web.routes.app.Action<C,T>> getActions(C context, T obj) {
		return Collections.emptyList();
	}
	
}
