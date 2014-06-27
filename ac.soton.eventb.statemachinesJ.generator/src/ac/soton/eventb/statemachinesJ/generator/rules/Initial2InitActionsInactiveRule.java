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
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class Initial2InitActionsInactiveRule extends AbstractRule  implements IRule {

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


	@Override
	public boolean fireLate() {
		return true;
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
		List<Action> generatedActions = (generateInactive(initEvent));

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
	
	private Action state2initAction(State s, String value){
		//Do nothing if initialisation to the given state has already been done
		//if(generatedStatus.get(s) != null) return null;

		generatedStatus.put(s, new Boolean(true));
		return (Action) Make.action(Strings.INIT_ + s.getName(),
				s.getName() + Strings.B_BEQ + value);
	}


}


