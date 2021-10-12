package edu.utdallas.amordahl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mainsoot {

	private static Random r = new Random();
	
    public static void main(String[] args) {
        foo();
        if (Mainsoot.li.contains(4)) {
        	throw new RuntimeException("Failure!");
        }
        System.out.println("Success!");
        
    }
    
    private static List<Integer> li = new ArrayList<Integer>();
    
    public static void foo() {
    	for (int i = 0; i <= 3; i++) {
    		li.add(r.nextInt(5));
    	}
    }
}
