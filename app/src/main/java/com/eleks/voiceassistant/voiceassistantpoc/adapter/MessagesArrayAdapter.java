package com.eleks.voiceassistant.voiceassistantpoc.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eleks.voiceassistant.voiceassistantpoc.R;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WordHolder;
import com.eleks.voiceassistant.voiceassistantpoc.mining.WordMeaning;
import com.eleks.voiceassistant.voiceassistantpoc.model.MessageHolder;
import com.eleks.voiceassistant.voiceassistantpoc.utils.FontsHolder;
import com.eleks.voiceassistant.voiceassistantpoc.utils.IndexWrapper;
import com.eleks.voiceassistant.voiceassistantpoc.utils.WholeWordIndexFinder;

import java.util.List;

/**
 * Created by Sergey on 06.06.2015.
 */
public class MessagesArrayAdapter extends ArrayAdapter<MessageHolder> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final FontsHolder mFontsHolder;
    private MessageHolder[] mMessages;
    private boolean mIsInvertedColors;

    public MessagesArrayAdapter(Context context, MessageHolder[] values) {
        super(context, R.layout.item_message);
        this.mContext = context;
        this.mFontsHolder = new FontsHolder(context);
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
        int whiteColor = mContext.getResources().getColor(R.color.message_white_color);
        int redColor = mContext.getResources().getColor(R.color.message_red_color);
        if (textView != null) {
            String textValue = textView.getText().toString();
            SpannableStringBuilder spanText = new SpannableStringBuilder(textValue);
            for (WordHolder word : words) {
                IndexWrapper indexes =
                        getWordIndexesInText(textValue.toLowerCase(), word.word.toLowerCase());
                if (indexes != null) {
                    if (word.wordMeaning != null && word.wordMeaning != WordMeaning.NOISE &&
                            word.wordMeaning != WordMeaning.POSSIBLE_PLACE) {
                        ForegroundColorSpan whiteColorSpan = new ForegroundColorSpan(whiteColor);
                        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(
                                mContext.getResources().getColor(R.color.color_for_highlighting));
                        spanText.setSpan(backgroundColorSpan,
                                indexes.getStart(), indexes.getEnd(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spanText.setSpan(whiteColorSpan,
                                indexes.getStart(), indexes.getEnd(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        ForegroundColorSpan redColorSpan = new ForegroundColorSpan(redColor);
                        spanText.setSpan(redColorSpan,
                                indexes.getStart(), indexes.getEnd(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            textView.setText(spanText);
            textView.setTag(spanText);
        }
    }

    private IndexWrapper getWordIndexesInText(String text, String word) {
        WholeWordIndexFinder wordFinder = new WholeWordIndexFinder(text);
        List<IndexWrapper> wordIndexes = wordFinder.findIndexesForWord(word);
        if (wordIndexes != null && wordIndexes.size() > 0) {
            return wordIndexes.get(0);
        } else {
            return null;
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
            viewHolder.message.setTypeface(mFontsHolder.getRobotomonoItalic());
            viewHolder.message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            viewHolder.divider.setTypeface(mFontsHolder.getRobotomonoItalic());
        } else {
            viewHolder.message.setTypeface(mFontsHolder.getRobotomonoLight());
            viewHolder.message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 31);
            viewHolder.divider.setTypeface(mFontsHolder.getRobotomonoLight());
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
