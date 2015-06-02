package com.eleks.voiceassistant.voiceassistantpoc.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class TemperatureModel {
    @SerializedName("fahrenheit")
    private String mFahrenheit;
    @SerializedName("celsius")
    private String mCelsius;

    public int getFahrenheit() {
        if (!TextUtils.isEmpty(mFahrenheit)) {
            return Integer.parseInt(mFahrenheit);
        } else {
            return 0;
        }
    }

    public int getCelsius() {
        if (!TextUtils.isEmpty(mCelsius)) {
            return Integer.parseInt(mCelsius);
        } else {
            return 0;
        }
    }
}
