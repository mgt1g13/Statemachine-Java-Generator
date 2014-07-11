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

public class RemoveRedundantContextsRule extends AbstractRule implements IRule{

	/**
	 * Only enabled for enumeration translation
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);
		return Utils.isRootStatemachine((Statemachine)sourceElement) &&
				Utils.getRootStatemachine((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR) &&
				container.getRefines().size() > 0 && //Just in refinements, when contexts get extended
				!container.getSeesNames().contains(Strings.CTX_NAME(container)); //Last condition is to verify if enumeration translation has happened
																				//before. If it has, there is no redundant context.
	}

	/**
	 * Fire late so we can be sure the context extends field has already been completely populated
	 */
	@Override
	public boolean fireLate(){
		return true;
	}
	
	/**
	 * Verifies if the context has already been generated. Also verifies if the newly generated context
	 * is already extending the abstract one
	 */
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		Machine container = (Machine)EcoreUtil.getRootContainer(sourceElement);
		Context implicitContext = (Context) Find.generatedElement(generatedElements, Find.project(container), components, Strings.CTX_NAME(container));
		
	
		if(implicitContext == null)
			return false;
		
		/**
		 * Depends on having found at least one context being extended
		 */
		for(Machine m : container.getRefines()){
			String extendedContext = (String) Find.generatedElement(generatedElements, implicitContext, extendsNames, Strings.CTX_NAME(m));
			if(extendedContext !=  null){
				return true;
			}
		}
		return false;
	}
	
	
	
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Machine sourceMachine = (Machine)EcoreUtil.getRootContainer(sourceElement);
		
		Context implicitContext = (Context) Find.generatedElement(generatedElements, Find.project(sourceMachine), components, Strings.CTX_NAME(sourceMachine));
				
				
		for(Machine im : sourceMachine.getRefines()){
			String extendedContext = (String) Find.generatedElement(generatedElements, implicitContext, extendsNames, Strings.CTX_NAME(im));
			if(extendedContext != null){
				Context redundant = (Context) Find.named(sourceMachine.getSees(), extendedContext);
				if(redundant != null)
					ret.add(Make.descriptor(sourceMachine, sees, redundant, 1, true));
			}
		}
		

		return ret;
	}
	

}

