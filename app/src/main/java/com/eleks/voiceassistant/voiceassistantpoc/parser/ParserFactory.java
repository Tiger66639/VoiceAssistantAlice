package com.eleks.voiceassistant.voiceassistantpoc.parser;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class ParserFactory {
    static public ParserBase create(String locale) {
        switch (locale){
            case "ru":
                return new ParserRu();
        }
        return new ParserEn();
    }
}
