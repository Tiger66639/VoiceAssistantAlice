package com.eleks.voiceassistant.voiceassistantpoc.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;

/**
 * Created by Sergey on 06.06.2015.
 */
public class MessagesArrayAdapter extends ArrayAdapter<MessageHolder> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private MessageHolder[] mMessages;
    private boolean mIsInvertedColors;

    public MessagesArrayAdapter(Context context, MessageHolder[] values) {
        super(context, R.layout.item_message);
        this.mContext = context;
        mMessages = values;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setInvertedColors(boolean isInvertedColors) {
        this.mIsInvertedColors = isInvertedColors;
    }

    public void setMessages(MessageHolder[] messages) {
        mMessages = messages;
        notifyDataSetChanged();
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
            viewHolder = getViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        MessageHolder item = getItem(position);
        viewHolder.message.setText(item.message);
        setTextViewsColor(viewHolder);
        setFont(viewHolder, item.isCursive);
        if (item.words != null) {
            highlightWordsInTextView(viewHolder.message, item.words);
        }
        return view;
    }

    private void highlightWordsInTextView(TextView textView, WordHolder[] words) {
        int color = mContext.getResources().getColor(R.color.message_white_color);
        textView.setTextColor(color);
        if (textView != null) {
            String textValue = textView.getText().toString();
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(textValue);

            for (WordHolder word : words) {
                if (word.wordMeaning != null) {
                    int startIdx = textValue.toLowerCase().indexOf(word.word.toLowerCase());
                    if (startIdx >= 0) {
                        int endIdx = startIdx + word.word.length();
                        spanText.setSpan(
                                new BackgroundColorSpan(
                                        mContext.getResources()
                                                .getColor(R.color.color_for_highlighting)),
                                startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            textView.setText(spanText);
            textView.setTag(spanText);
        }
    }

    private ViewHolder getViewHolder(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.messageContainer = view.findViewById(R.id.message_container);
        viewHolder.message = (TextView) view.findViewById(R.id.message);
        viewHolder.divider = (TextView) view.findViewById(R.id.divider);
        return viewHolder;
    }

    private void setFont(ViewHolder viewHolder, boolean isCursive) {
        if (isCursive) {
            viewHolder.message.setTypeface(null, Typeface.ITALIC);
        } else {
            viewHolder.message.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void setTextViewsColor(ViewHolder viewHolder) {
        int color = mContext.getResources().getColor(R.color.message_red_color);
        if (mIsInvertedColors) {
            color = mContext.getResources().getColor(R.color.message_white_color);

        }
        viewHolder.message.setTextColor(color);
        viewHolder.divider.setTextColor(color);
    }

    private class ViewHolder {
        View messageContainer;
        TextView message;
        TextView divider;
    }
}
