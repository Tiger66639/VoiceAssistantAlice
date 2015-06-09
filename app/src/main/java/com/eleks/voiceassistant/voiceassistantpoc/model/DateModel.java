package com.eleks.voiceassistant.voiceassistantpoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class DateModel implements Serializable {
    @SerializedName("day")
    private int mDay;
    @SerializedName("month")
    private int mMonth;
    @SerializedName("year")
    private int mYear;

    public Date getDate() {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(mYear, mMonth, mDay);
        return calendar.getTime();
    }
}
