package ac.soton.eventb.statemachines.generator.varRules;

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

public class RemoveImplicitContextRule extends AbstractRule  implements IRule {

	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		Statemachine sm = (Statemachine) sourceElement;
		
		return Utils.isRootStatemachine(sm) &&
			    sm.getTranslation().equals(TranslationKind.MULTIVAR);//Variables translation only

	}
	
	

	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)EcoreUtil.getRootContainer(sourceElement);
		
		if(((Machine)container).getSees().size() != 0){
			for(Context ctx : ((Machine)container).getSees()){
				if(ctx.getName().equals(container.getName() + Strings._IMPLICIT_CONTEXT)){
					ret.add(Make.descriptor(Find.project(container), components, Make.context(container.getName() + Strings._IMPLICIT_CONTEXT,""), 1, true) );
					ret.add(Make.descriptor(container, sees,  ctx , 1, true));
				}
			}
		}

		return ret;
		
	}

	
}