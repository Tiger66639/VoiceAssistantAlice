package com.eleks.voiceassistant.voiceassistantpoc.task;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by dnap on 09.08.15. VoiceAssistantPoC
 */
public class TaskFactory {
    public static TaskBase create(String name, HashMap<Integer, Object> hashMap) {
        if (Objects.equals(name, TaskAlarmClock.class.getName())) {
           return new TaskAlarmClock(hashMap);
        }
        throw new RuntimeException("Task "+name+" not find");
    }

    public static TaskBase create(String name) {
        if (Objects.equals(name, TaskAlarmClock.class.getName())) {
            return new TaskAlarmClock();
        }
        throw new RuntimeException("Task "+name+" not find");
    }
}
