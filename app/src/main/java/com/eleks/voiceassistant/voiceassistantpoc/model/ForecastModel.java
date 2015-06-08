package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class ForecastModel implements Serializable {
    @SerializedName("txt_forecast")
    public TextForecastModel textForecast;
    @SerializedName("simpleforecast")
    public SimpleForecastModel simpleForecast;
}
