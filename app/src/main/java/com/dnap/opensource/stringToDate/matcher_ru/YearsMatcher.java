/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 *
 *  @author: yg@dnap.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.dnap.opensource.stringToDate.matcher_ru;

import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Calendar;
import java.util.regex.Pattern;

public class YearsMatcher extends Matcher {

    private final Pattern years = Pattern.compile("([\\-\\+]?\\d+) (год|года|лет)");

    public Boolean tryConvert(String input, Calendar calendar) {

        java.util.regex.Matcher matcher = years.matcher(input);
        if (matcher.find()) {
            int y = Integer.parseInt(matcher.group(1));
            calendar.add(Calendar.DAY_OF_YEAR, y * 366);
            stringWithoutMatch = matcher.replaceFirst("");
            return true;
        }

        return false;
    }
}
