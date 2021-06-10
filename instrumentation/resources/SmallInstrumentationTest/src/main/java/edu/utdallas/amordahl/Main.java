package edu.utdallas.amordahl;

import java.util.Random;

public class Main {

    private static int addRandom(int a) {
        Random r = new Random();
        return a + r.nextInt();
    }

    public static void main(String[] args) {
        int a = 9;
        int b = addRandom(a);
    }
}
