package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.text.TextUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sergey on 29.05.2015.
 */
public class CommandsUtils {

    private static final double JAKKARD_MIN_COEFFICIENT = 0.055;
    private static final int LEVEINSTEIN_MAX_DISTANCE = 2;

    public static boolean isWordExistsInArrayFuzzyEquals(String word, String[] array) {
        if (!TextUtils.isEmpty(word)) {
            for (String arrayWord : array) {
                if (!TextUtils.isEmpty(arrayWord)) {
                    if (getLeveinsteinDistance(arrayWord.toLowerCase(), word) <=
                            LEVEINSTEIN_MAX_DISTANCE &&
                            getJakkardCoefficient(arrayWord.toLowerCase(), word) >
                                    JAKKARD_MIN_COEFFICIENT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected static int getLeveinsteinDistance(String word1, String word2) {
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

    protected static double getJakkardBigrammCoeficient(String word1, String word2) {
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

    protected static double getJakkardCoefficient(String word1, String word2) {
        return getJakkardBigrammCoeficient(word1, word2) *
                getJakkardThreegrammCoeficient(word1, word2);
    }

    protected static double getJakkardThreegrammCoeficient(String word1, String word2) {
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

    private static ArrayList<String> getThreegramm(String word) {
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

    private static ArrayList<String> getBigramms(String word) {
        ArrayList<String> result = new ArrayList<>();
        if (word.length() > 1) {
            for (int i = 0; i < word.length() - 1; i++) {
                result.add(Character.toString(word.charAt(i)) +
                        Character.toString(word.charAt(i + 1)));
            }
        }
        return result;
    }

    public static int getWordPositionInArray(String word, String[] array) {
        if (!TextUtils.isEmpty(word)) {
            for (int i = 0; i < array.length; i++) {
                if (!TextUtils.isEmpty(array[i])) {
                    if (getLeveinsteinDistance(array[i].toLowerCase(), word) <=
                            LEVEINSTEIN_MAX_DISTANCE &&
                            getJakkardCoefficient(array[i].toLowerCase(), word) >
                                    JAKKARD_MIN_COEFFICIENT) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static String clearMonthDate(String word) {
        String result = "";
        for (int i = 0; i < word.length(); i++) {
            if (TextUtils.isDigitsOnly(Character.toString(word.charAt(i)))) {
                result += Character.toString(word.charAt(i));
            }
        }
        return result;
    }

    public static boolean isWordContainsDigit(String word) {
        boolean result = false;
        for (int i = 0; i < word.length(); i++) {
            if (TextUtils.isDigitsOnly(Character.toString(word.charAt(i)))) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean fuzzyEquals(String word1, String word2) {
        return getLeveinsteinDistance(word1, word2) <=
                LEVEINSTEIN_MAX_DISTANCE &&
                getJakkardCoefficient(word1, word2) >
                        JAKKARD_MIN_COEFFICIENT;
    }

    public static int getMonthFromWord(String word) {
        String[] months = new DateFormatSymbols(Locale.ENGLISH).getMonths();
        int result = 0;
        for (int i = 0; i < months.length; i++) {
            if (months[i].toLowerCase().equals(word)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public static CommandPeriod getMonthDates(int month, int year) {
        CommandPeriod result = new CommandPeriod();
        Calendar startDate = Calendar.getInstance(Locale.ENGLISH);
        startDate.set(year, month, 1);
        result.startDate = startDate.getTime();
        int lastDayOfMonth = startDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar finishDate = Calendar.getInstance(Locale.ENGLISH);
        finishDate.set(year, month, lastDayOfMonth);
        result.finishDate = finishDate.getTime();
        return result;
    }

    public static String[] clearWordsInArray(String[] words, int startIndex, int length) {
        ArrayList<String> result = new ArrayList<>();
        if (startIndex + length < words.length) {
            for (int i = startIndex; i < startIndex + length; i++) {
                words[i] = "";
            }
            for (String word : words) {
                if (!TextUtils.isEmpty(word)) {
                    result.add(word);
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static String[] removeNoiseWords(String[] words, String[] noiseWords) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> noiseWordList = new ArrayList<>();
        for (String word : words) {
            if (!isWordExistsInArrayAccurateEquals(word, noiseWords)) {
                result.add(word);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isWordExistsInArrayAccurateEquals(String word, String[] array) {
        for (String arrayWord : array) {
            if (arrayWord.equals(word)) {
                return true;
            }
        }
        return false;
    }

    public static String getTextFromArray(String[] words) {
        String result = "";
        if (words.length > 0) {
            for (String word : words) {
                result += word + " ";
            }
        }
        return result.trim();
    }
}
