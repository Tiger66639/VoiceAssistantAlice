/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 * 
 *  @author: yg@dnap.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.dnap.opensource.stringToDate.matcher;

import java.util.Calendar;
import java.util.regex.Pattern;

public class WeeksMatcher extends Matcher {

    private final Pattern weeks = Pattern.compile("([\\-\\+]?\\d+) week[s]?");

    public Boolean tryConvert(String input, Calendar calendar) {

        java.util.regex.Matcher matcher = weeks.matcher(input);
        if (matcher.find()) {
            int w = Integer.parseInt(matcher.group(1));
            calendar.add(Calendar.DAY_OF_YEAR, w * 7);
            stringWithoutMatch = matcher.replaceFirst("");
            return true;
        }

        return false;
    }
}
