package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.test.InstrumentationTestCase;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser_Text extends InstrumentationTestCase {

    private final static String[] sPhrases = {
            "new show New York weather in June 22d",
            "show Boston weather in 22d of June",
            "show Boston weather in June"
    };

    public void test_class_WeatherCommandParser() {
        for (String phrase : sPhrases) {
            WeatherCommandParser weatherCommand = new WeatherCommandParser(getInstrumentation().getTargetContext(), phrase);
            assertTrue(weatherCommand.isCommand());
            if (weatherCommand.isCommand()) {
                assertNotNull(weatherCommand.getWhereLatLng());
                assertNotNull(weatherCommand.getWhereName());
            }
        }
    }
}
