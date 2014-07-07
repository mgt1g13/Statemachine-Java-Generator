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
import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class Statemachine2PartitionInvariantsRule extends AbstractRule  implements IRule {

	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		int numberOfStates = 0;
		Statemachine sourceSm = (Statemachine) sourceElement;
		//Only enabled for Variables Translation
		if(!Utils.getRootStatemachine(sourceSm).getTranslation().equals(TranslationKind.MULTIVAR))
			return false;
		
		for(AbstractNode abs : sourceSm.getNodes()){
			if(abs instanceof State)
				numberOfStates++;
		}
		
		return sourceSm.getRefines() == null && numberOfStates > 1;
	}
	
	
	/**
	 * States2PartitionInvariant
	 * 
	 * Generates a new Invariant representing the partitioning of a statemachine in its states.
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		Statemachine sourceSm = (Statemachine) sourceElement;
		
		
		int priority = 2;
		
		if(Utils.isRootStatemachine(sourceSm)){
				ret.add(Make.descriptor(container, invariants, partitionInv4RootSM(sourceSm), priority));
		}
		else{
				ret.add(Make.descriptor(container, invariants, partitionInv4NestedSM(sourceSm), priority));
		}
		return ret;
		
	}
	
	/**
	 * Generate the partition invariant for Roots Statemachines
	 * @param sm
	 * @return
	 */
	private Invariant partitionInv4RootSM(Statemachine sm){
		return Make.invariant(Strings.DISTINCT_STATES_IN_ + sm.getName(),
				getStatePartitionAnticedent4RootSM(sm) + "partition" + getStatePartitionContent(sm), 
				"");
		
		
	}
	
	private Invariant partitionInv4NestedSM(Statemachine sm){
		State parentState = Utils.getSuperState(sm);
		//FIXME Naming differs from different implementation
		return Make.invariant(Strings.DISTINCT_STATES_IN_ + sm.getName(),
				getStatePartitionAnticedent4NestedSM(sm, parentState) + "partition" + getStatePartitionContent(sm),
				"");
	}
	
	/**
	 * Returns an anticedent for the partition invariant for an unlifted nested statemachine
	 * or empty string otherwise 
	 * @param sm
	 * @param parentState
	 * @return
	 */
	private String getStatePartitionAnticedent4NestedSM(Statemachine sm, State parentState){
		if(Utils.getRootStatemachine(sm).getInstances() == null)
			return Utils.parenthesize(parentState.getName() + Strings.B_EQ + Strings.B_TRUE) + Strings.B_IMPL;
		else
			return "";
	}
	
	
	
	/**
	 * Returns an anticedent for the partition invariant for an unlifted root statemachine with final state.
	 * or empty string otherwise
	 * @param sm
	 * @return
	 */
	private String getStatePartitionAnticedent4RootSM(Statemachine sm){
		if(Utils.hasFinalState(sm) && Utils.getRootStatemachine(sm).getInstances() == null)
			return Strings.B_TRUE + Strings.B_IN + 
					Utils.asSet(Utils.toString(Utils.getStateNames(sm), Strings.B_COM))+
					Strings.B_IMPL;
		else
			return "";
		
		
	}
	
	/**
	 * Returns the partition content (the bit in brackets and including the brackets)
	 * taking into account whether the statemachine is lifted or not and if not
	 * whether this is a root statemachine and if so does it have a final state
	 * @param sm
	 * @return
	 */
	private String getStatePartitionContent(Statemachine sm){
		if(Utils.getRootStatemachine(sm).getInstances() == null)
			return Utils.parenthesize(Utils.asSet(Strings.B_TRUE) + Strings.B_COM + Utils.toString(getStatesString4Partition(sm), Strings.B_COM)); 
		else
			if(Utils.isRootStatemachine(sm))
				if(Utils.hasFinalState(sm))
					return Utils.parenthesize(Utils.parenthesize(Utils.toString(Utils.getStateNames(sm), Strings.B_UNION)) + Strings.B_COM + Utils.toString(Utils.getStateNames(sm), Strings.B_COM));
		
				else
					return Utils.parenthesize(sm.getInstances().getName() + Strings.B_COM + Utils.toString(Utils.getStateNames(sm), Strings.B_COM));
			else
				return Utils.parenthesize(Utils.getSuperState(sm).getName() + Strings.B_COM + Utils.toString(Utils.getStateNames(sm), Strings.B_COM));
	}
	
	/**
	 * Returns the part of the partition string constructed from the state variables of a statemachine.
	 * ONLY FOR NON-LIFTED
	 * @param sm
	 * @return
	 */
	private List<String> getStatesString4Partition(Statemachine sm){
		List<String> ret = new ArrayList<String>();
		for(AbstractNode abs : sm.getNodes()){
			if(abs instanceof State)
				ret.add( getStatesString4Partition((State)abs) );
		}
		return ret;
	}
	
	
	/**
	 * Returns a string for a single state variable suitable for the partition invariant.
	 * ONLY FOR NON-LIFTED - this is an intersection of the singleton state with TRUE
	 * @param s
	 * @return
	 */
	private String getStatesString4Partition(State s){
		return Utils.asSet(s.getName()) + Strings.B_INTER + Utils.asSet(Strings.B_TRUE);
	}
	
	

}
