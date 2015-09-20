package com.eleks.voiceassistant.voiceassistantpoc.parser;

import com.dnap.opensource.stringToDate.Str2Time;
import com.eleks.voiceassistant.voiceassistantpoc.parser.expression.SimpleRegexp;
import com.eleks.voiceassistant.voiceassistantpoc.parser.type.DateTimeType;
import com.eleks.voiceassistant.voiceassistantpoc.parser.type.StringType;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskAlarmClock;

import java.util.Locale;
import java.util.Vector;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class ParserRu extends ParserBase {
    @Override
    Vector<SimpleRegexp> getExpressionList() {
        Vector<SimpleRegexp> exprList = new Vector<SimpleRegexp>();
        Str2Time str2TimeRu = new Str2Time(new Locale("ru"));

        String taskAlarmClock = TaskAlarmClock.class.getName();
        exprList.add(new SimpleRegexp(taskAlarmClock, "^Будильник (на[ ]|в[ ]|)(\\2)(.*)$",
                new DateTimeType(2, TaskAlarmClock.DATETIME, str2TimeRu), new StringType(3, TaskAlarmClock.MESSAGE)));
        exprList.add(new SimpleRegexp(taskAlarmClock, "^Разбуди (в[ ]|)(\\2)(.*)$",
                new DateTimeType(2, TaskAlarmClock.DATETIME, str2TimeRu), new StringType(3, TaskAlarmClock.MESSAGE)));
        exprList.add(new SimpleRegexp(taskAlarmClock, "^Будильник$"));


        return exprList;
    }

    @Override
    public String getSpeechLocale() {
        return "ru_RU";
    }

    @Override
    public String getSpeechVoice() {
        return "Milena";
    }
}
