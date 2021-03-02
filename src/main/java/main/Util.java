package main;

import java.util.concurrent.ThreadLocalRandom;

public final class Util {

    private static final String[] ALPHABET = "a,b,c,d,e,f,1,2,3,4,5,6,7,8,9".split(",");

    private Util() {

    }

    public static String createRandomString(int length) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            out.append(ALPHABET[ThreadLocalRandom.current().nextInt(0, ALPHABET.length)]);
        }
        return out.toString();
    }
}
