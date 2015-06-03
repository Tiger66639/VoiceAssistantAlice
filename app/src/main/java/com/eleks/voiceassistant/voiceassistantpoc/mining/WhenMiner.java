package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;
import android.text.TextUtils;

import com.eleks.voiceassistant.voiceassistantpoc.command.CommandsUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WhenMiner implements ITextMiner {

    private static final String[] DATE_WORDS = {"today", "tomorrow", "yesterday", "weekend",
            "week", "this week", "next week", "month", "this month", "next month"};

    @Override
    public WordHolder[] investigate(Context context, WordHolder[] words) {
        boolean found = false;
        for (String dateWord : DATE_WORDS) {
            Integer[] result = findDateMatch(dateWord, words);
            if (result != null) {
                found = true;
                for (int index : result) {
                    words[index].wordMeaning = WordMeaning.DATE;
                }
            }
        }
        if (!found) {
            Integer[] result = findMonthDatePattern(words);
            if (result != null) {
                for (int index : result) {
                    words[index].wordMeaning = WordMeaning.DATE;
                }
            }
        }
        return words;
    }

    private Integer[] findMonthDatePattern(WordHolder[] words) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] months = new DateFormatSymbols(Locale.ENGLISH).getMonths();
        for (int i = 0; i < words.length; i++) {
            for (String month : months) {
                if (CommandsUtils.fuzzyEquals(words[i].word, month)) {
                    result = tryToFindMonthDate(words, i);
                    break;
                }
            }
        }
        if (result.size() > 0) {
            return result.toArray(new Integer[result.size()]);
        } else {
            return null;
        }
    }

    private ArrayList<Integer> tryToFindMonthDate(WordHolder[] words, int monthIndex) {
        ArrayList<Integer> result = new ArrayList<>();
        result.add(monthIndex);
        boolean found = false;
        for (int i = monthIndex; i >= 0; i--) {
            if (monthIndex - i <= 2) {
                if (ContainsNumber(words[i].word)) {
                    result.add(i);
                    found = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!found) {
            for (int i = monthIndex; i < words.length; i++) {
                if (i - monthIndex - i <= 1) {
                    if (ContainsNumber(words[i].word)) {
                        result.add(i);
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return result;
    }

    private boolean ContainsNumber(String word) {
        boolean result = false;
        for (int i = 0; i < word.length(); i++) {
            if (TextUtils.isDigitsOnly(Character.toString(word.charAt(i)))) {
                result = true;
                break;
            }
        }
        return result;
    }

    private Integer[] findDateMatch(String dateWord, WordHolder[] words) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] dateWords = dateWord.split(" ");
        for (int i = 0; i < words.length - dateWords.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < dateWords.length; j++) {
                found = found &&
                        (words[i + j].wordMeaning == null ||
                                words[i + j].wordMeaning == WordMeaning.DATE) &&
                        CommandsUtils.fuzzyEquals(words[i + j].word, dateWords[j]);
            }
            if (found) {
                for (int j = 0; j < dateWords.length; j++) {
                    result.add(i + j);
                }
            }
        }
        if (result.size() > 0) {
            return result.toArray(new Integer[result.size()]);
        } else {
            return null;
        }
    }
}
