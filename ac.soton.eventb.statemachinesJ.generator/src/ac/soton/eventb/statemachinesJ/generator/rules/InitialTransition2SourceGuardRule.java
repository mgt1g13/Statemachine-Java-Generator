package ac.soton.eventb.statemachinesJ.generator.rules;

import java.util.ArrayList;
import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;

import ac.soton.eventb.emf.diagrams.generator.AbstractRule;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;
import ac.soton.eventb.statemachines.Initial;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class InitialTransition2SourceGuardRule extends AbstractRule  implements IRule {

	
	/**
	 * TODO The number of calls to contains functions is inefficient
	 * 
	 */
	
	private Statemachine rootSM;
	
	
	/**
	 * Only for transitions from the root initial state when the root statemachine has a final state
	 * Skips transformation if event contains source guard on the same state already.
	 * Skips the transformation if every event elaborated by the transition already contaisn the guard
	 */
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception{
		Transition sourceTransition = (Transition) sourceElement;

		int shouldNotGenerate = 0;

		for(Event e : sourceTransition.getElaborates()){
			if(Utils.containsGuardWithPrefix(e, Strings.ISNOTIN_ + Utils.getStatemachine(sourceTransition.getSource()).getName()) ||
					Utils.containsGuardWithSuffix(e, Strings.ISNOTIN_ + Utils.getStatemachine(sourceTransition.getSource()).getName()) ||
					e.getName().equals(Strings.INIT))
				shouldNotGenerate++;

		}


		return 
				(sourceTransition.getSource() instanceof Initial) &&
				Utils.isRootStatemachine(Utils.getStatemachine(sourceTransition.getSource())) &&
				Utils.hasFinalState(Utils.getStatemachine(sourceTransition.getSource())) &&
				!(shouldNotGenerate == sourceTransition.getElaborates().size());


	}

	/**
	 * InitialTransition2SourceGuard
	 * 
	 * Transforms initial transition to guard that the statemachine is not active.
	 * Generates guard for source state and particular event.
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();

		Transition sourceTransition = (Transition) sourceElement;
		rootSM = Utils.getRootStatemachine(sourceTransition.getSource());
		
		
		String name = Strings.ISNOTIN_ + Utils.getStatemachine(sourceTransition.getSource()).getName();
		String predicate = getStateMachineNotActivePredicate(sourceTransition);
		Guard grd = (Guard) Make.guard(name, predicate);
		
		for(Event e : sourceTransition.getElaborates()){
			if(!(Utils.containsGuardWithPrefix(e, name) ||
					Utils.containsGuardWithSuffix(e, name)) ||
					e.getName().equals(Strings.INIT))
		
			ret.add(Make.descriptor(e, guards, grd, 10));
		}
		
		return ret;
	}

	/**
	 * Generates the predicate
	 * @param sourceTransition
	 * @return
	 */
	private String getStateMachineNotActivePredicate(Transition sourceTransition) {
		if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR))
			return getStateMachineNotActivePredicateForSinglevar(sourceTransition);
		else if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR))
			return getStateMachineNotActivePredicateForMultivar(sourceTransition);
		else
			return Strings.TRANSLATION_KIND_NOT_SUPPORTED_ERROR;
	}

	
	/**
	 * Generates the predicate for the enumerations translation
	 * @param sourceTransition
	 * @return
	 */
	private String getStateMachineNotActivePredicateForSinglevar(Transition sourceTransition) {
		if(rootSM.getInstances() == null)
			return Utils.getStatemachine(sourceTransition.getSource()).getName() +
					Strings.B_EQ + Utils.getStatemachine(sourceTransition.getSource()).getName() +
					Strings._NULL;
		else
			return Utils.getStatemachine(sourceTransition.getSource()).getName() + 
					Utils.parenthesize(rootSM.getSelfName()) + Strings.B_EQ + 
					Utils.getStatemachine(sourceTransition.getSource()).getName()+
					Strings._NULL;
	}

	
	/**
	 * Generates the predicate for the variables translation
	 * FIXME What exactly should it generate? Isn't it to state that, a transition
	 * from an initial node should only be enabled when the container statemachine is active??
	 * @param sourceTransition
	 * @return
	 */
	private String getStateMachineNotActivePredicateForMultivar(Transition sourceTransition) {
		if(rootSM.getInstances() == null){
			return Strings.B_TRUE + Strings.B_NOTIN +
					Utils.parenthesize(Utils.toString(Utils.getStateNamesAsSingletons(Utils.getStatemachine(sourceTransition.getSource())),
							Strings.B_UNION));
			
//			return Utils.getSuperState(Utils.getStatemachine(sourceTransition.getSource())).getName() +
//					Strings.B_EQ + Strings.B_TRUE;
//		
		}
		else
			return rootSM.getSelfName() + Strings.B_NOTIN +
					Utils.parenthesize(Utils.toString(Utils.getStateNames(Utils.getStatemachine(sourceTransition.getSource())), Strings.B_UNION));
	}	
	
}