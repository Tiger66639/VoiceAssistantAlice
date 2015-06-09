package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;

/**
 * Created by Sergey on 06.06.2015.
 */
public class MessageHolder {

    public String message;
    public WordHolder[] words;
    public boolean isCursive;

    public MessageHolder(String message, WordHolder[] words, boolean isCursive) {
        this.message = message;
        this.words = words;
        this.isCursive = isCursive;
    }

    public MessageHolder(String message, boolean isCursive) {
        this.message = message;
        this.isCursive = isCursive;
    }
}
