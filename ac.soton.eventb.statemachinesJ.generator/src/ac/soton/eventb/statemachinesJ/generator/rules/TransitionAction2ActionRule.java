package ac.soton.eventb.statemachinesJ.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;

public class TransitionAction2ActionRule extends AbstractRule  implements IRule {
	
	//TODO Check what happens if there is already a witness with the generated name
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		return true;
	
	}

	/**
	 * TrasitionAction2Action
	 * 
	 * Generates actions from transition actions
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
	
		Transition sourceTransition = (Transition) sourceElement;
		List<Action> generatedActions = generateActions(sourceTransition);
		
		
		for(Event ev : sourceTransition.getElaborates()){
			if(!ev.getName().equals(Strings.INIT)){
				for(Action a : generatedActions){
					ret.add(Make.descriptor(ev, actions, a, 10));
				}				
				
			}
		}
			
		return ret;		
	} 



	/**
	 * Calculate the actions from a given Transition
	 * @param sourceTransition
	 * @return
	 */
	private List<Action> generateActions(Transition sourceTransition){
		List<Action> ret = new ArrayList<Action>();
		
		for(Action a : sourceTransition.getActions()){
			ret.add(transitionAction2Action(a));
		}	
		return ret;
	}
	
	
	
	/**
	 * Generates an action from another action
	 * TODO checkif there is no problem in returning a only
	 * @param a
	 * @return
	 */
	private Action transitionAction2Action(Action a){
		//return (Action) Make.action(a.getName(),a.getAction(), a.getComment());
		return a;
	}
	
	
	
}