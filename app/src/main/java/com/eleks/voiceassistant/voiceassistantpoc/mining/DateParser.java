package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.text.TextUtils;

import com.eleks.voiceassistant.voiceassistantpoc.command.CommandPeriod;
import com.eleks.voiceassistant.voiceassistantpoc.command.CommandsUtils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sergey on 29.05.2015.
 */
public class DateParser {

    private static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final int DAYS_IN_WEEK = 7;
    private static final String TOMORROW = "tomorrow";
    private static final String YESTERDAY = "yesterday";
    private static final String WEEKEND = "weekend";
    private static final String THIS_MONTH = "this month";
    private static final String THIS_WEEK = "this week";
    private static final String[] sWeekdays = new DateFormatSymbols(Locale.ENGLISH).getWeekdays();
    private static final String[] sMonths = new DateFormatSymbols(Locale.ENGLISH).getMonths();
    private static final String NEXT_MONTH = "next month";
    private static final String NEXT_WEEK = "next week";
    private static final String TODAY = "today";
    private WordHolder[] mWords;
    private CommandPeriod mDates;
    private Date mToday;
    private Calendar mCalendar;

    public DateParser(WordHolder[] words) {
        mWords = words;
        String dateString = getDateString(mWords);
        if (!TextUtils.isEmpty(dateString)) {
            mDates = getDatesFromDateString(dateString);
        } else {
            mDates = new CommandPeriod();
        }
    }

    private String getDateString(WordHolder[] words) {
        String result = "";
        for (WordHolder wordHolder : words) {
            if (wordHolder.wordMeaning == WordMeaning.DATE) {
                result += wordHolder.word + " ";
            }
        }
        result = result.trim();
        if (!TextUtils.isEmpty(result)) {
            return result;
        } else {
            return null;
        }
    }

    private CommandPeriod getDatesFromDateString(String dateString) {
        CommandPeriod result = null;
        mToday = new Date();
        mCalendar = Calendar.getInstance(Locale.ENGLISH);
        if (CommandsUtils.fuzzyEquals(dateString, TODAY)) {                                         //today
            result = getTodayDates();
        } else if (CommandsUtils.fuzzyEquals(dateString, TOMORROW)) {                               //tomorrow
            result = getTomorrowDates();
        } else if (CommandsUtils.fuzzyEquals(dateString, YESTERDAY)) {                              //yesterday
            result = getYesterdayDates();
        } else if (CommandsUtils.fuzzyEquals(dateString, WEEKEND)) {                                //weekend
            result = getWeekend();
        } else if (CommandsUtils.fuzzyEquals(dateString, THIS_MONTH)) {                             //this month
            result = getThisMonth();
        } else if (CommandsUtils.fuzzyEquals(dateString, THIS_WEEK)) {                              //this week
            result = getThisWeek();
        } else if (getWeekDay(dateString) > 0) {                                                    //Monday, Tuesday...
            result = getWeekDaysFromDateString(dateString);
        } else if (existsMonthInDateString(dateString)) {                                           //January, February...
            result = getMonthDateFromDateString(dateString);
        } else if (CommandsUtils.fuzzyEquals(dateString, NEXT_MONTH)) {                             //Next month
            result = getNextMonth();
        } else if (CommandsUtils.fuzzyEquals(dateString, NEXT_WEEK)) {                              //Next week
            result = getNextWeek();
        }
        return result;
    }

    private CommandPeriod getTodayDates() {
        CommandPeriod result = new CommandPeriod();
        return result;
    }

