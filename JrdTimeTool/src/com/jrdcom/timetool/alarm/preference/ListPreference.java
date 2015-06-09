/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.jrdcom.timetool.alarm.preference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.provider.DialogAdapter;

/**
 * A {@link Preference} that displays a list of entries as a dialog.
 * <p>
 * This preference will store a string into the SharedPreferences. This string
 * will be the value from the {@link #setEntryValues(CharSequence[])} array.
 * 
 * @attr ref android.R.styleable#ListPreference_entries
 * @attr ref android.R.styleable#ListPreference_entryValues
 */
public class ListPreference extends android.preference.ListPreference {

    private boolean isChangeSummary = false;

    public void setChangeSummary(boolean isChangeSummary) {
        this.isChangeSummary = isChangeSummary;
    }

    public ListPreference(Context context) {
        super(context);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    private int getIndex() {
        return findIndexOfValue(getValue());
    }

    @Override
    protected void showDialog(Bundle state) {
        // TODO Auto-generated method stub

        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.set_alarm_dialog_layout,
                null);
        TextView title = (TextView) view.findViewById(R.id.tile);
        Button close = (Button) view.findViewById(R.id.cancle_button);

        title.setText(getDialogTitle());
        ListView listView = (ListView) view.findViewById(R.id.music_ringtone_list_view);
        listView.setAdapter(new DialogAdapter(getContext(), getEntries(), getIndex()));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                setValueIndex(position);
                if (isChangeSummary) {
                    setSummary(getEntry());
                }
                /*
                 * Clicking on an item simulates the positive button click, and
                 * dismisses the dialog.
                 */

                ListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }

        });

        dialog.setContentView(view);

        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {

    }

}
