package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;

/**
 * Created by Sergey on 06.06.2015.
 */
public class MessageHolder {

    public String message;
    public WordHolder[] words;

    public MessageHolder(String message, WordHolder[] words) {
        this.message = message;
        this.words = words;
    }

    public MessageHolder(String message) {
        this.message = message;
    }
}
