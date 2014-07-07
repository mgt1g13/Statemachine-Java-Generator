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
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class SelfLoopTransition2SourceGuardRule extends AbstractRule  implements IRule {

	private Statemachine rootSM;
	/**
	 * Works for both translations
	 * 
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		
		return Utils.isSelfLoop((Transition) sourceElement) &&
				((Transition) sourceElement).getSource() instanceof State; 
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
		rootSM = Utils.getRootStatemachine(sourceState);
		
		String name = Strings.ISIN_ + sourceState.getName();
		String predicate = generatePredicate(sourceState);
		
		Guard grd = (Guard) Make.guard(name, predicate);
		
		for(Event e : sourceTransition.getElaborates()){
			ret.add(Make.descriptor(e, guards, Make.guard(grd.getName(), grd.isTheorem(), grd.getPredicate(),grd.getComment()), 10));
			
		}
		
		return ret;
	
		
	}
	
	private String generatePredicate(State sourceState){
		if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR))
			return generatePredicateForMultivar(sourceState);
		else if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR))
			return generatePredicateForSinglevar(sourceState);
		else
			return Strings.TRANSLATION_KIND_NOT_SUPPORTED_ERROR;
			
			
	}
	/**
	 * Generates the predicate for Variables translation
	 * @param sourceState
	 */
	private String generatePredicateForMultivar(State sourceState){
		if(rootSM.getInstances() == null)
			return sourceState.getName() + Strings.B_EQ + Strings.B_TRUE;
		else
			return rootSM.getSelfName() + Strings.B_IN +sourceState.getName(); //Assuming that the variable that refers to state
																				//has the same name as the state
	}
	
	/**
	 * Generates the predicate for Enumeration translation
	 * @param sourceState
	 */
	private String generatePredicateForSinglevar(State sourceState){
		if(rootSM.getInstances() == null)
			return  Utils.getStatemachine(sourceState).getName() + Strings.B_EQ + sourceState.getName();
		else
			return Utils.getStatemachine(sourceState).getName() + Utils.parenthesize(rootSM.getSelfName())+
					Strings.B_EQ + sourceState.getName();

	}
	
}
