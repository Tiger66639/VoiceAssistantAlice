package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class WeatherCommand extends BaseCommand implements CommandInterface {

    private static final String[] DATE_WORDS = {"today", "tomorrow", "yesterday", "weekend",
            "week", "month"};
    private static final String[] COMMAND_WORDS = {"weather", "temperature", "how hot", "how cold"};
    private static final double JAKKARD_MIN_COEFFICIENT = 0.055;
    private static final int LEVEINSTEIN_MAX_DISTANCE = 2;
    private static final String MONTH = "month";
    private static final String WEEK = "week";
    private final String mText;
    private final Context mContext;
    private String[] mWords;
    private String mWhereName;
    private LatLng mWhereLatLng;
    private CommandPeriod mCommandDate;
    private boolean mIsCommand;

    public WeatherCommand(Context context, String text) {
        this.mContext = context;
        this.mText = text.toLowerCase();
        processText();
    }

    public CommandPeriod getCommandDate() {
        return mCommandDate;
    }

    private void processText() {
        mWords = mText.split(" ");
        verifyIsItCommand();
        if (mIsCommand) {
            prepareWhereData();
            prepareCommandDate();
        }
    }

    private void verifyIsItCommand() {
        for (String word : mWords) {
            if (isCommandWord(word)) {
                mIsCommand = true;
                break;
            }
        }
    }

    private void prepareCommandDate() {
        String dateString = getDateString();
    }

    private String getDateString() {
        String result = null;
        for (String word : mWords) {
            if (isWordExistsInArray(word, DATE_WORDS)) {
                if (word.equals(MONTH) || word.equals(WEEK)) {

                } else {
                    result = word;
                    break;
                }
            }
        }
        return result;
    }

    private void prepareWhereData() {
        LatLng result = null;
        if (!TextUtils.isEmpty(mText)) {
            String[] words = mText.split(" ");
            result = getLatLngByName(mText, words.length);
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
                result = getLatLngByName(name, windowSize);
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
                    if (getLeveinsteinDistance(arrayWord.toLowerCase(), word) <= LEVEINSTEIN_MAX_DISTANCE &&
                            getJakkardCoefficient(arrayWord.toLowerCase(), word) > JAKKARD_MIN_COEFFICIENT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCommandWord(String word) {
        return isWordExistsInArray(word, COMMAND_WORDS);
    }


    @Override
    public boolean getIsCommand() {
        return mIsCommand;
    }

    public String getWhereName() {
        return mWhereName;
    }

    public LatLng getWhere() {
        return mWhereLatLng;
    }
}
