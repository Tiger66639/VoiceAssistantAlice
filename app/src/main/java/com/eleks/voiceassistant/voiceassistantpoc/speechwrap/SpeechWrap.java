package com.eleks.voiceassistant.voiceassistantpoc.speechwrap;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.model.MainViewState;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.NuanceAppInfo;
import com.eleks.voiceassistant.voiceassistantpoc.nuance.RecognizerState;
import com.eleks.voiceassistant.voiceassistantpoc.utils.NetworkStateHelper;
import com.nuance.nmdp.speechkit.*;

/**
 * Created by dnap on 12.08.15. VoiceAssistantPoC
 */
public class SpeechWrap {
    private final Recognizer.Listener mNuanceListener;
    private SpeechKit sSpeechKit;
    private Vocalizer mVocalizer;
    private RecognizerState mRecognizerState;
    private Recognizer mCurrentRecognizer;
    private String speechLocale;
    private Handler mHandler;
    private Runnable mWatchDogRunnable;

    private static final long RECOGNIZER_DELAY = 5000;



    Vocalizer.Listener vocalizerListener = new Vocalizer.Listener() {

        @Override
        public void onSpeakingBegin(Vocalizer vocalizer, String s, Object o) {

        }

        @Override
        public void onSpeakingDone(Vocalizer vocalizer, String s, SpeechError speechError, Object o) {

        }
    };
    private Application app;

    public SpeechWrap(Application application, String speechLocale, Recognizer.Listener mNuanceListener) {
        this.app = application;
        this.mNuanceListener = createListener(mNuanceListener);
        this.speechLocale = speechLocale;
        if (sSpeechKit == null) {
            sSpeechKit = SpeechKit.initialize(application.getApplicationContext(),
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


    public boolean startRecognizer() {

        Handler _handler = new Handler();
        //showProgressDialog("Initializing...");
        mRecognizerState = RecognizerState.INITIALIZING;
        mCurrentRecognizer = sSpeechKit.createRecognizer(
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
    }

    public void stopRecognizer() {
        if (mCurrentRecognizer != null) {
            mCurrentRecognizer.stopRecording();
            makeBeep();
        }
        stopWatchDogTask();
    }

    public void makeBeep() {
        MediaPlayer mediaPlayer = MediaPlayer.create(app, R.raw.beep);
        mediaPlayer.start();
    }

    private void verifyRecognizerState() {
        if (mRecognizerState == RecognizerState.INITIALIZING ||
                mRecognizerState == RecognizerState.RECORDING) {
            stopRecognizer();
        }
    }

    private void stopWatchDogTask() {
        if (mHandler != null && mWatchDogRunnable != null) {
            mHandler.removeCallbacks(mWatchDogRunnable);
        }
    }

    public void speechText(String text) {
        Object lastTtsContext = new Object();
        try {
            mVocalizer.speakString(text, lastTtsContext);
        }catch (IllegalStateException e){
            Log.d("speech", e.getMessage(), e.fillInStackTrace());
            //do nothing
        }
    }

    public void onDestroy() {
        if (sSpeechKit != null) {
            sSpeechKit.release();
            sSpeechKit = null;
        }
    }

    public Recognizer.Listener createListener(final Recognizer.Listener listener) {

        return new Recognizer.Listener() {
            @Override
            public void onRecordingBegin(Recognizer recognizer) {
                mRecognizerState = RecognizerState.RECORDING;
                listener.onRecordingBegin(recognizer);
            }

            @Override
            public void onRecordingDone(Recognizer recognizer) {
                mRecognizerState = RecognizerState.PROCESSING;
                listener.onRecordingDone(recognizer);
            }

            @Override
            public void onError(Recognizer recognizer, SpeechError error) {
                if (recognizer != mCurrentRecognizer) {
                    return;
                }
                stopWatchDogTask();
                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;

                listener.onError(recognizer, error);
            }

            @Override
            public void onResults(Recognizer recognizer, Recognition results) {
                stopRecognizer();

                mRecognizerState = RecognizerState.STOPPED;
                mCurrentRecognizer = null;

                listener.onResults(recognizer, results);
            }
        };
    }


}
