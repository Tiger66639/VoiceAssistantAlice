package com.eleks.voiceassistant.voiceassistantpoc.parser.type;

/**
 * Created by dnap on 30.07.15. VoiceAssistantPoC
 */
public abstract class BaseType {
    private Integer paramName;
    private Integer numberOfExpression;

    public BaseType(Integer numberOfExpression, Integer paramName) {
        this.numberOfExpression = numberOfExpression;
        this.paramName = paramName;
    }

    public Integer getParamName() {
        return paramName;
    }

    public Integer getNumberOfExpression() {
        return numberOfExpression;
    }

    /**
     * Parse message
     * @return length expression
     */
    public abstract Integer process(String message);

    public abstract Object getValue();

    public abstract Boolean inEmpty();

}
