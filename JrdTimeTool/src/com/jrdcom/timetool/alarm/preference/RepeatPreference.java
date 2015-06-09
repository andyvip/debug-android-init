/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.SetAlarm;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.RepeatDialogAdapter;

public class RepeatPreference extends ListPreference {

    // Initial value that can be set with the values saved in the database.
    private Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
    // New value that will be set if a positive result comes back from the
    // dialog.
    private Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);

    public RepeatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        String[] weekdays = new DateFormatSymbols().getWeekdays();

        String[] values;

        /* CR 566879- Neo Skunkworks - Paul Xu modified - 001 Begin */
        if (isEsLanguage(context)) {

            values = new String[] {
                    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo",
            };

        } else {
            values = new String[] {
                    weekdays[Calendar.MONDAY], weekdays[Calendar.TUESDAY],
                    weekdays[Calendar.WEDNESDAY], weekdays[Calendar.THURSDAY],
                    weekdays[Calendar.FRIDAY], weekdays[Calendar.SATURDAY],
                    weekdays[Calendar.SUNDAY],
            };
        }
        /* CR 566879- Neo Skunkworks - Paul Xu modified - 001 End */
        setEntries(values);
        setEntryValues(values);
    }

    /* CR 566879- Neo Skunkworks - Paul Xu added - 001 Begin */
    private boolean isEsLanguage(Context context) {

        String language = context.getResources().getConfiguration().locale.getLanguage();

        if ("es".equals(language)) {
            return true;
        }

        return false;
    }

    /* CR 566879- Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void showDialog(Bundle state) {
        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.repeat_dialog_layout,
                null);
        TextView title = (TextView) view.findViewById(R.id.tile);
        Button cancel = (Button) view.findViewById(R.id.cancle_button);
        Button ok = (Button) view.findViewById(R.id.ok_button);

        title.setText(getDialogTitle());
        ListView listView = (ListView) view.findViewById(R.id.music_ringtone_list_view);
        final RepeatDialogAdapter repeatDialogAdapter = new RepeatDialogAdapter(getContext(),
                getEntries(), mDaysOfWeek.getBooleanArray());
        listView.setAdapter(repeatDialogAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                repeatDialogAdapter.changeItemCheckState(position);
                mNewDaysOfWeek.set(position, repeatDialogAdapter.getItemCheckState(position));
            }

        });

        dialog.setContentView(view);

        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onDialogClosed(true);

            }
        });

        dialog.show();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mDaysOfWeek.set(mNewDaysOfWeek);
            setSummary(mDaysOfWeek.toString(getContext(), true));
            callChangeListener(mDaysOfWeek);
            SetAlarm.mRepeatPref.setDaysOfWeek(mDaysOfWeek);// PR -596220 - Neo
                                                            // Skunworks - Soar
                                                            // Gao , add -001
                                                            // //update the
                                                            // display of the
                                                            // repeat in
                                                            // setAlarm
        } else {
            mNewDaysOfWeek.set(mDaysOfWeek);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
//        CharSequence[] entries = getEntries();
//        CharSequence[] entryValues = getEntryValues();
//
//        builder.setMultiChoiceItems(entries, mDaysOfWeek.getBooleanArray(),
//                new DialogInterface.OnMultiChoiceClickListener() {
//                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        mNewDaysOfWeek.set(which, isChecked);
//                    }
//                });
    }

    public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
        mDaysOfWeek.set(dow);
        mNewDaysOfWeek.set(dow);
        setSummary(dow.toString(getContext(), true));
    }

    public Alarm.DaysOfWeek getDaysOfWeek() {
        return mDaysOfWeek;
    }
}
