package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.VoiceAssistantApp;
import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButton;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonFragment;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonStates;
import com.eleks.voiceassistant.voiceassistantpoc.fragment.MainFragment;
import com.eleks.voiceassistant.voiceassistantpoc.fragment.WeatherFragment;
import com.eleks.voiceassistant.voiceassistantpoc.mining.CommandPeriod;
import com.eleks.voiceassistant.voiceassistantpoc.model.MainViewState;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;
import com.eleks.voiceassistant.voiceassistantpoc.model.ResponseModel;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.NuanceAppInfo;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.RecognizerState;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserBase;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserFactory;
import com.eleks.voiceassistant.voiceassistantpoc.server.WebServerMethods;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskBase;
import com.eleks.voiceassistant.voiceassistantpoc.utils.FontsHolder;
import com.eleks.voiceassistant.voiceassistantpoc.utils.NetworkStateHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private static final long RECOGNIZER_DELAY = 5000;
    private static final long DELAY_BETWEEN_SCREENS = 3000;
    private static final int THREE_DAYS = 3;
    private static SpeechKit sSpeechKit;
    private final Recognizer.Listener mNuanceListener;
    private final ParserBase messageParser;
    Vocalizer.Listener vocalizerListener = new Vocalizer.Listener() {

        @Override
        public void onSpeakingBegin(Vocalizer vocalizer, String s, Object o) {

        }

        @Override
        public void onSpeakingDone(
                Vocalizer vocalizer, String s, SpeechError speechError, Object o) {

        }
    };
    private Recognizer mCurrentRecognizer;
    private LocationController mLocationController;
    private Vocalizer mVocalizer;
    private RecognizerState mRecognizerState;
    private MainViewState mApplicationState = MainViewState.WELCOME_SCREEN;
    private FloatingActionButtonFragment mFabFragment;
    private View mWelcomeContainer;
    private ResponseModel mWeatherModel;
    private Handler mHandler;
    private Runnable mWatchDogRunnable;
    private AsyncTask<TaskBase, Void, ResponseModel> mGetWeatherForecastTask;
    private FontsHolder mFontsHolder;
    private MainFragment mMainFragment;
    private View mMainView;
    private AsyncTask<Recognition, Void, TaskBase> mRecognizeTextToCommandTask;
    private String speechLocale = "en_US";

    public MainActivity() {
        super();
        // @todo Only support language
        speechLocale = Resources.getSystem().getConfiguration().locale.toString();
        messageParser = ParserFactory.create(speechLocale);
        mNuanceListener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
    }

    private void speechText(String text) {
        Object lastTtsContext = new Object();
        try {
            mVocalizer.speakString(text, lastTtsContext);
        }catch (IllegalStateException e){
            //do nothing
        }
    }

    private Recognizer.Listener createListener() {
        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {
                mRecognizerState = RecognizerState.RECORDING;
                //showProgressDialog(MainActivity.this.getText(R.string.recording_message));
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                mRecognizerState = RecognizerState.PROCESSING;
                //showProgressDialog(MainActivity.this.getText(R.string.processing_message));
            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != mCurrentRecognizer) {
                    return;
                }
                stopWatchDogTask();
                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;
                String suggestion = error.getSuggestion();
                String nuanceSpeechNotRecognized =
                        MainActivity.this.getString(R.string.nuance_speech_not_recognized);
                if (suggestion == null ||
                        suggestion.toLowerCase().equals(nuanceSpeechNotRecognized.toLowerCase())) {
                    suggestion = MainActivity.this.getString(R.string.speech_not_recognized);
                }
                setApplicationState(MainViewState.SHOW_RESULT);
                speechText(suggestion);
                mMainFragment.replaceLastMessage(suggestion, true);
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                makeBeep();
                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;
                stopWatchDogTask();
                String resultStr = "";
                if (results.getResultCount() > 0) {
                    resultStr += results.getResult(0).getText();
                }
                mMainFragment.replaceLastMessage(resultStr, false);
                mRecognizeTextToCommandTask = new RecognizeTextToCommandTask().execute(results);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (checkGooglePlayServices()) {
            registerLocationController();
        } else {
            processGooglePlayServiceIsNotExists();
        }
        setVersionInfoControl();
        prepareSpeechKitAndVocalizer();
        mFontsHolder = new FontsHolder(MainActivity.this);
        prepareActivityControls();
        prepareMainFragment();
    }

    private void setVersionInfoControl() {
        TextView versionInfo = (TextView) findViewById(R.id.version_info);
        versionInfo.setText(getVersionNumber());
    }

    private String getVersionNumber() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.default_application_version);
        }
    }

    private void prepareActivityControls() {
        addFloatingActionButtonFragment();
        TextView welcomeText1 = (TextView) findViewById(R.id.welcome_text);
        welcomeText1.setTypeface(mFontsHolder.getRobotomonoLight());
        mWelcomeContainer = findViewById(R.id.welcome_container);
        mMainView = findViewById(R.id.main_container);
    }

    private void prepareSpeechKitAndVocalizer() {
        if (sSpeechKit == null) {
            sSpeechKit = SpeechKit.initialize(getApplication().getApplicationContext(),
                    NuanceAppInfo.SpeechKitAppId, NuanceAppInfo.SpeechKitServer,
                    NuanceAppInfo.SpeechKitPort, NuanceAppInfo.SpeechKitSsl,
                    NuanceAppInfo.SpeechKitApplicationKey);
            sSpeechKit.connect();
            Prompt beep = sSpeechKit.defineAudioPrompt(R.raw.beep);
            sSpeechKit.setDefaultRecognizerPrompts(beep, null, null, null);
        }
        mVocalizer = sSpeechKit
                .createVocalizerWithLanguage("en_US", vocalizerListener, new Handler()); // @todo speechLocale
        mVocalizer.setVoice("Samantha");
    }

    private void makeBeep() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);
        mediaPlayer.start();
    }

    private void addFloatingActionButtonFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mFabFragment = new FloatingActionButtonFragment();
        mFabFragment.setOnFabClickListener(new FloatingActionButton.OnFabClickListener() {
            @Override
            public void onFabClick(FloatingActionButton fabView) {
                processFabClick();
            }
        });
        transaction.replace(R.id.fab_fragment, mFabFragment, FloatingActionButtonFragment.TAG);
        transaction.commitAllowingStateLoss();
    }

    private void processFabClick() {
        switch (mApplicationState) {
            case WELCOME_SCREEN:
                mMainFragment.clearMessages();
                if (startRecognizer()) {
                    mMainFragment.addMessage(getString(R.string.listening_message), false);
                    setApplicationState(MainViewState.VOICE_RECORDING);
                }
                break;
            case VOICE_RECORDING:
                stopRecognizer();
                break;
            case SHOW_RESULT:
                if (startRecognizer()) {
                    mMainFragment.addMessage(getString(R.string.listening_message), false);
                    setApplicationState(MainViewState.VOICE_RECORDING);
                }
                break;
            case SHOW_WEATHER:
                getFragmentManager().popBackStackImmediate();
                if (startRecognizer()) {
                    mMainFragment.clearMessages();
                    mMainFragment.addMessage(getString(R.string.listening_message), false);
                    setApplicationState(MainViewState.VOICE_RECORDING);
                }
                break;
            case GET_WEATHER_FORECAST:
                if (mGetWeatherForecastTask != null) {
                    makeBeep();
                    mGetWeatherForecastTask.cancel(true);
                }
                setApplicationState(MainViewState.SHOW_RESULT);
                break;
            case RECOGNIZE_COMMAND:
                if (mRecognizeTextToCommandTask != null) {
                    makeBeep();
                    mRecognizeTextToCommandTask.cancel(true);
                }
                setApplicationState(MainViewState.SHOW_RESULT);
                break;
        }
    }

    private void changeMainViewAppearance() {
        switch (mApplicationState) {
            case WELCOME_SCREEN:
                mWelcomeContainer.setVisibility(View.VISIBLE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                changeStatusBarColor(R.color.status_bar_color);
                break;
            case VOICE_RECORDING:
                mWelcomeContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_WHITE);
                mMainFragment.setInvertedColors(true);
                changeBackgroundColor(R.color.background_red);
                changeStatusBarColor(R.color.status_bar_color_red);
                break;
            case SHOW_RESULT:
                mWelcomeContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                changeStatusBarColor(R.color.status_bar_color);
                mMainFragment.setInvertedColors(false);
                break;
            case RECOGNIZE_COMMAND:
                mWelcomeContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_RED);
                changeBackgroundColor(R.color.background_white);
                changeStatusBarColor(R.color.status_bar_color);
                mMainFragment.setInvertedColors(false);
                break;
            case GET_WEATHER_FORECAST:
                mWelcomeContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_RED);
                changeBackgroundColor(R.color.background_white);
                changeStatusBarColor(R.color.status_bar_color);
                mMainFragment.setInvertedColors(false);
                break;
            case SHOW_WEATHER:
                mWelcomeContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                changeStatusBarColor(R.color.status_bar_color);
                mMainFragment.setInvertedColors(false);
                prepareWeatherFragment();
                break;
        }
    }

    private void prepareWeatherFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        WeatherFragment mWeatherFragment = WeatherFragment.getInstance(mWeatherModel);
        transaction.addToBackStack(WeatherFragment.TAG);
        transaction.replace(R.id.fragment_container, mWeatherFragment, WeatherFragment.TAG);
        transaction.commitAllowingStateLoss();
    }

    private void prepareMainFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (mMainFragment == null) {
            mMainFragment = MainFragment.getInstance();
        }
        transaction.add(R.id.fragment_container, mMainFragment, MainFragment.TAG);
        transaction.commitAllowingStateLoss();
    }

    private void stopRecognizer() {
        if (mCurrentRecognizer != null) {
            mCurrentRecognizer.stopRecording();
            makeBeep();
        }
        stopWatchDogTask();
    }

    private void stopWatchDogTask() {
        if (mHandler != null && mWatchDogRunnable != null) {
            mHandler.removeCallbacks(mWatchDogRunnable);
        }
    }

    private boolean startRecognizer() {
        if (new NetworkStateHelper(MainActivity.this).isConnectionAvailable()) {
            Handler _handler = new Handler();
            //showProgressDialog("Initializing...");
            mRecognizerState = RecognizerState.INITIALIZING;
            mCurrentRecognizer = getSpeechKit().createRecognizer(
                    Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short,
                    speechLocale, mNuanceListener, _handler);
            mCurrentRecognizer.start();
            mHandler = new Handler();
            mWatchDogRunnable = new Runnable() {
                @Override
                public void run() {
                    verifyRecognizerState();
                }
            };
            mHandler.postDelayed(mWatchDogRunnable, RECOGNIZER_DELAY);
            return true;
        } else {
            mMainFragment.addMessage(getString(R.string.connection_error_message), true);
            setApplicationState(MainViewState.SHOW_RESULT);
            return false;
        }
    }

    private void setApplicationState(MainViewState state) {
        mApplicationState = state;
        changeMainViewAppearance();
    }

    private void changeBackgroundColor(int resourceColor) {
        Integer colorTo = getResources().getColor(resourceColor);
        Integer colorFrom = getBackgroundColor();
        if (!colorFrom.equals(colorTo)) {
            ValueAnimator colorAnimation =
                    ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(1000);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mMainView.setBackgroundColor((Integer) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        }
    }

    private void changeStatusBarColor(int resourceColor) {
        Integer colorTo = getResources().getColor(resourceColor);
        Integer colorFrom = getWindow().getStatusBarColor();
        if (!colorFrom.equals(colorTo)) {
            ValueAnimator colorAnimation =
                    ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(1000);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
                }

            });
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color_red));
            colorAnimation.start();
        }
    }

    private Integer getBackgroundColor() {
        int color = Color.TRANSPARENT;
        Drawable background = mMainView.getBackground();
        if (background instanceof ColorDrawable) {
            color = ((ColorDrawable) background).getColor();
        }
        return color;
    }

    private void verifyRecognizerState() {
        if (mRecognizerState == RecognizerState.INITIALIZING ||
                mRecognizerState == RecognizerState.RECORDING) {
            stopRecognizer();
        }
    }

    private void processGooglePlayServiceIsNotExists() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.gps_is_not_exists_message))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.gps_is_not_exists_ok_button_name),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .create().show();
    }

    private void registerLocationController() {
        LocationController.verifyGPSAvailability(this);
        mLocationController = LocationController.getInstance(MainActivity.this);
        if (!mLocationController.isStarted()) {
            mLocationController.startListenLocation();
        }
    }

    public boolean checkGooglePlayServices() {
        if (!((VoiceAssistantApp) getApplication()).isGpsVerified()) {
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(getApplicationContext());
            if (resultCode == ConnectionResult.SUCCESS) {
                ((VoiceAssistantApp) getApplication()).setGpsVerified();
                return true;
            } else if (resultCode == ConnectionResult.SERVICE_MISSING ||
                    resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                    resultCode == ConnectionResult.SERVICE_DISABLED) {
                GooglePlayServicesUtil
                        .getErrorDialog(
                                resultCode, MainActivity.this, REQUEST_CODE_RECOVER_PLAY_SERVICES)
                        .show();
                return false;
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sSpeechKit != null) {
            sSpeechKit.release();
            sSpeechKit = null;
        }
        if (mLocationController.isStarted()) {
            mLocationController.stopListenLocation();
            mLocationController.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        MainActivity.this.finish();
    }

    private void processVoiceCommand(final TaskBase command) {
        if(command.execute(this)){
            String message = getString(R.string.button_ok);
            mMainFragment.addMessage(message, true);
            speechText(message);
            setApplicationState(MainViewState.SHOW_RESULT);
        }else{
            String message = getString(R.string.cannot_recognize_place);
            mMainFragment.addMessage(message, true);
            speechText(message);
            setApplicationState(MainViewState.SHOW_RESULT);
        }

        /*
        if (command.getWhereLatLng() != null) {
            if (command.getWhenDates() != null) {
                MessageHolder lastMessage = mMainFragment.getLastMessage();
                mMainFragment.clearMessages();
                mMainFragment.addMessage(lastMessage.message, command.getWords(), false);
                if (!isDatesInThreeDaysPeriod(command.getWhenDates())) {
                    String message = getString(R.string.wrong_period_message);
                    mMainFragment.addMessage(message, true);
                    speechText(message);
                    setApplicationState(MainViewState.SHOW_RESULT);
                } else {
                    mGetWeatherForecastTask = new GetWeatherForecastTask().execute(command);
                }
            } else {
                String message = getString(R.string.cannot_recognize_period);
                mMainFragment.addMessage(message, true);
                speechText(message);
                setApplicationState(MainViewState.SHOW_RESULT);
            }
        } else {
            String message = getString(R.string.cannot_recognize_place);
            mMainFragment.addMessage(message, true);
            speechText(message);
            setApplicationState(MainViewState.SHOW_RESULT);
        }*/
    }

    private boolean isDatesInThreeDaysPeriod(CommandPeriod dates) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.add(Calendar.DAY_OF_YEAR, THREE_DAYS);
        Date endDate = calendar.getTime();
        return dates.startDate.after(startDate) && dates.startDate.before(endDate) &&
                dates.finishDate.after(startDate) && dates.finishDate.before(endDate);
    }

    private void processWeatherResult(ResponseModel weatherInfo) {
        if (weatherInfo != null) {
            mWeatherModel = weatherInfo;
            if (!TextUtils.isEmpty(weatherInfo.location.city)) {
                String message = getString(R.string.show_weather_message);
                message = String.format(message, weatherInfo.location.city);
                speechText(message);
            }
            setApplicationState(MainViewState.SHOW_WEATHER);
        } else {
            String message = getString(R.string.cannot_get_weather_from_server);
            mMainFragment.addMessage(message, true);
            speechText(message);
            setApplicationState(MainViewState.SHOW_RESULT);
        }
    }

    private class RecognizeTextToCommandTask
            extends AsyncTask<Recognition, Void, TaskBase> {

        @Override
        protected TaskBase doInBackground(Recognition... params) {
            Recognition recognition = params[0];
            TaskBase result = null;
            if (recognition.getResultCount() > 0) {
                result = messageParser.Parse(recognition.getResult(0).getText());

                /*result = new TaskBase(
                        MainActivity.this, recognition.getResult(0).getText());*/
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            setApplicationState(MainViewState.RECOGNIZE_COMMAND);
        }

        @Override
        protected void onPostExecute(final TaskBase command) {
            if (!isCancelled()) {
                if (command != null ) {
                    processVoiceCommand(command);
                } else {
                    String message = MainActivity.this
                            .getString(R.string.cannot_recognize_voice_command);
                    mMainFragment.addMessage(
                            message, true);
                    speechText(message);
                    setApplicationState(MainViewState.SHOW_RESULT);
                }
            }
        }
    }

    /*private class GetWeatherForecastTask
            extends AsyncTask<TaskBase, Void, ResponseModel> {

        private Date mCurrentTime;

        @Override
        protected ResponseModel doInBackground(TaskBase... params) {
            TaskBase command = params[0];
            return WebServerMethods
                    .getServerData(MainActivity.this, command.getWhereLatLng());
        }

        @Override
        protected void onPreExecute() {
            mCurrentTime = new Date();
            setApplicationState(MainViewState.GET_WEATHER_FORECAST);
        }

        @Override
        protected void onPostExecute(final ResponseModel responseModel) {
            if (!isCancelled()) {
                long delay = DELAY_BETWEEN_SCREENS -
                        (new Date().getTime() - mCurrentTime.getTime());
                if (responseModel != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled()) {
                                processWeatherResult(responseModel);
                            }
                        }
                    }, delay);
                } else {
                    processWeatherResult(null);
                }
            }
        }

    }*/
}
