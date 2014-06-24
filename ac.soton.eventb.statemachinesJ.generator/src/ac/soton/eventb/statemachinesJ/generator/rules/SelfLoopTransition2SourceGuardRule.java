package ac.soton.eventb.statemachinesJ.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class SelfLoopTransition2SourceGuardRule extends AbstractRule  implements IRule {

	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		
		return Utils.isSelfLoop((Transition) sourceElement) &&
				((Transition) sourceElement).getSource() instanceof State &&
				Utils.getRootStatemachine(((Transition) sourceElement).getSource()).getInstances() == null; 
	}
	

	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		return true;
	
	}

	
	/**
	 * SelfLoopTransition2Guard
	 * 
	 * Generates guard for self looping events
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Transition sourceTransition = (Transition) sourceElement;
		State sourceState = (State) sourceTransition.getSource();
		
		String name = Strings.ISIN_ + sourceState.getName();
		String predicate = sourceState.getName() + Strings.B_EQ + Strings.B_TRUE;
		
		Guard grd = (Guard) Make.guard(name, predicate);
		
		for(Event e : sourceTransition.getElaborates()){
			ret.add(Make.descriptor(e, guards, grd, 10));
			
		}
		
		return ret;
	
		
	}

}