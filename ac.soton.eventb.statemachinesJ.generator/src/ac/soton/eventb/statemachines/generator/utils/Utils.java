package ac.soton.eventb.statemachines.generator.utils;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;
import org.eventb.emf.core.machine.Parameter;

import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.Final;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.Initial;
import ac.soton.eventb.statemachines.Junction;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.generator.strings.Strings;

public class Utils {
	private Utils(){
		//DO NOT INSTANTIATE
	}
	
	
	
	
	/**
	 * Returns state's root level statemachine.
	 * @param abs
	 * @return
	 */
	public static Statemachine getRootStatemachine(AbstractNode abs){
		if(abs.eContainer().eContainer() instanceof State){
			return getRootStatemachine((AbstractNode)abs.eContainer().eContainer());
			
		}
		else
			return (Statemachine) abs.eContainer();
	}
	
	/**
	 * Returns statemachines's root level statemachine.
	 * @param sm
	 * @return
	 */
	public static Statemachine getRootStatemachine(Statemachine sm){
		if(isRootStatemachine(sm)) return sm;
		else return getRootStatemachine(getSuperState(sm));
	}
	
	/**
	 * Returns true if statemachine is root level statemachine.
	 * @param sm
	 * @return
	 */
	public static boolean isRootStatemachine(Statemachine sm){
		return !(sm.eContainer() instanceof State);
	}
	
	/**
	 * Returns state's statemachine.
	 * @param abs
	 * @return
	 */
	public static Statemachine getStatemachine(AbstractNode abs){
		return (Statemachine) abs.eContainer();
	}
	/**
	 * Returns statemachine's superstate
	 * @param sm
	 * @return
	 */
	public static State getSuperState(Statemachine sm){
		return (State) sm.eContainer();
	}

	/**
	 *  Returns all abstract nodes that are superstates of input node.
	 *  Result includes input node itself.
	 * @param abs
	 * @return
	 */
	public static List<AbstractNode> getSuperStates(AbstractNode abs){
		List<AbstractNode> ret = new ArrayList<AbstractNode>();
		ret.add(abs);
		if(abs.eContainer().eContainer() instanceof State){
			ret.addAll(getSuperStates((State) abs.eContainer().eContainer()));
		}
		return ret;
		
	}
	
	
	public static boolean hasParentState(Statemachine sm){
		return sm.eContainer() instanceof State;
	}
	
	
	/**
	 * Returns true if statemachine has a final state.
	 * @param sm
	 * @return
	 */
	public static boolean hasFinalState(Statemachine sm){
		
		for(AbstractNode  nd : sm.getNodes()){
			if(nd instanceof Final)
				return true;
		}
		return false;
	}
	
