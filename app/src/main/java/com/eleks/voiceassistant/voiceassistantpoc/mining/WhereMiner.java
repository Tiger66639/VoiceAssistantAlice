package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class WhereMiner implements ITextMiner {

    private static final String POSSESSIVE_SUFFIX = "'s";
    private Context mContext;

    @Override
    public WordHolder[] investigate(Context context, WordHolder[] words) {
        this.mContext = context;
        words = removePossessives(words);
        ArrayList<String> placePretenders = getPlacePretenders(words);
        Collections.sort(placePretenders, new PlaceComparator());
        if (placePretenders.size() > 0) {
            boolean foundPlace = false;
            for (String place : placePretenders) {
                WordMeaning wordMeaning = WordMeaning.POSSIBLE_PLACE;
                if (isRealPlace(place) && !foundPlace) {
                    foundPlace = true;
                    wordMeaning = WordMeaning.PLACE;
                }
                words = setWordsMeaning(words, place, wordMeaning);
            }
        }
        return words;
    }

    private WordHolder[] removePossessives(WordHolder[] words) {
        for (WordHolder wordHolder : words) {
            if (wordHolder.word.endsWith(POSSESSIVE_SUFFIX)) {
                wordHolder.word = wordHolder.word
                        .substring(0, wordHolder.word.length() - POSSESSIVE_SUFFIX.length());
            }
        }
        return words;
    }

    private boolean isRealPlace(String place) {
        boolean result = false;
        Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocationName(place, 1);
            if (isAddressCorrect(place, addresses)) {
                result = true;
            }
        } catch (IOException e) {
            //do nothing
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

    private WordHolder[] setWordsMeaning(
            WordHolder[] words, String place, WordMeaning wordMeaning) {
        String[] placeWords = place.split(" ");
        int startIndex = -1;
        for (int i = 0; i < words.length - placeWords.length; i++) {
            boolean found = true;
            for (int j = 0; j < placeWords.length; j++) {
                found = words[i + j].wordMeaning == null && words[i + j].word.equals(placeWords[j]);
            }
            if (found) {
                startIndex = i;
                break;
            }
        }
        for (int i = startIndex; i < startIndex + placeWords.length; i++) {
            words[i].wordMeaning = wordMeaning;
        }
        return words;
    }

    private ArrayList<String> getPlacePretenders(WordHolder[] words) {
        ArrayList<String> result = new ArrayList<>();
        String pretender = null;
        boolean startFlag = false;
        for (WordHolder wordHolder : words) {
            if (wordHolder.wordMeaning == null) {
                if (!startFlag) {
                    pretender = wordHolder.word;
                    startFlag = true;
                } else {
                    pretender += " " + wordHolder.word;
                }
            } else {
                if (startFlag) {
                    result.add(pretender);
                    pretender = null;
                    startFlag = false;
                }
            }
        }
        return result;
    }

    private class PlaceComparator implements java.util.Comparator<String> {

        @Override
        public int compare(String place1, String place2) {
            if (place1.length() > place2.length()) {
                return -1;
            } else if (place1.length() == place2.length()) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
