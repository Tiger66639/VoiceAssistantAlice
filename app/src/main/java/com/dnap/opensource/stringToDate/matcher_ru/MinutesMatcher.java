/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnap.opensource.stringToDate.matcher_ru;

import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Calendar;
import java.util.regex.Pattern;

public class MinutesMatcher extends Matcher {

    private final Pattern minutes = Pattern.compile("([\\-\\+]?\\d+) (минута|минут|минуты)");

    public Boolean tryConvert(String input, Calendar calendar) {
        java.util.regex.Matcher matcher = minutes.matcher(input);
        if (matcher.find()) {
            int m = Integer.parseInt(matcher.group(1));
            calendar.add(Calendar.MINUTE, m);
            stringWithoutMatch = matcher.replaceFirst("");
            return true;
        }

        return false;
    }
}
