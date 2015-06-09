package com.mt.mtxx.image;
/**
 * @author yaogang.hao
 * 
 *  To draw the String on the canvas dynamically.
 *  To calculate the font  metrics and font scale dynamically.
 *  To adjust the all string 's layout dynamically.
 */
import android.graphics.*;
import android.util.Log;
import  android.graphics.Paint.*;
public class WordsPlace {
    
    public static final int SHADOW_DISTANCE = 1;
    private String mWords;
    private Rect rect = new Rect(0, 0, 0, 0);
    private Canvas mCanvas;
    private Paint mpaint;
    private float mscale = 1.0f;
    
    public WordsPlace(String words, int left, int top, int right,
            int bottom, Canvas canvas) {
        this.mWords = words;
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
        mCanvas = canvas;
    }

    public void initDefaultValues(Paint paint,float scale) {
        mpaint = paint;
        mscale = scale;
    }

    public void drawWords(int flag) {
            
            //set the center flag
            mpaint.setTextAlign(Align.CENTER);
            //calculate the font height 
            FontMetrics fontMetrics = mpaint.getFontMetrics(); 
            float fontHeight = fontMetrics.bottom - fontMetrics.top; 
            int linenumbers = getLineNums();
            float totalFontHeight = fontHeight*linenumbers + 10*(linenumbers-1);
            // to calculate the suitable size of font
            if((rect.height()-totalFontHeight) < 0)
            {
                //
                mscale = mscale * ((rect.height())/(totalFontHeight-10*(linenumbers-1)));
                mpaint.setTextSize((float)(25.0f)*mscale);
                fontMetrics = mpaint.getFontMetrics(); 
                fontHeight = fontMetrics.bottom - fontMetrics.top;
                linenumbers = getLineNums();
                totalFontHeight = fontHeight*linenumbers + 10*(linenumbers-1);
            }
            //calculate the font width
            char[] strchar = mWords.toCharArray();
            float [] widths = new float[mWords.length()];
            mpaint.getTextWidths(mWords, 0, mWords.length(), widths);
            //to draw string one by one dynamically.
            float sum = 0;
            int rowsnum = 1;
            int rowsBegin = 0;
            boolean isCanDrawing = false;
            
            for(int i = 0 ;i< mWords.length();i++){
                sum += widths[i];
                if(sum > rect.width()){
                    isCanDrawing = true;
                    sum = 0;
                }
                // reach to the end of string or it's time to draw a newline.
                if(isCanDrawing || i == mWords.length()-1)
                {
                    float x = rect.left+8+rect.width()/2;
                    float textBaseY = rect.top+(rect.height()-totalFontHeight)/2+ fontHeight*(rowsnum-1)+30;
                    String tmp;
                    if(i == mWords.length()-1)
                         tmp = mWords.substring(rowsBegin,i+1);
                    else
                         tmp = mWords.substring(rowsBegin,i);
                    mCanvas.drawText(tmp, x, textBaseY, mpaint);
                    rowsBegin = i+1;
                    rowsnum++;
                    isCanDrawing = false;
                }
            }
    }
    
    /**
     * To get the number lines of String
     */
    public int getLineNums()
    {
        int rowsnum = 1;
        float sum = 0;
        float [] widths = new float[mWords.length()];
        mpaint.getTextWidths(mWords, 0, mWords.length(), widths);
        for(int i = 0 ;i< mWords.length();i++){
            sum += widths[i];
            if(sum > rect.width()){
                rowsnum++;
                sum = 0;
            }
        }
        
        return rowsnum;
    }
}
