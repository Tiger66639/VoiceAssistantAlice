package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class WindModel {
    @SerializedName("mph")
    public int milesPerHour;
    @SerializedName("kph")
    public int kilometersPerHour;
    @SerializedName("dir")
    public int direction;
    @SerializedName("degrees")
    public int degrees;
}
