package com.dnap.opensource.stringToDate.matcher_ru;

import android.text.TextUtils;
import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by dnap on 29.07.15. stringToDate-for-java
 */
public class MonthsMatcher extends Matcher {

    private final Pattern month;
    private String[][] monthNameList;

    public MonthsMatcher() {
        // напомни в январе = 1.01
        monthNameList = new String[][]{
                {"числа"},
                {"январь", "января", "январе", "январи", "январей", "январях"},
                {"февраль", "февраля", "феврале", "феврали", "февралей", "февралях"},
                {"марта", "марте", "марты", "мартов", "мартах", "марту", "март"},
                {"апреля", "апреле", "апрели", "апрелей", "апрелях", "апрель"},
                {"мае", "майя", "майи", "майю", "май", "мая"},
                {"июни", "июней", "июнях", "июнь", "июня", "июне"},
                {"июлей", "июлях", "июль", "июля", "июле", "июли"},
                {"август", "августа", "августе", "августы", "августов", "августах", "августу"},
                {"сентябрь", "сентября", "сентябре", "сентябри", "сентябрей", "сентябрях"},
                {"октябрь", "октября", "октябре", "октябри", "октябрей", "октябрях"},
                {"ноябрь", "ноября", "ноябре", "ноябри", "ноябрей", "ноябрях"},
                {"декабря", "декабре", "декабри", "декабрей", "декабрях", "декабрь"}
        };

        Vector<String> monthNameAll = new Vector<String>();

        for (String[] aMonthNameList : monthNameList) {
            Collections.addAll(monthNameAll, aMonthNameList);
        }
        month = Pattern.compile("(((^|[^0-9])[012]?\\d|30|31)\\s+|[^а-я]|^)(" + TextUtils.join("|", monthNameAll) + ")([^а-я]|$)");
    }

    public Boolean tryConvert(String input, Calendar refDate) {

        java.util.regex.Matcher matcher = month.matcher(input);

        if (matcher.find()) {
            String dayString = matcher.group(2);
            String month = matcher.group(4);
            Integer monthNumber = 0;
            for (; monthNumber < monthNameList.length; monthNumber++) {
                if (Arrays.asList(monthNameList[monthNumber]).contains(month)) {
                    break;
                }
            }
            if(monthNumber == monthNameList.length)
                throw new RuntimeException("MonthsMatcher fail: "+input);
            monthNumber--;

            Integer day;
            if (dayString != null && !dayString.isEmpty()) {
                try {
                    day = Integer.parseInt(matcher.group(1).trim());
                } catch (NumberFormatException ignore) {
                    day = 1;
                }
            } else {
                day = 1;
            }


            if (monthNumber == -1) { // auto month
                if (day < refDate.get(Calendar.DATE)) {
                    refDate.add(Calendar.MONTH, 1);
                }
            } else {
                if (monthNumber < refDate.get(Calendar.MONTH)) {
                    refDate.add(Calendar.YEAR, 1);
                }
                refDate.set(Calendar.MONTH, monthNumber);
            }

            refDate.set(Calendar.DATE, day);
            stringWithoutMatch = matcher.replaceFirst("$5");

            return true;
        }
        stringWithoutMatch = null;

        return false;
    }
}

