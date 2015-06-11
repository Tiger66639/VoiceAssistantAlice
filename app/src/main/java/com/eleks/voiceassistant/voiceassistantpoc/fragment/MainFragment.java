package com.eleks.voiceassistant.voiceassistantpoc.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.adapter.MessagesArrayAdapter;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;
import com.eleks.voiceassistant.voiceassistantpoc.utils.FontsHolder;

import java.util.ArrayList;

/**
 * Created by Serhiy.Krasovskyy on 11.06.2015.
 */
public class MainFragment extends Fragment {
    public static final String TAG = MainFragment.class.getName();
    private View mFragmentView;
    private Context mContext;
    private ListView mListView;
    private MessagesArrayAdapter mMessageAdapter;
    private ArrayList<MessageHolder> mMessages;

    public static MainFragment getInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
            }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragmentView == null) {
            mFragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        }
        prepareFragmentControls();
        return mFragmentView;
    }

    private void prepareFragmentControls() {
        mMessageAdapter = new MessagesArrayAdapter(mContext, null);
        mListView = (ListView) mFragmentView.findViewById(R.id.messages_list);
        mListView.setAdapter(mMessageAdapter);
    }

    public void addMessage(String message, boolean isCursive) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        mMessages.add(new MessageHolder(message, isCursive));
        refreshMessageList();
    }

    public void addMessage(String message, WordHolder[] words, boolean isCursive) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        mMessages.add(new MessageHolder(message, words, isCursive));
        refreshMessageList();
    }

    public void replaceLastMessage(String message, boolean isCursive) {
        if (mMessages == null) {
            mMessages = new ArrayList<>();
        }
        int messageSize = mMessages.size();
        if (messageSize > 0) {
            mMessages.remove(messageSize - 1);
        }
        mMessages.add(new MessageHolder(message, isCursive));
        refreshMessageList();
    }

    private void refreshMessageList() {
        if (mMessageAdapter != null) {
            if (mMessages != null) {
                mMessageAdapter.setMessages(mMessages.toArray(new MessageHolder[mMessages.size()]));
                mListView.setSelection(mMessages.size() - 1);
            } else {
                mMessageAdapter.setMessages(null);
            }
        }
    }

    public void clearMessages() {
        mMessages = null;
        refreshMessageList();
    }

    public void setInvertedColors(boolean invertedColors) {
        mMessageAdapter.setInvertedColors(invertedColors);
        refreshMessageList();
    }

    public MessageHolder getLastMessage() {
        int lastMessageIndex = mMessages.size() - 1;
        return mMessages.get(lastMessageIndex);
    }
}
