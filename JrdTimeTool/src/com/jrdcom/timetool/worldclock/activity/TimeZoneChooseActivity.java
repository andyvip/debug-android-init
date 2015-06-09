
package com.jrdcom.timetool.worldclock.activity;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.deskclock.R;
import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;

public class TimeZoneChooseActivity extends Activity implements OnItemClickListener {

    private ChooseListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Acquire all time zone from DB
        List<TimeZoneInfo> timeZoneList = TimeZones.getAllTimeZone(this);
        if (timeZoneList == null || timeZoneList.size() <= 0) {
            finish();
            return;
        }

        setContentView(R.layout.worldtime_choose_timezone);

        initActionBar();

        mListAdapter = new ChooseListAdapter(timeZoneList);

        ListView listView = (ListView) findViewById(R.id.worldtime_choose_list);

        listView.setAdapter(mListAdapter);

        listView.requestFocus();
        listView.setOnItemClickListener(this);

        initSearchView();

    }

    // add haifeng.tang PR 795187

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        View customActionBarView = getLayoutInflater().inflate(R.layout.set_alarm_action_bar, null);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
        actionBar.setCustomView(customActionBarView);
        View bk_btn = customActionBarView.findViewById(R.id.bk_btn);
        TextView title = (TextView) customActionBarView.findViewById(R.id.title);
        title.setText(getString(R.string.select_city));
        bk_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                TimeZoneChooseActivity.this.finish();

            }

        });
    }

    private void initSearchView() {
        final EditText searchEdit = (EditText) findViewById(R.id.worldtime_search_text);
        final View delSearchIconView = findViewById(R.id.worldtime_delete_search);

        searchEdit.addTextChangedListener(new TextWatcher() {
            private Toast toast = null;// add by wei.li for PR505034

            public void afterTextChanged(Editable s) {
                // show "Clear" button when user entered search content
                if (s.length() != 0) {
                    delSearchIconView.setVisibility(View.VISIBLE);
                } else {
                    delSearchIconView.setVisibility(View.GONE);
                }

                List<TimeZoneInfo> timezoneList = TimeZones.getTimeZoneByName(
                        TimeZoneChooseActivity.this, s.toString());
                mListAdapter.setTimeZoneList(timezoneList);
                if (mListAdapter.getCount() <= 0) {
                    // Modifyed by wei.li for PR505034 begin
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(TimeZoneChooseActivity.this, R.string.no_city,
                            Toast.LENGTH_SHORT);
                    toast.show();
                    // Modifyed by wei.li for PR505034 end
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        searchEdit.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View view, int actionId, KeyEvent keyEvent) {
                // TODO Auto-generated method stub

                if (actionId == KeyEvent.KEYCODE_ENTER) {
                    return true;
                } else {
                    return false;
                }
            }

        });

        // PR 597064 - Neo Skunkworks - Soar Gao - 001 begin
        // click 'search' to hide input
        searchEdit.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    ((InputMethodManager) searchEdit.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            TimeZoneChooseActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return true;
            }
        });
        // PR 597064 - Neo Skunkworks - Soar Gao - 001 end
        delSearchIconView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Editable editable = searchEdit.getEditableText();
                editable.clear();
            }
        });
    }

    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        TimeZoneInfo timeZoneInfo = mListAdapter.getItem(pos);
        if (timeZoneInfo == null) {
            return;
        }

        // Do not add again when the city was added
        /* PR 668964 - Neo Skunkworks - Paul Xu modified - 001 Begin */
        if (timeZoneInfo.isShow || timeZoneInfo.timeZoneId.equals(TimeZone.getDefault().getID())) {
            return;
        }
        /* PR 668964 - Neo Skunkworks - Paul Xu modified - 001 End */

        // Add city
        timeZoneInfo.isShow = true;
        timeZoneInfo.updateTime = Calendar.getInstance().getTimeInMillis();
        TimeZones.updateTimeZone(getContentResolver(), timeZoneInfo);
        mListAdapter.notifyDataSetChanged();
        finish();

    }

    private class ChooseListAdapter extends BaseAdapter {

        private List<TimeZoneInfo> mTimeZoneList;

        public ChooseListAdapter(List<TimeZoneInfo> timeZoneList) {
            mTimeZoneList = timeZoneList;
        }

        public int getCount() {
            if (mTimeZoneList == null) {
                return 0;
            }
            return mTimeZoneList.size();
        }

        public TimeZoneInfo getItem(int position) {
            if (mTimeZoneList == null) {
                return null;
            }

            if (position >= mTimeZoneList.size()) {
                return null;
            }
            return mTimeZoneList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void setTimeZoneList(List<TimeZoneInfo> timeZoneList) {
            mTimeZoneList = timeZoneList;
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            TimeZoneInfo timeZoneInfo = getItem(position);
            if (timeZoneInfo == null) {
                return null;
            }

            View view = null;
            if (convertView != null) {
                view = convertView;
            } else {
                view = LayoutInflater.from(TimeZoneChooseActivity.this).inflate(
                        R.layout.worldtime_choose_list_item, parent, false);
            }

            TextView displayNameView = (TextView) view.findViewById(R.id.worldtime_display_name);
            displayNameView.setText(timeZoneInfo.displayName);

            TextView gmtView = (TextView) view.findViewById(R.id.worldtime_GMT);
            /* PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin */
            TimeZones.setTimeZoneOffset(timeZoneInfo);
            /* PR 567420- Neo Skunkworks - Paul Xu added - 001 End */
            gmtView.setText(TimeZones.getGMTNameByOffset(timeZoneInfo.offset));

            View isShowed = view.findViewById(R.id.worldtime_is_showed);
            isShowed.setEnabled(false);
            /* PR 668964 - Neo Skunkworks - Paul Xu modified - 001 Begin */
            if (timeZoneInfo.isShow
                    || (timeZoneInfo.timeZoneId.equals(TimeZone.getDefault().getID()))) {
                /* PR 668964 - Neo Skunkworks - Paul Xu modified - 001 End */
                isShowed.setVisibility(View.VISIBLE);
                displayNameView.setEnabled(false);
                displayNameView.setTextColor(0xffc6c6c6);
                gmtView.setTextColor(0xffc6c6c6);
                view.setBackground(null);
               

            } else {
                isShowed.setVisibility(View.GONE);
                displayNameView.setEnabled(true);
                displayNameView.setTextColor(0xff222222);
                gmtView.setTextColor(0xff222222);
                view.setBackgroundResource(R.drawable.list_selector);
            }

            return view;
        }

    }

}
