package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class TextForecastDayModel  implements Serializable {
    @SerializedName("period")
    public int period;
    @SerializedName("icon")
    public String iconName;
    @SerializedName("icon_url")
    public String iconUrl;
    @SerializedName("title")
    public String dayTitle;
    @SerializedName("fcttext")
    public String forecastText;
    @SerializedName("fcttext_metric")
    public String forecastTextMetric;
}
