package com.eleks.voiceassistant.voiceassistantpoc.parser;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class ParserFactory {
    static public ParserBase create(String locale) {
        if (locale.equals("ru_RU")) {
            return new ParserRu();
        }
        return new ParserEn();
    }
}
