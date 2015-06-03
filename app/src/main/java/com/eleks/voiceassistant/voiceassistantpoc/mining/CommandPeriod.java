package com.eleks.voiceassistant.voiceassistantpoc.mining;

import java.util.Date;

/**
 * Created by Serhiy.Krasovskyy on 29.05.2015.
 */
public class CommandPeriod {
    public Date startDate;
    public Date finishDate;

    public CommandPeriod() {
        startDate = new Date();
        finishDate = new Date();
    }
}
