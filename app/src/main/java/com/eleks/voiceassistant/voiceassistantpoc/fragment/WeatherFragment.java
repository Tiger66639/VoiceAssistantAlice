package com.eleks.voiceassistant.voiceassistantpoc.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eleks.voiceassistant.voiceassistantpoc.R;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class WeatherFragment extends Fragment {

    public static final String TAG = WeatherFragment.class.getName();
    private View mFragmentView;

    public static WeatherFragment getInstance() {
        return new WeatherFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mFragmentView == null) {
            mFragmentView = inflater.inflate(R.layout.fragment_weather, container, false);
        }
        return mFragmentView;
    }
}
