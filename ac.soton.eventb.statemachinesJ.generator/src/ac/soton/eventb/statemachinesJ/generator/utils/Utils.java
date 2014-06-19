package ac.soton.eventb.statemachinesJ.generator.utils;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Parameter;

import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.Final;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.Initial;
import ac.soton.eventb.statemachines.Junction;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;

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
	
	/**
	 * Returns true if statemachine has a final state.
	 * @param sm
	 * @return
	 */
	//TODO try to improve efficiency 
	public static boolean hasFinalState(Statemachine sm){
		
		for(AbstractNode  nd : sm.getNodes()){
			if(nd instanceof Final)
				return true;
		}
		return false;
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
				if(elaboratesEvent(it, event))
					return true;
		}
		
		if(t.getTarget() instanceof Fork){
			Fork temp = (Fork) t.getTarget();
			for(Transition it : temp.getOutgoing())
				if(elaboratesEvent(it, event))
					return true;
		}
		
		return false;
	}
	
	
	
	public static boolean elaboratesEventBkw(Transition t, Event event){
		for(Event ev : t.getElaborates()){
			if(ev.equals(event)) return true;
		}
		
		if(t.getTarget() instanceof Junction){
			Junction temp = (Junction) t.getTarget();
			for(Transition it : temp.getIncoming())
				if(elaboratesEvent(it, event))
					return true;
		}
		
		if(t.getTarget() instanceof Fork){
			Fork temp = (Fork) t.getTarget();
			for(Transition it : temp.getIncoming())
				if(elaboratesEvent(it, event))
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
	 * Returns original string surrounded by parentheses.
	 * @param s
	 * @return
	 */
	public static String parenthesize(String s){
		return Strings.B_LPAR + s + Strings.B_RPAR;
	}
	
	
}
