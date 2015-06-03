package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class CommandMiner implements ITextMiner {

    private static final String POSSESSIVE_SUFFIX = "'s";
    private static final String[] COMMAND_KEYWORDS = {
            "weather", "show weather", "get weather", "what weather", "what temperature",
            "temperature", "how hot", "how cold"
    };
    private static final int INFINITY_DISTANCE = 999;

    @Override
    public WordHolder[] investigate(Context context, WordHolder[] words) {
        words = removePossessives(words);
        for (String command : COMMAND_KEYWORDS) {
            String[] commandWords = command.split(" ");
            if (words.length >= commandWords.length) {
                Integer[] results = findCommandInWords(words, commandWords);
                if (results != null && getMaxDistance(results) < 4) {
                    for (int index : results) {
                        words[index].wordMeaning = WordMeaning.COMMAND;
                    }
                }
            }
        }
        return words;
    }

    private WordHolder[] removePossessives(WordHolder[] words) {
        for (WordHolder wordHolder : words) {
            if (wordHolder.word.endsWith(POSSESSIVE_SUFFIX)) {
                wordHolder.word = wordHolder.word
                        .substring(0, wordHolder.word.length() - POSSESSIVE_SUFFIX.length());
            }
        }
        return words;
    }

    private int getMaxDistance(Integer[] results) {
        int maxDistance = 0;
        if (results != null) {
            if (results.length > 1) {
                for (int i = 0; i < results.length - 1; i++) {
                    if (results[i + 1] - results[i] > maxDistance) {
                        maxDistance = results[i + 1] - results[i];
                    }
                }
            }
        } else {
            maxDistance = INFINITY_DISTANCE;
        }
        return maxDistance;
    }

    private Integer[] findCommandInWords(WordHolder[] words, String[] commandWords) {
        ArrayList<Integer> result = new ArrayList<>();
        int pointer = 0;
        boolean foundSomething = false;
        for (int i = 0; i < words.length; i++) {
            if (CommandsUtils.fuzzyEquals(words[i].word, commandWords[pointer])) {
                foundSomething = true;
                result.add(i);
                pointer++;
            }
            if (foundSomething & pointer >= commandWords.length) {
                break;
            }
        }
        if (result.size() > 0 && foundSomething && pointer >= commandWords.length) {
            return result.toArray(new Integer[result.size()]);
        } else {
            return null;
        }
    }
}
