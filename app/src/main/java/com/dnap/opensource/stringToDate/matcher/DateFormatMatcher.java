/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 * 
 *  @author: yg@dnap.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.dnap.opensource.stringToDate.matcher;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class DateFormatMatcher extends Matcher {

    private final DateFormat dateFormat;

    public DateFormatMatcher(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Boolean tryConvert(String input, Calendar refDate) {
        try {
            refDate.setTime(dateFormat.parse(input));
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}
