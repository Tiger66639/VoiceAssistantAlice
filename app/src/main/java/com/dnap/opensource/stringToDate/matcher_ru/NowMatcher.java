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
import java.util.Date;
import java.util.regex.Pattern;

public class NowMatcher extends Matcher {

    private static final Pattern now = Pattern.compile("(сейчас|сегодня)");

    public Boolean tryConvert(String input, Calendar calendar) {
        java.util.regex.Matcher matcher = now.matcher(input);
        if (matcher.find()) {
            calendar.setTime(new Date());
            stringWithoutMatch = matcher.replaceFirst("");
            setting.future = false;
            return true;
        }
        stringWithoutMatch = null;
        return false;
    }
}
