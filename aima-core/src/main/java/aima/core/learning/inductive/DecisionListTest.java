package aima.core.learning.inductive;

import aima.core.learning.framework.Attribute;
import aima.core.learning.framework.DataSet;
import aima.core.learning.framework.Example;
import java.util.LinkedList;

/**
 * Represents a single test in a decision list, see figure 18.10, page 716,
 * AIMAv3
 * @author Ravi Mohan
 * @author Andrew Brown
 */
public class DecisionListTest {

    /**
     * The test is a conjunction; every attribute in the test is a conjunct 
     * that, when all proven true, it returns true.
     */
    private LinkedList<Attribute> conjuncts;

    /**
     * Constructor
     */
    public DecisionListTest() {
        this.conjuncts = new LinkedList<Attribute>();
    }

    /**
     * Add a conjunct to the test
     * @param attribute
     * @param decision 
     */
    public void add(Attribute attribute) {
        this.conjuncts.add(attribute);
    }

    /**
     * Test an example for matching
     * @param e
     * @return 
     */
    public boolean matches(Example example) {
        for (Attribute conjunct : this.conjuncts) {
            Attribute exampleAttribute = example.get(conjunct.getName());
            if (!conjunct.equals(exampleAttribute)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return matching examples from an example set
     * @param examples
     * @return 
     */
    public DataSet getMatchingExamples(DataSet examples) {
        DataSet matched = new DataSet();
        for (Example e : examples) {
            if (matches(e)) {
                matched.add(e);
            }
        }
        return matched;
    }

    /**
     * Return non-matching examples from an example set
     * @param examples
     * @return 
     */
    public DataSet getNonMatchingExamples(DataSet examples) {
        DataSet unmatched = new DataSet();
        for (Example e : examples) {
            if (!matches(e)) {
                unmatched.add(e);
            }
        }
        return unmatched;
    }

    /**
     * Return string representation
     * @return 
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[IF ");
        if( this.conjuncts.size() > 0 ){
            Attribute last = this.conjuncts.peekLast();
            for (Attribute conjunct : this.conjuncts) {
                s.append(conjunct.getName());
                s.append("=");
                s.append(conjunct.getValue());
                if(!conjunct.equals(last)) s.append(" AND ");
            }
        }
        else{
            s.append("true ");
        }
        // decision
        s.append(" THEN output(true) ELSE output(false)]");
        // return
        return s.toString();
    }
}