package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.test.InstrumentationTestCase;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserBase;
import com.eleks.voiceassistant.voiceassistantpoc.parser.ParserFactory;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskAlarmClock;
import com.eleks.voiceassistant.voiceassistantpoc.task.TaskBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dnap on 09.08.15. VoiceAssistantPoC
 */
public class RuParserTest extends InstrumentationTestCase {
    private ParserBase parserRu;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        parserRu = ParserFactory.create("ru");

    }

    public void testCommands() {
        TaskBase task;

        HashMap<String, Class> caseList = new HashMap<String, Class>() {{
            put("Будильник на 10 30", TaskAlarmClock.class);
            put("Будильник на 10 30 да проснись уже", TaskAlarmClock.class);


            put("Кукареку", null);
        }};

        for (Map.Entry<String, Class> entry : caseList.entrySet()) {
            task = parserRu.Parse(entry.getKey());
            if (entry.getValue() == null) {
                assertNull(task);
            } else {
                assertNotNull("Null object on \"" + entry.getKey() + "\"", task);
                assertEquals("Fail object on \"" + entry.getKey() + "\"", entry.getValue(), task.getClass());
            }
        }
    }



}
