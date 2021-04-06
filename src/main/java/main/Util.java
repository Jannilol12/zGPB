package main;

import discord.command.Command;

import java.util.concurrent.ThreadLocalRandom;

public final class Util {

    private static final String[] ALPHABET = "a,b,c,d,e,f,1,2,3,4,5,6,7,8,9".split(",");

    private Util() {

    }

    public static boolean isValidDiscordID(String in) {
        if (in == null || in.length() != 18)
            return false;

        return in.chars().allMatch(Character::isDigit);
    }

    public static String createRandomString(int length) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length; i++) {
            out.append(ALPHABET[ThreadLocalRandom.current().nextInt(0, ALPHABET.length)]);
        }
        return out.toString();
    }

    public static Command getFuzzyMatchedCommand(String s) {
        Command curr = null;
        int lowLD = 1000;
        for (Command c : zGPB.INSTANCE.commandHandler.getRegisteredCommands()) {
            int ld = getLevenshteinDistance(s, c.getName());
            if (ld < lowLD) {
                lowLD = ld;
                curr = c;
            }
        }

        if (lowLD > 2)
            return null;

        return curr;
    }

    public static int getLevenshteinDistance(String s1, String s2) {
        int[][] distanceMatrix = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++)
            distanceMatrix[i][0] = i;

        for (int i = 1; i <= s2.length(); i++)
            distanceMatrix[0][i] = i;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int substitutionValue = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                distanceMatrix[i][j] = Math.min(Math.min(distanceMatrix[i - 1][j] + 1,
                        distanceMatrix[i][j - 1] + 1), distanceMatrix[i - 1][j - 1] + substitutionValue);
            }
        }

        return distanceMatrix[s1.length()][s2.length()];
    }

}
