package com.eleks.voiceassistant.voiceassistantpoc.task;

import android.app.Fragment;
import com.eleks.voiceassistant.voiceassistantpoc.activity.MainActivity;
import com.eleks.voiceassistant.voiceassistantpoc.activity.SimpleActivity;

import java.util.HashMap;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
abstract public class TaskBase extends Fragment {

    protected HashMap<Integer, Object> inParams;

    abstract Integer[] getParams();
    abstract public boolean execute(SimpleActivity mainActivity);

    public TaskBase( HashMap<Integer, Object> inParam) {
        this.inParams = inParam;
    }
    public TaskBase() {
        inParams = new HashMap<>();
    }
}
