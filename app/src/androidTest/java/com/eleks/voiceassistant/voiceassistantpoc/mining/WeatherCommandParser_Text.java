package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.test.InstrumentationTestCase;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser_Text extends InstrumentationTestCase {

    private final static String[] sPhrases = {
            "what would be the weather in Boston in two days",
            "what would be the weather in Boston next two days",
            "weather New York next three days",
            "show Boston's weather on Monday",
            "show Boston weather in June"
    };

    public void test_class_WeatherCommandParser() {
        for (String phrase : sPhrases) {
            WeatherCommandParser weatherCommand = new WeatherCommandParser(getInstrumentation().getTargetContext(), phrase);
            assertTrue(weatherCommand.isCommand());
            if (weatherCommand.isCommand()) {
                assertNotNull(weatherCommand.getWhereLatLng());
                assertNotNull(weatherCommand.getWhereName());
                assertNotNull(weatherCommand.getWhenDates());
            }
        }
    }
}
