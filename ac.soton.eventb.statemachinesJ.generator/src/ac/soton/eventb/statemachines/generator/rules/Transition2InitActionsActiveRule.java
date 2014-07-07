package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class Transition2InitActionsActiveRule extends AbstractRule  implements IRule {

	
	/**
	 * TODO The number of calls to contains functions is inefficient
	 * 
	 */
	
	private Statemachine rootSM;
	private Map<AbstractNode, String> mapOfGeneratedActions;
	private Event initEvent;
	
	/**
	 * Transformation only for the the transition that elaborates init
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		Transition sourceTransition = (Transition) sourceElement;

		for(Event e : sourceTransition.getElaborates()){
			if(e.getName().equals(Strings.INIT))
				return true;
		}
		
		return false;
	}

	/**
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		List<Action> generatedActions = new ArrayList<Action>();
		Transition sourceTransition = (Transition) sourceElement;
		
		mapOfGeneratedActions = new  HashMap<AbstractNode, String>();
		rootSM = Utils.getRootStatemachine(sourceTransition.getSource());
		initEvent = getInitEvent(sourceTransition); 
				
		generatedActions.addAll(generateActionsActive(sourceTransition));
		
		
		
		for(Action a : generatedActions){
			ret.add(Make.descriptor(initEvent, actions, a, 1));
		}
		
		return ret;
	}
	
	/**
	 * Finds the init event
	 * @param t
	 * @return
	 */
	private Event getInitEvent(Transition t){
		for(Event e : t.getElaborates()){
			if(e.getName().equals(Strings.INIT))
				return e;
		}
		return null;
	}
	
	
	/**
	 * Transforms active target state to initialisation actions.
	 * Generates actions for all the superstates of target state,
	 * as well as actions for target state's statemachines.
	 * 
	 * 
	 * @param sourceTransition
	 * @return
	 */
	private List<Action> generateActionsActive(Transition sourceTransition) {
		List<Action> ret = new ArrayList<Action>();
		List<AbstractNode> superStates = Utils.getSuperStates(sourceTransition.getTarget());
		
		if(sourceTransition.getTarget() instanceof State)
			ret.addAll(state2initActionsActive((State) sourceTransition.getTarget(), (State) sourceTransition.getTarget()));
		else if(sourceTransition.getTarget() instanceof Fork)
			for(Transition t : ((Fork)sourceTransition.getTarget()).getOutgoing())
				ret.addAll(generateActionsActive(t));
		for(AbstractNode node : superStates){
			if(node instanceof State)
				ret.addAll(superState2InitActionsActive((State) node));
		}
		
		return ret;
	}
	
	/**
	 * Generates the initialization actions for the superstates
	 * @param s
	 * @return
	 */
	private List<Action> superState2InitActionsActive(State s) {
		List<Action> ret = new ArrayList<Action>();
		if(mapOfGeneratedActions.get(s) == null)
			ret.add(state2initActionActive(s));
		
		
		for(Statemachine sm : s.getStatemachines()){
			if(!Utils.containsEventTarget(sm, initEvent)){
				ret.addAll(statemachine2initActionsActive(sm));
			}
		}
		return ret;
	}

	
	/**
	 * Transforms active statemachine to initialisation actions.
	 * Finds initial state in statemachine (state, linked by transition with Initial state)
	 * and generates initialisation actions for all superstates of found initial state,
	 * as well as actions for initial state itself and its statemachines.
	 * @param sm
	 * @return
	 */
	private List<Action> statemachine2initActionsActive(Statemachine sm) {
		List<Action> ret = new ArrayList<Action>();
		State target = Utils.getStartingStateFromInitialisation(sm);
		if(target == null) return ret;
		for(State s : Utils.getSuperstateTo(target, sm))
			ret.addAll(state2initActionsActive(s, target));
		
		ret.addAll(state2initActionsActive(target, target));
		return ret;
	}

	
	
	private List<Action> state2initActionsActive(State s, State target) {
		List<Action> ret = new ArrayList<Action>();
		if(mapOfGeneratedActions.get(s) == null)
			ret.add(state2initActionActive(s));
		
		for(Statemachine sm : s.getStatemachines()){
			if(!Utils.contains(sm, target))
				ret.addAll(statemachine2initActionsActive(sm));
		}
		return ret;
	}

	private Action state2initActionActive(State s) {
		String name;
		String expression = "";
		if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR)){
			name = Strings.INIT_ + Utils.getStatemachine(s).getName();
			if(rootSM.getInstances() == null)
				expression = Utils.getStatemachine(s).getName() + Strings.B_BEQ + s.getName();
			else
				expression = Utils.getStatemachine(s).getName() + Strings.B_BEQ + rootSM.getInstances().getName() 
				+ Strings.B_CPROD + Utils.asSet(s.getName());
		}
		else if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR)){
			name = Strings.INIT_ + s.getName();
			if(rootSM.getInstances() == null)
				expression = s.getName() + Strings.B_BEQ  + Strings.B_TRUE;
			else
				expression = s.getName() + Strings.B_BEQ + rootSM.getInstances().getName();
			
		}
		else{
			name = "ERROR";
			expression = Strings.TRANSLATION_KIND_NOT_SUPPORTED_ERROR;
		}
		mapOfGeneratedActions.put(s, expression);
		return (Action) Make.action(name, expression);
	}

		
}
