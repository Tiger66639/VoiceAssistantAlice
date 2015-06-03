package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public interface ITextMiner {
    WordHolder[] investigate(Context context, WordHolder[] words);
}
