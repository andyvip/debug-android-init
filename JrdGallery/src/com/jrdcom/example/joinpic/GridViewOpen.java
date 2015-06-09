package com.jrdcom.example.joinpic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.GridView;

public class GridViewOpen extends GridView {

	public GridViewOpen(Context paramContext, AttributeSet paramAttributeSet) {
		this(paramContext, paramAttributeSet, 0);
	}

	public GridViewOpen(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent) {
		return super.onKeyUp(paramInt, paramKeyEvent);
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		switch (paramMotionEvent.getAction()) {
		case 0:
		}
		return super.onTouchEvent(paramMotionEvent);
	}
}