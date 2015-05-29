package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.test.InstrumentationTestCase;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class WeatherCommand_Test extends InstrumentationTestCase {

    private final static String[] sPhrases = {
            "show weather in saint petersburg florida tomorrow"
    };

    public void test_Weather() {
        for (String phrase : sPhrases) {
            WeatherCommand weatherCommand =
                    new WeatherCommand(getInstrumentation().getTargetContext(), phrase);
            LatLng cityCoord = weatherCommand.getWhere();
            assertNotNull(cityCoord);
            assertNotNull(weatherCommand.getWhere());
        }
    }

    public void test_JakkardCoefficients() {
        BaseCommand baseCommand = new BaseCommand();
        double coef1 = baseCommand.getJakkardBigrammCoeficient("weather", "weather");
        double coef2 = baseCommand.getJakkardThreegrammCoeficient("weather", "wather");
        assertTrue(coef1 > 0);
        assertTrue(coef2 > 0);
    }

}
