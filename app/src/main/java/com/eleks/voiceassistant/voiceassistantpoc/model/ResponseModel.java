package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class ResponseModel implements Serializable {
    @SerializedName("forecast")
    public ForecastModel forecast;
}
