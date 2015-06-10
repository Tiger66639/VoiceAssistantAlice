package com.eleks.voiceassistant.voiceassistantpoc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Serhiy.Krasovskyy on 10.06.2015.
 */
public class WholeWordIndexFinder {

    private String mSearchString;

    public WholeWordIndexFinder(String searchString) {
        this.mSearchString = searchString;
    }

    public List<IndexWrapper> findIndexesForWord(String word) {
        String regex = "\\b" + word + "\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mSearchString);

        List<IndexWrapper> wrappers = new ArrayList<>();

        while (matcher.find() == true) {
            int end = matcher.end();
            int start = matcher.start();
            IndexWrapper wrapper = new IndexWrapper(start, end);
            wrappers.add(wrapper);
        }
        return wrappers;
    }
}
