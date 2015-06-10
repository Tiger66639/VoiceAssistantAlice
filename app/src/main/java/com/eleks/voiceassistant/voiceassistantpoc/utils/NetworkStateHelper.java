package com.eleks.voiceassistant.voiceassistantpoc.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Serhiy.Krasovskyy on 10.04.2015.
 */
public class NetworkStateHelper {
    private static final String TAG = NetworkStateHelper.class.getName();
    private final Context mContext;
    private ConnectivityManager mConnectivityManager;

    public NetworkStateHelper(Context context) {
        this.mContext = context;
        mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isConnectionAvailable() {
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isWiFiAvailable() {
        NetworkInfo networkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
