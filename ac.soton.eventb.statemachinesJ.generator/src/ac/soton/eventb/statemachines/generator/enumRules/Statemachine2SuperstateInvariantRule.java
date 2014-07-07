package ac.soton.eventb.statemachines.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class Statemachine2SuperstateInvariantRule extends AbstractRule  implements IRule {

	
	/**
	 * Only enabled for enumeration translation. Root statemachines is not used
	 * 
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		TranslationKind translatioKind = Utils.getRootStatemachine((Statemachine) sourceElement).getTranslation();
		return !Utils.isRootStatemachine((Statemachine) sourceElement) &&
				((Statemachine) sourceElement).getRefines() == null && 
				translatioKind.equals(TranslationKind.SINGLEVAR);
		
	}
	
	
	
	/**
	 * Generates superstate axiom in the implicit context
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Statemachine sourceSM = (Statemachine) sourceElement;
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);

		String name = Strings.SUPERSTATEOF_ +  sourceSM.getName();
		String predicate = generatePredicate(sourceSM);
		
		ret.add(Make.descriptor(container, invariants, Make.invariant(name, predicate, ""), 6));
		
		return ret;
		
	}



	private String generatePredicate(Statemachine sourceSM) {
		State parentState = Utils.getSuperState(sourceSM);
		if(Utils.getRootStatemachine(sourceSM).getInstances() == null)
			return sourceSM.getName() + Strings.B_NEQ + sourceSM.getName()+
					Strings._NULL + Strings.B_EQUIV + Utils.getStatemachine(parentState).getName() +
					Strings.B_EQ + parentState.getName();
		else
			return Strings.B_DOM + Utils.parenthesize(sourceSM.getName() + Strings.B_RANSUB + Utils.asSet(sourceSM.getName() + Strings._NULL)) +
					Strings.B_EQ + Strings.B_DOM + 
					Utils.parenthesize(Utils.getStatemachine(parentState).getName() +Strings.B_RANRES + Utils.asSet(parentState.getName()));
		
		
	}

	
	
}

