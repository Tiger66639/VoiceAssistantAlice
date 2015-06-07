package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.VoiceAssistantApp;
import com.eleks.voiceassistant.voiceassistantpoc.adapter.MessagesArrayAdapter;
import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButton;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonFragment;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonStates;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WeatherCommandParser;
import com.eleks.voiceassistant.voiceassistantpoc.model.ApplicationState;
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

import java.text.DateFormat;
import java.util.ArrayList;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private static final long RECOGNIZER_DELAY = 5000;
    private static SpeechKit sSpeechKit;
    private final Recognizer.Listener mNuanceListener;
    Vocalizer.Listener vocalizerListener = new Vocalizer.Listener() {

        @Override
        public void onSpeakingBegin(Vocalizer vocalizer, String s, Object o) {

        }

        @Override
        public void onSpeakingDone(Vocalizer vocalizer, String s, SpeechError speechError, Object o) {

        }
    };
    private Recognizer mCurrentRecognizer;
    private ProgressDialog mProgressDialog;
    private LocationController mLocationController;
    private Vocalizer mVocalizer;
    private RecognizerState mRecognizerState;
    private ApplicationState mApplicationState = ApplicationState.WAIT_USER_ACTION;
    private View mMainView;
    private ListView mListView;
    private ArrayList<MessageHolder> mMessages;
    private View mListContainer;
    private MessagesArrayAdapter mMessageAdapter;

    public MainActivity() {
        super();
        mNuanceListener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
    }

    private void addMessage(String message) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        mMessages.add(new MessageHolder(message));
    }

    private void refreshMessageList() {
        if (mMessageAdapter != null) {
            if (mMessages != null) {
                mMessageAdapter.setMessages(mMessages.toArray(new MessageHolder[mMessages.size()]));
            } else {
                mMessageAdapter.setMessages(null);
            }
        }
    }

    private void speechText(String text) {
        Object lastTtsContext = new Object();
        mVocalizer.speakString(text, lastTtsContext);
    }

    private void showProgressDialog(final CharSequence message) {
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(message);
        } else {
            final Context context = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(context, getString(R.string.app_name), message);
                }
            });
        }
    }

    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        });
    }

    private Recognizer.Listener createListener() {
        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {
                mRecognizerState = RecognizerState.RECORDING;
                showProgressDialog("Recording...");
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                mRecognizerState = RecognizerState.PROCESSING;
                showProgressDialog("Processing...");
            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != mCurrentRecognizer) {
                    return;
                }
                mRecognizerState = RecognizerState.STOPPED;
                dismissProgressDialog();
                mCurrentRecognizer = null;

                // Display the error + suggestion in the edit box
                String detail = error.getErrorDetail();
                String suggestion = error.getSuggestion();

                if (suggestion == null) suggestion = "";
                changeRecognizerState();
                addMessage(detail + "\n" + suggestion);
                refreshMessageList();
                // for debugging purpose: printing out the speechkit session id
                android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onError: session id ["
                        + getSpeechKit().getSessionId() + "]");
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                dismissProgressDialog();
                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;
                int count = results.getResultCount();
                String resultStr = "";
                for (int i = 0; i < count; i++) {
                    resultStr += "[" + results.getResult(i).getScore() + "] " +
                            results.getResult(i).getText() + "\n";
                }
                changeRecognizerState();
                addMessage(resultStr);
                refreshMessageList();
                new RecognizeTextToCommandTask().execute(results);
                // for debugging purpose: printing out the speechkit session id
                android.util.Log.d("Nuance SampleVoiceApp", "Recognizer.Listener.onResults: session id ["
                        + getSpeechKit().getSessionId() + "]");
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
        addFloatingActionButtonFragment();
        mMainView = MainActivity.this.findViewById(R.id.main_container);
        //Nuance
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
        mListView = (ListView) findViewById(R.id.messages_list);
        mListContainer = findViewById(R.id.messages_container);
        mMessageAdapter = new MessagesArrayAdapter(MainActivity.this, null);
        mListView.setAdapter(mMessageAdapter);
    }

    private void addFloatingActionButtonFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        FloatingActionButtonFragment fragment = new FloatingActionButtonFragment();
        fragment.setOnFabClickListener(new FloatingActionButton.OnFabClickListener() {
            @Override
            public void onFabClick(FloatingActionButton fabView) {
                processFabClick(fabView);
            }
        });
        transaction.replace(R.id.fab_fragment, fragment, FloatingActionButtonFragment.TAG);
        transaction.commit();
    }

    private void processFabClick(FloatingActionButton fabView) {
        changeRecognizerState();
        changeFabState(mApplicationState, fabView);
        changeBackgroundColor(mApplicationState);
    }

    private void changeRecognizerState() {
        switch (mApplicationState) {
            case WAIT_USER_ACTION:
                mApplicationState = ApplicationState.RECOGNIZE_VOICE;
                startRecognizer();
                mMessageAdapter.setMessages(null);
                break;
            case RECOGNIZE_VOICE:

                mListContainer.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startRecognizer() {
        Handler _handler = new Handler();
        showProgressDialog("Initializing...");
        mRecognizerState = RecognizerState.INITIALIZING;
        mCurrentRecognizer = getSpeechKit().createRecognizer(
                Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short,
                "en_US", mNuanceListener, _handler);
        mCurrentRecognizer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                verifyRecognizerState();
            }
        }, RECOGNIZER_DELAY);
    }

    private void changeFabState(ApplicationState applicationState, FloatingActionButton fabView) {
        FloatingActionButtonStates fabState = FloatingActionButtonStates.MICROPHONE_ACTIVATED;
        switch (applicationState) {
            case WAIT_USER_ACTION:
                fabState = FloatingActionButtonStates.MICROPHONE_ACTIVATED;
                break;
            case RECOGNIZE_VOICE:
                fabState = FloatingActionButtonStates.MICROPHONE_DEACTIVATED;
        }
        fabView.setFabState(fabState);
    }

    private void changeBackgroundColor(ApplicationState applicationState) {
        Integer colorFrom = getBackgroundColor();
        Integer colorTo = getResources().getColor(R.color.background_active_color);
        switch (applicationState) {
            case WAIT_USER_ACTION:
                colorTo = getResources().getColor(R.color.background_passive_color);
                break;
            case RECOGNIZE_VOICE:
                colorTo = getResources().getColor(R.color.background_active_color);
                break;
        }
        if (colorFrom != colorTo) {
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
            mCurrentRecognizer.stopRecording();
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

    private void processGetWeatherForecast(WeatherCommandParser command) {
        if (command.getWhereLatLng() != null) {
            new GetWeatherForecastTask().execute(command);
        }
    }

    private class RecognizeTextToCommandTask extends AsyncTask<Recognition, Void, WeatherCommandParser> {

        @Override
        protected WeatherCommandParser doInBackground(Recognition... params) {
            Recognition recognition = params[0];
            WeatherCommandParser result = null;
            if (recognition.getResultCount() > 0) {
                result =
                        new WeatherCommandParser(MainActivity.this, recognition.getResult(0).getText());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Try recognize voice command...");
        }

        @Override
        protected void onPostExecute(WeatherCommandParser command) {
            dismissProgressDialog();
            if (command != null && command.isCommand()) {
                DateFormat dateFormat = DateFormat.getDateInstance();
                String text = "";
                if (!TextUtils.isEmpty(command.getWhereName())) {
                    text += command.getWhereName();
                } else {
                    text += "Can not recognize place.";
                }
                if (command.getWhenDates() != null) {
                    text += "\n" +
                            dateFormat.format(command.getWhenDates().startDate) + "\n" +
                            dateFormat.format(command.getWhenDates().finishDate);
                } else {
                    text += "\nCan not recognize dates";
                }
                //mCommandResult.setText(text);
                processGetWeatherForecast(command);
            } else {
                speechText(MainActivity.this.getString(R.string.cannot_recognize_voice_command));
                Toast.makeText(MainActivity.this,
                        MainActivity.this.getString(R.string.cannot_recognize_voice_command),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetWeatherForecastTask extends AsyncTask<WeatherCommandParser, Void, ResponseModel> {

        @Override
        protected ResponseModel doInBackground(WeatherCommandParser... params) {
            WeatherCommandParser command = params[0];
            return WebServerMethods
                    .getServerData(MainActivity.this, command.getWhereLatLng());
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Get weather forecast...");
        }

        @Override
        protected void onPostExecute(ResponseModel responseModel) {
            dismissProgressDialog();
            //speechText("Weather forecast from server was gotten successfully");
        }
    }
}
