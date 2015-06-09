package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class TextForecastModel  implements Serializable {
    @SerializedName("forecastday")
    public TextForecastDayModel[] forecastDays;
}
