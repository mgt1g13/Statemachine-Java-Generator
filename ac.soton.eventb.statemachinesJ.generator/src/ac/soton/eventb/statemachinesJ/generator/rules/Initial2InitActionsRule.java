package ac.soton.eventb.statemachinesJ.generator.rules;

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
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class Initial2InitActionsRule extends AbstractRule  implements IRule {

	private Map<State, Boolean> generatedStatus;
	private Statemachine rootSm;
	
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		Transition sourceTransition = (Transition) sourceElement;
		if(!Utils.getRootStatemachine(sourceTransition.getTarget()).getTranslation().equals(TranslationKind.MULTIVAR))
			return false;
	
		
		for(Event e : sourceTransition.getElaborates())
			if(e.getName().equals(Strings.INIT))
				return true;
		return false;

	}


	/**
	 * Initial2InitActions
	 * 
	 * Generates the intialisation actions
	 * Implementing as previous implementation
	 * TODO check if enabling all states reached from a Initial node is correct
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {


		Transition sourceTransition = (Transition) (sourceElement);

		rootSm = (Statemachine) Utils.getRootStatemachine(sourceTransition.getTarget());

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();

		//Map that stores if the init action was generated or not
		generatedStatus = new HashMap<State, Boolean>();
		

	  
		Event initEvent = getInitEvent(sourceTransition);
		List<Action> generatedActions = generateActive(sourceTransition, initEvent);
		generatedActions.addAll(generateInactive(initEvent));

		for(Action a : generatedActions){
			ret.add(Make.descriptor(initEvent, actions, a, 10));
		}


		return ret;

	}

	/**
	 * Get the initialisation event
	 * @param sourceTransition
	 * @return the init event (should never return null)
	 */
	private Event getInitEvent(Transition sourceTransition){
		for(Event e : sourceTransition.getElaborates()){
			if(e.getName().equals(Strings.INIT))
				return e;
		}
		return null;
	}


	/**
	 * Generate initialisations for all states to be initialised as inactive
	 * @return
	 */
	private List<Action> generateInactive(Event event){
		List<Action> ret = new ArrayList<Action>();
		ret.addAll(statemachine2initActionsInactive(rootSm, event));		
		return ret;
	}

	/**
	 * Transforms statemachine to initialisation actions on inactive states.
	 * @param sm
	 * @param event
	 * @return
	 */
	private List<Action> statemachine2initActionsInactive(Statemachine sm, Event event){
		List<Action> ret = new ArrayList<Action>();
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof State){
				ret.addAll(state2initActionsInactive( (State)abs , event ));
			}
		}
		return ret;
	}

	private List<Action> state2initActionsInactive(State s, Event event){
		List<Action> ret = new ArrayList<Action>();
		String value;

		if(rootSm.getInstances() == null)
			value = Strings.B_FALSE;
		else
			value = Strings.B_EMPTYSET;
		if(generatedStatus.get(s) == null)
			ret.add(state2initAction(s, value));

		for(Statemachine sm : s.getStatemachines()){
			ret.addAll(statemachine2initActionsInactive(sm, event));
		}


		return ret;
	}


	/**
	 * Generate initialisations for all states to be initialised as active
	 * @return
	 */
	private List<Action> generateActive(Transition init, Event event){
		List<Action> ret = new ArrayList<Action>();
		State target; 
		
		if(init.getTarget() instanceof State)
			target = (State) init.getTarget();
		else if(init.getTarget() instanceof Fork){
			for(Transition t : ((Fork)init.getTarget()).getOutgoing())
				ret.addAll(generateActive(t, event));
			return ret;
		}
		else
			return ret;
		
		List<AbstractNode> superStates = Utils.getSuperStates(target);

		for(AbstractNode abs : superStates){
			if(abs instanceof State){
				ret.addAll(superState2initActionsActive( (State) abs, event));
			}
		}
		ret.addAll(state2initActionsActive(target,event, target));
		return ret;
	}

	private List<Action> state2initActionsActive(State s, Event v, State target){
		List<Action> ret = new ArrayList<Action>();
		String value;
		//if(Utils.getRootStatemachine(s).getInstances() == null)
		if(rootSm.getInstances() == null)
			value = Strings.B_TRUE;
		else
			//value = Utils.getRootStatemachine(s).getInstances().getName();
			value = rootSm.getInstances().getName();
		if(generatedStatus.get(s) == null)
			ret.add(state2initAction(s, value));

		for(Statemachine sm : s.getStatemachines()){
			if(!Utils.contains(sm, target))
				ret.addAll(statemachine2initActionsActive(sm,v));
		}
		return ret;

	}



	private List<Action> superState2initActionsActive(State s, Event event){
		List<Action> ret = new ArrayList<Action>();
		String value;
//		if(Utils.getRootStatemachine(s).getInstances() == null)
		if(rootSm.getInstances() == null)
			value = Strings.B_TRUE;
		else
//			value = Utils.getRootStatemachine(s).getInstances().getName();
			value = rootSm.getInstances().getName();
		if(generatedStatus.get(s) == null)
			ret.add(state2initAction(s, value));


		for(Statemachine sm : s.getStatemachines()){
			if(!Utils.containsEventTarget(sm, event))
				ret.addAll(statemachine2initActionsActive(sm, event));

		}

		return ret;
	}





	private List<Action> statemachine2initActionsActive(Statemachine s, Event event){
		List<Action> ret = new ArrayList<Action>();
		State target = Utils.getStartingStateFromInitialisation(s);
		if(target == null) return ret; //XXX REPEATED CODE
		for(State is : Utils.getSuperstateTo(target, s)){
			ret.addAll(state2initActionsActive(is, event, target));
		}

		ret.addAll(state2initActionsActive(target, event, target));
		return ret;
	}



	/**
	 * Transforms state to initialisation action.
	 * Generates action for state and event.
	 * Skips transformation if event is extended and contains same action already.
	 * 
	 * @param s
	 * @param value
	 * @return
	 */
	private Action state2initAction(State s, String value){
		//Do nothing if initialisation to the given state has already been done
		//if(generatedStatus.get(s) != null) return null;

		generatedStatus.put(s, new Boolean(true));
		return (Action) Make.action(Strings.INIT_ + s.getName(),
				s.getName() + Strings.B_BEQ + value);
	}


}


