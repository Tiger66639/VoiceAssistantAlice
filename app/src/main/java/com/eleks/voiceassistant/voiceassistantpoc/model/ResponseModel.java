package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class ResponseModel implements Serializable {
    @SerializedName("location")
    public LocationModel location;
    @SerializedName("forecast")
    public ForecastModel forecast;
    @SerializedName("current_observation")
    public CurrentObservationModel currentObservation;
}
