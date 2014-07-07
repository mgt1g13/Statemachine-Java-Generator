package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Find;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class Transition2EnterActionRule extends AbstractRule  implements IRule {
	
	private Statemachine rootSM;

	
	/**
	 * Rule should only fire on non circular transitions. Note, No check is done
	 * to whether the event already contains an action named as the one generated.
	 * The generator does not replace the user generated with the automatic one, 
	 * and simply discard the latter.
	 * 
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		return !Utils.isSelfLoop((Transition)sourceElement);
	}
	
	
	/**
	 * Trasition2EnterAction
	 * 
	 * Generates enter actions from a transition and add it to the events the transition elaborates
	 * 
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Transition sourceTransition = (Transition) sourceElement;
		List<Action> generatedActions = new ArrayList<Action>();
		
		rootSM = Utils.getRootStatemachine(sourceTransition.getTarget());
		
		
		if(sourceTransition.getTarget() instanceof State) {
			generatedActions.addAll(generateEnterActions(sourceTransition));
			 
		}
		else
			//Generate the actions for all outgoing transition from a fork
			if(sourceTransition.getTarget() instanceof Fork){
				for(Transition t : ((Fork)sourceTransition.getTarget()).getOutgoing()){
					generatedActions.addAll(generateEnterActions(t));
				}
			
			}
		
		for(Event ev : sourceTransition.getElaborates()){
			if(!ev.getName().equals(Strings.INIT)){
				for(Action a : generatedActions){
					if (!a.getName().equals("") &&
							Find.generatedElement(generatedElements, ev, actions, a.getName()) == null){
						ret.add(Make.descriptor(ev, actions, Make.action(a.getName(), a.getAction()), 10));
					}
				}				
				
			}
		}

		return ret;		
	} 


	private List<Action> generateEnterActions(Transition t){
		List<Action> ret = new ArrayList<Action>();
		
		List<AbstractNode> target = Utils.getSuperStates(t.getTarget());
		target.removeAll(Utils.getCommonSourceSuperstates(t, Utils.getSuperStates(t.getTarget())));
		
		for(AbstractNode abs : target){
			if(abs instanceof State){
				ret.add(state2enterAction( (State) abs ));
			}
			if(abs instanceof Fork){
				for(Transition it : ((Fork)abs).getOutgoing())
					ret.addAll(generateEnterActions(it));			
				}
		}
		
		return ret;
		
	}
	
	/**
	 * Generates an enter action for the given state
	 * @param s
	 * @return
	 */
	private Action state2enterAction(State s){
		String name = Strings.ENTER_ + s.getName();

		
		if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR)){
			
			return (Action) Make.action(name, generateMultivarAction(s));
		}
		else if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR)){
			return (Action) Make.action(name, generateSinglevarAction(s));
		}
		else
			return (Action) Make.action(name, "<Statemachine translation error: unknown translation kind>");
		
	}
	
	/**
	 * Generate the action on a MULTIVAR translation
	 * 
	 * @param s
	 * @return
	 */
	private String generateMultivarAction(State s) {
		if(rootSM.getInstances() == null)
			return s.getName() + Strings.B_BEQ + Strings.B_TRUE;
		else
			return s.getName() + Strings.B_BEQ + s.getName() + Strings.B_UNION +
					Utils.asSet(rootSM.getSelfName());
	}

	/**
	 * Generate the action on a SINGLEVAR translation
	 * @param s
	 * @return
	 */
	private String generateSinglevarAction(State s) {
		if(rootSM.getInstances() == null)
			return Utils.getStatemachine(s).getName() + Strings.B_BEQ + s.getName();
		else
			return Utils.getStatemachine(s).getName() + Utils.parenthesize(rootSM.getSelfName())+
					Strings.B_BEQ + s.getName();

	}
	
}
