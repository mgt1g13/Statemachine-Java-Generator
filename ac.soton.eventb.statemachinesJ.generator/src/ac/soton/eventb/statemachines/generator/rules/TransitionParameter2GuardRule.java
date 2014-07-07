package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;

import ac.soton.eventb.emf.core.extension.coreextension.TypedParameter;
import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.generator.strings.Strings;

public class TransitionParameter2GuardRule extends AbstractRule  implements IRule {

	/**
	 * TransitionGuard2Guard
	 * 
	 * Adds the transition guard to all events the transition elaborates
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Transition sourceTransition = (Transition) sourceElement;

		for(TypedParameter p : sourceTransition.getParameters()){
			
			Guard grd = generateTransitionGuardFromTypedParameter(p);
			for(Event e : sourceTransition.getElaborates()){
				ret.add(Make.descriptor(e, guards, Make.guard(grd.getName(), grd.getPredicate()), 10));
			}
			
		}	
		return ret;
	}
	
	
	/**
	 * Generate a guard from a given predicate
	 * @param p
	 * @return
	 */
	private Guard generateTransitionGuardFromTypedParameter(TypedParameter p){
		return (Guard) Make.guard(p.getName() + Strings._TYPE, 
				p.getName() + Strings.B_IN + p.getType());
	}
}
