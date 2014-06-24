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
import ac.soton.eventb.statemachines.Any;
import ac.soton.eventb.statemachines.Fork;
import ac.soton.eventb.statemachines.Junction;
import ac.soton.eventb.statemachines.State;
import ac.soton.eventb.statemachines.Statemachine;
import ac.soton.eventb.statemachines.Transition;
import ac.soton.eventb.statemachines.TranslationKind;
import ac.soton.eventb.statemachinesJ.generator.strings.Strings;
import ac.soton.eventb.statemachinesJ.generator.utils.Utils;

public class Transition2SourceGuardRule extends AbstractRule  implements IRule {


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
		return !Utils.isSelfLoop(sourceTransition);
	}

	/**
	 * Transition2SourceGuard
	 * 
	 * Transforms initial transition to guard that the statemachine is not active.
	 * Generates guard for source state and particular event.
	 */
	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		List<GenerationDescriptor> ret = new ArrayList<GenerationDescriptor>();

		Transition sourceTransition = (Transition) sourceElement;
		rootSM = Utils.getRootStatemachine(sourceTransition.getSource());
		
		List<Guard> generatedGuards = generateGuards(sourceTransition);

		for(Guard grd : generatedGuards){
			for(Event e : sourceTransition.getElaborates()){
				if(!e.getName().equals(Strings.INIT) && grd.getName() != null){
					ret.add(Make.descriptor(e, guards, grd, 10));
				}
			}
		}
		return ret;

	}

	private List<Guard> generateGuards(Transition sourceTransition) {
		List<Guard> ret = new ArrayList<Guard>();
		if(sourceTransition.getSource() instanceof State){
			ret.add(sourceState2sourceGuard((State)sourceTransition.getSource()));
		}
		else if(sourceTransition.getSource() instanceof Fork){
			for(Transition t : ((Fork)sourceTransition.getSource()).getIncoming())
				ret.addAll(generateGuards(t));
		}
		else if(sourceTransition.getSource() instanceof Junction){
			ret.add(junction2disjunctiveSourceGuard(sourceTransition));
		}
		else if(rootSM.getInstances() != null && sourceTransition.getSource() instanceof Any)
			ret.add(LiftedAny2SourceGuard(sourceTransition));


		return ret;
	}

	/**
	 * Generates source guards on lifted Statemachines for Initial and Any Nodes
	 * @param sourceTransition
	 * @return
	 */
	private Guard LiftedAny2SourceGuard(Transition sourceTransition) {
		String predicate = "";
		String name = "";
		if(Utils.hasFinalState(Utils.getStatemachine(sourceTransition.getSource()))){
			name = Strings.ISIN_ + "top_level_states";
			predicate = rootSM.getSelfName() + Strings.B_IN +
					Utils.parenthesize(Utils.toString(Utils.getStateNames(Utils.getStatemachine(sourceTransition.getSource())), Strings.B_UNION));
		}
		else{
			name = Strings.ISIN_ + rootSM.getInstances().getName();
			predicate = rootSM.getSelfName() + Strings.B_IN + rootSM.getInstances().getName();
		}
		return (Guard) Make.guard(name, predicate);
	}

	/**
	 * Generates a single disjunctive guard representing the condition for an outgoing transition from a junction to be enabled.
	 * Any upstream junctions or forks are considered in the forming of the predicate.
	 * @param sourceTransition
	 * @return
	 */
	private Guard junction2disjunctiveSourceGuard(Transition sourceTransition) {
		String name = "";
		String pred = "";
		List<Guard> generatedGuards = new ArrayList<Guard>();

		for(Transition t : sourceTransition.getSource().getIncoming()){
			if(!Utils.isSelfLoop(t)){
				if(t.getSource() instanceof State)
					generatedGuards.add(guardedTransition2conjunctiveSourceGuard(t));
				else if(t.getSource() instanceof Junction)
					generatedGuards.add(junction2disjunctiveSourceGuard(t));
				else if(t.getSource() instanceof Fork)
					generatedGuards.add(fork2conjunctiveSourceGuard(t));

			}		
		}
		for(Guard gs : generatedGuards){
			if(gs.getName() != null){
				if(name.equals(""))
					name = gs.getName();
				else
					name = name + "_or_" + gs.getName();
				if(pred.equals(""))
					pred = gs.getPredicate();
				else
					pred = pred + Strings.B_OR + gs.getPredicate();
			}
		}

		return (Guard) Make.guard(name, addTransitionGuards(sourceTransition, Utils.parenthesize(pred)));
	}



	/**
	 * Generates a single conjunctive guard representing the condition for a Join (fork) to be enabled.
	 * N.B. this should only be used when the outgoing leads to a downstream junction which means that the conjunctive guard will be used as a clause in a disjunction.
	 * When a downstream junction is not involved the fork should be handled by generating multiple seperate guards.
	 * @param t
	 * @return
	 */
	private Guard fork2conjunctiveSourceGuard(Transition sourceTransition) {
		String name = "";
		String predicate = "";
		
		List<Guard> generatedGuards = new ArrayList<Guard>();
		
		for(Transition t : sourceTransition.getSource().getIncoming()){
			generatedGuards.add(guardedTransition2conjunctiveSourceGuard(t));
		}
		
		for(Guard gs : generatedGuards){
			if(name.equals(""))
				name = gs.getName();
			else
				name = name + "_and_" + gs.getName();
			
			if(predicate.equals(""))
				predicate = gs.getPredicate();
			else
				predicate = predicate + Strings.B_AND + gs.getPredicate();
		}
		
		predicate = addTransitionGuards(sourceTransition, predicate);
		if(sourceTransition.getTarget() instanceof Junction)
			predicate = Utils.parenthesize(predicate);
	
		return (Guard) Make.guard(name, predicate);
	}

	/**
	 * Transforms guarded transition from source state to a conjunctive source guard representing the transition source and transition guards.
	 * Skips transformation if event contains source guard on the same state already.
	 * NOTE - THIS RULE SHOULD ONLY BE USED TO GENERATE A SUB-CLAUSE IN A DISJUNCTION (E.G. WHEN THERE IS A DOWNSTREAM JUNCTION).
	 *  NORMALLY TRANSITION GUARDS ARE JUST ADDED TO THE EVENT'S GUARD COLLECTION
	 * @param t
	 * @return
	 */
	private Guard guardedTransition2conjunctiveSourceGuard(Transition t) {
		String name = Strings.ISIN_ + ((State) t.getSource()).getName();
		String predicate = addTransitionGuards(t, getSourceStatePredicate((State) t.getSource()));
		return (Guard) Make.guard(name, predicate);
	}


	/**
	 * Adds to the given predicate, by conjunction, the predicates from the guards of the
	 * contextual transition segment, which does not target a real state.
	 * NOTE - THIS RULE SHOULD ONLY BE USED TO GENERATE A SUB-CLAUSE IN A DISJUNCTION (E.G. WHEN THERE IS A DOWNSTREAM JUNCTION).
	 *  NORMALLY TRANSITION GUARDS ARE JUST ADDED TO THE EVENT'S GUARD COLLECTION
	 * @param t
	 * @param predicate
	 * @return
	 */
	private String addTransitionGuards(Transition t, String predicate) {
		String p = predicate;

		if((! (t.getTarget() instanceof State) ) && (!t.getGuards().isEmpty()))
			for(Guard grd : t.getGuards())
				p = p + Strings.B_AND + grd.getPredicate();

		return p; 
	}


	private Guard sourceState2sourceGuard(State s) {
		return (Guard) Make.guard(Strings.ISIN_ + s.getName(),
				getSourceStatePredicate(s));

	}

	/**
	 * Returns a predicate string representing the guard for the transition source state
	 * for enumeration translation this is either statemachine = state or statemachine(self) = state depending on lifting
	 * for variables translation this is either state = TRUE of self : state depending on lifting
	 * @param s
	 * @return
	 */
	private String getSourceStatePredicate(State s) {
		if(rootSM.getTranslation().equals(TranslationKind.SINGLEVAR))
			return getSourceStatePredicateForSinglevar(s);
		else if(rootSM.getTranslation().equals(TranslationKind.MULTIVAR))
			return getSourceStatePredicateForMultivar(s);
		else
			return Strings.TRANSLATION_KIND_NOT_SUPPORTED_ERROR;
	}

	
	private String getSourceStatePredicateForSinglevar(State s) {
		if(rootSM.getInstances() == null)
			return Utils.getStatemachine(s).getName() + Strings.B_EQ + s.getName();
		else
			return Utils.getStatemachine(s).getName() + Utils.parenthesize(rootSM.getName())+
					Strings.B_EQ + s.getName();
	}


	private String getSourceStatePredicateForMultivar(State s) {
		if(rootSM.getInstances() == null)
			return s.getName() + Strings.B_EQ + Strings.B_TRUE;
		else
			return rootSM.getSelfName() + Strings.B_IN + s.getName();	
	}


}
