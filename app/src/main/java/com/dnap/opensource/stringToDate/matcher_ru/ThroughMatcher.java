package com.dnap.opensource.stringToDate.matcher_ru;

import com.dnap.opensource.stringToDate.matcher.Matcher;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by dnap on 29.07.15. strtotime-for-java
 */
public class ThroughMatcher extends Matcher {
    private final Pattern pattern = Pattern.compile("([^а-я]|^)через\\s+(.+)");
    private final HashMap<Integer, Pattern> subPaterns;

    public ThroughMatcher() {
        subPaterns = new HashMap<Integer, Pattern>();

        subPaterns.put(Calendar.YEAR, Pattern.compile("((\\d+)\\s+|)(год|года|лет|годы|годах)([^а-я]|$)"));
        subPaterns.put(Calendar.MONTH, Pattern.compile("((\\d+)\\s+|)(месяц|месяца|месяцы|месяцев|месяцах)([^а-я]|$)"));
        subPaterns.put(Calendar.WEEK_OF_YEAR, Pattern.compile("((\\d+)\\s+|)(неделя|недели|неделю|неделе|недель|неделях)([^а-я]|$)"));
        subPaterns.put(Calendar.DATE, Pattern.compile("((\\d+)\\s+|)(день|дня|дней)([^а-я]|$)"));
        subPaterns.put(Calendar.HOUR, Pattern.compile("((\\d+)\\s+|)(час|час|часа|часов|часах)([^а-я]|$)"));
        subPaterns.put(Calendar.MINUTE, Pattern.compile("((\\d+)\\s+|)(минут|минута|минуты|минуту|минуте|минутах)([^а-я]|$)"));

    }

    @Override
    public Boolean tryConvert(String input, Calendar calendar) {

        java.util.regex.Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            Boolean success = false;
            String through = matcher.group(2).trim();
            if(through.isEmpty())
                return false;

            java.util.regex.Matcher matcherSub;
            for(Map.Entry<Integer, Pattern> entry : subPaterns.entrySet()) {

                matcherSub = entry.getValue().matcher(through);
                if(matcherSub.find()) {
                    int interval = 1;
                    if(matcherSub.group(1).length() > 0){
                        interval = Integer.parseInt(matcherSub.group(2));
                    }
                    //noinspection MagicConstant,ResourceType
                    calendar.add(entry.getKey(), interval);
                    through = matcherSub.replaceFirst("$4");
                    success = true;
                }
            }
            if(!success)
                return false;
            through = through.trim();

            if(through.isEmpty()) {
                stringWithoutMatch = matcher.replaceFirst("$1");
            }
            int l = 1;
            String out = through;
            for (int i = through.length()-1; i >= 0; i--) {
                if(input.charAt(input.length() - l) != through.charAt(i)) {
                    out = input.substring(input.length() - l);
                }
                l++;
            }

            stringWithoutMatch = matcher.replaceFirst("$1"+Pattern.quote(out));
            return true;
        }

        return false;
    }
}
