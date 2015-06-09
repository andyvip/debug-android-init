
package com.jrdcom.timetool.countdown.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.countdown.view.VerticalTextPicker.OnChangedListener;

public class PickerLable extends LinearLayout {

    private TextView lable;
    
    
    public TextView getLable() {
        return lable;
    }

    private String lableContent;
    
    private VerticalTextPicker picker;
  
    public VerticalTextPicker getPicker() {
        return picker;
    }

    public PickerLable(Context context) {
        this(context, null);
    }

    public PickerLable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerLable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.vertical_text_picker_lable_layout, this,true);
        
        TypedArray typedArray = context.getResources().obtainAttributes(attrs, R.styleable.pickerlable);
        lableContent = typedArray.getString(R.styleable.pickerlable_lable);
        typedArray.recycle();
        
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        lable = (TextView) findViewById(R.id.picker_lable);
        picker = (VerticalTextPicker)findViewById(R.id.picker);
        lable.setText(lableContent);
    }

 

}
