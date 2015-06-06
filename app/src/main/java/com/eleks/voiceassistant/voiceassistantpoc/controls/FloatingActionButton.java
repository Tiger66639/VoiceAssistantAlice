/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eleks.voiceassistant.voiceassistantpoc.controls;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.eleks.voiceassistant.voiceassistantpoc.R;

/**
 * A Floating Action Button is a {@link android.widget.Checkable} view distinguished by a circled
 * icon floating above the UI, with special motion behaviors.
 */
public class FloatingActionButton extends FrameLayout// implements Checkable
{

    /**
     * An array of states.
     */
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };
    private static final String TAG = "FloatingActionButton";
    private ImageView mFabIcon;
    // A boolean that tells if the FAB is checked or not.
    private boolean mChecked;
    // A listener to communicate that the FAB has changed it's state
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnFabClickListener mOnFabClickListener;
    private FloatingActionButtonStates mFabState;

    public FloatingActionButton(Context context) {
        this(context, null, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr,
                                int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        mFabState = FloatingActionButtonStates.MICROPHONE_ACTIVATED;
        // Set the outline provider for this view. The provider is given the outline which it can
        // then modify as needed. In this case we set the outline to be an oval fitting the height
        // and width.
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, getWidth(), getHeight());
            }
        });

        // Finally, enable clipping to the outline, using the provider we set above
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
        switch (state) {
            case MICROPHONE_ACTIVATED:
                setBackgroundColor(getResources().getColor(R.color.fab_color_1));
                mFabIcon.setImageResource(R.drawable.ic_mic_white);
                break;
            case MICROPHONE_DEACTIVATED:
                setBackgroundColor(getResources().getColor(R.color.fab_color_2));
                mFabIcon.setImageResource(R.drawable.ic_mic_red);
                break;
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button
     * changes.
     *
     * @param listener the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public void setOnFabClickListener(OnFabClickListener listener) {
        mOnFabClickListener = listener;
    }

    /**
     * Sets the checked/unchecked state of the FAB.
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        // If trying to set the current state, ignore.
        if (checked == mChecked) {
            return;
        }
        mChecked = checked;

        // Now refresh the drawable state (so the icon changes)
        refreshDrawableState();

        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    /**
     * Override performClick() so that we can toggle the checked state when the view is clicked
     */
    @Override
    public boolean performClick() {
        if (mOnFabClickListener != null) {
            mOnFabClickListener.onFabClick(FloatingActionButton.this);
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


    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changes.
     */
    public static interface OnCheckedChangeListener {

        /**
         * Called when the checked state of a FAB has changed.
         *
         * @param fabView   The FAB view whose state has changed.
         * @param isChecked The new checked state of buttonView.
         */
        void onCheckedChanged(FloatingActionButton fabView, boolean isChecked);
    }

    public static interface OnFabClickListener {
        void onFabClick(FloatingActionButton fabView);
    }
}
