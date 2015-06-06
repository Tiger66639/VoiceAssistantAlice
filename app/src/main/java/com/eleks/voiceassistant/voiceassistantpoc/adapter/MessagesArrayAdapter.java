package com.eleks.voiceassistant.voiceassistantpoc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;

/**
 * Created by Sergey on 06.06.2015.
 */
public class MessagesArrayAdapter extends ArrayAdapter<MessageHolder> {

    private final LayoutInflater mInflater;
    private MessageHolder[] mMessages;

    public MessagesArrayAdapter(Context context, MessageHolder[] values) {
        super(context, R.layout.item_message);
        mMessages = values;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setMessages(MessageHolder[] messages) {
        mMessages = messages;
    }

    @Override
    public int getCount() {
        if (mMessages == null) {
            return 0;
        } else {
            return mMessages.length;
        }
    }

    @Override
    public MessageHolder getItem(int position) {
        if (mMessages != null) {
            return mMessages[position];
        } else {
            return null;
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_message, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.messageContainer = view.findViewById(R.id.message_container);
            viewHolder.message = (TextView) view.findViewById(R.id.message);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.message.setText(getItem(position).message);
        return view;
    }

    private class ViewHolder {
        View messageContainer;
        TextView message;
    }
}
