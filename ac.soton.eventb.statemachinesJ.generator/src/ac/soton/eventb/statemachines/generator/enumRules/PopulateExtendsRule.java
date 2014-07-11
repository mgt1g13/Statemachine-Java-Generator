package ac.soton.eventb.statemachines.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
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

public class PopulateExtendsRule extends AbstractRule implements IRule{
	
	
	
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {	
		return Utils.isRootStatemachine((Statemachine)sourceElement) &&
				((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR);
				
	
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
	 * Generates the implicit context
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {


		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Context ctx = (Context) Find.generatedElement(generatedElements, Find.project(container), components, container.getName() + Strings._IMPLICIT_CONTEXT);
		
		if(ctx == null)
			for(Context ictx : ((Machine)container).getSees())
				if(ictx.getName().equals(Strings.CTX_NAME(container)))
					ctx = ictx;
		
		
		if(container.getRefines().size() != 0){
			List<Context> abstractCtxs = getGeneratedAbstractContext(container);

			for(Context ictx : abstractCtxs)
				if(!ctx.getExtendsNames().contains(ictx.getName()))
					ret.add(Make.descriptor(ctx, extendsNames, ictx.getName(),1 ));
		}

		return ret;

	}
	
	
	/**
	 * Returns a context automatically generated seen by one of the 
	 * machinhes mac refine
	 * @param mac
	 * @return
	 */
	private List<Context> getGeneratedAbstractContext(Machine mac){
		List<Context> abstractCtxs = new ArrayList<Context>();
		for(Machine imac : mac.getRefines()){
			for(Context ctx : imac.getSees()){
				if(ctx.getName().equals(imac.getName() + Strings._IMPLICIT_CONTEXT)){
					abstractCtxs.add(ctx);
				}
			}

		}
		return abstractCtxs;
	}
}



	
	
	
