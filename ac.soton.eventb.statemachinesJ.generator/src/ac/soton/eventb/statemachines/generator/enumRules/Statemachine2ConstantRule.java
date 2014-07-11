package ac.soton.eventb.statemachines.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Find;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachines.generator.strings.Strings;
import ac.soton.eventb.statemachines.generator.utils.Utils;

public class Statemachine2ConstantRule extends AbstractRule implements IRule{

	/**
	 * Only enabled for enumeration translation
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		Statemachine sourceSM = (Statemachine) sourceElement;
		return Utils.getRootStatemachine(sourceSM).getTranslation().equals(TranslationKind.SINGLEVAR) &&
				sourceSM.getRefines() == null && (Utils.hasParentState(sourceSM) || Utils.hasFinalState(sourceSM));
	}

	/**
	 * Waits until context has not being generated
	 */
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		
		for(Context ctx : ((Machine)container).getSees())
			if(ctx.getName().equals(Strings.CTX_NAME(container)))
				return true;
		
		return Find.generatedElement(generatedElements, Find.project(container), components, Strings.CTX_NAME(container)) != null;
	}
	
	/**
	 * Generates the NULL constants (to state that the state is not active)
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		
		Statemachine sourceSM = (Statemachine) sourceElement;
		Context ctx = (Context)Find.generatedElement(generatedElements, Find.project(container), components, Strings.CTX_NAME(container));
		
		if(ctx == null){
			for(Context ictx : ((Machine)container).getSees())
				if(ictx.getName().equals(Strings.CTX_NAME(container))){
					ctx = ictx;
					break;
				}
		}
		
		Constant newConstant = (Constant) Make.constant(sourceSM.getName() + Strings._NULL, "");
		
		ret.add(Make.descriptor(ctx, constants, newConstant, 1));
		return ret;
	}

}
