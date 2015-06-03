package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.test.InstrumentationTestCase;

import com.eleks.voiceassistant.voiceassistantpoc.mining.WeatherCommandParser;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser_Text extends InstrumentationTestCase {

    private final static String[] sPhrases = {
            "show Boston weather in June 22d",
            "show Boston weather in 22d of June"
    };

    public void test_class_WeatherCommandParser() {
        for (String phrase : sPhrases) {
            WeatherCommandParser weatherCommand = new WeatherCommandParser(phrase);
            assertTrue(weatherCommand.isCommand());
        }
    }
}
