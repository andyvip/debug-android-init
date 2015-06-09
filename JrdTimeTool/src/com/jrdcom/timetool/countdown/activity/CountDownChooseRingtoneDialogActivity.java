
package com.jrdcom.timetool.countdown.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.deskclock.R;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CountDownChooseRingtoneDialogActivity extends Activity {

    public static final String MEDIA_PATH_DEFAULT = Environment.getRootDirectory().toString()
            + "/media/audio/alarms";

    public static final String MEDIA_PATH_ALCATEL = Environment.getRootDirectory().toString()
            + "/media_alcatel/audio/alarms";

    public static final String TIMER_RPREFERENCES = "timetool.timer";

    public static final String ALERT_RINGTONE_PATH_KEY = "countdown.alert.ringtone.path";

    public static final String ALERT_SILENT_PATH = "silent";

    private static String mFilePath;
    private int checkedItemPosition = 0;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.countdown_set_ringtone_layout);
        chooseRing();
    }

    // choose rings
    private void chooseRing() {
        String mediaPath = MEDIA_PATH_DEFAULT;
        String company = "";
        if ("ALCATEL".equals(company) || "FPT".equals(company)) {
            mediaPath = MEDIA_PATH_ALCATEL;
        }

        String[] ringItems = null;

        final File[] mediaFiles = new File(mediaPath).listFiles();
        if (mediaFiles == null || mediaFiles.length <= 0) {
            ringItems = new String[1];
            ringItems[0] = getString(R.string.timer_ringtone_silent);
        } else {
            ringItems = new String[mediaFiles.length + 1];
            ringItems[0] = getString(R.string.timer_ringtone_silent);

            for (int i = 0; i < mediaFiles.length; i++) {
                File mediaFile = mediaFiles[i];
                // PR 610734 - Neo Skunkworks - Soar Gao - 001 begin
                // Delete extension in display of ringtone
                // ringItems[i + 1] = mediaFile.getName()
                // modify by wei_zhang for PR798733 start
                // when music name contains '_', remove '_'
                String fileName = mediaFile.getName().replace("_", " ");
                ringItems[i + 1] = fileName.substring(0, mediaFile.getName().lastIndexOf("."));
                // modify by wei_zhang for PR798733 end
                // PR 610734 - Neo Skunkworks - Soar Gao - 001 end
            }

        }

        // retrieve the former ring
        final SharedPreferences sharedPre = getSharedPreferences(TIMER_RPREFERENCES, MODE_PRIVATE);
        String preRingtonePath = sharedPre.getString(ALERT_RINGTONE_PATH_KEY, "");
        int checkedItem = 0;

        // when the former ring is not silent
        if (!ALERT_SILENT_PATH.equals(preRingtonePath)) {
            // when the ring does not exit ,set to be silent
            if (mediaFiles == null || mediaFiles.length <= 0) {
                sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH).commit();
            } else {
                boolean isFindPreMediaFile = false;
                for (int i = 0; i < mediaFiles.length; i++) {
                    if (preRingtonePath.equals(mediaFiles[i].getAbsolutePath())) {
                        isFindPreMediaFile = true;
                        // set the current pitched on item
                        checkedItem = i + 1;
                        break;
                    }
                }

                // added by lijuan.li at 2012.4.18 begin
                if (preRingtonePath != mFilePath)
                    mFilePath = preRingtonePath;
                // lijuan.li end

                // when do not find the former ring
                if (!isFindPreMediaFile) {

                    if (preRingtonePath == null) {
                        sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH)
                                .commit();
                    } else if (preRingtonePath != null) {
                        checkedItem = mediaFiles.length + 1;
                    }

                }
            }
        }

        // show the dialog

        String[] from = {
            "RINGTONE_NAME"
        };
        int[] to = {
            R.id.ringtone_name
        };

        final ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Log.i("GetView========================", "ringItems.length=====================--->>"
                + ringItems.length);
        for (String item : ringItems) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(from[0], item);
            data.add(map);
        }

        // add by liang.zhang for PR 851921 at 2014-12-17 begin
        checkedItemPosition = checkedItem;
        // add by liang.zhang for PR 851921 at 2014-12-17 end
        
        listView = (ListView) findViewById(R.id.music_ringtone_list_view);
        listView.requestFocus();

         final BaseAdapter baseAdapter = new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.alarm_list_view_ringtone,
                            null);
                    holder = new ViewHolder();
                    holder.text_name = (TextView) convertView.findViewById(R.id.list_name);
                    holder.radio_button = (RadioButton) convertView.findViewById(R.id.radio_button);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.text_name.setText(data.get(position).get("RINGTONE_NAME"));
                holder.radio_button.setChecked(checkedItemPosition == position);
                Log.i("GetView========================", "getview=====================--->>"
                        + position);
                return convertView;

            }

            @Override
            public long getItemId(int position) {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Object getItem(int position) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return data.size();
            }
        };
        listView.setAdapter(baseAdapter);
        
        // add by liang.zhang for PR 851921 at 2014-12-17 begin
        listView.setSelection(checkedItemPosition);
        // add by liang.zhang for PR 851921 at 2014-12-17 end
        
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                stopService(new Intent(CountDownChooseRingtoneDialogActivity.this,
                        MediaPlayerService.class));
                checkedItemPosition = position;

                if (position == 0) {
                    mFilePath = ALERT_SILENT_PATH;
                    
                    // add by liang.zhang for PR 851921 at 2014-12-17 begin
                    baseAdapter.notifyDataSetChanged();
                    // add by liang.zhang for PR 851921 at 2014-12-17 end
                    
                    return;
                }

                if (position < mediaFiles.length + 1) {
                    File file = mediaFiles[position - 1];

                    mFilePath = file.getAbsolutePath();

                    Intent intent = new Intent(CountDownChooseRingtoneDialogActivity.this,
                            MediaPlayerService.class);

                    intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA,
                            file.getAbsolutePath());

                    startService(intent);

                }

                Log.i("GetView========================", "mFilePath=====================--->>"
                        + mFilePath);

                baseAdapter.notifyDataSetChanged();
            }
        });

        Button button = (Button) findViewById(R.id.cancle_button);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                stopService(new Intent(CountDownChooseRingtoneDialogActivity.this,
                        MediaPlayerService.class));
                finish();

            }
        });
        Button okButton = (Button) findViewById(R.id.ok_button);

        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mFilePath == null) {
                    sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH).commit();
                } else {
                    sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, mFilePath).commit();
                }

                stopService(new Intent(CountDownChooseRingtoneDialogActivity.this,
                        MediaPlayerService.class));
                finish();

            }
        });

    }

    public static class ViewHolder {
        TextView text_name;
        RadioButton radio_button;
    }

}
