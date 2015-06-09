package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Sergey on 08.06.2015.
 */
public class CurrentObservationModel implements Serializable {
    @SerializedName("display_location")
    public DisplayLocation displayLocation;
    @SerializedName("weather")
    public String weather;
    @SerializedName("temp_f")
    public double temperatureFahrenheit;
    @SerializedName("temp_c")
    public double temperatureCelsius;
    @SerializedName("relative_humidity")
    public String relativeHumidity;
    @SerializedName("wind_string")
    public String windString;
    @SerializedName("wind_dir")
    public String windDirection;
    @SerializedName("wind_mph")
    public String windMph;
    @SerializedName("feelslike_f")
    public double feelsLikeFahrenheit;
    @SerializedName("icon")
    public String iconName;
}
