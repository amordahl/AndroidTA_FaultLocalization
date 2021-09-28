package edu.utdallas.amordahl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;

public class Main {

    private static int addRandom(int a) {
        Random r = new Random();
        return a + r.nextInt();
    }

    public static void main(String[] args) {
        int a = 9;
        try {
        	FileReader fr = new FileReader(Integer.toString(a));
        } catch (FileNotFoundException fn) {
        	System.out.println("Oops");
        }
        int b = addRandom(a);
    }
}
