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
		return Utils.isRootStatemachine((Statemachine)sourceElement) &&
				Utils.getRootStatemachine((Statemachine) sourceElement).getTranslation().equals(TranslationKind.SINGLEVAR);
	}

	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		Machine sourceMachine = (Machine)EcoreUtil.getRootContainer(sourceElement);
		
		List<Context> redundantCTX = new ArrayList<Context>();
		
		//For needed just so the implicit context created for sourceMachine does not get erased
		for(Machine im : sourceMachine.getRefines()){
			redundantCTX.addAll(generateRedundantContextsToBeRemove(im));
		}
		
		
		
		
		for(Context ctx : redundantCTX ){
			ret.add(Make.descriptor(sourceMachine, sees, ctx, 1, true));
			
		}

		return ret;
	}
	

	private List<Context> generateRedundantContextsToBeRemove(Machine m){
		List<Context> ret = new ArrayList<Context>();
		ret.add((Context)Make.context(m.getName() + Strings._IMPLICIT_CONTEXT, ""));
		
		for(Machine im : m.getRefines()){
			ret.addAll(generateRedundantContextsToBeRemove(im));
		}
	
		return ret;
		
	}
	
}

