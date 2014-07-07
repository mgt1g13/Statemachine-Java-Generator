package ac.soton.eventb.statemachines.generator.varRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Variable;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.utils.Utils;

/**
 * Rule to transform a state in to a variable
 * 
 * @author mgt1g13
 *
 */
public class State2VariablesRule extends AbstractRule  implements IRule {

	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		TranslationKind translatioKind = Utils.getRootStatemachine((State) sourceElement).getTranslation();
		return translatioKind.equals(TranslationKind.MULTIVAR);
	}
	
	
	
	/**
	 * States2Variables
	 * 
	 * Generates a new variable named as the states it represents
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
	
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		Variable newVariable = Make.variable(((State)sourceElement).getName(), "");
		ret.add(Make.descriptor(container, variables, newVariable, 10));
		
		return ret;
		
	}

	
}

