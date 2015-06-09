
package com.jrdcom.example.joinpic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import com.jrdcom.android.gallery3d.R;
import android.view.View;
import android.content.res.Configuration;
import android.widget.Button;
import android.view.View;
@SuppressWarnings("deprecation")
public class PinTuTabActivity extends TabActivity 
implements View.OnClickListener

{
    
    public static TabHost tab_host;
    //yaogang.hao for PR 543034
    public static Button radio_tab_template;
    public static Button radio_tab_freedom;
    public static Button radio_tab_joint;
    public static RadioGroup genderGroup;
    AlertDialog dialog;
    public static int i = 0;

    private SharedPreferences sharedPrefrences;
    public static Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tab_host);
        tab_host = (TabHost) findViewById(android.R.id.tabhost);

        // 模板拼图
        TabHost.TabSpec ts1 = tab_host.newTabSpec("template");
        ts1.setIndicator("template");
        //yaogang.hao for PR 543034
        Intent templateIntent = new Intent();
        templateIntent.setClass(this, ActivityTemplate.class);
        ts1.setContent(templateIntent);
        tab_host.addTab(ts1);

        // 自由拼图
        TabHost.TabSpec ts2 = tab_host.newTabSpec("freedom");
        ts2.setIndicator("freedom");
        //yaogang.hao for PR 543034
        Intent intent = new Intent();
        intent.setClass(this, ActivityFreedom.class);
        ts2.setContent(intent);
        tab_host.addTab(ts2);

        // 图片拼接
        TabHost.TabSpec ts3 = tab_host.newTabSpec("joint");
        ts3.setIndicator("joint");
        // Intent jointintent =getIntent();
        // jointintent.setClass(this,ActivityJoint.class);
        // ts3.setContent(jointintent);
        ts3.setContent(new Intent(this, ActivityJoint.class));
        tab_host.addTab(ts3);

        saveTab();
        i = sharedPrefrences.getInt("tabid", 0);// 将数据读出
        initRadios();
        tab_host.setCurrentTab(i);

    }

    private void initRadios() {
        genderGroup = (RadioGroup) findViewById(R.id.bottom_menu);
        //yaogang.hao for PR 543034
        radio_tab_template = ((Button) findViewById(R.id.rb_pintu_tab_template));
        radio_tab_freedom = ((Button) findViewById(R.id.rb_pintu_tab_freedom));
        radio_tab_joint = ((Button) findViewById(R.id.rb_pintu_tab_joint));
        //yaogang.hao for PR 543034
        radio_tab_template.setOnClickListener(this);
        radio_tab_freedom.setOnClickListener(this);
        radio_tab_joint.setOnClickListener(this);
        
        if (i == 0) {
            radio_tab_template.setSelected(true);
        } else if (i == 1) {
            radio_tab_freedom.setSelected(true);
        } else if (i == 2) {
            radio_tab_joint.setSelected(true);
        }
        genderGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == radio_tab_template.getId()) {
                            tab_host.setCurrentTab(0);
                            tab_host.setCurrentTabByTag("template");
                            editor.putInt("tabid", 0);
                            editor.commit();
                        } else if (checkedId == radio_tab_freedom.getId()) {
                            tab_host.setCurrentTab(1);
                            tab_host.setCurrentTabByTag("freedom");
                            editor.putInt("tabid", 1);
                            editor.commit();
                        } else if (checkedId == radio_tab_joint.getId()) {
                            tab_host.setCurrentTab(2);
                            tab_host.setCurrentTabByTag("joint");
                            editor.putInt("tabid", 2);
                            editor.commit();
                        }
                    }
                });
    }

    // 保存数据
    public void saveTab() {
        sharedPrefrences = this.getSharedPreferences("user",
                MODE_WORLD_READABLE);// 得到SharedPreferences，会生成user.xml
        editor = sharedPrefrences.edit();
    }
    
    public static void  setGenderGroup(boolean mean) {
        if(mean){
            genderGroup.setVisibility(View.GONE);
        }else {
            genderGroup.setVisibility(View.VISIBLE);
        }
    }
    //yaogang.hao for PR 543034
    public void changeSelected()
    {
        radio_tab_template.setSelected(false);
        radio_tab_freedom.setSelected(false);
        radio_tab_joint.setSelected(false);
    }
    //yaogang.hao for PR 543034
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.rb_pintu_tab_template:
            {
                
                changeSelected();
                radio_tab_template.setSelected(true);
                
                tab_host.setCurrentTab(0);
                tab_host.setCurrentTabByTag("template");
                editor.putInt("tabid", 0);
                editor.commit();
                break;
            }
            case R.id.rb_pintu_tab_freedom:
            {
                changeSelected();
                radio_tab_freedom.setSelected(true);
                
                tab_host.setCurrentTab(1);
                tab_host.setCurrentTabByTag("freedom");
                editor.putInt("tabid", 1);
                editor.commit();
                break;
            }
            case R.id.rb_pintu_tab_joint:
            {
                changeSelected();
                radio_tab_joint.setSelected(true);
                
                tab_host.setCurrentTab(2);
                tab_host.setCurrentTabByTag("joint");
                editor.putInt("tabid", 2);
                editor.commit();
                break;
            }
        }
    }
}
