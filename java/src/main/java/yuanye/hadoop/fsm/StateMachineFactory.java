package yuanye.hadoop.fsm;

import yuanye.container.Interface;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2014/9/4.
 */
public class StateMachineFactory<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT> {

    private Map<STATE,Map<EVENTTYPE,STATE>> transitionTopology;
    private TransitionListNode listNode;
    private STATE defaultInitState;

    public StateMachineFactory(STATE defaultInitState){
        this.defaultInitState = defaultInitState;
    }

    private StateMachineFactory(StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> that,
                                ApplicableSingleOrMutipleArcTransition<OPERAND,STATE,EVENTTYPE,EVENT> transition) {
        this.listNode = new TransitionListNode(transition,that.listNode);
        this.defaultInitState = that.defaultInitState;
    }

    private interface Transition<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT>{
        STATE doTransition(OPERAND operand,STATE preState,EVENT event,EVENTTYPE eventType);
    }

    private interface Applicable<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT>{
        void apply(StateMachineFactory<OPERAND,STATE,EVENTTYPE,EVENT> factory);
    }

    private class ApplicableSingleOrMutipleArcTransition<OPERAND,STATE extends Enum<STATE>,EVENTTYPE extends Enum<EVENTTYPE>,EVENT>
            implements Applicable<OPERAND,STATE,EVENTTYPE,EVENT>{

        private final STATE preState;
        private final EVENTTYPE eventType;
        private final Transition<OPERAND,STATE,EVENTTYPE,EVENT> transition;

        public ApplicableSingleOrMutipleArcTransition(STATE preState,
                                                      EVENTTYPE eventType,
                                                      Transition<OPERAND,STATE,EVENTTYPE,EVENT> transition){
            this.preState = preState;
            this.eventType = eventType;
            this.transition = transition;
        }

        @Override
        public void apply(StateMachineFactory<OPERAND, STATE, EVENTTYPE, EVENT> factory) {
            //TODO
        }
    }

    private class TransitionListNode{
        private final ApplicableSingleOrMutipleArcTransition transition;
        private final TransitionListNode next;

        public TransitionListNode(ApplicableSingleOrMutipleArcTransition transition,TransitionListNode next){
            this.transition = transition;
            this.next = next;
        }

        public ApplicableSingleOrMutipleArcTransition transition(){
            return transition;
        }

        public TransitionListNode next(){
            return next;
        }

    }



    private class SingleArc<OPERAND,EVENT>
            implements Transition<OPERAND,STATE,EVENTTYPE,EVENT>{
        private final SingleArcTransition<OPERAND,EVENT> hook;
        private final STATE postState ;

        public SingleArc(STATE postState,SingleArcTransition<OPERAND,EVENT> hook){
            this.postState = postState;
            this.hook = hook;
        }

        @Override
        public STATE doTransition(OPERAND operand, STATE preState, EVENT event, EVENTTYPE eventType) {
            if (hook != null){
                hook.transition(operand, event);
            }
            return postState;
        }
    }

    private class MutipleArc<OPERAND,EVENT,STATE extends Enum<STATE>>
            implements Transition<OPERAND,STATE,EVENTTYPE,EVENT>{
        private final MultipleArcTransition<OPERAND,EVENT,STATE> hook;
        private final Set<STATE> postStates;

        public MutipleArc(Set<STATE> postStates,MultipleArcTransition<OPERAND,EVENT,STATE> hook){
            this.postStates = new HashSet<>(postStates);
            this.hook = hook;
        }
        @Override
        public STATE doTransition(OPERAND operand, STATE preState, EVENT event, EVENTTYPE eventType) {
            STATE state = hook.transition(operand,event);
            if (!postStates.contains(state)){
                throw new IllegalStateException(state.toString());
            }
            return state;
        }
    }

    public StateMachineFactory addTransition(STATE preState,STATE postState,
                                             Set<EVENTTYPE> eventTypes,SingleArcTransition<OPERAND,EVENT> hook){
        StateMachineFactory factory = this;
        for (EVENTTYPE eventType : eventTypes){
            factory = addTransition(preState,postState,eventType,hook);
        }
        return factory;
    }

    public StateMachineFactory addTransition(STATE preState,STATE postState,
                                             EVENTTYPE eventType,SingleArcTransition<OPERAND,EVENT> hook){
        return new StateMachineFactory(
                this,
                new ApplicableSingleOrMutipleArcTransition<OPERAND,STATE,EVENTTYPE,EVENT>(
                        preState,
                        eventType,
                        new SingleArc<OPERAND, EVENT>(postState,hook)));
    }

    public StateMachineFactory addTransition(STATE preState,Set<STATE> postStates,
                                             EVENTTYPE eventType,MultipleArcTransition<OPERAND,EVENT,STATE> hook){
        return new StateMachineFactory(
                this,
                new ApplicableSingleOrMutipleArcTransition<OPERAND,STATE,EVENTTYPE,EVENT>(
                        preState,
                        eventType,
                        new MutipleArc<OPERAND, EVENT, STATE>(postStates, hook)));
    }

    public StateMachineFactory addTransition(STATE preState,Set<STATE> postStates,
                                             Set<EVENTTYPE> eventTypes,MultipleArcTransition<OPERAND,EVENT,STATE> hook){
        StateMachineFactory factory = this;
        for (EVENTTYPE eventType : eventTypes){
            factory = addTransition(preState,postStates,eventType,hook);
        }
        return factory;
    }

    public StateMachineFactory installTopology(){};

    public StateMachine make(OPERAND operand){};
}
