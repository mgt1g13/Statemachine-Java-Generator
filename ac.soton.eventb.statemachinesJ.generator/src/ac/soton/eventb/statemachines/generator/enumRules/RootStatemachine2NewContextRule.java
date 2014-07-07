package ac.soton.eventb.statemachines.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
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

public class RootStatemachine2NewContextRule extends AbstractRule implements IRule{
	
	
	
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {	
		return Utils.isRootStatemachine((Statemachine)sourceElement) &&
				((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR);
				
	
	}
	/**
	 * Generates the implicit context
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {


		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Context newCtx = (Context) Make.context(Strings.CTX_NAME(container), "");
		ret.add(Make.descriptor(Find.project(container), components, newCtx ,1));
		ret.add(Make.descriptor(container, sees, newCtx, 1));


		if(container.getRefines().size() != 0){
			Context abstractCtx = getGeneratedAbstractContext(container);

			if(abstractCtx != null)
				ret.add(Make.descriptor(newCtx, _extends, abstractCtx,1 ));
		}

		return ret;

	}
	
	
	/**
	 * Returns a context automatically generated seen by one of the 
	 * machinhes mac refine
	 * @param mac
	 * @return
	 */
	private Context getGeneratedAbstractContext(Machine mac){
		Context abstractCtx = null;
		for(Machine imac : mac.getRefines()){
			for(Context ctx : imac.getSees()){
				if(ctx.getName().equals(imac.getName() + Strings._IMPLICIT_CONTEXT)){
					System.out.println(ctx.getName());
					abstractCtx = ctx;
					break;
				}
			}

		}
		return abstractCtx;
	}
}



	
	
	
