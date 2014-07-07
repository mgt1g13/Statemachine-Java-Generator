package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Transition;

public class TransitionGuard2GuardRule extends AbstractRule  implements IRule {

	/**
	 * TransitionGuard2Guard
	 * 
	 * Adds the transition guard to all events the transition elaborates
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Transition sourceTransition = (Transition) sourceElement;

		for(Event e : sourceTransition.getElaborates()){
			for(Guard grd : sourceTransition.getGuards()){
				ret.add(Make.descriptor(e, guards, Make.guard(grd.getName(), grd.isTheorem(), grd.getPredicate(), grd.getComment()), 10));
			}
		}	
		return ret;
	}
}
