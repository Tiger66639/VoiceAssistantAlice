/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnap.opensource.stringToDate.matcher;


import java.util.Calendar;
import java.util.regex.Pattern;

public class DateMatcher extends Matcher {

    private final Pattern yymmdd = Pattern.compile("([12]\\d{3})[- /]?([0]?\\d|1[0-2])[- /]?([0-2]?\\d|30|31)");

    public Boolean tryConvert(String input, Calendar calendar) {

        java.util.regex.Matcher matcher = yymmdd.matcher(input);
        if (matcher.find()) {
            int yy = Integer.parseInt(matcher.group(1));
            int mm = Integer.parseInt(matcher.group(2));
            int dd = Integer.parseInt(matcher.group(3));

            calendar.set(Calendar.YEAR, yy);
            calendar.set(Calendar.MONTH, mm-1);
            calendar.set(Calendar.DAY_OF_MONTH, dd);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            stringWithoutMatch = matcher.replaceFirst("");

            return true;
        }
        stringWithoutMatch = null;

        return false;
    }
}
