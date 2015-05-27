package com.eleks.voiceassistant.voiceassistantpoc.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.ListeningDialog;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.NuanceAppInfo;
import com.nuance.nmdp.speechkit.Prompt;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements SpellCheckerSessionListener {

    private static final int LISTENING_DIALOG = 0;
    private static final int SPEECH_REQUEST_CODE = 777;
    private static SpeechKit sSpeechKit;
    private final Recognizer.Listener _listener;
    private EditText mSpeechResult;
    private ListeningDialog mListeningDialog;
    private Recognizer mCurrentRecognizer;
    private Handler _handler = null;
    private boolean _destroyed;
    private EditText mCorrectedSpeechResult;
    private SpellCheckerSession mScs;

    public MainActivity() {
        super();
        _listener = createListener();
    }

    static SpeechKit getSpeechKit() {
        return sSpeechKit;
    }

    private void startSpellCheck(String text) {
        if (mScs != null) {
            if (isSentenceSpellCheckSupported()) {
                mScs.getSentenceSuggestions(new TextInfo[]{new TextInfo(text)}, 1);

            }else{
                mScs.getSuggestions(new TextInfo(text), 1);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LISTENING_DIALOG:
                return mListeningDialog;
        }
        return null;
    }

    private void createListeningDialog() {
        mListeningDialog = new ListeningDialog(MainActivity.this);
        mListeningDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mCurrentRecognizer != null) // Cancel the current recognizer
                {
                    mCurrentRecognizer.cancel();
                    mCurrentRecognizer = null;
                }

                if (!_destroyed) {
                    // Remove the dialog so that it will be recreated next time.
                    // This is necessary to avoid a bug in Android >= 1.6 where the
                    // animation stops working.
                    MainActivity.this.removeDialog(LISTENING_DIALOG);
                    createListeningDialog();
                }
            }
        });
    }

    private Recognizer.Listener createListener() {
        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {
                mListeningDialog.setText("Recording...");
                mListeningDialog.setStoppable(true);
                mListeningDialog.setRecording(true);

                // Create a repeating task to update the audio level
                Runnable r = new Runnable() {
                    public void run() {
                        if (mListeningDialog != null && mListeningDialog.isRecording() &&
                                mCurrentRecognizer != null) {
                            mListeningDialog
                                    .setLevel(Float.toString(mCurrentRecognizer.getAudioLevel()));
                            _handler.postDelayed(this, 500);
                        }
                    }
                };
                r.run();
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                mListeningDialog.setText("Processing...");
                mListeningDialog.setLevel("");
                mListeningDialog.setRecording(false);
                mListeningDialog.setStoppable(false);
            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != mCurrentRecognizer) {
                    return;
                }
                if (mListeningDialog.isShowing()) {
                    //dismissDialog(LISTENING_DIALOG);
                }
                mCurrentRecognizer = null;
                mListeningDialog.setRecording(false);

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
                if (mListeningDialog.isShowing()) dismissDialog(LISTENING_DIALOG);
                mCurrentRecognizer = null;
                mListeningDialog.setRecording(false);
                int count = results.getResultCount();
                Recognition.Result[] rs = new Recognition.Result[count];
                String resultStr = "";
                for (int i = 0; i < count; i++) {
                    resultStr += "[" + results.getResult(i).getScore() + "] " +
                            results.getResult(i).getText() + "\n";
                    startSpellCheck(results.getResult(i).getText());
                }
                mSpeechResult.setText(resultStr);
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
        _destroyed = false;
        mSpeechResult = (EditText) findViewById(R.id.speechResult);
        Button googleButton = (Button) findViewById(R.id.googleButton);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeechResult.setText("");
                mCorrectedSpeechResult.setText("");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, SPEECH_REQUEST_CODE);

            }
        });
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
        createListeningDialog();
        Button nuanceButton = (Button) findViewById(R.id.nuanceButton);
        nuanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListeningDialog.setText("Initializing...");
                showDialog(LISTENING_DIALOG);
                mListeningDialog.setStoppable(false);
                mSpeechResult.setText("");
                mCorrectedSpeechResult.setText("");
                mCurrentRecognizer = getSpeechKit().createRecognizer(
                        Recognizer.RecognizerType.Search, Recognizer.EndOfSpeechDetection.Short,
                        "en_US", _listener, _handler);
                mCurrentRecognizer.start();
            }
        });
        mCorrectedSpeechResult = (EditText) findViewById(R.id.correctedSpeechResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sSpeechKit != null) {
            sSpeechKit.release();
            sSpeechKit = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final TextServicesManager tsm = (TextServicesManager) getSystemService(
                Context.TEXT_SERVICES_MANAGER_SERVICE);
        mScs = tsm.newSpellCheckerSession(null, Locale.getDefault(), MainActivity.this, false);
    }

    private boolean isSentenceSpellCheckSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @Override
    public void onGetSuggestions(final SuggestionsInfo[] arg0) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg0.length; ++i) {
            // Returned suggestions are contained in SuggestionsInfo
            final int len = arg0[i].getSuggestionsCount();
            for (int j = 0; j < len; ++j) {
                sb.append(arg0[i].getSuggestionAt(j));
            }
            sb.append("\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = mCorrectedSpeechResult.getText().toString();
                text += sb.toString();
                mCorrectedSpeechResult.setText(text);
            }
        });
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] arg0) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg0.length; ++i) {
            final int len = arg0[i].getSuggestionsCount();
            for (int j = 0; j < len; ++j) {
                //sb.append(arg0[i].getSuggestionAt(j));
            }
            sb.append("\n");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (results != null) {
                String spokenText = "";
                for (String result : results) {
                    spokenText += result + "\n";
                    startSpellCheck(result);
                }
                mSpeechResult.setText(spokenText);
            }
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
