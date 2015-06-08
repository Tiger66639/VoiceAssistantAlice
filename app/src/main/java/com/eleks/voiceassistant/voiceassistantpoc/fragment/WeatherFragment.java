package com.eleks.voiceassistant.voiceassistantpoc.fragment;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.model.ResponseModel;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class WeatherFragment extends Fragment {

    public static final String TAG = WeatherFragment.class.getName();
    private static final String WEATHER_MODEL = "weather_model";
    private static final String WEATHER_FONT_NAME = "fonts/weathericons.ttf";
    private View mFragmentView;
    private ResponseModel mWeatherModel;
    private Typeface mTypeface;

    public static WeatherFragment getInstance(ResponseModel weatherModel) {
        WeatherFragment weatherFragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(WEATHER_MODEL, weatherModel);
        weatherFragment.setArguments(bundle);
        return weatherFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTypeface = Typeface.createFromAsset(getActivity().getAssets(), WEATHER_FONT_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mFragmentView == null) {
            mFragmentView = inflater.inflate(R.layout.fragment_weather, container, false);
        }
        getExtras();
        fillControls();
        return mFragmentView;
    }

    private void fillControls() {
        TextView cityName = (TextView) mFragmentView.findViewById(R.id.city_name);
        cityName.setTypeface(mTypeface);
        TextView weatherIcon = (TextView) mFragmentView.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(mTypeface);
    }

    private void getExtras() {
        mWeatherModel = (ResponseModel) getArguments().getSerializable(WEATHER_MODEL);
    }
}
