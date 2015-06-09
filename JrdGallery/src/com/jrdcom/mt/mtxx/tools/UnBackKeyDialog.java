package com.jrdcom.mt.mtxx.tools;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

public class UnBackKeyDialog extends Dialog{

	public UnBackKeyDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_SEARCH){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
