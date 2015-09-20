package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButton;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonFragment;
import com.eleks.voiceassistant.voiceassistantpoc.controls.FloatingActionButtonStates;
import com.eleks.voiceassistant.voiceassistantpoc.fragment.MainFragment;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.RecognizerState;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserBase;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserFactory;
import com.eleks.voiceassistant.voiceassistantpoc.speechwrap.SpeechWrap;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskBase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;

/**
 * Created by dnap on 15.08.15.
 */
public class SimpleActivity extends Activity {
    private final ParserBase messageParser;
    private SpeechWrap speechWrap;
    private FloatingActionButtonFragment mFabFragment;
    private MainFragment mMainFragment;
    private AsyncTask<Recognition, Void, TaskBase> mRecognizeTextToCommandTask;

    public SimpleActivity() {
        super();
        // @todo Only support language
        messageParser = ParserFactory.create(Resources.getSystem().getConfiguration().locale.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (!checkGooglePlayServices()) {
            processGooglePlayServiceIsNotExists();
            return;
        }

        TextView versionInfo = (TextView) findViewById(R.id.version_info);
        versionInfo.setText(getVersionNumber());

        speechWrap = new SpeechWrap(getApplication(), createListener(), messageParser.getSpeechLocale(), messageParser.getSpeechVoice());


        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        mFabFragment = new FloatingActionButtonFragment();
        mFabFragment.setOnFabClickListener(new FloatingActionButton.OnFabClickListener() {
            @Override
            public void onFabClick(FloatingActionButton fabView) {
                processFabClick();
            }
        });
        transaction.replace(R.id.fab_fragment, mFabFragment, FloatingActionButtonFragment.TAG);

        mMainFragment = MainFragment.getInstance();
        transaction.add(R.id.fragment_container, mMainFragment, MainFragment.TAG);
        transaction.commitAllowingStateLoss();
    }


    private void processFabClick() {
        if(speechWrap.getStatus() != RecognizerState.STOPPED) {
            speechWrap.stopRecognizer();
            mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
            return;
        }
        if (speechWrap.startRecognizer()) {
            mFabFragment.setFabState(FloatingActionButtonStates.CLOSE_RED);
            mMainFragment.addMessage(getString(R.string.listening_message), false);
        }
    }


    private Recognizer.Listener createListener() {
        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {}

            @Override
            public void onRecordingDone(Recognizer recognizer) {}

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                String suggestion = error.getSuggestion();
                String nuanceSpeechNotRecognized =
                        SimpleActivity.this.getString(R.string.nuance_speech_not_recognized);
                if (suggestion == null ||
                        suggestion.toLowerCase().equals(nuanceSpeechNotRecognized.toLowerCase())) {
                    suggestion = SimpleActivity.this.getString(R.string.speech_not_recognized);
                }
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
                speechWrap.speechText(suggestion);
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                String resultStr = "";
                if (results.getResultCount() > 0) {
                    resultStr += results.getResult(0).getText();
                }
                mMainFragment.replaceLastMessage(resultStr, false);
                mRecognizeTextToCommandTask = new RecognizeTextToCommandTask().execute(results);
                mFabFragment.setFabState(FloatingActionButtonStates.MICROPHONE_RED);
            }
        };
    }

    private class RecognizeTextToCommandTask
            extends AsyncTask<Recognition, Void, TaskBase> {

        @Override
        protected TaskBase doInBackground(Recognition... params) {
            Recognition recognition = params[0];
            TaskBase result = null;
            if (recognition.getResultCount() > 0) {
                result = messageParser.Parse(recognition.getResult(0).getText());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final TaskBase command) {
            if (!isCancelled()) {
                if (command != null ) {
                    processVoiceCommand(command);
                } else {
                    String message = SimpleActivity.this.getString(R.string.cannot_recognize_voice_command);
                    mMainFragment.addMessage(
                            message, true);
                    speechWrap.speechText(message);
                }
            }
        }
    }

    private void processVoiceCommand(final TaskBase command) {
        if (command.execute(this)) {
            String message = getString(R.string.button_ok);
            mMainFragment.addMessage(message, true);
            speechWrap.speechText(message);
        } else {
            String message = getString(R.string.cannot_recognize_place);
            mMainFragment.addMessage(message, true);
            speechWrap.speechText(message);
        }
    }


    public boolean checkGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        return resultCode == ConnectionResult.SUCCESS;
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


    private String getVersionNumber() {
        try {
            return getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.default_application_version);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(speechWrap != null)
            speechWrap.onDestroy();
    }
}
