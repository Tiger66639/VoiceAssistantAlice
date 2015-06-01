package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.text.TextUtils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sergey on 29.05.2015.
 */
public class DateParser {

    private static final String TODAY = "today";
    private static final String TOMORROW = "tomorrow";
    private static final String YESTERDAY = "yesterday";
    private static final String WEEKEND = "weekend";
    private static final String MONTH = "month";
    private static final String WEEK = "week";
    private static final String NEXT = "next";
    private static final String[] DATE_WORDS = {TODAY, TOMORROW, YESTERDAY, WEEKEND, WEEK,
            MONTH};
    private static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final int DAYS_IN_WEEK = 7;
    private String mText;
    private String[] mWords;
    private CommandPeriod mDates;

    public DateParser(String[] words) {
        mWords = words;
        mText = CommandsUtils.getTextFromArray(mWords);
        String dateString = getDateString();
        if (!TextUtils.isEmpty(dateString)) {
            mDates = getDatesFromDateString(dateString);
        } else {
            mDates = new CommandPeriod();
        }
    }

    private CommandPeriod getDatesFromDateString(String dateString) {
        CommandPeriod result = new CommandPeriod();
        String[] dateWords = dateString.split(" ");
        Date today = new Date();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        if (dateWords.length == 1) {
            if (CommandsUtils.fuzzyEquals(dateWords[0], TOMORROW)) {
                result.startDate = new Date(today.getTime() + MILLISECONDS_IN_DAY);
                result.finishDate = result.startDate;
            } else if (CommandsUtils.fuzzyEquals(dateWords[0], YESTERDAY)) {
                result.startDate = new Date(today.getTime() - MILLISECONDS_IN_DAY);
                result.finishDate = result.startDate;
            } else if (CommandsUtils.fuzzyEquals(dateWords[0], WEEKEND)) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int diffFromSaturday;
                if (dayOfWeek <= Calendar.SATURDAY && dayOfWeek > Calendar.SUNDAY) {
                    diffFromSaturday = Calendar.SATURDAY - dayOfWeek;
                } else {
                    diffFromSaturday = -1;
                }
                result.startDate =
                        new Date(today.getTime() + diffFromSaturday * MILLISECONDS_IN_DAY);
                result.finishDate = new Date(result.startDate.getTime() + MILLISECONDS_IN_DAY);
            } else if (CommandsUtils.fuzzyEquals(dateWords[0], WEEK)) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int diffFromSunday = dayOfWeek - Calendar.SUNDAY;
                result.startDate = new Date(today.getTime() - diffFromSunday * MILLISECONDS_IN_DAY);
                result.finishDate = new Date(result.startDate.getTime() +
                        (DAYS_IN_WEEK - 1) * MILLISECONDS_IN_DAY);
            } else if (CommandsUtils.fuzzyEquals(dateWords[0], MONTH)) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                result = CommandsUtils.getMonthDates(month, year);
            } else {
                int month = CommandsUtils.getMonthFromWord(dateWords[0]);
                if (month > 0) {
                    int currentMonth = calendar.get(Calendar.MONTH);
                    int currentYear = calendar.get(Calendar.YEAR);
                    if (currentMonth > month) {
                        currentYear++;
                    }
                    result = CommandsUtils.getMonthDates(month, currentYear);
                }
            }
        } else {
            if (dateWords[0].equals(NEXT)) {
                if (dateWords[1].equals(MONTH)) {
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int year = calendar.get(Calendar.YEAR);
                    if (month > 11) {
                        month = 0;
                        year++;
                    }
                    result = CommandsUtils.getMonthDates(month, year);
                } else if (dateWords[1].equals(WEEK)) {
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int diffFromSunday = dayOfWeek - Calendar.SUNDAY;
                    result.startDate = new Date(today.getTime() -
                            diffFromSunday * MILLISECONDS_IN_DAY +
                            DAYS_IN_WEEK * MILLISECONDS_IN_DAY);
                    result.finishDate = new Date(result.startDate.getTime() +
                            (DAYS_IN_WEEK - 1) * MILLISECONDS_IN_DAY);
                }
            } else {
                int month = CommandsUtils.getMonthFromWord(dateWords[0]);
                if (month > 0) {
                    int currentMonth = calendar.get(Calendar.MONTH);
                    int currentYear = calendar.get(Calendar.YEAR);
                    int dayInMonth = Integer.parseInt(dateWords[1]);
                    if (currentMonth > month) {
                        currentYear++;
                    }
                    Calendar monthCalendar = Calendar.getInstance(Locale.ENGLISH);
                    monthCalendar.set(currentYear, month, dayInMonth);
                    result.startDate = monthCalendar.getTime();
                    result.finishDate = result.startDate;
                }
            }

        }
        return result;
    }

    private String getDateString() {
        String result = null;
        for (int i = 0; i < mWords.length; i++) {
            if (CommandsUtils.isWordExistsInArrayFuzzyEquals(mWords[i], DATE_WORDS)) {
                if (mWords[i].equals(MONTH) || mWords[i].equals(WEEK)) {
                    if (mText.contains(NEXT + " " + mWords[i])) {
                        result = NEXT + " " + mWords[i];
                        mWords[i - 1] = "";
                        mWords[i] = "";
                        break;
                    }
                } else {
                    result = mWords[i];
                    mWords[i] = "";
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(result)) {
            result = tryFindMonth();
        }
        return result;
    }

    private String tryFindMonth() {
        String result = null;
        String[] months = new DateFormatSymbols(Locale.ENGLISH).getMonths();
        for (String month : months) {
            int monthPosition = CommandsUtils.getWordPositionInArray(month.toLowerCase(), mWords);
            if (monthPosition > 0) {
                result = tryFindMonthDate(month.toLowerCase(), monthPosition);
                break;
            }
        }
        return result;
    }

    private String tryFindMonthDate(String month, int monthPosition) {
        String result = month;
        for (int i = 0; i < mWords.length; i++) {
            if (CommandsUtils.isWordContainsDigit(mWords[i]) &&
                    Math.abs(monthPosition - i) <= 2) {
                int startIndex = monthPosition;
                int finishIndex = i;
                if (monthPosition > i) {
                    startIndex = i;
                    finishIndex = monthPosition;
                }
                result += " " + CommandsUtils.clearMonthDate(mWords[i]);
                for (int j = startIndex; j <= finishIndex; j++) {
                    mWords[j] = "";
                }
                break;
            }
        }
        mWords[monthPosition] = "";
        return result;
    }

    public CommandPeriod getDates() {
        if (mDates != null) {
            return mDates;
        } else {
            return new CommandPeriod();
        }
    }

    public String[] getRemainingWords() {
        ArrayList<String> result = new ArrayList<>();
        for (String word : mWords) {
            if (!TextUtils.isEmpty(word)) {
                result.add(word);
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
