package ac.soton.eventb.statemachines.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.CarrierSet;
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

public class Statemachine2SetRule extends AbstractRule implements IRule{

	/**
	 * Only enabled for enumeration translation
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		return ((Statemachine)sourceElement).getRefines() == null &&
				Utils.getRootStatemachine((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR);
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
	 * Generates the the sets for the enumeration translation
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
		
		CarrierSet newSet = (CarrierSet) Make.set(sourceSM.getName() + Strings._STATES, "");
		
		ret.add(Make.descriptor(ctx, sets, newSet, 1));
		return ret;
	}
	

		

	
}
