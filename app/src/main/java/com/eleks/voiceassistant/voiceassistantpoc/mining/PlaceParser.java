package com.eleks.voiceassistant.voiceassistantpoc.mining;

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
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class PlaceParser {

    private final Context mContext;
    private WordHolder[] mWords;
    private LatLng mPlaceLatLng;
    private String mPlaceName;

    public PlaceParser(Context context, WordHolder[] words) {
        this.mContext = context;
        this.mWords = words;
        preparePlaceInfo();
    }

    public void preparePlaceInfo() {
        String place = getPlace(mWords);
        if (!TextUtils.isEmpty(place)) {
            getLatLngByLocationName(place);
        } else {
            if (!existsPossiblePlaces(mWords)) {
                mPlaceLatLng = LocationController.getInstance(mContext).getCurrentLocation();
                getNameByLocationLatLng(mPlaceLatLng);
            } else {
                mPlaceLatLng = null;
            }
        }
    }

    private boolean existsPossiblePlaces(WordHolder[] words) {
        for (WordHolder wordHolder : words) {
            if (wordHolder.wordMeaning == WordMeaning.POSSIBLE_PLACE) {
                return true;
            }
        }
        return false;
    }

    private String getPlace(WordHolder[] words) {
        String result = "";
        for (WordHolder wordHolder : words) {
            if (wordHolder.wordMeaning == WordMeaning.PLACE) {
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

    private void getNameByLocationLatLng(LatLng latLng) {
        if (latLng != null) {
            Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder
                        .getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    mPlaceName = getWhereNameFromAddress(addresses.get(0));
                }
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    private void getLatLngByLocationName(String placeName) {
        if (!TextUtils.isEmpty(placeName)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
                if (addresses != null && addresses.size() > 0) {
                    mPlaceLatLng = new LatLng(addresses.get(0).getLatitude(),
                            addresses.get(0).getLongitude());
                    mPlaceName = getWhereNameFromAddress(addresses.get(0));
                }
            } catch (IOException e) {
                //do nothing
            }
        }
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

    public LatLng getPlaceLatLng() {
        return mPlaceLatLng;
    }

    public String getPlaceName() {
        return mPlaceName;
    }
}
