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
        weatherIcon.setText(
                getWeatherIcon(mWeatherModel.forecast.simpleForecast.forecastDays[0].iconName));
    }

    private String getWeatherIcon(String iconName) {
        iconName = iconName.toLowerCase();
        int resourceId = 0;
        switch (iconName) {
            case "chanceflurries":
                resourceId = R.string.chanceflurries;
                break;
            case "chancerain":
                resourceId = R.string.chancerain;
                break;
            case "chancesleet":
                resourceId = R.string.chancesleet;
                break;
            case "chancesnow":
                resourceId = R.string.chancesnow;
                break;
            case "chancetstorms":
                resourceId = R.string.chancetstorms;
                break;
            case "clear":
                resourceId = R.string.clear;
                break;
            case "cloudy":
                resourceId = R.string.cloudy;
                break;
            case "flurries":
                resourceId = R.string.flurries;
                break;
            case "fog":
                resourceId = R.string.fog;
                break;
            case "hazy":
                resourceId = R.string.hazy;
                break;
            case "mostlycloudy":
                resourceId = R.string.mostlycloudy;
                break;
            case "mostlysunny":
                resourceId = R.string.mostlysunny;
                break;
            case "partlycloudy":
                resourceId = R.string.partlycloudy;
                break;
            case "partlysunny":
                resourceId = R.string.partlysunny;
                break;
            case "sleet":
                resourceId = R.string.sleet;
                break;
            case "rain":
                resourceId = R.string.rain;
                break;
            case "snow":
                resourceId = R.string.snow;
                break;
            case "sunny":
                resourceId = R.string.sunny;
                break;
            case "tstorms":
                resourceId = R.string.tstorms;
                break;
        }
        return getString(resourceId);
    }

    private void getExtras() {
        mWeatherModel = (ResponseModel) getArguments().getSerializable(WEATHER_MODEL);
    }
}
