package com.jrdcom.mt.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.Log;



public class MosaicPaintAction {

    private Path path = new Path() ;
    private Paint paint;;
    private float scale;
    private boolean isEarse;
    
    public MosaicPaintAction()
    {
        
    }
    
    public MosaicPaintAction(Path ph,Paint pt,float scale,boolean isbool)
    {
        path.addPath(ph);
        paint = pt;
        paint.setAlpha(255);
        this.scale = scale;
        this.isEarse = isbool;
    }
    public void setShader(Shader shader)
    {
        this.paint.setShader(shader);
    }
    
    public Path getPath()
    {
        return this.path;
    }
    public Paint getPaint()
    {
        return this.paint;
    }
    public float getScale()
    {
        return this.scale;
    }
    public boolean isPaint()
    {
        return this.isEarse;
    }
    
    public void doPaintAction(Canvas canvas,Paint paint)
    {
        canvas.drawPath(this.path, this.paint);
    }
}
