package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.VoiceAssistantApp;
import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WeatherCommandParser;
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


public class MainActivity extends ActionBarActivity {

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
    private EditText mSpeechResult;
    private Recognizer mCurrentRecognizer;
    private Handler _handler = null;
    private ProgressDialog mProgressDialog;
    private EditText mCommandResult;
    private LocationController mLocationController;
    private Vocalizer mVocalizer;
    private RecognizerState mRecognizerState;

    public MainActivity() {
        super();
        mNuanceListener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
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
                mSpeechResult.setText(detail + "\n" + suggestion);
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
                mSpeechResult.setText(resultStr);
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
        mSpeechResult = (EditText) findViewById(R.id.speechResult);
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
        _handler = new Handler();
        Button nuanceButton = (Button) findViewById(R.id.nuanceButton);
        nuanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog("Initializing...");
                mRecognizerState = RecognizerState.INITIALIZING;
                mSpeechResult.setText("");
                mCommandResult.setText("");
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
        });
        mCommandResult = (EditText) findViewById(R.id.commandResult);
        mVocalizer = sSpeechKit
                .createVocalizerWithLanguage("en_US", vocalizerListener, new Handler());
        mVocalizer.setVoice("Samantha");
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
                text += "\n" +
                        dateFormat.format(command.getWhenDates().startDate) + "\n" +
                        dateFormat.format(command.getWhenDates().finishDate);
                mCommandResult.setText(text);
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
