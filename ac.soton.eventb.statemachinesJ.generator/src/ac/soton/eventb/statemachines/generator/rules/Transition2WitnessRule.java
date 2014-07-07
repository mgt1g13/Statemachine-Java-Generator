package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Witness;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.generator.strings.Strings;


/**
 * Works for both kinds of translations
 * @author matheus
 *
 */

public class Transition2WitnessRule extends AbstractRule  implements IRule {
	


	/**
	 * Trasition2Witness
	 * 
	 * Generates the witness for events elaborated by the transition
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
	
		Transition sourceTransition = (Transition) sourceElement;
		
		for(Event ev : sourceTransition.getElaborates()){
			if(!ev.getName().equals(Strings.INIT)){
				List<Witness> generatedWitness = generateWitness(sourceTransition, ev);
				for(Witness w : generatedWitness){
					ret.add(Make.descriptor(ev, witnesses, Make.witness(w.getName(), w.getPredicate()), 10));
				}				
				
			}
		}
			
		return ret;		
	}
	
	/**
	 * Generate the witnesses for a given event from a transition which elaborates it.
	 * @param t
	 * @param event
	 * @return
	 */
	private List<Witness> generateWitness(Transition t, Event event){
		List<Witness> ret = new ArrayList<Witness>();		
		for(Witness w : t.getWitnesses()){
			ret.add(transitionWitness2witness(w));
		}		
		return ret;
	}
	
	/**
	 * Creates a witness from the Transition witness
	 * @param w
	 * @return
	 */
	private Witness transitionWitness2witness(Witness w){
		return w;
	}
	
	
}
