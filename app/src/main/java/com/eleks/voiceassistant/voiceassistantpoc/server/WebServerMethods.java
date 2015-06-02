package com.eleks.voiceassistant.voiceassistantpoc.server;

import android.content.Context;
import android.text.TextUtils;

import com.eleks.voiceassistant.voiceassistantpoc.model.ResponseModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Serhiy.Krasovskyy on 02.06.2015.
 */
public class WebServerMethods {

    private static final int DEFAULT_TIMEOUT_SEC = 30;
    private static final String API_KEY = "d70daf057a579b33";
    private static final String SERVER_METHOD = "http://api.wunderground.com/api/%s/forecast/geolookup/conditions/q/%f,%f.json";
    private static OkHttpClient sOkHttpClient;

    public static ResponseModel getServerData(Context context, LatLng position) {
        String serverMethodUrl = String.format(Locale.ENGLISH, SERVER_METHOD,
                API_KEY, position.latitude, position.longitude);
        Request request = new Request.Builder()
                .url(serverMethodUrl)
                .build();
        ResponseModel result = null;
        try {
            Response response = getOkHttpClient(context).newCall(request).execute();
            if (response.isSuccessful()) {
                String responseJson = response.body().string();
                if (!TextUtils.isEmpty(responseJson)) {
                    Gson gson = new Gson();
                    result = gson.fromJson(responseJson, ResponseModel.class);
                }
            }
        } catch (IOException e) {
            //do nothing
        }
        return result;
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient();
            sOkHttpClient.setConnectTimeout(
                    DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);
            sOkHttpClient.setReadTimeout(
                    DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);
        }
        return sOkHttpClient;
    }
}
