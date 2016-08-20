package org.nasdanika.cdo.web.doc.story;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.nasdanika.cdo.web.doc.DependencyTracer;
import org.nasdanika.story.Scenario;
import org.nasdanika.story.State;

class StateTransitionDiagramSpecGenerator implements DiagramSpecGenerator {

	private StoryDocumentationGenerator storyDocumentationGenerator;

	public StateTransitionDiagramSpecGenerator(StoryDocumentationGenerator storyDocumentationGenerator) {
		this.storyDocumentationGenerator = storyDocumentationGenerator;
	}


	@Override
	public boolean hasDiagram(EObject obj) {
		if (isDiagramElement(obj)) {
			return true;
		}
		
		TreeIterator<EObject> tit = obj.eAllContents();
		while (tit.hasNext()) {
			if (isDiagramElement(tit.next())) {
				return true;
			}
		}
		
		return false;		
	}
		
	private static DependencyTracer<EObject> IN_DEPENDENCY_TRACER = new DependencyTracer<EObject>() {

		@Override
		protected Iterable<EObject> getDependencies(EObject obj) {
			Set<EObject> ret = new HashSet<>();
			TreeIterator<Notifier> tit = obj.eResource().getResourceSet().getAllContents();
			while (tit.hasNext()) {
				Notifier next = tit.next();
				if (next instanceof Scenario && ((Scenario) next).getOutcomeState() == obj) {
					ret.addAll(((Scenario) next).getContextStates());
				}
			}
			return ret;
		}
		
	}; 		
	
	private static DependencyTracer<EObject> OUT_DEPENDENCY_TRACER = new DependencyTracer<EObject>() {

		@Override
		protected Iterable<EObject> getDependencies(EObject obj) {			
			Set<EObject> ret = new HashSet<>();
			TreeIterator<Notifier> tit = obj.eResource().getResourceSet().getAllContents();
			while (tit.hasNext()) {
				Notifier next = tit.next();
				if (next instanceof Scenario && ((Scenario) next).getContextStates().contains(obj) && ((Scenario) next).getOutcomeState() != null) {
					ret.add(((Scenario) next).getOutcomeState());
				}
			}
			return ret;
		}
		
	}; 		
	
	private static boolean isDiagramElement(EObject obj) {
		return obj instanceof State;
	}	
	
	private static class StateEntry {
		
		public StateEntry(int id) {
			this.id = id;
		}
		
		int id;
		List<Scenario> outboundScenarios = new ArrayList<>();
	}		

	@SuppressWarnings("unchecked")
	@Override
	public void diagramSpec(EObject obj, int depth, Direction direction, StringBuilder specBuilder) {
		Set<EObject> diagramElements = new HashSet<EObject>();
		if (isDiagramElement(obj)) {
			diagramElements.add(obj);			
		}
		
		TreeIterator<EObject> tit = obj.eAllContents();
		while (tit.hasNext()) {
			EObject next = tit.next();
			if (isDiagramElement(next)) {
				diagramElements.add(next);
			}
		}
		
		switch (direction) {
		case both:
			diagramElements = IN_DEPENDENCY_TRACER.trace(diagramElements, depth, OUT_DEPENDENCY_TRACER);
			break;
		case in:
			diagramElements = IN_DEPENDENCY_TRACER.trace(diagramElements, depth);
			break;
		case out:
			diagramElements = OUT_DEPENDENCY_TRACER.trace(diagramElements, depth);
			break;
		default:
			break;
		
		}
		
		int counter = 0;		
				
		Map<State, StateEntry> deMap = new HashMap<>();
		for (EObject de: diagramElements) {
			if (de instanceof State) {
				deMap.put((State) de, new StateEntry(counter++));
			}
		}		
		
		TreeIterator<Notifier> rstit = obj.eResource().getResourceSet().getAllContents();
		while (rstit.hasNext()) {
			Notifier next = rstit.next();
			if (next instanceof Scenario) {
				for (Entry<State, StateEntry> e: deMap.entrySet()) {
					if (((Scenario) next).getContextStates().contains(e.getKey())) {
						e.getValue().outboundScenarios.add((Scenario) next);
					}
				}
			}
		}
		
		for (StateEntry ev: deMap.values()) {
			Collections.sort(ev.outboundScenarios, CatalogDocumentationGenerator.CATALOG_ELEMENT_NAME_COMPARATOR);			
		}
		

		for (EObject de: diagramElements) {
			if (de instanceof State) {
				diagramElementDefinition((State) de, deMap, specBuilder);
			}
		}		
		for (EObject de: diagramElements) {
			if (de instanceof State) {
				diagramElementRelations((State) de, deMap, specBuilder);
			}
		}		
	}
	
	protected void diagramElementDefinition(State diagramElement, Map<State, StateEntry> deMap, StringBuilder specBuilder) {
		specBuilder
			.append("state \"")
			.append(diagramElement.getName())
			.append("\" as DE")
			.append(deMap.get(diagramElement).id)
			.append(System.lineSeparator());
		
	}

	protected void diagramElementRelations(State diagramElement, Map<State, StateEntry> deMap, StringBuilder specBuilder) {
		for (Scenario os: deMap.get(diagramElement).outboundScenarios) {
			if (os.getOutcomeState() != null && deMap.containsKey(os.getOutcomeState())) {
				specBuilder
					.append("DE")
					.append(deMap.get(diagramElement).id)
					.append(" --> DE")
					.append(deMap.get(os.getOutcomeState()).id)
					.append(" : ")
					.append(os.getName())
					.append(System.lineSeparator());
			}
		}
	}

	@Override
	public String getLabel() {
		return "State Transition";
	}

	@Override
	public String getName() {
		return "state-transition";
	}

}
