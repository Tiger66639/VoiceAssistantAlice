package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class NoiseMiner implements ITextMiner {

    private static final String[] NOISE_WORDS = {"in", "of", "the", "a", "please", "for", "what",
            "would", "be", "on", "is"};

    @Override
    public WordHolder[] investigate(Context context, WordHolder[] words) {
        for (WordHolder wordHolder : words) {
            if (wordHolder.wordMeaning == null) {
                for (String noiseWord : NOISE_WORDS) {
                    if (noiseWord.equals(wordHolder.word)) {
                        wordHolder.wordMeaning = WordMeaning.NOISE;
                    }
                }
            }
        }
        return words;
    }
}
