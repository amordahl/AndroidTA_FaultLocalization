package edu.utdallas.amordahl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Mainsoot {

	private static Random r = new Random();
	
    private static int addRandom(int a) {
        return a + r.nextInt();
    }

    public static void main(String[] args) {
        int a = 9;
        foo();
        try {
        	FileReader fr = new FileReader(Integer.toString(a));
        } catch (FileNotFoundException fn) {
        	System.out.println("Oops");
        }
        int b = addRandom(a);
    }
    
    private static List<Integer> li = new ArrayList<Integer>();
    
    public static void foo() {
    	for (int i = 0; i <= 1000; i++) {
    		li.add(r.nextInt());
    	}
    }
}
