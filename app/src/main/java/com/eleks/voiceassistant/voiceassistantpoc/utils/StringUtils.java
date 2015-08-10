package com.eleks.voiceassistant.voiceassistantpoc.utils;

/**
 * Created by dnap on 08.08.15. VoiceAssistantPoC
 */
public class StringUtils {
    public static String replaceAll(String needle, String replace, String stack) {
        if (((stack == null) || (stack.length() == 0)) || ((needle == null) || (needle.length() == 0))) {
            return stack;
        }

        while ((stack != null) && (stack.contains(needle))) {
            stack = stack.replace(needle, replace);
        }
        return stack;
    }

}
