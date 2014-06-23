package ac.soton.eventb.statemachinesJ.generator.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.Any;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.Junction;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class Transition2LeaveActionRule extends AbstractRule  implements IRule {
	
	private Statemachine rootSM;
	private HashMap<Event, ArrayList<String>> generatedElements = new HashMap<Event, ArrayList<String>>();
	
	/**
	 * Rule should only fire on non circular transitions
	 * 
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		return !Utils.isSelfLoop((Transition)sourceElement);
	}
	
	
	/**
	 * Trasition2LeaveAction
	 * 
	 * Generates leave actions from a transition and add it to the events the transition elaborates
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Transition sourceTransition = (Transition) sourceElement;
		List<Action> generatedActions = new ArrayList<Action>();
		
		rootSM = Utils.getRootStatemachine(sourceTransition.getTarget());
		

		for(Event ev : sourceTransition.getElaborates()){
			generatedActions.clear();
			if(!ev.getName().equals(Strings.INIT)){
				generatedActions.addAll(generateLeaveActionsFromNode(sourceTransition.getSource(), sourceTransition, ev));
			}
			for(Action a : generatedActions){
				ret.add(Make.descriptor(ev, actions, a, 1));
			}
		}



		return ret;		
	} 

	/**
     * recurses up the chain of incomers until a real source state is found 
 	 * then invokes sourceState to leave Actions
	 * @param node
	 * @param t
	 * @return
	 */
	private List<Action> generateLeaveActionsFromNode(AbstractNode node, Transition t, Event e){
		List<Action> ret = new ArrayList<Action>();
		if(node instanceof State){
			return sourceState2leaveAction((State) node, t, e);
		}
		else if(node instanceof Junction){
			
			for(Transition it : ( (Junction)node ).getIncoming() ){
				ret.addAll(generateLeaveActionsFromNode(it.getSource(), t, e));
			}
			return ret;
			
		}
		else if(node instanceof Fork){
			for(Transition it : ( (Fork)node ).getIncoming() ){
				ret.addAll(generateLeaveActionsFromNode(it.getSource(), t, e));
			}
			return ret;
			
			
		}
		else if (node instanceof Any){ //Node can only be an Any node
			ret.addAll(any2LeaveActions( (Any) node , t ,e));
		}
		return ret;
	}
	
	/**
	 * Generate actions for nodes of type Any
	 * @param node
	 * @param t
	 * @param e
	 * @return
	 */
	private List<Action> any2LeaveActions(Any node, Transition t, Event e) {
		List<Action> ret = new ArrayList<Action>();
		List<AbstractNode> target = new ArrayList<AbstractNode>();
		
		target.addAll(Utils.getAllStates(Utils.getStatemachine(node)));
		target.removeAll(Utils.getSuperStates(t.getTarget()));
		
		for(AbstractNode abs : target){
			if(abs instanceof State)
				ret.addAll(sourceState2leaveAction((State)abs, t, e));
		}
		
		return ret;
	}

	/**
	 * Generates actios for a given source state
	 * @param s
	 * @param t
	 * @param e
	 * @return
	 */
	private List<Action> sourceState2leaveAction(State s, Transition t, Event e){
		List<Action> ret = new ArrayList<Action>();
		if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR)){
			return generateLeaveActionsForSinglevar(s, t, e);
		}
		
		
		else if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR)){
			return generateLeaveActionsForMultivar(s, t, e);
			
		}
		
		
		return ret;
	}

	/**
	 * Generate leave actions for Multivar (variables) translation.
	 * 
	 * @param s
	 * @param t
	 * @param e
	 * @return
	 */
	private List<Action> generateLeaveActionsForMultivar(State s, Transition t, Event e) {
		List<Action> ret = new ArrayList<Action>();
		List<AbstractNode> target = Utils.getSuperStates(s);
		target.removeAll(Utils.getSuperStates(t.getTarget()));

		for(AbstractNode node : target){
			if(node instanceof State)
				ret.addAll(vars_superState2leaveActions((State)node, e, t));
		}
		
		for(Statemachine sm : s.getStatemachines()){
			if(!(Utils.isLocalToSource(t) && !Utils.contains(sm, t.getTarget())))
				ret.addAll(vars_statemachine2leaveActions(sm, e));	
		}	
		return ret;
	}

	/**
	 * Create leave actions for statemachines
	 * @param sm
	 * @param e
	 * @return
	 */
	private List<Action> vars_statemachine2leaveActions(Statemachine sm, Event e) {
		List<Action> ret = new ArrayList<Action>();
		
		for(AbstractNode abs : sm.getNodes())
			if(abs instanceof State)
				ret.addAll(vars_state2leaveActions((State)abs, e));
		return ret;
	}

	/**
	 * Generates leave event for a state and its statemachines
	 * @param s
	 * @param e
	 * @return
	 */
	private List<Action> vars_state2leaveActions(State s, Event e) {
		List<Action> ret = new ArrayList<Action>();
		
		if(canGenerateLeaveEvent(s, e))
			ret.add(vars_state2leaveAction(s, e));
		
		for(Statemachine sm : s.getStatemachines()){
			ret.addAll(vars_statemachine2leaveActions(sm, e));
		}
		
		return ret;
	}

	

	private Action vars_state2leaveAction(State s, Event e) {
		String action;
		String name = Strings.LEAVE_ + s.getName();
		if(rootSM.getInstances() == null){
			action = s.getName() + Strings.B_BEQ + Strings.B_FALSE;
		}
		else
			action = s.getName() + Strings.B_BEQ + s.getName() + Strings.B_SETMINUS + 
						Utils.asSet(rootSM.getSelfName());
		
		
		if(generatedElements.get(e) == null){
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(name);
		}
		return (Action) Make.action(Strings.LEAVE_ + s.getName(), action);
	}


	private List<Action> vars_superState2leaveActions(State node, Event e, Transition t) {
		List<Action> ret = new ArrayList<Action>();
		
		if(canGenerateLeaveEvent(node, e))
			ret.add(vars_state2leaveAction(node, e));
		
		for(Statemachine s : node.getStatemachines()){
			if(!Utils.containsEventSource(s,e))
				ret.addAll(vars_statemachine2leaveActions(s, e));
			
		}
		return ret;
	}

	/**
	 * Checks if a certain action to be generated already exists on a given event
	 * XXX needs checking
	 * @param s
	 * @param e
	 * @return
	 */
	private boolean canGenerateLeaveEvent(State s, Event e){
		return !Utils.containsAction(e, Strings.ENTER_ + s.getName()) &&
				//&& !Utils.containsAction(e, Strings.LEAVE_ + s.getName()) 
				/*&&*/ (generatedElements.get(e) == null || !generatedElements.get(e).contains(Strings.LEAVE_ + s.getName()));
	}

	private List<Action> generateLeaveActionsForSinglevar(State s, Transition t, Event e) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
