/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnap.opensource.stringToDate.matcher;

import java.util.Calendar;
import java.util.regex.Pattern;

public class TimeMatcher extends Matcher {

    private final Pattern hhmmss = Pattern.compile("([0-1]?\\d|2[0-4])([: ]+([0-6]?\\d)|)([: ]+([0-6]?\\d)|)$");

    public Boolean tryConvert(String input, Calendar calendar) {

        java.util.regex.Matcher matcher = hhmmss.matcher(input);
        if (matcher.find()) {
            int hh = Integer.parseInt(matcher.group(1));
            String mmString = matcher.group(3);
            int mm = 0;
            if(mmString != null) {
                mm = Integer.parseInt(mmString);
            }
            int ss = 0;
            String ssString = matcher.group(5);
            if(ssString != null) {
                ss = Integer.parseInt(ssString);
            }
            calendar.set(Calendar.HOUR_OF_DAY, hh);
            calendar.set(Calendar.MINUTE, mm);
            if(isFuture() && calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            calendar.set(Calendar.SECOND, ss);
            stringWithoutMatch = matcher.replaceFirst("");
            return true;
        }

        return false;
    }
}
