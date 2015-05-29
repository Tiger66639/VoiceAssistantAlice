package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class WeatherCommand extends BaseCommand implements CommandInterface {

    private static final String[] NOISE_WORDS = {"in", "of", "from", "the", "a"};
    private static final String[] DATE_WORDS = {"today", "tomorrow", "yesterday", "weekend",
            "week", "month"};
    private final String mText;
    private final Context mContext;
    private String[] mWords;
    private String mWhere;
    private String mWhereName;
    private LatLng mWhereLatLng;

    public WeatherCommand(Context context, String text) {
        this.mContext = context;
        this.mText = text.toLowerCase();
        processText();
    }

    private void processText() {
        mWords = mText.split(" ");
        ArrayList<WordMeaning> firstTextMeanings = getFirstTextMeanings();
        mWhere = findWhere(firstTextMeanings);
        prepareWhereData();
    }

    private void prepareWhereData() {
        LatLng result = null;
        if (!TextUtils.isEmpty(mWhere)) {
            String[] words = mWhere.split(" ");
            if (words.length > 1) {
                result = getLatLngByName(mWhere, words.length);
            } else {
                result = getLatLngByName(mWhere);
            }
            if (result == null) {
                result = new LatLng(0, 0);
            }
        } else {
            //TODO need to return current position
        }
        mWhereLatLng = result;
    }

    private LatLng getLatLngByName(String name, int windowSize) {
        LatLng result = null;
        if (windowSize > 0) {
            String[] words = name.split(" ");
            int startIndex = 0;
            while (startIndex + windowSize <= words.length) {
                result = getLatLngByName(prepareName(words, startIndex, windowSize));
                if (result != null) {
                    break;
                }
                startIndex++;
            }
            if (result == null) {
                windowSize--;
                getLatLngByName(name, windowSize);
            }
        }
        return result;
    }

    private String prepareName(String[] words, int startIndex, int windowSize) {
        String result = "";
        for (int i = startIndex; i < startIndex + windowSize; i++) {
            result += words[i] + " ";
        }
        return result.trim();
    }

    private LatLng getLatLngByName(String name) {
        LatLng result = null;
        if (!TextUtils.isEmpty(name)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder.getFromLocationName(name, 1);
                if (addresses != null && addresses.size() > 0) {
                    result = new LatLng(addresses.get(0).getLatitude(),
                            addresses.get(0).getLongitude());
                    mWhereName = getWhereNameFromAddress(addresses.get(0));
                }
            } catch (IOException e) {
                //do nothing
            }
        }
        return result;
    }

    private String getWhereNameFromAddress(Address address) {
        String result = address.getFeatureName();
        if (!TextUtils.isEmpty(address.getAdminArea())) {
            result += ", " + address.getAdminArea();
        }
        if (!TextUtils.isEmpty(address.getCountryName())) {
            result += ", " + address.getCountryName();
        }
        return result;
    }

    private String findWhere(ArrayList<WordMeaning> textMeanings) {
        boolean commandFlag = false;
        boolean dateFlag = false;
        String betweenCommandAndDate = "";
        String afterDate = "";
        for (int i = 0; i < mWords.length; i++) {
            if (textMeanings.get(i).equals(WordMeaning.COMMAND)) {
                commandFlag = true;
                continue;
            }
            if (commandFlag) {
                if (textMeanings.get(i).equals(WordMeaning.DATE)) {
                    dateFlag = true;
                    continue;
                }
                if (!dateFlag) {
                    betweenCommandAndDate += mWords[i] + " ";
                    continue;
                } else {
                    afterDate += mWords[i] + " ";
                    continue;
                }
            }
        }
        betweenCommandAndDate = clearNoiseWordFromText(betweenCommandAndDate);
        afterDate = clearNoiseWordFromText(afterDate);
        if (betweenCommandAndDate.length() > afterDate.length()) {
            return betweenCommandAndDate;
        } else {
            return afterDate;
        }
    }

    private String clearNoiseWordFromText(String text) {
        text = text.trim();
        if (!TextUtils.isEmpty(text)) {
            String[] words = text.split(" ");
            for (int i = 0; i < words.length; i++) {
                for (String noiseWord : NOISE_WORDS) {
                    if (words[i].equals(noiseWord)) {
                        words[i] = "";
                    }
                }
            }
            text = "";
            for (String word : words) {
                if (!TextUtils.isEmpty(word)) {
                    text += word + " ";
                }
            }
        }
        return text.trim();
    }

    private ArrayList<WordMeaning> getFirstTextMeanings() {
        ArrayList<WordMeaning> result = new ArrayList<>();
        for (String word : mWords) {
            if (isCommandWord(word)) {
                result.add(WordMeaning.COMMAND);
            } else if (isMonthWord(word)) {
                result.add(WordMeaning.MONTH);
            } else if (isDayWord(word)) {
                result.add(WordMeaning.DATE);
            } else if (isNumberWord(word)) {
                result.add(WordMeaning.NUMBER);
            } else {
                result.add(WordMeaning.UNKNOWN);
            }
        }
        return result;
    }

    private boolean isNumberWord(String word) {
        return TextUtils.isDigitsOnly(word);
    }

    private boolean isDayWord(String word) {
        String[] days = new DateFormatSymbols(Locale.ENGLISH).getWeekdays();
        return isWordExistsInArray(word, days) || isWordExistsInArray(word, DATE_WORDS);
    }

    private boolean isMonthWord(String word) {
        String[] months = new DateFormatSymbols(Locale.ENGLISH).getMonths();
        return isWordExistsInArray(word, months);
    }

    private boolean isWordExistsInArray(String word, String[] array) {
        if (!TextUtils.isEmpty(word)) {
            for (String arrayWord : array) {
                if (!TextUtils.isEmpty(arrayWord)) {
                    if (getLeveinsteinDistance(arrayWord.toLowerCase(), word) <= 2 &&
                            getJakkardCoefficient(arrayWord.toLowerCase(), word) > 0.055) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCommandWord(String word) {
        if (getLeveinsteinDistance("weather", word) <= 2) {
            return true;
        }
        return false;
    }


    @Override
    public boolean getIsCommand() {
        return false;
    }

    public String getWhereName() {
        return mWhereName;
    }

    public LatLng getWhere() {
        return mWhereLatLng;
    }
}
