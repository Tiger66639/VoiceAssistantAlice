package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class TextForecastModel {
    @SerializedName("forecastday")
    public TextForecastModel[] forecastDays;
}
