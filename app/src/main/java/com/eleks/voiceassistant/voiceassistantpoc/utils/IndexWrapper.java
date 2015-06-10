package com.eleks.voiceassistant.voiceassistantpoc.utils;

/**
 * Created by Serhiy.Krasovskyy on 10.06.2015.
 */
public class IndexWrapper {
    private int mStart;
    private int mEnd;

    public IndexWrapper(int start, int end) {
        this.mStart = start;
        this.mEnd = end;
    }

    public int getEnd() {
        return mEnd;
    }

    public int getStart() {
        return mStart;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mEnd;
        result = prime * result + mStart;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IndexWrapper other = (IndexWrapper) obj;
        if (mEnd != other.mEnd)
            return false;
        if (mStart != other.mStart)
            return false;
        return true;
    }
}
