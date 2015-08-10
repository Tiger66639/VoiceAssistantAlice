/***
 *   Copyleft 2014 - WareNinja.com / Rumble In The Jungle!
 * 
 *  @author: yg@dnap.com
 *  @see https://github.com/WareNinja
 *  disclaimer: I code for fun, dunno what I'm coding about :-)
 */

package com.dnap.opensource.stringToDate;

import com.dnap.opensource.stringToDate.matcher.*;
import com.dnap.opensource.stringToDate.matcher_ru.StringToNumberMatcher;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * source adapted & extended from: http://stackoverflow.com/questions/1268174/phps-strtotime-in-java
 * 
 * example usage:
 * Date now = Str2Time.convert("now");
 * Date tomorrow = Str2Time.convert("tomorrow");
 * Date bla1 = Str2Time.convert("3 days");
 * Date bla2 = Str2Time.convert("-3 days");
 */
public class Str2Time {

    private List<Matcher> matchers;
    private List<Matcher> matchersFull;
    private Locale locale;

    public Str2Time() {
        this(Locale.ENGLISH);
    }

    public Str2Time(Locale locale) {
        this.locale = locale;
        if(locale.getLanguage().equalsIgnoreCase("en")) {
            initEnLocale();
            return;
        }
        if(locale.getLanguage().equalsIgnoreCase("ru")) {
            initRuLocale();
            return;
        }
        throw new RuntimeException("Unsupported language "+locale.getLanguage());
    }

    private void initEnLocale()
    {
        matchersFull = new LinkedList<Matcher>();

        matchersFull.add(new DateFormatMatcher(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")));// format used by FACEBOOK
        matchersFull.add(new DateFormatMatcher(new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")));
        matchersFull.add(new DateFormatMatcher(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")));
        matchersFull.add(new DateFormatMatcher(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")));// e.g. Mon, 03 Dec 2012 20:00:00
        matchersFull.add(new DateFormatMatcher(new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy"))); // format used by TWITTER! e.g. "Mon Sep 24 03:35:21 +0000 2012"

        matchers = new LinkedList<Matcher>();
        // set full date
        matchers.add(new NowMatcher());
        matchers.add(new TomorrowMatcher());
        matchers.add(new YesterdayMatcher());
        matchers.add(new DateMatcher());

        // add/sup
        matchers.add(new DaysMatcher());
        matchers.add(new WeeksMatcher());
        matchers.add(new YearsMatcher());
        matchers.add(new MinutesMatcher());

        // set time
        matchers.add(new TimeMatcher());
    }


    private void initRuLocale()
    {
        matchersFull = new LinkedList<Matcher>();

        matchers = new LinkedList<Matcher>();
        matchers.add(new StringToNumberMatcher());
        // set full date
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.NowMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.BeforeYesterdayMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.AfterTomorrowMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.TomorrowMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.YesterdayMatcher());
        matchers.add(new DateMatcher());

        // add/sup
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.YearsMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.MonthsMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.DaysMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.WeeksMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.WeekdayMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.ThroughMatcher());
        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.MinutesMatcher());

        matchers.add(new com.dnap.opensource.stringToDate.matcher_ru.HoursMatcher());

        // set time
        matchers.add(new TimeMatcher());
    }

    public Date convert(String input) {
    	return convert(input, new Date());
    }
    public Date convert(String input, Date refDate) {
        Calendar calendar = Calendar.getInstance();
        if(refDate != null) {
            calendar.setTime(refDate);
        }
        calendar = convert(input, calendar);
        if(calendar != null) {
            return calendar.getTime();
        }
        return null;
    }

    String lastInput = "";
    String lastRemaining = "";

    public Calendar convert(String input, Calendar calendar) {
        Boolean success = false;
        lastInput = input;
        input = input.toLowerCase(locale).trim();
        Setting setting = new Setting();

        for (Matcher matcher : matchersFull) {
            matcher.setSetting(setting);
            if(matcher.tryConvert(input, calendar)) {
                return calendar;
            }
        }

        for (Matcher matcher : matchers) {
            matcher.setSetting(setting);
            if(matcher.tryConvert(input, calendar)) {
                success = true;
                //System.out.println(matcher);
                // @todo refactor
                if(matcher.getStringWithoutMatch() != null) {
                    input = matcher.getStringWithoutMatch();
                }
            }
        }
        //System.out.println(input);
        lastRemaining = input;
        if(success){
            return calendar;
        }

        return null;
    }

    public String getBefore() {
        String input = lastInput.toLowerCase();
        if(lastRemaining.length() == 0 || lastRemaining.equals(input))
            return lastRemaining;

        String result = "";
        for (int i = 0; i < lastInput.length(); i++) {
            if(i >= lastRemaining.length())
                break;
            if(input.charAt(i) == lastRemaining.charAt(i)) {
                result += lastInput.charAt(i);
            } else {
                break;
            }
        }
        return result;
    }

    public String getAfter() {
        String input = lastInput.toLowerCase();
        //System.out.println(lastRemaining);
        if(lastRemaining.length() == 0 || lastRemaining.equals(input))
            return "";

        String result = "";
        int j = lastRemaining.length()-1;
        for (int i = lastInput.length()-1; i >= 0; i--) {
            if(j < 0)
                break;
            //System.out.println(lastRemaining.charAt(j)+ " "+input.charAt(i));

            if(input.charAt(i) == lastRemaining.charAt(j)) {
                result = lastInput.charAt(i) + result;
            } else {
                break;
            }
            j--;
        }


        return result;
    }


}
