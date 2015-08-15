package com.eleks.voiceassistant.voiceassistantpoc.task;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dnap.opensource.stringToDate.Str2Time;
import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.activity.MainActivity;

import java.util.*;

/**
 * Created by dnap on 28.07.15. VoiceAssistantPoC
 */
public class TaskAlarmClock extends TaskBase {
    public static final Integer DATETIME = 1;
    public static final Integer MESSAGE = 2;

    @Override
    Integer[] getParams() {
        return new Integer[]{DATETIME, MESSAGE};
    }


    @Override
    public boolean execute(MainActivity mainActivity) {
        if(inParams.size() == 0) {
            Intent i = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            mainActivity.startActivity(i);
        }

        if(inParams.containsKey(DATETIME)){
            Calendar date = (Calendar)inParams.get(DATETIME);
            if(date != null) {
                Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
                //i.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm");

                ArrayList<Integer> days = new ArrayList<Integer>();

                i.putExtra(AlarmClock.EXTRA_HOUR, date.get(Calendar.HOUR));
                i.putExtra(AlarmClock.EXTRA_MINUTES, date.get(Calendar.MINUTE));
                Calendar calendarTmp = Calendar.getInstance();
                calendarTmp.add(Calendar.HOUR, 24);
                if(date.after(calendarTmp)) {
                    calendarTmp.add(Calendar.DAY_OF_WEEK, 6);
                    if(date.after(calendarTmp)) {
                        return false; // go to notify
                    }
                    i.putExtra(AlarmClock.EXTRA_DAYS, date.get(Calendar.DAY_OF_WEEK));
                }
                mainActivity.startActivity(i);
            }else{
                Intent i = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                mainActivity.startActivity(i);
            }
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.task_alarm_clock, container, false);
    }


}
