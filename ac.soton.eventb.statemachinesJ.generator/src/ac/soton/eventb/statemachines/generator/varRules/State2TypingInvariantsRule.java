package ac.soton.eventb.statemachines.generator.varRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Invariant;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class State2TypingInvariantsRule extends AbstractRule  implements IRule  {
	
	/**
	 * Rule not to be applied on lifted statemachines
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		State sourceState = (State) sourceElement;
		return Utils.getRootStatemachine(sourceState).getTranslation().equals(TranslationKind.MULTIVAR) &&
				(sourceState.getRefines() == null) && 
				Utils.getRootStatemachine(sourceState).getInstances() == null; //If it is not a state from the root statemachine
	}
	
	
	/**
	 * Generates a new variable named as the states it represents
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		State sourceState = (State) sourceElement;
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceState);
	
		Invariant newInvariant = Make.invariant(Strings.TYPEOF_ + sourceState.getName(), generatePredicate(sourceState), "");
		
		ret.add(Make.descriptor(container, invariants, newInvariant, 1));
		return ret;
	}
	/**
	 * Calculates the predicate needed for the given State
	 * 
	 * @param sourceState
	 * @return
	 */
	private String generatePredicate (State sourceState){

			return sourceState.getName() + Strings.B_IN + Strings.B_BOOL;

	}
}
