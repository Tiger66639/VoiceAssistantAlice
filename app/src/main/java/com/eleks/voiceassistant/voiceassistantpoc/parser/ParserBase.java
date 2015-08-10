package com.eleks.voiceassistant.voiceassistantpoc.parser;

import com.eleks.voiceassistant.voiceassistantpoc.parser.expression.SimpleRegexp;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskAlarmClock;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskBase;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dnap on 28.07.15.
 */
abstract public class ParserBase {

    private Vector<SimpleRegexp> exprList;

    public ParserBase() {
        this.exprList = getExpressionList();
    }

    public TaskBase Parse(String message) {
        TaskBase task = null;
        for(SimpleRegexp expr: exprList) {
            task = expr.Check(message);
            if(task != null){
                break;
            }
        }

        return task;
    }

    abstract Vector<SimpleRegexp> getExpressionList();


}

