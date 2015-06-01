package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.VoiceAssistantApp;
import com.eleks.voiceassistant.voiceassistantpoc.command.WeatherCommand;
import com.eleks.voiceassistant.voiceassistantpoc.controller.LocationController;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.NuanceAppInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

import java.text.DateFormat;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private static SpeechKit sSpeechKit;
    private final Recognizer.Listener mNuanceListener;
    private EditText mSpeechResult;
    private Recognizer mCurrentRecognizer;
    private Handler _handler = null;
    private ProgressDialog mProgressDialog;
    private EditText mCommandResult;

    public MainActivity() {
        super();
        mNuanceListener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
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
                showProgressDialog("Recording...");
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                showProgressDialog("Processing...");
            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != mCurrentRecognizer) {
                    return;
                }
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
        }else{
            //TODO need to implement
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
                mSpeechResult.setText("");
                mCommandResult.setText("");
                mCurrentRecognizer = getSpeechKit().createRecognizer(
                        Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short,
                        "en_US", mNuanceListener, _handler);
                mCurrentRecognizer.start();
            }
        });
        mCommandResult = (EditText) findViewById(R.id.commandResult);
    }

    private void registerLocationController() {
        LocationController.verifyGPSAvailability(this);
        if (!LocationController.getInstance(this).isStarted()) {
            LocationController.getInstance(this).startListenLocation();
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
        if (LocationController.getInstance(MainActivity.this).isStarted()) {
            LocationController.getInstance(MainActivity.this).destroy();
        }
    }

    private class RecognizeTextToCommandTask extends AsyncTask<Recognition, Void, WeatherCommand> {

        @Override
        protected WeatherCommand doInBackground(Recognition... params) {
            Recognition recognition = params[0];
            WeatherCommand result = null;
            if (recognition.getResultCount() > 0) {
                result =
                        new WeatherCommand(MainActivity.this, recognition.getResult(0).getText());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Try recognize voice command...");
        }

        @Override
        protected void onPostExecute(WeatherCommand command) {
            dismissProgressDialog();
            if (command != null && command.getIsCommand()) {
                DateFormat dateFormat = DateFormat.getDateInstance();
                String text = command.getWhereName() + "\n" +
                        dateFormat.format(command.getCommandDate().startDate) + "\n" +
                        dateFormat.format(command.getCommandDate().finishDate);
                mCommandResult.setText(text);
            } else {
                Toast.makeText(MainActivity.this, "Can not recognize voice command", Toast.LENGTH_LONG).show();
            }
        }
    }
}
