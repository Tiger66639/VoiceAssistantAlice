package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.VoiceAssistantApp;
import com.eleks.voiceassistant.voiceassistantpoc.adapter.MessagesArrayAdapter;
import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButton;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonFragment;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonStates;
import com.eleks.voiceassistant.voiceassistantpoc.fragment.WeatherFragment;
import com.eleks.voiceassistant.voiceassistantpoc.mining.CommandPeriod;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WeatherCommandParser;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;
import com.eleks.voiceassistant.voiceassistantpoc.model.MainViewState;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;
import com.eleks.voiceassistant.voiceassistantpoc.model.ResponseModel;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.NuanceAppInfo;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.RecognizerState;
import com.eleks.voiceassistant.voiceassistantpoc.server.WebServerMethods;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private static final long RECOGNIZER_DELAY = 5000;
    private static final long DELAY_BETWEEN_SCREENS = 3000;
    private static final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final int THREE_DAYS = 3;
    private static SpeechKit sSpeechKit;
    private final Recognizer.Listener mNuanceListener;
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
    private View mMainView;
    private ListView mListView;
    private ArrayList<MessageHolder> mMessages;
    private View mListContainer;
    private MessagesArrayAdapter mMessageAdapter;
    private FloatingActionButtonFragment mFabFragment;
    private View mWelcomeContainer;
    private View mWeatherContainer;
    private WeatherFragment mWeatherFragment;
    private WeatherCommandParser mWeatherCommand;
    private ResponseModel mWeatherModel;
    private Handler mHandler;
    private Runnable mWatchDogRunnable;
    private AsyncTask<WeatherCommandParser, Void, ResponseModel> mGetWeatherForecastTask;

    public MainActivity() {
        super();
        mNuanceListener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
    }

    private void addMessage(String message, boolean isCursive) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        mMessages.add(new MessageHolder(message, isCursive));
    }

    private void addMessage(String message, WordHolder[] words, boolean isCursive) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        mMessages.add(new MessageHolder(message, words, isCursive));
    }

    private void refreshMessageList() {
        if (mMessageAdapter != null) {
            if (mMessages != null) {
                mMessageAdapter.setMessages(mMessages.toArray(new MessageHolder[mMessages.size()]));
                mListView.setSelection(mMessages.size() - 1);
            } else {
                mMessageAdapter.setMessages(null);
            }
        }
    }

    private void speechText(String text) {
        Object lastTtsContext = new Object();
        mVocalizer.speakString(text, lastTtsContext);
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
                if (suggestion == null) {
                    suggestion = MainActivity.this.getString(R.string.speech_not_recognized);
                }
                mApplicationState = MainViewState.SHOW_RESULT;
                changeMainViewAppearance();
                addMessage(suggestion, true);
                refreshMessageList();
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;
                stopWatchDogTask();
                String resultStr = "";
                if (results.getResultCount() > 0) {
                    resultStr += results.getResult(0).getText();
                }
                addMessage(resultStr, false);
                refreshMessageList();
                new RecognizeTextToCommandTask().execute(results);
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
        prepareSpeechKitAndVocalizer();
        prepareActivityControls();
    }

    private void prepareActivityControls() {
        addFloatingActionButtonFragment();
        mMainView = MainActivity.this.findViewById(R.id.main_container);
        mListView = (ListView) findViewById(R.id.messages_list);
        mListContainer = findViewById(R.id.messages_container);
        mMessageAdapter = new MessagesArrayAdapter(MainActivity.this, null);
        mListView.setAdapter(mMessageAdapter);
        mWelcomeContainer = findViewById(R.id.welcome_container);
        mWeatherContainer = findViewById(R.id.weather_container);
    }

    private void prepareSpeechKitAndVocalizer() {
        if (sSpeechKit == null) {
            sSpeechKit = SpeechKit.initialize(getApplication().getApplicationContext(),
                    NuanceAppInfo.SpeechKitAppId, NuanceAppInfo.SpeechKitServer,
                    NuanceAppInfo.SpeechKitPort, NuanceAppInfo.SpeechKitSsl,
                    NuanceAppInfo.SpeechKitApplicationKey);
            sSpeechKit.connect();
            Prompt beep = sSpeechKit.defineAudioPrompt(R.raw.beep);
            sSpeechKit.setDefaultRecognizerPrompts(beep, Prompt.vibration(100), null, null);
        }
        mVocalizer = sSpeechKit
                .createVocalizerWithLanguage("en_US", vocalizerListener, new Handler());
        mVocalizer.setVoice("Samantha");
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeMainViewAppearance();
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
        transaction.commit();
    }

    private void processFabClick() {
        switch (mApplicationState) {
            case WELCOME_SCREEN:
                mMessages = null;
                refreshMessageList();
                mApplicationState = MainViewState.VOICE_RECORDING;
                startRecognizer();
                changeMainViewAppearance();
                addMessage(getString(R.string.listening_message), false);
                refreshMessageList();
                break;
            case VOICE_RECORDING:
                stopRecognizer();
                break;
            case SHOW_RESULT:
                mApplicationState = MainViewState.VOICE_RECORDING;
                startRecognizer();
                changeMainViewAppearance();
                addMessage(getString(R.string.listening_message), false);
                refreshMessageList();
                break;
            case SHOW_WEATHER:
                mMessages = null;
                refreshMessageList();
                mApplicationState = MainViewState.VOICE_RECORDING;
                startRecognizer();
                changeMainViewAppearance();
                addMessage(getString(R.string.listening_message), false);
                refreshMessageList();
                break;
            case GET_WEATHER_FORECAST:
                if (mGetWeatherForecastTask != null) {
                    mGetWeatherForecastTask.cancel(true);
                }
                mApplicationState = MainViewState.SHOW_RESULT;
                changeMainViewAppearance();
                break;
        }
    }

    private void changeMainViewAppearance() {
        switch (mApplicationState) {
            case WELCOME_SCREEN:
                mMessageAdapter.setMessages(null);
                mWelcomeContainer.setVisibility(View.VISIBLE);
                mListContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                break;
            case VOICE_RECORDING:
                mListContainer.setVisibility(View.VISIBLE);
                mWelcomeContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_WHITE);
                changeBackgroundColor(R.color.background_red);
                mMessageAdapter.setInvertedColors(true);
                break;
            case SHOW_RESULT:
                mListContainer.setVisibility(View.VISIBLE);
                mWelcomeContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                mMessageAdapter.setInvertedColors(false);
                break;
            case RECOGNIZE_COMMAND:
                mListContainer.setVisibility(View.VISIBLE);
                mWelcomeContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_RED);
                changeBackgroundColor(R.color.background_white);
                mMessageAdapter.setInvertedColors(false);
                break;
            case GET_WEATHER_FORECAST:
                mListContainer.setVisibility(View.VISIBLE);
                mWelcomeContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.GONE);
                mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_RED);
                changeBackgroundColor(R.color.background_white);
                mMessageAdapter.setInvertedColors(false);
                break;
            case SHOW_WEATHER:
                mListContainer.setVisibility(View.GONE);
                mWelcomeContainer.setVisibility(View.GONE);
                mWeatherContainer.setVisibility(View.VISIBLE);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                changeBackgroundColor(R.color.background_white);
                mMessageAdapter.setInvertedColors(false);
                prepareWeatherFragment();
                break;
        }
    }

    private void prepareWeatherFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mWeatherFragment = WeatherFragment.getInstance(mWeatherModel);
        transaction.replace(R.id.weather_container, mWeatherFragment, WeatherFragment.TAG);
        transaction.commit();
    }

    private void stopRecognizer() {
        if (mCurrentRecognizer != null) {
            mCurrentRecognizer.stopRecording();
        }
        stopWatchDogTask();
    }

    private void stopWatchDogTask() {
        if (mHandler != null && mWatchDogRunnable != null) {
            mHandler.removeCallbacks(mWatchDogRunnable);
        }
    }

    private void startRecognizer() {
        Handler _handler = new Handler();
        //showProgressDialog("Initializing...");
        mRecognizerState = RecognizerState.INITIALIZING;
        mCurrentRecognizer = getSpeechKit().createRecognizer(
                Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short,
                "en_US", mNuanceListener, _handler);
        mCurrentRecognizer.start();
        mHandler = new Handler();
        mWatchDogRunnable = new Runnable() {
            @Override
            public void run() {
                verifyRecognizerState();
            }
        };
        mHandler.postDelayed(mWatchDogRunnable, RECOGNIZER_DELAY);

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

    private void processVoiceCommand(final WeatherCommandParser command) {
        if (command.getWhereLatLng() != null) {
            if (command.getWhenDates() != null) {
                if (mMessages.size() > 0) {
                    int lastMessageIndex = mMessages.size() - 1;
                    MessageHolder lastMessage = mMessages.get(lastMessageIndex);
                    mMessages.remove(lastMessageIndex);
                    addMessage(lastMessage.message, command.getWords(), false);
                    refreshMessageList();
                }
                if (!isDatesInThreeDaysPeriod(command.getWhenDates())) {
                    addMessage(getString(R.string.wrong_period_message), true);
                    refreshMessageList();
                    mApplicationState = MainViewState.SHOW_RESULT;
                    changeMainViewAppearance();
                } else {
                    mGetWeatherForecastTask = new GetWeatherForecastTask().execute(command);
                }
            } else {
                addMessage(getString(R.string.cannot_recognize_period), true);
                refreshMessageList();
                mApplicationState = MainViewState.SHOW_RESULT;
                changeMainViewAppearance();
            }
        } else {
            addMessage(getString(R.string.cannot_recognize_place), true);
            refreshMessageList();
            mApplicationState = MainViewState.SHOW_RESULT;
            changeMainViewAppearance();
        }
    }

    private boolean isDatesInThreeDaysPeriod(CommandPeriod dates) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.add(Calendar.DAY_OF_YEAR, THREE_DAYS);
        Date endDate = calendar.getTime();
        if (dates.startDate.after(startDate) && dates.startDate.before(endDate) &&
                dates.finishDate.after(startDate) && dates.finishDate.before(endDate)) {
            return true;
        } else {
            return false;
        }
    }

    private void processWeatherResult(ResponseModel weatherInfo) {
        if (weatherInfo != null) {
            mApplicationState = MainViewState.SHOW_WEATHER;
            mWeatherModel = weatherInfo;
        } else {
            addMessage(getString(R.string.cannot_get_weather_from_server), true);
            refreshMessageList();
            mApplicationState = MainViewState.SHOW_RESULT;
        }
        changeMainViewAppearance();
    }

    private class RecognizeTextToCommandTask
            extends AsyncTask<Recognition, Void, WeatherCommandParser> {

        @Override
        protected WeatherCommandParser doInBackground(Recognition... params) {
            Recognition recognition = params[0];
            WeatherCommandParser result = null;
            if (recognition.getResultCount() > 0) {
                result = new WeatherCommandParser(
                        MainActivity.this, recognition.getResult(0).getText());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            mApplicationState = MainViewState.RECOGNIZE_COMMAND;
            changeMainViewAppearance();
        }

        @Override
        protected void onPostExecute(final WeatherCommandParser command) {
            //dismissProgressDialog();
            if (command != null && command.isCommand()) {
                mWeatherCommand = command;
                processVoiceCommand(command);
            } else {
                addMessage(
                        MainActivity.this.getString(R.string.cannot_recognize_voice_command), true);
                refreshMessageList();
                mApplicationState = MainViewState.SHOW_RESULT;
                changeMainViewAppearance();
            }
        }
    }

    private class GetWeatherForecastTask
            extends AsyncTask<WeatherCommandParser, Void, ResponseModel> {

        private Date mCurrentTime;
        private boolean mCanceled = false;

        @Override
        protected ResponseModel doInBackground(WeatherCommandParser... params) {
            WeatherCommandParser command = params[0];
            return WebServerMethods
                    .getServerData(MainActivity.this, command.getWhereLatLng());
        }

        @Override
        protected void onPreExecute() {
            mCurrentTime = new Date();
            mApplicationState = MainViewState.GET_WEATHER_FORECAST;
            changeMainViewAppearance();
        }

        @Override
        protected void onPostExecute(final ResponseModel responseModel) {
            if (!mCanceled) {
                long delay = DELAY_BETWEEN_SCREENS -
                        (new Date().getTime() - mCurrentTime.getTime());
                if (responseModel != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            processWeatherResult(responseModel);
                        }
                    }, delay);
                } else {
                    processWeatherResult(responseModel);
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mCanceled = true;
        }
    }
}
