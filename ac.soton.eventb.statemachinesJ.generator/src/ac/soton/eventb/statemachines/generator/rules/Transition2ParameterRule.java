package ac.soton.eventb.statemachines.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Parameter;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;


/**
 * Works for both kinds of translations
 * @author matheus
 *
 */

public class Transition2ParameterRule extends AbstractRule  implements IRule {
	
	/**
	 * Trasition2Parameters
	 * 
	 * Generates the parameter for events elaborated by the transition
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
	
		Transition sourceTransition = (Transition) sourceElement;
		
		for(Event ev : sourceTransition.getElaborates()){
			if(!ev.getName().equals(Strings.INIT)){
				List<Parameter> generatedParameters = generateParameters(sourceTransition, ev);
				for(Parameter p : generatedParameters){
					ret.add(Make.descriptor(ev, parameters, Make.parameter(p.getName(), p.getComment()), 10));
				}
				
				
			}
		}
		
		
		return ret;
		
	}
	
	/**
	 * Method to generate parameters from a given function
	 * @param t
	 * @return
	 */
	private List<Parameter> generateParameters(Transition t, Event event){
		List<Parameter> ret = new ArrayList<Parameter>();
		Statemachine rootSm = Utils.getRootStatemachine(t.getTarget());
		
		if(rootSm.getInstances() != null){
			ret.add(rootStatemachine2parameter(rootSm));
		}
		
		for(Parameter p : t.getParameters()){
			ret.add(transitionParameter2parameter(p));
		}
		
		return ret;
	}
	
	
	
	private Parameter transitionParameter2parameter(Parameter p){
		return (Parameter) Make.parameter(p.getName(),p.getComment());
	}
	
	
	/**
	 * Transforms a root Statemachine into parameter
	 * NOTE: Do not pass a non root state machine to it. No check is done for efficiency 
	 * @param sm
	 * @return
	 */
	private Parameter rootStatemachine2parameter(Statemachine sm){
		return (Parameter) Make.parameter(sm.getSelfName());
	}
	

}
