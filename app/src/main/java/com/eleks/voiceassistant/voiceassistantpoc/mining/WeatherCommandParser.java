package com.eleks.voiceassistant.voiceassistantpoc.mining;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser extends BaseCommandParser {

    public WeatherCommandParser(String text) {
        super(text);
        fillMiners();
        applyMiners();
    }

    private void fillMiners() {
        mMiners = new ITextMiner[3];
        mMiners[0]=new CommandMiner();
        mMiners[1]=new WhenMiner();
    }
}
