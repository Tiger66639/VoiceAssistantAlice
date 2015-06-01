package com.eleks.voiceassistant.voiceassistantpoc.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


/**
 * Created by Sergey on 01.06.2015.
 */
public class LocationController implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LocationController.class.getName();
    private static final long POLLING_FREQ = 1000 * 30;
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;
    private static LocationController mLocationController;
    private final LocationRequest mLocationRequest;
    private final GoogleApiClient mGoogleApiClient;

    private Location mBestReading;
    private boolean started = false;

    private LocationController(Context context) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);

        mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public static LocationController getInstance(Context context) {
        if (mLocationController == null) {
            mLocationController = new LocationController(context.getApplicationContext());
        }
        return mLocationController;
    }

    public static void verifyGPSAvailability(final Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_gps_message);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean isStarted() {
        return started;
    }

    public LatLng getCurrentLocation() {
        if (mBestReading != null) {
            return new LatLng(mBestReading.getLatitude(), mBestReading.getLongitude());
        } else {
            return null;
        }
    }

    public void destroy() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void startListenLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            started = true;
        }

    }

    public void stopListenLocation() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            started = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mBestReading = location;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /* empty */
    }

}

