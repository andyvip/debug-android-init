package com.jrdcom.example.joinpic;
  
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.jrdcom.example.layout.TemplateLayoutItem;
import com.jrdcom.example.layout.TemplateTool;
  

import static com.jrdcom.example.joinpic.ActivityTemplate.PADDING_FRAME;
import static com.jrdcom.example.joinpic.ActivityTemplate.PADDING_TOP;  
  
public class TemplateViewGroup extends ViewGroup {  
    private int x = 0;
    private int y = 0;
    private int mSelectView = -1;
    private Bitmap bitmap = null;
    
    private int mkey;
    private int mNum;
    
    private TemplatePuzzleModel model;
    private ArrayList<Bitmap> m_vecTexture = null;
    private Path mPath;
    private int index_l = 0;
    private int index_t = 0;
    private int index_r = 0;
    private int index_b = 0;
    
    private OnMeasureCompleteListener onMeasureCompleteListener;
    private boolean isFirstDraw = false;
    private boolean isFirstMeasure = false;
    private int mWholeHeight;
    private int mWholeWidth;
    private int displayHeight;
    private int MARGIN_TOP;
    public TemplateViewGroup(Context context,AttributeSet attrs) {
        super(context,attrs);  
        bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
        setWillNotDraw(false);
    }
    
    public void setOnMeasureCompleteListener(OnMeasureCompleteListener listener)
    {
    	this.onMeasureCompleteListener = listener;
    }
    public void setKey(int key){
        mkey = key;
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //added by yaogang.hao optimize loading speed
//        if(!isFirstDraw)
        {
            isFirstDraw = true;
            MARGIN_TOP = (mWholeHeight-displayHeight)/2;
        }
        model = ActivityTemplate.getModel();
        for (int i = getChildCount()-1; i >= 0; i--) {
            TemplateItemView v = (TemplateItemView)getChildAt(i);
          //yaogang.hao for PR533196
            if(model.getTemplateLayout().size() <= v.getKey())
            {
                break;
            }
            //yaogang.hao end
            TemplateLayoutItem lLayoutItem =model.getTemplateLayout().getItem(v.getKey());
            mPath = new Path(lLayoutItem.getPath());
            v.layout((int) (lLayoutItem.getX()*TemplateTool.mPuzzleScale +PADDING_FRAME),
                    (int) (lLayoutItem.getY()*TemplateTool.mPuzzleScaleY +PADDING_TOP+MARGIN_TOP),
                    (int) ((lLayoutItem.getWidth() + lLayoutItem.getX())*TemplateTool.mPuzzleScale +PADDING_FRAME),
                    (int) ((lLayoutItem.getHeight() + lLayoutItem.getY())*TemplateTool.mPuzzleScaleY+PADDING_TOP+MARGIN_TOP));
        }
    }
    
    public void setPuzzleTexture(ArrayList<Bitmap> list)
    {
        m_vecTexture=list;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int lCurrentHeight=0;
        canvas.save();
        if(m_vecTexture!=null)
        {
            Bitmap pLeftBitmap = m_vecTexture.get(3);
            if (pLeftBitmap != null) {
                canvas.drawBitmap(pLeftBitmap,0,MARGIN_TOP, null);
            }
            Bitmap pTopBitmap = m_vecTexture.get(1);
            if (pTopBitmap != null) {
                canvas.drawBitmap(pTopBitmap,0,MARGIN_TOP, null);
            }
            Bitmap pRightBitmap = m_vecTexture.get(4);
            if (pRightBitmap != null) {
                canvas.drawBitmap(pRightBitmap, pTopBitmap.getWidth()
                        - pRightBitmap.getWidth(), MARGIN_TOP, null);
            }
            
            Bitmap pMiddleBitmap = m_vecTexture.get(0);
            Paint mpaint = new Paint();
            mpaint.setShader(new BitmapShader(pMiddleBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
            mpaint.setStyle(Paint.Style.FILL);
            if (pMiddleBitmap != null) {
                canvas.drawRect(pLeftBitmap.getWidth(), pTopBitmap.getHeight()+MARGIN_TOP, mWholeWidth-pRightBitmap.getWidth(),MARGIN_TOP+pRightBitmap.getHeight()-10, mpaint);
            }
            lCurrentHeight=pLeftBitmap.getHeight()+MARGIN_TOP+pTopBitmap.getHeight();
            
            for(int i=0;i<(m_vecTexture.size()-5)/2;i++)
             {
                Bitmap lTempBitmap = m_vecTexture.get(i*2+5);
                if (pLeftBitmap != null) {
                    canvas.drawBitmap(lTempBitmap, 0, lCurrentHeight, null);
                }
                lTempBitmap=m_vecTexture.get(i*2+5+1);
                if (pLeftBitmap != null) {
                    canvas.drawBitmap(lTempBitmap,  pTopBitmap.getWidth()
                            - pRightBitmap.getWidth(), lCurrentHeight, null);
                }
             }
            
            Bitmap pBottomBitmap = m_vecTexture.get(2);
            if (pBottomBitmap != null) {
//                canvas.drawBitmap(pBottomBitmap, 0, pLeftBitmap.getHeight()+MARGIN_TOP+pTopBitmap.getHeight()
//                        - pBottomBitmap.getHeight(), null);
            	canvas.drawBitmap(pBottomBitmap, 0, pLeftBitmap.getHeight()+MARGIN_TOP
                        - pBottomBitmap.getHeight(), null);
            }
        }
        canvas.restore();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mWholeHeight = heightSize;
        mWholeWidth = widthSize;
        if(!isFirstMeasure)
        {
            isFirstMeasure = true;
            if(null != this.onMeasureCompleteListener)
            {
                this.onMeasureCompleteListener.onMeasureComplete(widthSize, heightSize);
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) { 
//        if (mSelectView != -1) {  
//            View v = getChildAt(mSelectView);  
//            if (v != null)  
//                v.layout(x, y, x + 200, y + 300);
//            return;  
//        }  
//        x=l;
//        y=t;
//        View[] views = new View[mkey];
//        switch (mkey) {
//            case 2:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 3:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 4:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 5:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 6:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 7:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 8:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            case 9:
//            	for (int i = 0; i < mkey; i++) {
//                    views[i] = getChildAt(i);
//                    if(views[i] != null){
//                        views[i].layout(x+ActivityTemplate.startX[mNum][i], y+ActivityTemplate.startY[mNum][i], x+ActivityTemplate.endX[mNum][i], y+ActivityTemplate.endY[mNum][i]);
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }  
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    
    public void setNum(int num){
        mNum = num;
    }
    
    public void setDisplayHeight(int height)
    {
        this.displayHeight = height;
    }
    
    public interface OnMeasureCompleteListener
    {
        public void onMeasureComplete(int width,int height);
    }

}  