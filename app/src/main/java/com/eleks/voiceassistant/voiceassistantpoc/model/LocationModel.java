package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey on 08.06.2015.
 */
public class LocationModel implements Serializable {
    @SerializedName("country_iso3166")
    public String countryCode;
    @SerializedName("country_name")
    public String countryName;
    @SerializedName("state")
    public String state;
    @SerializedName("city")
    public String city;
}
