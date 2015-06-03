package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WeatherCommandParser extends BaseCommandParser {

    private final PlaceParser mPlaceParser;

    public WeatherCommandParser(Context context, String text) {
        super(context, text);
        fillMiners();
        applyMiners();
        mPlaceParser = new PlaceParser(mContext, mWords);
    }

    private void fillMiners() {
        mMiners = new ITextMiner[4];
        mMiners[0] = new CommandMiner();
        mMiners[1] = new WhenMiner();
        mMiners[2] = new NoiseMiner();
        mMiners[3] = new WhereMiner();
    }

    public LatLng getWhereLatLng() {
        return mPlaceParser.getPlaceLatLng();
    }

    public String getWhereName() {
        return mPlaceParser.getPlaceName();
    }
}
