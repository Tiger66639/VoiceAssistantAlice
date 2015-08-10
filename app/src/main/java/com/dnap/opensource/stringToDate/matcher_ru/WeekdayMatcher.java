package com.dnap.opensource.stringToDate.matcher_ru;

import android.text.TextUtils;
import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by dnap on 02.08.15. strtotime-for-java
 */
public class WeekdayMatcher extends Matcher {
    private final Pattern weekday;
    private String[][] weekdayNameList;

    public WeekdayMatcher() {
        weekdayNameList = new String[][]{
                {"воскресенье", "воскресенья", "воскресеньи", "воскресений", "воскресеньях", "вс"},
                {"понедельник", "понедельника", "понедельнике", "понедельники", "понедельников", "понедельниках", "пн"},
                {"вторник", "вторника", "вторнике", "вторники", "вторников", "вторниках", "вт"},
                {"среда", "среды", "среду", "среде", "сред", "средах", "ср"},
                {"четверг", "четверга", "четверге", "четверги", "четвергов", "четвергах", "чт"},
                {"пятница", "пятницы", "пятницу", "пятнице", "пятниц", "пятницах", "пт"},
                {"суббота", "субботы", "субботу", "субботе", "суббот", "субботах", "сб"},

        };

        Vector<String> weekdayNameAll = new Vector<String>();

        for (String[] aWeekdayNameList : weekdayNameList) {
            Collections.addAll(weekdayNameAll, aWeekdayNameList);
        }
        weekday = Pattern.compile("([^а-я]|^)(" + TextUtils.join("|", weekdayNameAll) + ")([^а-я]|$)");
    }

    @Override
    public Boolean tryConvert(String input, Calendar refDate) {

        java.util.regex.Matcher matcher = weekday.matcher(input);

        if (matcher.find()) {
            String day = matcher.group(2);
            Integer dayNumber = 0;
            for (; dayNumber < weekdayNameList.length; dayNumber++) {
                if (Arrays.asList(weekdayNameList[dayNumber]).contains(day)) {
                    break;
                }
            }
            if(dayNumber == weekdayNameList.length)
                throw new RuntimeException("MonthsMatcher fail: "+input);

            dayNumber++;
            if (isFuture()) {
                if (dayNumber != refDate.get(Calendar.DAY_OF_WEEK)) {
                    Calendar tmpCalendar = (Calendar) refDate.clone();
                    refDate.set(Calendar.DAY_OF_WEEK, dayNumber);
                    if (tmpCalendar.after(refDate)) {
                        refDate.add(Calendar.DATE, 7);
                    }
                }
            } else {
                refDate.set(Calendar.DAY_OF_WEEK, dayNumber);
            }
            stringWithoutMatch = matcher.replaceFirst("$1$3");

            return true;
        }
        stringWithoutMatch = null;

        return false;
    }
}
