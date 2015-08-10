package com.eleks.voiceassistant.voiceassistantpoc.task;

import com.eleks.voiceassistant.voiceassistantpoc.activity.MainActivity;

import java.util.HashMap;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
abstract public class TaskBase {

    protected HashMap<Integer, Object> inParams;

    abstract Integer[] getParams();
    abstract public boolean execute(MainActivity mainActivity);

    public TaskBase( HashMap<Integer, Object> inParam) {
        this.inParams = inParam;
    }
    public TaskBase() {
        inParams = new HashMap<>();
    }
}
