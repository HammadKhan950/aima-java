package aima.test.core.unit.search.nondeterministic;
import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.EnvironmentState;
import aima.core.agent.Percept;
import aima.core.environment.vacuum.VacuumEnvironment;
import aima.core.environment.vacuum.VacuumEnvironmentState;
import aima.core.search.framework.ActionsFunction;
import aima.core.search.framework.GoalTest;
import aima.core.search.framework.StepCostFunction;
import aima.core.search.nondeterministic.AndOrSearch;
import aima.core.search.nondeterministic.NondeterministicProblem;
import aima.core.search.nondeterministic.Plan;
import aima.core.search.nondeterministic.ResultsFunction;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the AND-OR search algorithm using the erratic vacuum world of page 133,
 * AIMAv3. In essence, a two-square office is cleaned by a vacuum that 
 * randomly (1) cleans the square, (2) cleans both squares, or (3) dirties the
 * square it meant to clean.
 * @author Andrew Brown
 */
public class AndOrSearchTest {

    Agent agent;
    VacuumEnvironment world;
    NondeterministicProblem problem;

    @Before
    public void setUp() {
        this.agent = new VacuumWorldAgent();
        // create world
        this.world = new VacuumEnvironment(VacuumEnvironment.LocationState.Dirty, VacuumEnvironment.LocationState.Dirty);
        this.world.addAgent(this.agent);
        // create state
        VacuumEnvironmentState state = new VacuumEnvironmentState();
        state.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        state.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        state.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create problem
        this.problem = new NondeterministicProblem(
                state,
                new VacuumWorldActions(),
                new VacuumWorldResults(this.agent),
                new VacuumWorldGoalTest(this.agent),
                new VacuumWorldStepCost());
    }

