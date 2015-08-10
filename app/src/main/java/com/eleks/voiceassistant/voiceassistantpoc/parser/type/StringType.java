package com.eleks.voiceassistant.voiceassistantpoc.parser.type;

/**
 * Created by dnap on 08.08.15. VoiceAssistantPoC
 */
public class StringType extends BaseType {
    private String message = "";

    public StringType(Integer numberOfExpression, Integer paramName) {
        super(numberOfExpression, paramName);
    }

    @Override
    public Integer process(String message) {
        this.message = message;
        return message.length();
    }

    @Override
    public String getValue() {
        return message;
    }

    @Override
    public Boolean inEmpty() {
        return true;
    }
}
