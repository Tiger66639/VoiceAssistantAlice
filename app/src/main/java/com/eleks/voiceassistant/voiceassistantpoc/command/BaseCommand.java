package com.eleks.voiceassistant.voiceassistantpoc.command;

import java.util.ArrayList;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class BaseCommand {

    protected int getLeveinsteinDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[] d1;
        int[] d2 = new int[n + 1];

        for (int i = 0; i <= n; i++)
            d2[i] = i;

        for (int i = 1; i <= m; i++) {
            d1 = d2;
            d2 = new int[n + 1];
            for (int j = 0; j <= n; j++) {
                if (j == 0) d2[j] = i;
                else {
                    int cost = (word1.charAt(i - 1) != word2.charAt(j - 1)) ? 1 : 0;
                    if (d2[j - 1] < d1[j] && d2[j - 1] < d1[j - 1] + cost)
                        d2[j] = d2[j - 1] + 1;
                    else if (d1[j] < d1[j - 1] + cost)
                        d2[j] = d1[j] + 1;
                    else
                        d2[j] = d1[j - 1] + cost;
                }
            }
        }
        return d2[n];
    }

    protected double getJakkardBigrammCoeficient(String word1, String word2) {
        int countOfSame = 0;
        ArrayList<String> bigramms1 = getBigramms(word1);
        ArrayList<String> bigramms2 = getBigramms(word2);
        for (String bigramm : bigramms1) {
            if (bigramms2.contains(bigramm)) {
                countOfSame++;
            }
        }
        return countOfSame * 1.0 / (bigramms1.size() + bigramms2.size());
    }

    protected double getJakkardCoefficient(String word1, String word2) {
        return getJakkardBigrammCoeficient(word1, word2) *
                getJakkardThreegrammCoeficient(word1, word2);
    }

    protected double getJakkardThreegrammCoeficient(String word1, String word2) {
        int countOfSame = 0;
        ArrayList<String> threegramm1 = getThreegramm(word1);
        ArrayList<String> threegramm2 = getThreegramm(word2);
        for (String threegramm : threegramm1) {
            if (threegramm2.contains(threegramm)) {
                countOfSame++;
            }
        }
        return countOfSame * 1.0 / (threegramm1.size() + threegramm2.size());
    }

    private ArrayList<String> getThreegramm(String word) {
        ArrayList<String> result = new ArrayList<>();
        if (word.length() > 2) {
            for (int i = 0; i < word.length() - 2; i++) {
                result.add(Character.toString(word.charAt(i)) +
                        Character.toString(word.charAt(i + 1)) +
                        Character.toString(word.charAt(i + 2)));
            }
        }
        return result;
    }

    private ArrayList<String> getBigramms(String word) {
        ArrayList<String> result = new ArrayList<>();
        if (word.length() > 1) {
            for (int i = 0; i < word.length() - 1; i++) {
                result.add(Character.toString(word.charAt(i)) +
                        Character.toString(word.charAt(i + 1)));
            }
        }
        return result;
    }
}
