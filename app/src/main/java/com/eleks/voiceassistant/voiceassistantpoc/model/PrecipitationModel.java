package com.eleks.voiceassistant.voiceassistantpoc.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class PrecipitationModel {
    @SerializedName("in")
    private String mInches;
    @SerializedName("mm")
    private String mMillimeters;

    public double getInches() {
        if (!TextUtils.isEmpty(mInches)) {
            return Double.parseDouble(mInches);
        } else {
            return 0;
        }
    }

    public double getMillimeters() {
        if (!TextUtils.isEmpty(mMillimeters)) {
            return Double.parseDouble(mMillimeters);
        } else {
            return 0;
        }
    }
}
