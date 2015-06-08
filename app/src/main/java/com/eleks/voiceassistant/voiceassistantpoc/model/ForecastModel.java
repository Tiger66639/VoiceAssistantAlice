package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class ForecastModel {
    @SerializedName("txt_forecast")
    public TextForecastModel textForecast;
    @SerializedName("simpleforecast")
    public SimpleForecastModel simpleForecast;
}
