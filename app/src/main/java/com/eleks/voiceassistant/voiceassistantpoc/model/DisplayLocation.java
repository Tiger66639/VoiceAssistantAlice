package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey on 08.06.2015.
 */
public class DisplayLocation implements Serializable {
    @SerializedName("full")
    public String fullName;
    @SerializedName("city")
    public String city;
    @SerializedName("state")
    public String state;
    @SerializedName("state_name")
    public String stateName;
    @SerializedName("country_iso3166")
    public String countryCode;
}
