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
		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);

		return Utils.isRootStatemachine((Statemachine)sourceElement) &&
				((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR) && 
				getImplicitContext(container) == null;
				
	
	}
	/**
	 * Generates the implicit context
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		
		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);

		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		
		Context ctx = getImplicitContext(container);


		ctx =  (Context) Make.context(container.getName() + Strings._IMPLICIT_CONTEXT, "");
		ret.add(Make.descriptor(Find.project(container), components,ctx ,1));
		ret.add(Make.descriptor(container, seesNames, ctx.getName(), 1));



		return ret;

	}
	
	private Context getImplicitContext(Machine m){
		Context ctx = null;
		for(Context ictx : m.getSees()){
			if(ictx.getName().equals(m.getName() + Strings._IMPLICIT_CONTEXT)){
				ctx = ictx;
				break;
			}
		}
		return ctx;
	}
	
	
	
}



	
	
	
