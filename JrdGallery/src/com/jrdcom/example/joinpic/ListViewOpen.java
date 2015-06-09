package com.jrdcom.example.joinpic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.ListView;

public class ListViewOpen extends ListView
{
  public boolean a = false;

  public ListViewOpen(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
  }

  public ListViewOpen(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    this.a = true;
    return super.onKeyDown(paramInt, paramKeyEvent);
  }

  public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent)
  {
    this.a = false;
    return super.onKeyUp(paramInt, paramKeyEvent);
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    switch (paramMotionEvent.getAction())
    {
    case 0:
    }
    return super.onTouchEvent(paramMotionEvent);
  }
}