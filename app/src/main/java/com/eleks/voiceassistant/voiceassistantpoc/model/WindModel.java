package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class WindModel implements Serializable {
    @SerializedName("mph")
    public int milesPerHour;
    @SerializedName("kph")
    public int kilometersPerHour;
    @SerializedName("dir")
    public String direction;
    @SerializedName("degrees")
    public int degrees;
}
