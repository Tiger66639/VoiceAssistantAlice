package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.test.InstrumentationTestCase;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class WeatherCommand_Test extends InstrumentationTestCase {

    private final static String[] sPhrases = {
            "show weather in July 22nd in saint petersburg florida"
    };

    public void test_Weather() {
        for (String phrase : sPhrases) {
            WeatherCommand weatherCommand =
                    new WeatherCommand(getInstrumentation().getTargetContext(), phrase);
            LatLng cityCoord = weatherCommand.getWhere();
            assertNotNull(cityCoord);
            assertNotNull(weatherCommand.getWhereName());
            CommandPeriod commandDate=weatherCommand.getCommandDate();
            assertNotNull(commandDate);
        }
    }

    public void test_JakkardCoefficients() {
        double coef1 = CommandsUtils.getJakkardBigrammCoeficient("weather", "weather");
        double coef2 = CommandsUtils.getJakkardThreegrammCoeficient("weather", "wather");
        assertTrue(coef1 > 0);
        assertTrue(coef2 > 0);
    }

}
