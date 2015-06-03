package com.eleks.voiceassistant.voiceassistantpoc.mining;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public interface ITextMiner {
    WordHolder[] investigate(WordHolder[] words);
}
