package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class ForecastDayModel implements Serializable {
    @SerializedName("date")
    public DateModel dateModel;
    @SerializedName("high")
    public TemperatureModel highTemperature;
    @SerializedName("low")
    public TemperatureModel lowTemperature;
    @SerializedName("conditions")
    public String conditions;
    @SerializedName("icon")
    public String iconName;
    @SerializedName("icon_url")
    public String iconUrl;
    @SerializedName("qpf_allday")
    public PrecipitationModel qpfAllDay;
    @SerializedName("qpf_day")
    public PrecipitationModel qpfDay;
    @SerializedName("qpf_night")
    public PrecipitationModel qpfNight;
    @SerializedName("snow_allday")
    public PrecipitationModel snowAllDay;
    @SerializedName("snow_day")
    public PrecipitationModel snowDay;
    @SerializedName("snow_night")
    public PrecipitationModel snowNight;
    @SerializedName("maxwind")
    public WindModel maxWind;
    @SerializedName("avewind")
    public WindModel averageWind;
    @SerializedName("avehumidity")
    public int averageHumidity;
    @SerializedName("maxhumidity")
    public int maxHumidity;
    @SerializedName("minhumidity")
    public int minHumidity;
}
