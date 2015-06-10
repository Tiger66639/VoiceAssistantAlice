package com.eleks.voiceassistant.voiceassistantpoc.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.model.DisplayLocation;
import com.eleks.voiceassistant.voiceassistantpoc.model.ResponseModel;
import com.eleks.voiceassistant.voiceassistantpoc.utils.FontsHolder;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Serhiy.Krasovskyy on 08.06.2015.
 */
public class WeatherFragment extends Fragment {

    public static final String TAG = WeatherFragment.class.getName();
    private static final String WEATHER_MODEL = "weather_model";

    private View mFragmentView;
    private ResponseModel mWeatherModel;
    private FontsHolder mFontsHolder;


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
        mFontsHolder = new FontsHolder(getActivity());
        setRetainInstance(true);
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
        cityName.setTypeface(mFontsHolder.getRobotomonoRegular());
        cityName.setText(getCityName(mWeatherModel.currentObservation.displayLocation));
        TextView weatherIcon = (TextView) mFragmentView.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(mFontsHolder.getWeatherIconFont());
        weatherIcon.setText(
                getWeatherIcon(mWeatherModel.currentObservation.iconName));
        TextView iconName = (TextView) mFragmentView.findViewById(R.id.weather_text);
        iconName.setTypeface(mFontsHolder.getRobotomonoRegular());
        iconName.setText(mWeatherModel.currentObservation.weather);
        TextView temperature = (TextView) mFragmentView.findViewById(R.id.temperature);
        temperature.setTypeface(mFontsHolder.getRobotomonoBold());
        temperature.setText(
                String.format("%.0f", mWeatherModel.currentObservation.temperatureFahrenheit));
        TextView fahrenheitDegree = (TextView) mFragmentView.findViewById(R.id.fahrenheit_degree);
        fahrenheitDegree.setTypeface(mFontsHolder.getRobotomonoBold());
        //day 1
        TextView firstDayName = (TextView) mFragmentView.findViewById(R.id.first_day_name);
        firstDayName.setTypeface(mFontsHolder.getRobotomonoRegular());
        firstDayName.setText(getShortDayName(1));
        TextView firstDayWeatherIcon = (TextView) mFragmentView.findViewById(R.id.first_day_weather_icon);
        firstDayWeatherIcon.setTypeface(mFontsHolder.getWeatherIconFont());
        firstDayWeatherIcon.setText(
                getWeatherIcon(mWeatherModel.forecast.simpleForecast.forecastDays[1].iconName));
        TextView firstDayHigh = (TextView) mFragmentView.findViewById(R.id.first_day_high_icon);
        firstDayHigh.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView firstDayHighTemperature = (TextView) mFragmentView.findViewById(R.id.first_day_high_temperature);
        firstDayHighTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        firstDayHighTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[1].highTemperature.getFahrenheit()));
        TextView firstDayLow = (TextView) mFragmentView.findViewById(R.id.first_day_low_icon);
        firstDayLow.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView firstDayLowTemperature = (TextView) mFragmentView.findViewById(R.id.first_day_low_temperature);
        firstDayLowTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        firstDayLowTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[1].lowTemperature.getFahrenheit()));
        TextView firstDayHighDegree = (TextView) mFragmentView.findViewById(R.id.first_day_high_degree);
        firstDayHighDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
        TextView firstDayLowDegree = (TextView) mFragmentView.findViewById(R.id.first_day_low_degree);
        firstDayLowDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
        //day 2
        TextView secondDayName = (TextView) mFragmentView.findViewById(R.id.second_day_name);
        secondDayName.setTypeface(mFontsHolder.getRobotomonoRegular());
        secondDayName.setText(getShortDayName(2));
        TextView secondDayWeatherIcon = (TextView) mFragmentView.findViewById(R.id.second_day_weather_icon);
        secondDayWeatherIcon.setTypeface(mFontsHolder.getWeatherIconFont());
        secondDayWeatherIcon.setText(
                getWeatherIcon(mWeatherModel.forecast.simpleForecast.forecastDays[2].iconName));
        TextView secondDayHigh = (TextView) mFragmentView.findViewById(R.id.second_day_high_icon);
        secondDayHigh.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView secondDayHighTemperature = (TextView) mFragmentView.findViewById(R.id.second_day_high_temperature);
        secondDayHighTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        secondDayHighTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[2].highTemperature.getFahrenheit()));
        TextView secondDayLow = (TextView) mFragmentView.findViewById(R.id.second_day_low_icon);
        secondDayLow.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView secondDayLowTemperature = (TextView) mFragmentView.findViewById(R.id.second_day_low_temperature);
        secondDayLowTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        secondDayLowTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[2].lowTemperature.getFahrenheit()));
        TextView secondDayHighDegree = (TextView) mFragmentView.findViewById(R.id.second_day_high_degree);
        secondDayHighDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
        TextView secondDayLowDegree = (TextView) mFragmentView.findViewById(R.id.second_day_low_degree);
        secondDayLowDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
        //day 3
        TextView thirdDayName = (TextView) mFragmentView.findViewById(R.id.third_day_name);
        thirdDayName.setTypeface(mFontsHolder.getRobotomonoRegular());
        thirdDayName.setText(getShortDayName(3));
        TextView thirdDayWeatherIcon = (TextView) mFragmentView.findViewById(R.id.third_day_weather_icon);
        thirdDayWeatherIcon.setTypeface(mFontsHolder.getWeatherIconFont());
        thirdDayWeatherIcon.setText(
                getWeatherIcon(mWeatherModel.forecast.simpleForecast.forecastDays[3].iconName));
        TextView thirdDayHigh = (TextView) mFragmentView.findViewById(R.id.third_day_high_icon);
        thirdDayHigh.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView thirdDayHighTemperature = (TextView) mFragmentView.findViewById(R.id.third_day_high_temperature);
        thirdDayHighTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        thirdDayHighTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[3].highTemperature.getFahrenheit()));
        TextView thirdDayLow = (TextView) mFragmentView.findViewById(R.id.third_day_low_icon);
        thirdDayLow.setTypeface(mFontsHolder.getWeatherIconFont());
        TextView thirdDayLowTemperature = (TextView) mFragmentView.findViewById(R.id.third_day_low_temperature);
        thirdDayLowTemperature.setTypeface(mFontsHolder.getRobotomonoRegular());
        thirdDayLowTemperature.setText(
                String.format("%d", mWeatherModel.forecast.simpleForecast.forecastDays[3].lowTemperature.getFahrenheit()));
        TextView thirdDayHighDegree = (TextView) mFragmentView.findViewById(R.id.third_day_high_degree);
        thirdDayHighDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
        TextView thirdDayLowDegree = (TextView) mFragmentView.findViewById(R.id.third_day_low_degree);
        thirdDayLowDegree.setTypeface(mFontsHolder.getRobotomonoRegular());
    }

    private String getShortDayName(int shiftOfDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, shiftOfDays);
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH);
    }

    private String getCityName(DisplayLocation location) {
        String result = "";
        if (location != null && !TextUtils.isEmpty(location.fullName)) {
            result += location.fullName;
        }
        return result;
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
