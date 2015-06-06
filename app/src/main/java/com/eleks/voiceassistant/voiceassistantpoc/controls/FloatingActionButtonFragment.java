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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eleks.voiceassistant.voiceassistantpoc.R;

import java.util.ArrayList;

/**
 * This fragment inflates a layout with two Floating Action Buttons and acts as a listener to
 * changes on them.
 */
public class FloatingActionButtonFragment extends Fragment
        implements FloatingActionButton.OnCheckedChangeListener,
        FloatingActionButton.OnFabClickListener {

    public final static String TAG = FloatingActionButtonFragment.class.getName();
    private ArrayList<FloatingActionButton.OnCheckedChangeListener> mCheckedListeners;
    private ArrayList<FloatingActionButton.OnFabClickListener> mFabClickListeners;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fab_layout, container, false);

        // Make this {@link Fragment} listen for changes in both FABs.
        FloatingActionButton fab1 = (FloatingActionButton) rootView.findViewById(R.id.fab_1);
        fab1.setOnCheckedChangeListener(this);
        fab1.setOnFabClickListener(this);
        return rootView;
    }

    public void setCheckedChangeListener(FloatingActionButton.OnCheckedChangeListener listener) {
        if (mCheckedListeners == null) {
            mCheckedListeners = new ArrayList<>();
        }
        mCheckedListeners.add(listener);
    }

    public void setOnFabClickListener(FloatingActionButton.OnFabClickListener listener) {
        if (mFabClickListeners == null) {
            mFabClickListeners = new ArrayList<>();
        }
        mFabClickListeners.add(listener);
    }

    @Override
    public void onCheckedChanged(FloatingActionButton fabView, boolean isChecked) {
        if (mCheckedListeners != null) {
            for (FloatingActionButton.OnCheckedChangeListener listener : mCheckedListeners) {
                listener.onCheckedChanged(fabView, isChecked);
            }
        }
    }

    @Override
    public void onFabClick(FloatingActionButton fabView) {
        if (mFabClickListeners != null) {
            for (FloatingActionButton.OnFabClickListener listener : mFabClickListeners) {
                listener.onFabClick(fabView);
            }
        }
    }
}
