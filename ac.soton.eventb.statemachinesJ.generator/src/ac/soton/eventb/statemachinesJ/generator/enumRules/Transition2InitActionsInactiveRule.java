package ac.soton.eventb.statemachinesJ.generator.enumRules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Event;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Find;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.AbstractNode;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class Transition2InitActionsInactiveRule extends AbstractRule  implements IRule {

	
	private Statemachine rootSM;
	private Event initEvent;
	private List<GenerationDescriptor> generatedElements;
	
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		Transition sourceTransition = (Transition) sourceElement;
		if(!Utils.getRootStatemachine(sourceTransition.getTarget()).getTranslation().equals(TranslationKind.SINGLEVAR))
			return false;
		for(Event e : sourceTransition.getElaborates()){
			if(e.getName().equals(Strings.INIT))
				return true;
		}
		
		return false;
	}
	
	@Override
	public boolean fireLate() {
		return true;
	}

	/**
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();
		List<Action> generatedActions = new ArrayList<Action>();
		Transition sourceTransition = (Transition) sourceElement;
		
		this.generatedElements = generatedElements;
		rootSM = Utils.getRootStatemachine(sourceTransition.getSource());
		initEvent = getInitEvent(sourceTransition); 
				
		
		generatedActions.addAll(statemachine2initActionsInactive(rootSM));
		
		
		for(Action a : generatedActions){
			ret.add(Make.descriptor(initEvent, actions, a, 1));
		}
		
		return ret;
	}
	
	private List<Action> statemachine2initActionsInactive(Statemachine sm) {
		List<Action> ret = new ArrayList<Action>();
		if(canGenerate(sm)){
			System.out.println(sm.getName());
			ret.add(statemachine2initActionInactive(sm));
		}
		for(AbstractNode node : sm.getNodes()){
			if(node instanceof State)
				for(Statemachine ism : ((State)node).getStatemachines())
					ret.addAll(statemachine2initActionsInactive(ism));
		}
		
		
		
		return ret;
	}

	private Action statemachine2initActionInactive(Statemachine sm) {
		String name = Strings.INIT_ + sm.getName();
		String expression = "";
				
		if(rootSM.getInstances() == null)
			expression = sm.getName() + Strings.B_BEQ + sm.getName() + Strings._NULL;
		else
			expression = sm.getName() + Strings.B_BEQ + rootSM.getInstances().getName() +
						 Strings.B_CPROD + Utils.asSet(sm.getName() + Strings._NULL);
		
		return (Action) Make.action(name, expression);
	}

	/**
	 * Finds the init event
	 * @param t
	 * @return
	 */
	private Event getInitEvent(Transition t){
		for(Event e : t.getElaborates()){
			if(e.getName().equals(Strings.INIT))
				return e;
		}
		return null;
	}
	
	private boolean canGenerate(Statemachine sm){
		return 	Find.generatedElement(generatedElements, initEvent, actions, Strings.INIT_ + sm.getName()) == null;
	
	}
}

