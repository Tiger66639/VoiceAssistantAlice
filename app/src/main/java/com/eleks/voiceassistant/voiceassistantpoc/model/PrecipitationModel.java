package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class PrecipitationModel {
    @SerializedName("in")
    public int inches;
    @SerializedName("mm")
    public int millimeters;
}
