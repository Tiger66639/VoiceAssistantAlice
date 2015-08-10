package com.dnap.opensource.stringToDate.matcher_ru;

import android.text.TextUtils;
import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Created by dnap on 29.07.15. stringToDate-for-java
 */
public class StringToNumberMatcher extends Matcher {

    private final Pattern[] numbers;

    public StringToNumberMatcher() {
        String[][] numbersVariant = new String[][]{
                {"ноль", "нуль"},
                {"один", "одного", "одному", "одним", "одном", "одна", "одной", "одну", "одних", "одними", "одни", "одною", "одно"},
                {"два", "двух", "двум", "двумя", "две", "пару", "паре"},
                {"три", "трем", "трех", "тремя"},
                {"четырех", "четырем", "четырьмя", "четыре"},
                {"пяти", "пятью", "пять"},
                {"шести", "шестью", "шесть"},
                {"семи", "семью", "семь"},
                {"восьми", "восьмью", "восемь"},
                {"девяти", "девятью", "девять"},
                {"десяти", "десятью", "десять"}
        };

        numbers = new Pattern[numbersVariant.length];

        for (int i = 0; i < numbersVariant.length; i++) {
            numbers[i] = Pattern.compile("([^а-я]|^)("+ TextUtils.join("|", numbersVariant[i])+")([^а-я]|$)");
        }


    }

    public Boolean tryConvert(String input, Calendar refDate) {

        java.util.regex.Matcher matcher;
        String out = input;
        for (Integer i = 0; i < numbers.length; i++) {
            matcher = numbers[i].matcher(out);
            out = matcher.replaceAll("$1\\"+i.toString()+"$3");
        }
        stringWithoutMatch = out;
        return !out.equals(input);
    }

}
