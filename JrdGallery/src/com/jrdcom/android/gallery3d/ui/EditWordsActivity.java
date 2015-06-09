
package com.jrdcom.android.gallery3d.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.jrdcom.mt.MTActivity;
import android.text.*;

public class EditWordsActivity extends MTActivity implements OnClickListener, TextWatcher {
    private EditText mEditWordsText;
    private TextView mTextView;
    private int mCount = 0;
    private String editstring="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_words);
        findview();
        if(getIntent().getExtras() != null)
        {
        editstring = getIntent().getStringExtra("words");
        mEditWordsText.setText(editstring);
        }
    }

    private void findview() {
        mEditWordsText = (EditText) findViewById(R.id.edit_text);
        mTextView = (TextView) findViewById(R.id.edit_text_length);
        mEditWordsText.addTextChangedListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.text_bubble_add_content_title);
    }

    @Override
    public void onClick(View v) {
        isClicked = true; // added by jipu.xiong@tcl.com
        switch (v.getId()) {
            case R.id.btn_ok:
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
                if (!Utils.updateCacheDirEditPicture()) {
                    Utils.showToast(this, R.string.storage_full_tag);
                }
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
                String string = mEditWordsText.getText().toString();
                if ("".equals(string.trim()))
                {
//                    Toast.makeText(EditWordsActivity.this, "please input characters",
//                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("words", string);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.btn_cancel:
                finish();
                break;

            default:
                break;
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //yaogang.hao
//        setEditable(mEditWordsText, true);
//        mCount = start + count;

        mTextView.setText(s.length() + "/" + "140");
        if (s.length() > 140)
        {
//            setEditable(mEditWordsText, false);
            handlerWordCount(s);
        }
    }

    //yaognag.hao
    public void handlerWordCount(CharSequence s)
    {
        String temp = s.toString().substring(0,140);
        mEditWordsText.setText(temp);
        
        //set the selection position
        CharSequence text = mEditWordsText.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable)text;
            Selection.setSelection(spanText,text.length());
        }
    }
    @Override
    public void afterTextChanged(Editable s) {

    }

    private void setEditable(EditText mEdit, boolean value) {
        if (value) {
            mEdit.setCursorVisible(true);
            mEdit.setFocusableInTouchMode(true);
            mEdit.requestFocus();
        } else {
            mEdit.setFilters(new InputFilter[] {
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end,
                                Spanned dest, int dstart, int dend) {
                            return source.length() < 140 ? dest.subSequence(dstart, dend) : dest.subSequence(dstart, 140);
                        }
                    }
            });
            mEdit.setCursorVisible(true);
            mEdit.setFocusableInTouchMode(true);
            mEdit.requestFocus();
        }
    }

}
