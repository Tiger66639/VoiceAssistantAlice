package com.eleks.voiceassistant.voiceassistantpoc.command;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class WeatherCommand extends BaseCommand implements CommandInterface {

    private static final String[] COMMAND_WORDS = {"weather", "temperature", "how hot", "how cold"};
    private static final String[] NOISE_WORDS = {"in", "of", "the", "a", "please", "for",
            "this", "show"};
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
            prepareCommandDate();
            mWords = CommandsUtils.removeNoiseWords(mWords, NOISE_WORDS);
            prepareWhereData();
        }
    }

    private void verifyIsItCommand() {
        for (String commandWord : COMMAND_WORDS) {
            String[] commandWords = commandWord.split(" ");
            if (mWords.length >= commandWords.length) {
                for (int i = 0; i < mWords.length - commandWords.length + 1; i++) {
                    boolean result = true;
                    for (int j = 0; j < commandWords.length; j++) {
                        result = result && CommandsUtils
                                .fuzzyEquals(mWords[i + j], commandWords[j]);
                    }
                    if (result) {
                        mWords = CommandsUtils
                                .clearWordsInArray(mWords, i, commandWords.length);
                        mIsCommand = true;
                        break;
                    }
                }
            }
            if (mIsCommand) {
                break;
            }
        }
    }

    private void prepareCommandDate() {
        DateParser dateParser = new DateParser(mWords);
        mCommandDate = dateParser.getDates();
        mWords = dateParser.getRemainingWords();
    }

    private void prepareWhereData() {
        LatLng result;
        if (mWords.length > 0) {
            result = getLatLngByName(mWords, mWords.length);
            if (result == null) {
                result = new LatLng(0, 0);
            }
        } else {
            mWhereName = "Current position";
            result = LocationController.getInstance(mContext).getCurrentLocation();
            if (result != null) {
                Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
                try {
                    List<Address> addresses =
                            geocoder.getFromLocation(result.latitude, result.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        mWhereName = getWhereNameFromAddress(addresses.get(0));
                    }
                } catch (IOException e) {
                    //do nothing
                }
            }
        }
        mWhereLatLng = result;
    }

    private LatLng getLatLngByName(String[] words, int windowSize) {
        LatLng result = null;
        if (windowSize > 0) {
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
                result = getLatLngByName(words, windowSize);
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

    private LatLng getLatLngByName(String placeName) {
        LatLng result = null;
        if (!TextUtils.isEmpty(placeName)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
                if (isAddressCorrect(placeName, addresses)) {
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

    private boolean isAddressCorrect(String placeName, List<Address> addresses) {
        boolean result = false;
        if (addresses != null && addresses.size() > 0) {
            if (addresses.get(0).getAddressLine(0).toLowerCase().contains(placeName)) {
                result = true;
            }
        }
        return result;
    }

    private String getWhereNameFromAddress(Address address) {
        String result = address.getAddressLine(1);
        if (!TextUtils.isEmpty(address.getAdminArea())) {
            result += ", " + address.getAdminArea();
        }
        if (!TextUtils.isEmpty(address.getCountryName())) {
            result += ", " + address.getCountryName();
        }
        return result;
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
