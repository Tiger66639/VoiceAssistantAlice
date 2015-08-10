package com.eleks.voiceassistant.voiceassistantpoc.parser.type;

import com.dnap.opensource.stringToDate.Str2Time;

import java.security.PublicKey;
import java.util.Date;

/**
 * Created by dnap on 30.07.15. VoiceAssistantPoC
 */
public class DateTimeType extends BaseType {

    private final Str2Time str2Time;
    private Date date;

    public DateTimeType(Integer numberOfExpression, Integer paramName, Str2Time str2Time) {
        super(numberOfExpression, paramName);
        this.str2Time = str2Time;
    }

    @Override
    public Integer process(String message) {
        date = str2Time.convert(message);
        if(date == null) {
            return 0;
        }
        return message.length() - str2Time.getAfter().length() ;// - str2Time.getAfter().length()
    }

    public Date getValue() {
        return date;
    }

    @Override
    public Boolean inEmpty() {
        return false;
    }
}
