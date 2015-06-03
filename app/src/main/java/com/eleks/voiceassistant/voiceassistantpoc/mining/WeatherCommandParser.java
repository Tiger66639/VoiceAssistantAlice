package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser extends BaseCommandParser {

    public WeatherCommandParser(Context context, String text) {
        super(context, text);
        fillMiners();
        applyMiners();
    }

    private void fillMiners() {
        mMiners = new ITextMiner[4];
        mMiners[0] = new CommandMiner();
        mMiners[1] = new WhenMiner();
        mMiners[2] = new NoiseMiner();
        mMiners[3] = new WhereMiner();
    }
}