    private CommandPeriod getMonthDateFromDateString(String dateString) {
        CommandPeriod result = null;
        String[] words = dateString.split(" ");
        int currentMonth = mCalendar.get(Calendar.MONTH);
        int currentYear = mCalendar.get(Calendar.YEAR);
        if (words.length == 1) {
            int month = getMonthFromWord(dateString);
            if (currentMonth > month) {
                currentYear++;
            }
            result = CommandsUtils.getMonthDates(month, currentYear);
        } else {
            int month = -1;
            int day = -1;
            for (String word : words) {
                int tmpMonth = getMonthFromWord(word);
                if (tmpMonth < 0) {
                    day = getDayFromWord(word);
                } else {
                    month = tmpMonth;
                }
            }
            if (month > 0 && day > 0) {
                result = new CommandPeriod();
                if (currentMonth > month) {
                    currentYear++;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(currentYear, month, day);
                result.startDate = calendar.getTime();
                result.finishDate = result.startDate;
            }
        }
        return result;
    }

    private int getDayFromWord(String word) {
        int result = -1;
        if (CommandsUtils.isWordContainsDigit(word)) {
            word = CommandsUtils.clearMonthDate(word);
            try {
                result = Integer.parseInt(word);
            } catch (NumberFormatException e) {
                //do nothing
            }
        }
        return result;
    }

    private int getMonthFromWord(String word) {
        int result = -1;
        for (int i = 0; i < sMonths.length; i++) {
            if (CommandsUtils.fuzzyEquals(sMonths[i].toLowerCase(), word)) {
                result = i;
                break;
            }
        }
        return result;
    }

    private CommandPeriod getNextWeek() {
        CommandPeriod result = new CommandPeriod();
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int diffFromSunday = dayOfWeek - Calendar.SUNDAY;
        result.startDate = new Date(mToday.getTime() -
                diffFromSunday * MILLISECONDS_IN_DAY +
                DAYS_IN_WEEK * MILLISECONDS_IN_DAY);
        result.finishDate = new Date(result.startDate.getTime() +
                (DAYS_IN_WEEK - 1) * MILLISECONDS_IN_DAY);
        return result;
    }

    private CommandPeriod getNextMonth() {
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int year = mCalendar.get(Calendar.YEAR);
        if (month > 11) {
            month = 0;
            year++;
        }
        return CommandsUtils.getMonthDates(month, year);
    }

    private CommandPeriod getWeekDaysFromDateString(String dateString) {
        CommandPeriod result = new CommandPeriod();
        int currentDayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int dayOfWeek = getWeekDay(dateString);
        int dayDiff;
        if (dayOfWeek > currentDayOfWeek) {
            dayDiff = dayOfWeek - currentDayOfWeek;
        } else if (dayOfWeek < currentDayOfWeek) {
            dayDiff = DAYS_IN_WEEK + dayOfWeek - currentDayOfWeek;
        } else {
            dayDiff = 0;
        }
        result.startDate = new Date(mToday.getTime() + dayDiff * MILLISECONDS_IN_DAY);
        result.finishDate = result.startDate;
        return result;
    }

    private CommandPeriod getThisWeek() {
        CommandPeriod result = new CommandPeriod();
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int diffFromSunday = dayOfWeek - Calendar.SUNDAY;
        result.startDate = new Date(mToday.getTime() - diffFromSunday * MILLISECONDS_IN_DAY);
        result.finishDate = new Date(result.startDate.getTime() +
                (DAYS_IN_WEEK - 1) * MILLISECONDS_IN_DAY);
        return result;
    }

    private CommandPeriod getThisMonth() {
        CommandPeriod result = new CommandPeriod();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        result = CommandsUtils.getMonthDates(month, year);
        return result;
    }

    private CommandPeriod getWeekend() {
        CommandPeriod result = new CommandPeriod();
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int diffFromSaturday;
        if (dayOfWeek <= Calendar.SATURDAY && dayOfWeek > Calendar.SUNDAY) {
            diffFromSaturday = Calendar.SATURDAY - dayOfWeek;
        } else {
            diffFromSaturday = -1;
        }
        result.startDate =
                new Date(mToday.getTime() + diffFromSaturday * MILLISECONDS_IN_DAY);
        result.finishDate = new Date(result.startDate.getTime() + MILLISECONDS_IN_DAY);
        return result;
    }

    private CommandPeriod getYesterdayDates() {
        CommandPeriod result = new CommandPeriod();
        result.startDate = new Date(mToday.getTime() - MILLISECONDS_IN_DAY);
        result.finishDate = result.startDate;
        return result;
    }

    private CommandPeriod getTomorrowDates() {
        CommandPeriod result = new CommandPeriod();
        result.startDate = new Date(mToday.getTime() + MILLISECONDS_IN_DAY);
        result.finishDate = result.startDate;
        return result;
    }

    private boolean existsMonthInDateString(String dateString) {
        String[] words = dateString.split(" ");
        for (String month : sMonths) {
            for (String word : words) {
                if (CommandsUtils.fuzzyEquals(month.toLowerCase(), word)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getWeekDay(String dateString) {

        int result = -1;
        for (int i = 1; i < sWeekdays.length; i++) {
            if (sWeekdays[i].toLowerCase().equals(dateString)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public CommandPeriod getDates() {
        return mDates;
    }
}
