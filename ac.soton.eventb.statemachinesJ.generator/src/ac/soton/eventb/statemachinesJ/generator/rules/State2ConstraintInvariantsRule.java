package ac.soton.eventb.statemachinesJ.generator.rules;

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
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class State2ConstraintInvariantsRule extends AbstractRule  implements IRule {


	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		return true;

	}


	/**
	 * State2ConstraintInvariant
	 * 
	 * Generates new invariants based on state invariants
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		List<Invariant> newInvariants = new ArrayList<Invariant>();

		State sourceState = (State) sourceElement;
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);


		for(Invariant inv : sourceState.getInvariants()){
			newInvariants.add(Make.invariant(inv.getName(),
					generatePredicate(sourceState, inv), ""));


		}
		for (Invariant inv : newInvariants){
			ret.add(Make.descriptor(container, invariants, inv, 10));

		}


		return ret;

	}
	/**
	 * Generates the predicate from a state and its invariant
	 * 
	 * @param s
	 * @param inv
	 * @return
	 */
	private String generatePredicate(State s, Invariant inv){
		if(Utils.getRootStatemachine(s).getInstances() == null)
			return Utils.parenthesize(s.getName() + Strings.B_EQ + Strings.B_TRUE) + Strings.B_IMPL + Utils.parenthesize(inv.getPredicate());
		else
			return Strings.B_FORALL + Utils.getRootStatemachine(s).getSelfName() + Strings.B_MIDDOT +
					Utils.parenthesize(Utils.getRootStatemachine(s).getSelfName() + Strings.B_IN + s.getName())+
					Strings.B_IMPL + Utils.parenthesize(inv.getPredicate());	
	}


}
