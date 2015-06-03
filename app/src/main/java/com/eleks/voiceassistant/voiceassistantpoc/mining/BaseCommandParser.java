package com.eleks.voiceassistant.voiceassistantpoc.mining;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by Serhiy.Krasovskyy on 03.06.2015.
 */
public class BaseCommandParser {

    protected final Context mContext;
    protected WordHolder[] mWords;
    protected ITextMiner[] mMiners;

    public BaseCommandParser(Context context, String text) {
        this.mContext = context;
        if (!TextUtils.isEmpty(text)) {
            mWords = getWords(text);
        }
    }

    protected void applyMiners() {
        if (mMiners != null && mMiners.length > 0) {
            for (ITextMiner miner : mMiners) {
                if (miner != null) {
                    mWords = miner.investigate(mContext, mWords);
                }
            }
        }
    }

    private WordHolder[] getWords(String text) {
        String[] words = text.split(" ");
        ArrayList<WordHolder> result = new ArrayList<>();
        for (String word : words) {
            result.add(new WordHolder(word.toLowerCase()));
        }
        return result.toArray(new WordHolder[result.size()]);
    }

    public boolean isCommand() {
        boolean result = false;
        for (WordHolder word : mWords) {
            if (word.wordMeaning == WordMeaning.COMMAND) {
                result = true;
                break;
            }
        }
        return result;
    }
}