    @Test
    public void testEquality() {
        // create state 1
        VacuumEnvironmentState s1 = new VacuumEnvironmentState();
        s1.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s1.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s1.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // create state 2
        VacuumEnvironmentState s2 = new VacuumEnvironmentState();
        s2.setLocationState(VacuumEnvironment.LOCATION_A, VacuumEnvironment.LocationState.Dirty);
        s2.setLocationState(VacuumEnvironment.LOCATION_B, VacuumEnvironment.LocationState.Dirty);
        s2.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_A);
        // test
        boolean expected = true;
        boolean actual = s1.equals(s2);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSearch() {
        AndOrSearch s = new AndOrSearch();
        try {
            Plan p = s.search(this.problem);
            System.out.println(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Overrides VacuumEnvironment executeAction() to handle the erratic vacuum
 * of page 134.
 * @author Andrew Brown
 */
class NondeterministicVacuumEnvironment extends VacuumEnvironment {

    @Override
    public EnvironmentState executeAction(Agent a, Action agentAction) {
        if (ACTION_MOVE_RIGHT == agentAction) {
            envState.setAgentLocation(a, LOCATION_B);
            updatePerformanceMeasure(a, -1);
        } else if (ACTION_MOVE_LEFT == agentAction) {
            envState.setAgentLocation(a, LOCATION_A);
            updatePerformanceMeasure(a, -1);
        } else if (ACTION_SUCK == agentAction) {
            // case: square is dirty
            if (LocationState.Dirty == envState.getLocationState(envState.getAgentLocation(a))) {
                String current_location = envState.getAgentLocation(a);
                String adjacent_location = (current_location.equals("A")) ? "B" : "A";
                // possibly clean current square
                if (Math.random() > 0.5) {
                    envState.setLocationState(current_location, LocationState.Clean);
                }
                // possibly clean adjacent square
                if (Math.random() > 0.5) {
                    envState.setLocationState(adjacent_location, LocationState.Clean);
                }
            } // case: square is clean
            else if (LocationState.Clean == envState.getLocationState(envState.getAgentLocation(a))) {
                // possibly dirty current square
                if (Math.random() > 0.5) {
                    envState.setLocationState(envState.getAgentLocation(a), LocationState.Dirty);
                }
            }
        } else if (agentAction.isNoOp()) {
            isDone = true;
        }
        return envState;
    }
}

/**
 * Creates the vacuum agent
 * @author andrew
 */
class VacuumWorldAgent implements Agent {

    @Override
    public Action execute(Percept percept) {
        return null;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void setAlive(boolean alive) {
        // do nothing
    }
}

/**
 * Specifies the actions available to the agent at state s
 * @author andrew
 */
class VacuumWorldActions implements ActionsFunction {

    public static Set<Action> actions = new HashSet<Action>();

    /**
     * Returns possible actions given this state
     * @param s
     * @return 
     */
    @Override
    public Set<Action> actions(Object s) {
        if (VacuumWorldActions.actions.isEmpty()) {
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_SUCK);
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_MOVE_LEFT);
            VacuumWorldActions.actions.add(VacuumEnvironment.ACTION_MOVE_RIGHT);
        }
        return VacuumWorldActions.actions;
    }
}

/**
 * Returns possible results
 * @author Andrew Brown
 */
class VacuumWorldResults implements ResultsFunction {

    public Agent agent;

    /**
     * Constructor
     * @param agent 
     */
    public VacuumWorldResults(Agent agent) {
        this.agent = agent;
    }

    /**
     * Returns a list of possible results for a given state and action
     * @param _state
     * @param action
     * @return 
     */
    @Override
    public Set<Object> results(Object _state, Action action) {
        // setup
        VacuumEnvironmentState state = (VacuumEnvironmentState) _state;
        Set<Object> results = new HashSet<Object>();
        String current_location = state.getAgentLocation(agent);
        String adjacent_location = (current_location.equals(VacuumEnvironment.LOCATION_A)) ? VacuumEnvironment.LOCATION_B : VacuumEnvironment.LOCATION_A;
        //
        if (VacuumEnvironment.ACTION_MOVE_RIGHT == action) {
            VacuumEnvironmentState s = new VacuumEnvironmentState();
            s.setLocationState(current_location, state.getLocationState(current_location));
            s.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
            s.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
            results.add(s);
        } else if (VacuumEnvironment.ACTION_MOVE_LEFT == action) {
            VacuumEnvironmentState s = new VacuumEnvironmentState();
            s.setLocationState(current_location, state.getLocationState(current_location));
            s.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
            s.setAgentLocation(this.agent, VacuumEnvironment.LOCATION_B);
            results.add(s);
        } else if (VacuumEnvironment.ACTION_SUCK == action) {
            // case: square is dirty
            if (VacuumEnvironment.LocationState.Dirty == state.getLocationState(state.getAgentLocation(this.agent))) {
                // always clean current
                VacuumEnvironmentState s1 = new VacuumEnvironmentState();
                s1.setLocationState(current_location, VacuumEnvironment.LocationState.Clean);
                s1.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s1.setAgentLocation(this.agent, current_location);
                results.add(s1);
                // sometimes clean adjacent as well
                VacuumEnvironmentState s2 = new VacuumEnvironmentState();
                s2.setLocationState(current_location, VacuumEnvironment.LocationState.Clean);
                s2.setLocationState(adjacent_location, VacuumEnvironment.LocationState.Clean);
                s2.setAgentLocation(this.agent, current_location);
                results.add(s2);
            } // case: square is clean
            else {
                // sometimes do nothing
                VacuumEnvironmentState s1 = new VacuumEnvironmentState();
                s1.setLocationState(current_location, state.getLocationState(current_location));
                s1.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s1.setAgentLocation(this.agent, current_location);
                results.add(s1);
                // sometimes deposit dirt
                VacuumEnvironmentState s2 = new VacuumEnvironmentState();
                s2.setLocationState(current_location, VacuumEnvironment.LocationState.Dirty);
                s2.setLocationState(adjacent_location, state.getLocationState(adjacent_location));
                s2.setAgentLocation(this.agent, current_location);
                results.add(s2);
            }
        } else if (action.isNoOp()) {
            // do nothing
        }

        return results;
    }
}

/**
 * Tests for goals states
 * @author Andrew Brown
 */
class VacuumWorldGoalTest implements GoalTest {

    public Agent agent;

    /**
     * Constructor
     * @param agent 
     */
    public VacuumWorldGoalTest(Agent agent) {
        this.agent = agent;
    }

    /**
     * Tests whether the search has identified a goal state
     * @param _state
     * @return 
     */
    @Override
    public boolean isGoalState(Object _state) {
        // setup
        VacuumEnvironmentState state = (VacuumEnvironmentState) _state;
        String current_location = state.getAgentLocation(this.agent);
        String adjacent_location = (current_location.equals(VacuumEnvironment.LOCATION_A)) ? VacuumEnvironment.LOCATION_B : VacuumEnvironment.LOCATION_A;
        // test goal state
        if (VacuumEnvironment.LocationState.Clean != state.getLocationState(current_location)) {
            return false;
        } else if (VacuumEnvironment.LocationState.Clean != state.getLocationState(adjacent_location)) {
            return false;
        } else {
            return true;
        }
    }
}

/**
 * Measures step cost
 * @author Andrew Brown
 */
class VacuumWorldStepCost implements StepCostFunction {

    public double c(Object state, Action action, Object sDelta) {
        return 1.0;
    }
}
