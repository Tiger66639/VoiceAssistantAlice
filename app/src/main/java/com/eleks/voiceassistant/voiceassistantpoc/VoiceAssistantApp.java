package com.eleks.voiceassistant.voiceassistantpoc;

import android.app.Application;

/**
 * Created by Sergey on 01.06.2015.
 */
public class VoiceAssistantApp extends Application {

    private boolean mIsGpsVerified;

    public boolean isGpsVerified() {
        return mIsGpsVerified;
    }

    public void setGpsVerified() {
        mIsGpsVerified = true;
    }
}
