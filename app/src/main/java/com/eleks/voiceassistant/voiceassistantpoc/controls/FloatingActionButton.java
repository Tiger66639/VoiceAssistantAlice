package com.eleks.voiceassistant.voiceassistantpoc.controls;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eleks.voiceassistant.voiceassistantpoc.R;

import java.util.ArrayList;

public class FloatingActionButton extends FrameLayout {
    private ImageView mFabIcon;
    private ArrayList<OnFabClickListener> mOnFabClickListener;
    private FloatingActionButtonStates mFabState;

    public FloatingActionButton(Context context) {
        this(context, null, 0, FloatingActionButtonStates.MICROPHONE_RED);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0, FloatingActionButtonStates.MICROPHONE_RED);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, FloatingActionButtonStates.MICROPHONE_RED);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr,
                                FloatingActionButtonStates state) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setFabState(state);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, getWidth(), getHeight());
            }
        });

        setClipToOutline(true);
    }

    public FloatingActionButtonStates getFabState() {
        return mFabState;
    }

    public void setFabState(FloatingActionButtonStates state) {
        if (mFabIcon == null) {
            mFabIcon = (ImageView) findViewById(R.id.fab_icon);
        }
        mFabState = state;
        if (mFabIcon != null) {
            switch (state) {
                case MICROPHONE_RED:
                    setBackgroundColor(getResources().getColor(R.color.fab_color_red));
                    mFabIcon.setImageResource(R.drawable.ic_mic_white);
                    break;
                case CLOSE_RED:
                    setBackgroundColor(getResources().getColor(R.color.fab_color_red));
                    mFabIcon.setImageResource(R.drawable.ic_close_white);
                    break;
                case CLOSE_WHITE:
                    setBackgroundColor(getResources().getColor(R.color.fab_color_white));
                    mFabIcon.setImageResource(R.drawable.ic_close_red);
            }
        }
    }

    public void setOnFabClickListener(OnFabClickListener listener) {
        if (mOnFabClickListener == null) {
            mOnFabClickListener = new ArrayList<>();
        }
        mOnFabClickListener.add(listener);
    }

    /**
     * Override performClick() so that we can toggle the checked state when the view is clicked
     */
    @Override
    public boolean performClick() {
        if (mOnFabClickListener != null) {
            for (OnFabClickListener listener : mOnFabClickListener) {
                listener.onFabClick(FloatingActionButton.this);
            }
        }
        return super.performClick();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // As we have changed size, we should invalidate the outline so that is the the
        // correct size
        invalidateOutline();
    }


    public interface OnFabClickListener {
        void onFabClick(FloatingActionButton fabView);
    }
}
