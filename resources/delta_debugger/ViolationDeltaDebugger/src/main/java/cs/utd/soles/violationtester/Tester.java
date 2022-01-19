package cs.utd.soles.violationtester;

import java.util.ArrayList;

public interface Tester {


    //method that does all the required steps to test whether the violation was recreated or not, depends on tester object what stuff is required
    boolean runTest(ArrayList<Object> requireds);
}
