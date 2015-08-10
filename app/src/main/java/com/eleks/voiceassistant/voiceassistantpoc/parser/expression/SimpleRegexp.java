package com.eleks.voiceassistant.voiceassistantpoc.parser.expression;

import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserBase;
import com.eleks.voiceassistant.voiceassistantpoc.parser.type.BaseType;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskAlarmClock;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskBase;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class SimpleRegexp {
    private final String className;
    private final String expression;
    private final BaseType[] params;

    public SimpleRegexp(String className, String expression, BaseType... params) {
        this.className = className;
        this.expression = expression;
        this.params = params;
    }

    /**
     *
     * @param message
     * @return
     */
    public TaskBase Check(String message) {
        Vector<Integer> dynamicExpression = new Vector<>();
        if (params.length > 0) {
            String exprPrepared = expression;
            for (BaseType param : params) {
                String exprPrepared2 = exprPrepared.replace("\\" + param.getNumberOfExpression(), ".+");
                if (!exprPrepared.equals(exprPrepared2)) {
                    exprPrepared = exprPrepared2;
                    dynamicExpression.add(param.getNumberOfExpression());
                }
            }

            Pattern pattern = Pattern.compile(exprPrepared);
            Matcher matcher = pattern.matcher(message);

            if (!matcher.matches()) {
                return null;
            }

            HashMap<Integer, Object> resultParams = new HashMap<>();

            if (dynamicExpression.size() > 0) {
                exprPrepared = expression;
                for (BaseType param : params) {
                    if( dynamicExpression.contains(param.getNumberOfExpression()) ) {
                        String text = matcher.group(param.getNumberOfExpression());
                        Integer length = param.process(text);
                        if (length == 0)
                            return null;
                        exprPrepared = exprPrepared.replace("\\" + param.getNumberOfExpression(), ".{" + length + "}");
                    }
                }
                pattern = Pattern.compile(exprPrepared);
                matcher = pattern.matcher(message);
                if (!matcher.matches()) {
                    return null;
                }
            }

            for (BaseType param : params) {
                if (param.process(matcher.group(param.getNumberOfExpression())) == 0 && !param.inEmpty()) {
                    return null;
                }
                resultParams.put(param.getParamName(), param.getValue());
            }


            return TaskFactory.create(className, resultParams);

        }

        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(message);

        if (matcher.matches()) {
            return TaskFactory.create(className);
        }

        return null;
    }


}
