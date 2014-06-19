package ac.soton.eventb.statemachinesJ.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Invariant;
import org.eventb.emf.core.machine.impl.MachineImpl;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class State2SubstateInvariantRule extends AbstractRule  implements IRule {

	
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		return true;
	
	}

	
	/**
	 * States2SubstateInvariants
	 * TODO CHECK LIFTED STATE MACHINES!!!
	 * Generates a new substate invariant for a state.
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		State sourceState = (State)sourceElement;
		if(!(sourceState.eContainer().eContainer() instanceof MachineImpl) && sourceState.getRefines() == null){//If it is not a state from the root statemachine
			State parentState = (State) sourceState.eContainer().eContainer();
			Invariant newInvariant = Make.invariant(sourceState.getName() + Strings._SUBSTATEOF_ + parentState.getName(),
									Utils.parenthesize(sourceState.getName() + Strings.B_EQ + Strings.B_TRUE) + Strings.B_IMPL + Utils.parenthesize(parentState.getName() + Strings.B_EQ + Strings.B_TRUE), "");
			ret.add(Make.descriptor(container, invariants, newInvariant, 8));
		}
		

		
		return ret;
		
	}

	
}