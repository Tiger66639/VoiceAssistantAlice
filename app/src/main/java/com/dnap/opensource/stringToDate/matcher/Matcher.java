/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 * 
 *  @author: yg@dnap.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.dnap.opensource.stringToDate.matcher;

import com.dnap.opensource.stringToDate.Setting;

import java.util.Calendar;


public abstract class Matcher {

    protected Setting setting;

    protected String stringWithoutMatch = null;

    /**
	 * @param input
	 * @param refDate
	 *
	 * @return the converted Date
	 */
	public abstract Boolean tryConvert(String input, Calendar refDate);

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public boolean isFuture() {
        return setting.future;
    }

    public String getStringWithoutMatch() {
		return stringWithoutMatch;
	}
}