	/**
	 * Returns a sequence of names of the states in a statemachine, each name in curly braces to form a singleton set
	 * @param sm
	 * @return
	 */
	public static List<String> getStateNamesAsSingletons(Statemachine sm){
		List<String> ret = new ArrayList<String>();
		
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof State)
				ret.add(asSet(((State) abs).getName()));
		}
			
		return ret;
	}
	
	/**
	 * Returns a sequence of names of the states in a statemachine.
	 * @param sm
	 * @return
	 */
	public static List<String> getStateNames(Statemachine sm){
		List<String> ret = new ArrayList<String>();
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof State){
				ret.add(((State) abs).getName());
			}
			
		}
		return ret;
	}
	
	/**
	 * Returns string representation of a sequence of strings, separated by separator.
	 * @param inStr
	 * @param separator
	 * @return
	 */
	public static String toString(List<String> inStr, String separator){
		String ret = "";
		for(int i = 0 ; i < inStr.size() ; i++){
			ret += inStr.get(i);
			if (i != inStr.size() -1 ) ret += separator;
		}
		return ret;
	}
	/**
	 * Returns original string surrounded by curly brackets.
	 * @param s
	 * @return
	 */
	public static String asSet(String s){
		return Strings.B_LBRC + s + Strings.B_RBRC;
		
	}
	
	/**
	 * Returns all true States that are directly or indirectly contained in the statemachine
	 * @param s
	 * @return
	 */
	public static List<State> getAllStates(Statemachine sm){
		List<State> ret = new ArrayList<State>();
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof State){
				ret.add((State)abs);
				for(Statemachine ism : ((State) abs).getStatemachines()){
					ret.addAll(getAllStates(ism));
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns true if statemachine contains nested state, which is target for event (transition's elaborated).
	 * @param s
	 * @param event
	 * @return
	 */
	public static boolean containsEventTarget(Statemachine s, Event event){
		for(AbstractNode abs : s.getNodes()){
			if(abs instanceof State){
				State temp = (State) abs;
				for(Transition t : temp.getIncoming())
					if(elaboratesEventBkw(t, event))
						return true;
				for(Statemachine sm : temp.getStatemachines())
					if(containsEventTarget(sm, event))
						return true;
					
			}
		}
		return false;
	}
	
	
	public static boolean containsEventSource(Statemachine s, Event e){
		for(AbstractNode abs : s.getNodes()){
			if(abs instanceof State){
				for(Transition t : ((State)abs).getOutgoing() )
					if(elaboratesEventFwd(t, e)) return true;
				for(Statemachine is : ((State)abs).getStatemachines())
					if(containsEventSource(is, e)) return true;
			}
			
		}
		return false;
	}
	
	
	public static boolean elaboratesEvent(Transition t, Event ev){
		for(Event iev : t.getElaborates()){
			if(iev.equals(ev)) return true;
		}
		
		return elaboratesEventBkw(t, ev) || elaboratesEventFwd(t, ev) ;
	}
	
	
	public static boolean elaboratesEventFwd(Transition t, Event event){
		for(Event ev : t.getElaborates()){
			if(ev.equals(event)) return true;
		}
		
		if(t.getTarget() instanceof Junction){
			Junction temp = (Junction) t.getTarget();
			for(Transition it : temp.getOutgoing())
				if(elaboratesEventFwd(it, event))
					return true;
		}
		
		if(t.getTarget() instanceof Fork){
			Fork temp = (Fork) t.getTarget();
			for(Transition it : temp.getOutgoing())
				if(elaboratesEventFwd(it, event))
					return true;
		}
		
		return false;
	}
	
	
	
	public static boolean elaboratesEventBkw(Transition t, Event event){
		for(Event ev : t.getElaborates()){
			if(ev.equals(event)) return true;
		}
		
		if(t.getSource() instanceof Junction){
			Junction temp = (Junction) t.getSource();
			for(Transition it : temp.getIncoming())
				if(elaboratesEventBkw(it, event))
					return true;
		}
		
		if(t.getSource() instanceof Fork){
			Fork temp = (Fork) t.getSource();
			for(Transition it : temp.getIncoming())
				if(elaboratesEventBkw(it, event))
					return true;
		}
		
		return false;
	}
	
	
	
	public static boolean contains(Statemachine s, AbstractNode node){
		for(AbstractNode abs : s.getNodes()){
			if(abs.equals(node)) return true;
			if(abs instanceof State){
				for(Statemachine ism : ((State)abs).getStatemachines() )
					if(contains(ism, node)) return true;
			}
		}
		return false;
	}

	/**
	 *  Returns starting state found in statemachine i.e. state that is linked by incoming transition from 'Initial' state.
	 * @param sm
	 * @return
	 */
	public static State getStartingState(Statemachine sm){
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof Initial)
				return (State) ((Initial)abs).getOutgoing().get(0).getTarget();
		}
		
		
		
		return null;
	}
	
	/**
	 * Returns starting state found in statemachine i.e. state that is linked by incoming transition from 'Initial' state.
	 * and is target of a transition that elaborates Initialisation
	 * @param sm
	 * @return
	 */
	public static State getStartingStateFromInitialisation(Statemachine sm){
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof Initial)
				for(Transition t :  ((Initial) abs).getOutgoing())
					for(Event e : t.getElaborates())
						if(e.getName().equals(Strings.INIT))
							return (State) t.getTarget();
		}
	
		return null;
	}
	
	
	
	/**
	 * Returns all states that are superstates of input state up to container statemachine.
	 * @param container
	 * @return
	 */
	public static List<State> getSuperstateTo(State s, Statemachine container){
		List<State> ret = new ArrayList<State>();
		if(s.eContainer().equals(container)){
			ret.add(s);
		}
		else
			if(s.eContainer().eContainer() instanceof State){
				ret.addAll(getSuperstateTo((State) s.eContainer().eContainer(), container));
				ret.add(s);
			}
			else
				ret.add(s);
		
		return ret;
	}
	
	/**
	 * Returns true if event and its extensions contain an parameter of specified label.
	 * @param event
	 * @param label
	 * @return
	 */
	public static boolean containsGuardWithName(Event event, String label){
		for(Parameter p : event.getParameters()){
			if(p.getName().equals(label)){
				return true;
			}
			
			
		}
		
		if(event.isExtended() && containsGuardWithName(event.getRefines().get(0), label))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if event and its extensions contain an parameter of specified label. 
	 * @param e
	 * @param label
	 * @return
	 */
	public static boolean containsParameterWithName(Event e, String label){
		for(Parameter p : e.getParameters()){
			if(p.getName().equals(label))
				return true;
		}
		if(e.isExtended())
			return containsParameterWithName(e.getRefines().get(0), label);
		
		
		return false;
	}
	
	
	/**
	 * * Returns true if event and its extensions contain an guard of specified label prefix.
	 * @param e
	 * @param labelPrefix
	 * @return
	 */
	public static boolean containsGuardWithPrefix(Event e, String labelPrefix){
		for(Guard grd : e.getGuards()){
			if(grd.getName().startsWith(labelPrefix))
				return true;
		}
		
		return e.isExtended() &&
				containsGuardWithPrefix(e.getRefines().get(0), labelPrefix);
		
		
	}
	
	/**
	 * Returns true if event and its extensions contain an guard of specified label suffix.
	 * @param e
	 * @param labelPrefix
	 * @return
	 */
	public static boolean containsGuardWithSuffix(Event e, String labelPrefix){
		for(Guard grd : e.getGuards()){
			if(grd.getName().endsWith(labelPrefix))
				return true;
		}
		
		return e.isExtended() &&
				containsGuardWithSuffix(e.getRefines().get(0), labelPrefix);
		
		
	}
	
	
	public static List<AbstractNode> getAllTrueSources(Transition t){
		List<AbstractNode> ret = new ArrayList<AbstractNode>();
		if(t.getSource() instanceof Junction){
			for(Transition it : t.getSource().getIncoming())
				ret.addAll(getAllTrueSources(it));
		}
		else
			ret.add(t.getSource());
		return ret;
	}
	
	
	
	public static List<AbstractNode> getCommonSourceSuperstates(Transition t, List<AbstractNode> base){
		List<AbstractNode> ret = base;
		for(AbstractNode ts : getAllTrueSources(t)){
			List<AbstractNode> superStates = getSuperStates(ts);
			ret.retainAll(superStates);
			
		}
		return ret;
	}
	
	
	public static boolean containsAction(Event event, String label){
		for(Action action : event.getActions()){
			if(action.getName().equals(label))
				return true;
		}
		
		if(event.isExtended() && containsAction(event.getRefines().get(0), label))
			return true;
		return false;
	}
	
	
	/**
	 *  Returns true if transition is local to source state i.e.
	 *  source state is abstract state and target state is its substate.
	 * @param t
	 * @return
	 */
	public static boolean isSelfLoop(Transition t){
		return t.getSource().equals(t.getTarget());
	}
	
	/**
	 * Returns true if transition is local to source state i.e.
	 * source state is state and target state is its substate.
	 * @param t
	 * @return
	 */
	public static boolean isLocalToSource(Transition t){
		if(t.getSource() instanceof State)
			for(Statemachine sm : ( (State)t.getSource()).getStatemachines() )
				if(contains(sm, t.getTarget()))
					return true;
		
		return false;
	}
	
	
	/**
	 * Returns original string surrounded by parentheses.
	 * @param s
	 * @return
	 */
	public static String parenthesize(String s){
		return Strings.B_LPAR + s + Strings.B_RPAR;
	}



	/**
	 * Returns true if statemachine contains a node amongst its states or substates.
	 * @param sm
	 * @param t
	 * @return
	 */
	public static boolean hasExit(Statemachine sm, Transition t) {
		if(t.getTarget() instanceof Final){
			for(AbstractNode node : sm.getNodes())
				if(node.equals(t.getTarget())) 
					return true;
			return false;
		}
		else return false; 
				
	}



	/**
	 * Returns the superstate of a given state
	 * @param sourceState
	 * @return
	 */
	public static State getSuperState(State sourceState) {
		return (State) sourceState.eContainer().eContainer();
	}
	
	
}
