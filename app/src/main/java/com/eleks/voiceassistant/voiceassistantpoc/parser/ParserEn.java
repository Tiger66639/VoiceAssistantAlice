package com.eleks.voiceassistant.voiceassistantpoc.parser;

import com.eleks.voiceassistant.voiceassistantpoc.parser.expression.SimpleRegexp;

import java.util.Vector;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class ParserEn extends ParserBase {
    @Override
    Vector<SimpleRegexp> getExpressionList() {
        return new Vector<>();
    }

    @Override
    public String getSpeechLocale() {
        return "en_US";
    }

    @Override
    public String getSpeechVoice() {
        return "Samantha";
    }
}
