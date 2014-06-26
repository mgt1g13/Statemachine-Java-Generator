package ac.soton.eventb.statemachinesJ.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Find;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class State2ConstantRule extends AbstractRule implements IRule{

	/**
	 * Only enabled for enumeration translation
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		return Utils.getRootStatemachine((State) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR);
	}

	/**
	 * Waits until context has not being generated
	 */
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		return Find.generatedElement(generatedElements, Find.project(container), components, Strings.CTX_NAME(container)) != null;
	}
	
	
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		
		State sourceState = (State) sourceElement;
		Context ctx = (Context)Find.generatedElement(generatedElements, Find.project(container), components, Strings.CTX_NAME(container));
		
		Constant newConstant = (Constant) Make.constant(sourceState.getName(), "");
		
		ret.add(Make.descriptor(ctx, constants, newConstant, 1));
		return ret;
	}

}
