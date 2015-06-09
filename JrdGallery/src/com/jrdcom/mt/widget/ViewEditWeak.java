
package com.jrdcom.mt.widget;

import java.lang.ref.WeakReference;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jrdcom.mt.core.ToolBlur;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;
import com.jrdcom.mt.util.MyData;

/**
 * 背景虚化View
 * 
 * @author
 */
public class ViewEditWeak extends View {
    public static final int SRC_RADIUS = 40;

    private final int ACTION_ANIMATION = 1;//动画消息  pr 544524 from chen.xiang@tct-nj.com
    private final int MIN_INNER_RADIUS = 1;// 最小内圈大小
    public int m_nType; //
    public Bitmap bmpBack = null;
    private int nPosX = 0;// 图片的偏移
    private int nPosY = 0;
    public int nSrcPosX = 0;// 初始偏移
    public int nSrcPosY = 0;// 初始偏移
    public float fSrcScale = 1.0f;
    public int nViewWidth = 0;
    public int nViewHeight = 0;
    public int nCicleAlpha = 255;// 白圈透明度
    // public BackWeak mBackWeak = new BackWeak();

    private int nInRadius = 150;
    private int nOutRadius = 200;
    public float fOutScale = 1.0f;// 外圈是内圈的倍数
    public int nCurPt; // 当前处理到的点的序号
    public Point mPointHit = new Point(-1, -1);
    public Vector<Point> mPt = new Vector<Point>();
    public WeakReference<Context> mContext = null;
    public boolean isLoadOver = false;// 是否初始化完毕
    public boolean isLoadBitmap = false;
    public boolean isMove = false;
    public int nWeakKind = 0;// =0 点，=1 4条线形

    public int nArrX[] = new int[8];
    public int nArrY[] = new int[8];
    public int nArrDrawX[] = new int[8];// 绘制时的点
    public int nArrDrawY[] = new int[8];// 绘制时的点
    public float nLineAngle = 0; // 斜线的角度 0 ~PI
    public boolean isProcessing = false;// 是否正在处理图像叠加

    private int nOriginal = 0;
    private int nOriginalInnerRadius = 0;// 双点调整内径时的初始内径

    private Point m_ptHitFirst = new Point();
    private Point m_ptHitLast = new Point();
    private int m_nFirstSize = 0;
    private boolean isDown = false;
    private boolean isOneDown = false;

    private float fOrignalAngle = 0;// 鼠标点下时的倾角
    private float fOrignalRealAngle = 0;// 鼠标点下时原始的角度
    private boolean isMultiMoving;
    private boolean isMultiDown = false;
    private Listener mListener;
    private ToolBlur m_tool;
    private boolean isAlphaing = false;
    private  int alphaValue = 0;
    //yaogang.hao for PR 547506
    private boolean isReakOver = false;
    private boolean isSaving = false;

    public interface Listener {
        void onSizeChanged(int size);
    }

    public ViewEditWeak(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = new WeakReference<Context>(context);
        m_tool = new ToolBlur();
        m_tool.init(MyData.getJNI());
    }

    private boolean loadBitmap() {
        try {
            new MtprogressDialog(mContext.get()) {
                @Override
                public void process() {
                    // TODO Auto-generated method stub
                    // mBackWeak.setType(0);
                    isLoadOver = true;
                }
            }.show();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public boolean setType(int kind) {
        nWeakKind = kind;
        // mBackWeak.setType(kind);
        return true;
    }
    
    public int getType() {
        return nWeakKind;
    }

    public boolean setPic() {
        try {
            nViewWidth = this.getWidth();
            nViewHeight = this.getHeight();
            if (bmpBack != null && !bmpBack.isRecycled()) {
                bmpBack.recycle();
            }
            bmpBack = m_tool.getShowOralImage();
            int w = bmpBack.getWidth();
            bmpBack = BitmapOperate.FittingWindow(bmpBack,
                    (int) (nViewWidth - 20 * MyData.nDensity),
                    (int) (nViewHeight - 20 * MyData.nDensity), true);
            float scale = 1.0f * w / bmpBack.getWidth();
            // mBackWeak.setPreScale(scale);
            // mBackWeak.setSize(bmpBack.getWidth(), bmpBack.getHeight());

            nSrcPosX = (nViewWidth - bmpBack.getWidth()) / 2;
            nSrcPosY = (nViewHeight - bmpBack.getHeight()) / 2;
            fSrcScale = 1.0f * bmpBack.getWidth() / m_tool.getShowOralImage().getWidth();
            mPointHit.set(bmpBack.getWidth() / 2, bmpBack.getHeight() / 2);
            getDrawLinePiont();
        } catch (Exception e) {
            // TODO: handle exception
            // MTDebug.PrintError(e);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        drawCanvas(canvas);
    }
    long time_mark = System.currentTimeMillis();
    public void drawCanvas(Canvas canvas) {
        try {
            if (nViewWidth <= 0) {
                setPic();
            }
            if (!isLoadOver) {
                loadBitmap();
            }
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            //pr 544524 from chen.xiang@tct-nj.com begin
            if(isAlphaing&&(System.currentTimeMillis() - time_mark > 50))
            {
//            if(isAlphaing){
            	time_mark = System.currentTimeMillis();
                handler.sendEmptyMessageDelayed(ACTION_ANIMATION,400);
            }
            //pr 544524 from chen.xiang@tct-nj.com end
            if (bmpBack != null && !bmpBack.isRecycled()) {
                canvas.drawBitmap(bmpBack, nPosX + nSrcPosX, nPosY + nSrcPosY, null);
            }
            if (mPointHit.x >= 0 && isLoadOver && nCicleAlpha > 0 && nCicleAlpha <= 255 && isDown) {
                Paint paintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintWhite.setStyle(Paint.Style.STROKE);
                paintWhite.setColor(0x9b9b9b + (nCicleAlpha << 24));
                paintWhite.setFilterBitmap(true);
                paintWhite.setAntiAlias(true);
                paintWhite.setAlpha(alphaValue);
                paintWhite.setStrokeWidth((int) (1.5 * MyData.nDensity));
                if (nWeakKind == 0) {
                    canvas.drawCircle(mPointHit.x + nSrcPosX, mPointHit.y + nSrcPosY, nInRadius,
                            paintWhite);
                    canvas.drawCircle(mPointHit.x + nSrcPosX, mPointHit.y + nSrcPosY, nOutRadius,
                            paintWhite);
                    m_tool.procRadiusDealPic((int)(mPointHit.x/fSrcScale), (int)(mPointHit.y/fSrcScale),
                            (int)(nInRadius/fSrcScale), (int)(nOutRadius/fSrcScale));
                } else if (nWeakKind == 1) {
                    canvas.drawLine(nArrDrawX[0], nArrDrawY[0], nArrDrawX[1], nArrDrawY[1],
                            paintWhite);
                    canvas.drawLine(nArrDrawX[2], nArrDrawY[2], nArrDrawX[3], nArrDrawY[3],
                            paintWhite);
                    canvas.drawLine(nArrDrawX[4], nArrDrawY[4], nArrDrawX[5], nArrDrawY[5],
                            paintWhite);
                    canvas.drawLine(nArrDrawX[6], nArrDrawY[6], nArrDrawX[7], nArrDrawY[7],
                            paintWhite);
                    int x = (nArrDrawX[0]+nArrDrawX[1]+nArrDrawX[2]+nArrDrawX[3])/4;
                    int y = (nArrDrawY[2]+nArrDrawY[3]+nArrDrawY[4]+nArrDrawY[5])/4;
                    m_tool.procLineDealPic((int)((x-nSrcPosX)/fSrcScale + nSrcPosX*Math.sin(nLineAngle)),
                            (int)((y-nSrcPosY)/fSrcScale + nSrcPosX*Math.sin(nLineAngle)),
                            (float) (180 * nLineAngle / Math.PI),
                            (int)(nInRadius/fSrcScale), (int)(nOutRadius/fSrcScale));
                }
                
            }
            
            //yaogang.hao for PR 547506
            if(isAlphaing == false)
            {
            	isReakOver = true;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }
    public void startAnimation() {
        isAlphaing = true;
        //yaogang.hao for PR 547506
        isReakOver = false;
        alphaValue = 255;
        handler.sendEmptyMessageDelayed(ACTION_ANIMATION, 500);//pr 544524 from chen.xiang@tct-nj.com
    }
    
    public void setAlpha()
    {
        isAlphaing = false;
        alphaValue = 255;
    }
    
    public boolean addPoint(int x, int y) {
        try {
            isMove = true;
            mPointHit.x += x;
            mPointHit.y += y;

            if (mPointHit.x < 0) {
                mPointHit.x = 0;
            } else if (mPointHit.x > bmpBack.getWidth()) {
                mPointHit.x = bmpBack.getWidth();
            }
            if (mPointHit.y < 0) {
                mPointHit.y = 0;
            } else if (mPointHit.y > bmpBack.getHeight()) {
                mPointHit.y = bmpBack.getHeight();
            }

            if (nWeakKind == 1) {
                // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                // mPointHit.x, mPointHit.y, nLineAngle,
                // nInRadius, nOutRadius);
                getDrawLinePiont();
            }
            refresh();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public int getInnerRadius() {
        return nInRadius;
    }

    public int getOutRadius() {
        return nOutRadius;
    }

    public boolean isPtInRect(int x, int y) {
        if (nWeakKind == 0 && isPtInCircle(x, y)) {
            return true;
        } else if (nWeakKind == 1 && isPtInLines(x, y)) {
            return true;
        }
        return false;
    }

    // 点是否在图像区域内,更新为检查只要在view区域内
    public boolean isPtInBmp(int x, int y) {
        try {
            if (x >= 0 && x <= nViewWidth && y >= 0 && y <= nViewHeight) {
                return true;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return false;
    }

    public boolean showPic(int x, int y, boolean is_Move, boolean isAddPt) {
        try {
            mPointHit.x += x;
            mPointHit.y += y;

            if (mPointHit.x < 0) {
                mPointHit.x = 0;
            } else if (mPointHit.x > bmpBack.getWidth()) {
                mPointHit.x = bmpBack.getWidth();
            }
            if (mPointHit.y < 0) {
                mPointHit.y = 0;
            } else if (mPointHit.y > bmpBack.getHeight()) {
                mPointHit.y = bmpBack.getHeight();
            }
            if (!is_Move) {// 正在拖动的状态不进行此处理
                reWeak(true);
                isMove = false;
            }
            refresh();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    // 初始化内外径
    public void initRadius(int inner, int outer) {
        try {
            nInRadius = (int) inner;
            nOutRadius = (int) (inner + outer);
            if (0 == nWeakKind) {
                // mBackWeak.setRadius(nInRadius, nOutRadius);
            } else if (1 == nWeakKind) {
                // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                // mPointHit.x, mPointHit.y, nLineAngle,
                // nInRadius, nOutRadius);
                getDrawLinePiont();
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }

    // 设置内半径 0.2~2.0
    public void setInRadius(float val, boolean isMove, boolean isResizeRadius) {
        try {
            if (isProcessing) {
                // MTDebug.Print("MTXX", "_____setInRadius_______isProcessing");
                return;
            }
            isProcessing = true;
            nOutRadius = (int) (nOutRadius - nInRadius + val);
            nInRadius = (int) val;

            if (isResizeRadius) {
                // mBackWeak.setRadius(nInRadius, nOutRadius);
            }
            if (1 == nWeakKind) {
                // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                // mPointHit.x, mPointHit.y, nLineAngle,
                // nInRadius, nOutRadius);
                getDrawLinePiont();
            }

            isProcessing = false;
            if (!isMove) {// 正在拖动的状态不进行此处理
                reWeak(true);
            }
            refresh();

        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }

    // 设置内半径 0.2~2.0
    public void setInRadius(float val, int x, int y, boolean isMove, boolean isResizeRadius) {
        try {
            if (isProcessing) {
                // MTDebug.Print("MTXX",
                // "_____setInRadius2_______isProcessing");
                return;
            }
            isProcessing = true;
            mPointHit.x += x; // 0129
            mPointHit.y += y;

            if (mPointHit.x < 0) {
                mPointHit.x = 0;
            } else if (mPointHit.x > bmpBack.getWidth()) {
                mPointHit.x = bmpBack.getWidth();
            }
            if (mPointHit.y < 0) {
                mPointHit.y = 0;
            } else if (mPointHit.y > bmpBack.getHeight()) {
                mPointHit.y = bmpBack.getHeight();
            }
            nOutRadius = (int) (nOutRadius - nInRadius + val);
            nInRadius = (int) val;

            if (1 == nWeakKind) {
                // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                // mPointHit.x, mPointHit.y, nLineAngle,
                // nInRadius, nOutRadius);
                getDrawLinePiont();
            }
            if (isResizeRadius) {
                // mBackWeak.setRadius(nInRadius, nOutRadius);
            }

            isProcessing = false;
            if (!isMove) {// 正在拖动的状态不进行此处理
                reWeak(true);
            }
            refresh();

        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }

    // 设置外半径 1.0~3.0 + 5
    public void setOutRadius(float val, boolean isMove, boolean isResizeRadius) {
        try {
            if (isProcessing) {
                // MTDebug.Print("MTXX",
                // "_____setOutRadius_______isProcessing");
                return;
            }
            isProcessing = true;

            nOutRadius = (int) (nInRadius + val);
            if (1 == nWeakKind) {
                // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                // mPointHit.x, mPointHit.y, nLineAngle,
                // nInRadius, nOutRadius);
                getDrawLinePiont();
            }
            if (isResizeRadius) {
                // mBackWeak.setRadius(nInRadius, nOutRadius);
            }

            isProcessing = false;
            if (!isMove) {// 正在拖动的状态不进行此处理
                reWeak(true);
            }
            refresh();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }

    // 计算倾斜角度
    public float getAngle(int x1, int y1, int x2, int y2) {
        float angle = 0;
        try {
            int pix = 5;// 忽略临近差异
            int w = (x1 - x2) / pix;

            int h = (y2 - y1) / pix;
            if (w == 0) {
                angle = (float) (Math.PI / 2);
            } else {
                angle = (float) Math.atan((1.0f * h / w));
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return angle;
    }

    // 调整线条的角度
    public boolean resizeAngle(int x1, int y1, int x2, int y2, float orignalAngle,
            float orignalRealAngle) {
        try {
            int pix = 5;// 忽略临近差异
            int w = (x1 - x2) / pix;
            int h = (y2 - y1) / pix;

            if (w == 0) {
                nLineAngle = orignalRealAngle + ((float) Math.PI / 2) - orignalAngle;
            } else {
                nLineAngle = orignalRealAngle + ((float) Math.atan((1.0f * h / w))) - orignalAngle;
            }
            if (nLineAngle < 0) {
                nLineAngle += Math.PI;
            } else if (nLineAngle > Math.PI) {
                nLineAngle -= Math.PI;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public boolean reSizeRadius() {
        try {
            // mBackWeak.setRadius(nInRadius, nOutRadius);
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public boolean reWeak(boolean isRefresh) {
        try {
            // MTDebug.memeryUsed("MTXX", "___reWeak");
            // MTDebug.Print("MTXX", "_____reWeak  isRefresh="+isRefresh);
            if (isProcessing) {
                // MTDebug.Print("MTXX", "_____reWeak_______isProcessing");
                return false;
            }
            isMove = false;
            isProcessing = true;
            if (isRefresh) {
                if (0 == nWeakKind) {
                    // mBackWeak.getWeak(mPointHit.x, mPointHit.y);
                    Bitmap bmpTemp = m_tool.getShowProcImage();
                    bmpTemp = BitmapOperate.FittingWindow(bmpTemp,
                            (int) (nViewWidth - 20 * MyData.nDensity),
                            (int) (nViewHeight - 20 * MyData.nDensity), true);

                    if (bmpBack != null && !bmpBack.isRecycled()) {
                        bmpBack.recycle();
                    }
                    bmpBack = bmpTemp;
                } else if (1 == nWeakKind) {
                    // mBackWeak.getLinePoint(nArrX, nArrY, nSrcPosX, nSrcPosY,
                    // mPointHit.x, mPointHit.y, nLineAngle,
                    // nInRadius, nOutRadius);
                    getDrawLinePiont();

                    // mBackWeak.getLineWeak(nArrX, nArrY, nLineAngle);

                    Bitmap bmpTemp = m_tool.getShowProcImage();
                    bmpTemp = BitmapOperate.FittingWindow(bmpTemp,
                            (int) (nViewWidth - 20 * MyData.nDensity),
                            (int) (nViewHeight - 20 * MyData.nDensity), true);
                    if (bmpBack != null && !bmpBack.isRecycled()) {
                        bmpBack.recycle();
                    }
                    bmpBack = bmpTemp;
                    // MTDebug.memeryUsed("MTXX", "reweak");
                }
            }
            // MTDebug.Print("MTXX", "_____reWeak_______end");
            // MTDebug.memeryUsed("MTXX", "___reWeak________2");
            isProcessing = false;
        } catch (Exception e) {
            isProcessing = false;
            // MTDebug.PrintError(e);
        }
        return true;
    }

    // 是否正在生成处理后的结果
    public boolean isInProcessing() {
        return isProcessing;
    }

    // 获取缩略图的宽和高的最小值
    public int getMinSize() {
        // 返回对角线长度
        int n = 0;
        try {
            if (bmpBack == null || bmpBack.isRecycled()) {
                return 0;
            }
            n = (int) Math.sqrt(bmpBack.getWidth() * bmpBack.getWidth() + bmpBack.getHeight()
                    * bmpBack.getHeight());
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return n;
    }

    // 点是否在圆形区域内
    private boolean isPtInCircle(int x, int y) {
        try {
            x -= nSrcPosX;
            y -= nSrcPosY;
            int rn = (mPointHit.x - x) * (mPointHit.x - x) + (mPointHit.y - y) * (mPointHit.y - y);
            if (rn > nOutRadius * nOutRadius) {
                return false;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    // 点是否在线形区域内
    private boolean isPtInLines(int x, int y) {
        try {
            x -= nSrcPosX;
            y -= nSrcPosY;
            if (nArrDrawX[1] == nArrDrawX[0] || nArrDrawX[7] == nArrDrawX[6]) {// 竖直线
                if (x >= nArrDrawY[7] && x <= nArrDrawY[1] && nArrDrawX[1] > nArrDrawY[7]) {
                    return true;
                } else if (x >= nArrDrawY[1] && x <= nArrDrawY[7] && nArrDrawX[1] < nArrDrawY[7]) {
                    return true;
                }
                return false;
            } else {// 其它
                int y1 = nArrDrawY[0] + (x - nArrDrawX[0]) * (nArrDrawY[1] - nArrDrawY[0])
                        / (nArrDrawX[1] - nArrDrawX[0]);
                int y2 = nArrDrawY[6] + (x - nArrDrawX[6]) * (nArrDrawY[7] - nArrDrawY[6])
                        / (nArrDrawX[7] - nArrDrawX[6]);

                if (y >= y2 && y <= y1) {
                    return true;
                } else if (y >= y1 && y <= y2) {
                    return true;
                }
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return false;
    }

    public boolean Release() {
        try {
            if (bmpBack != null && !bmpBack.isRecycled()) {
                bmpBack.recycle();
                bmpBack = null;
            }
            // mBackWeak.Release();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    // 获取绘制时的交点
    public boolean getDrawLinePiont() {
        try {
            float angle = nLineAngle;
            int x = mPointHit.x + nSrcPosX;
            int y = mPointHit.y + nSrcPosY;
            int InRadius = nInRadius;
            int OutRadius = nOutRadius;
            nInRadius = InRadius;
            nOutRadius = OutRadius;
            int width = nViewWidth;
            int height = nViewHeight;
            int i;
            int mpX[] = new int[4];// 点到线的垂直交点
            int mpY[] = new int[4];// 点到线的垂直交点
            mpX[0] = (int) (x - OutRadius * Math.sin(angle));
            mpY[0] = (int) (y - OutRadius * Math.cos(angle));
            mpX[1] = (int) (x - InRadius * Math.sin(angle));
            mpY[1] = (int) (y - InRadius * Math.cos(angle));
            mpX[2] = (int) (x + InRadius * Math.sin(angle));
            mpY[2] = (int) (y + InRadius * Math.cos(angle));
            mpX[3] = (int) (x + OutRadius * Math.sin(angle));
            mpY[3] = (int) (y + OutRadius * Math.cos(angle));

            while (angle < 0) {
                angle += Math.PI;
            }
            while (angle > Math.PI) {
                angle -= Math.PI;
            }
            if (angle <= Math.PI / 2) {
                double tanA = Math.tan(angle);
                for (i = 0; i < 4; i++) {
                    // 左边的点
                    if (mpX[i] * tanA <= height - mpY[i]) {
                        nArrDrawX[i * 2] = 0;
                        nArrDrawY[i * 2] = (int) (mpX[i] * tanA + mpY[i]);
                    } else {
                        nArrDrawX[i * 2] = (int) (mpX[i] - (height - mpY[i]) / tanA);
                        nArrDrawY[i * 2] = height;
                    }
                    // 右边的点
                    if ((width - mpX[i]) * tanA <= mpY[i]) {
                        nArrDrawX[i * 2 + 1] = width;
                        nArrDrawY[i * 2 + 1] = (int) (mpY[i] - (width - mpX[i]) * tanA);
                    } else {
                        nArrDrawX[i * 2 + 1] = (int) (mpX[i] + mpY[i] / tanA);
                        nArrDrawY[i * 2 + 1] = 0;
                    }
                }
            } else if (angle <= Math.PI) {
                double tanA = -1 * Math.tan(angle);
                for (i = 0; i < 4; i++) {
                    // 左边的点
                    if (mpX[i] * tanA <= mpY[i]) {
                        nArrDrawX[6 - i * 2] = 0;
                        nArrDrawY[6 - i * 2] = (int) (mpY[i] - mpX[i] * tanA);
                    } else {
                        nArrDrawX[6 - i * 2] = (int) (mpX[i] - mpY[i] / tanA);
                        nArrDrawY[6 - i * 2] = 0;
                    }
                    // 右边的点
                    if ((width - mpX[i]) * tanA <= height - mpY[i]) {
                        nArrDrawX[8 - i * 2 - 1] = width;
                        nArrDrawY[8 - i * 2 - 1] = (int) (mpY[i] + (width - mpX[i]) * tanA);
                    } else {
                        nArrDrawX[8 - i * 2 - 1] = (int) (mpX[i] + (height - mpY[i]) / tanA);
                        nArrDrawY[8 - i * 2 - 1] = height;
                    }
                }
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (!isLoadOver) {// 未处理完，不响应
                return super.onTouchEvent(event);
            }
            if (isInProcessing()) {
                return super.onTouchEvent(event);
            }
            // int action = event.getAction();
            // int pointerCount = VerifyManager.getPointerCount(event);
            int pointerCount = event.getPointerCount();
            int x, y, x1, y1, x2, y2, n;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:// 如果为单点触控，则认为是拖拽模式
                    // MTDebug.Print("MTXX", "ACTION_DOWN");
                    isAlphaing = false;
                    alphaValue = 255;
                    if (!isMultiDown) {
                        x = (int) event.getX();
                        y = (int) event.getY();

                        isOneDown = true;
                        if (isPtInRect(x, y)) {
                            isDown = true;
                            nCicleAlpha = 255;
                            m_ptHitFirst.set(x, y);
                            refresh();
                        } else {
                            nCicleAlpha = 255;
                            refresh();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:// 单点触控Up事件
                    // MTDebug.Print("MTXX", "ACTION_UP isDown="+isDown);
                    x = (int) event.getX();
                    y = (int) event.getY();
                    if (isDown && !isMultiDown) {
                        if (isPtInBmp(x, y)) {
                            showPic(x - m_ptHitFirst.x, y - m_ptHitFirst.y, false, true);
                        } else {
                            showPic(x - m_ptHitFirst.x, y - m_ptHitFirst.y, false, false);
                        }
                        nCicleAlpha = 255;
                        // ((ActivityStyleEmptiness)
                        // mContext.get()).startReweak(true);
                        isDown = false;
                    } else if (nCicleAlpha == 255 && !isMultiDown) {
                        // ((ActivityStyleEmptiness)
                        // mContext.get()).startReweak(false);
                    }
                    isDown = false;
                    isOneDown = false;
                    isMultiDown = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // MTDebug.Print("MTXX",
                    // "ACTION_MOVE isDown="+isDown+" pointerCount="+pointerCount);
                    if (isDown && 1 == pointerCount && !isMultiDown) {
                        nCicleAlpha = 255;// 0129
                        x = (int) event.getX();
                        y = (int) event.getY();
                        if (isPtInBmp(x, y)) {
                            addPoint(x - m_ptHitFirst.x, y - m_ptHitFirst.y);
                            m_ptHitFirst.set(x, y);
                        }
                    } else if (isMultiDown && 2 == pointerCount) {
                        if (isMultiMoving) {
                            return true;
                        }
                        isMultiMoving = true;
                        // x1 = (int) VerifyMothod.getX(event, 0);
                        // y1 = (int) VerifyMothod.getY(event, 0);
                        // x2 = (int) VerifyMothod.getX(event, 1);
                        // y2 = (int) VerifyMothod.getY(event, 1);
                        x1 = (int) event.getX(0);
                        y1 = (int) event.getY(0);
                        x2 = (int) event.getX(1);
                        y2 = (int) event.getY(1);
                        n = (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                        int newX = (x1 + x2) / 2;
                        int newY = (y1 + y2) / 2;

                        int dx = (x1 + x2) / 2 - m_ptHitFirst.x;
                        int dy = (y1 + y2) / 2 - m_ptHitFirst.y;
                        float scale = 1.0f * (1.0f + 1.0f * (n - nOriginal) / 200);
                        dx = (int) (dx * scale / 2);
                        dy = (int) (dy * scale / 2);
                        m_nFirstSize = nOriginalInnerRadius + (n - nOriginal) / 2;

                        if (nWeakKind == 0) {
                            if (m_nFirstSize < MIN_INNER_RADIUS) {
                                m_nFirstSize = MIN_INNER_RADIUS;
                            }else if (m_nFirstSize>360) {
                                m_nFirstSize = 360;
                            }
                            // else if (m_nFirstSize > ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.getMax()
                            // + MIN_INNER_RADIUS) {
                            // m_nFirstSize = ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.getMax()
                            // + MIN_INNER_RADIUS;
                            // }
                            // ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.setProgress(m_nFirstSize
                            // - MIN_INNER_RADIUS);
                        } else if (nWeakKind == 1) {
                            if (m_nFirstSize < MIN_INNER_RADIUS) {
                                m_nFirstSize = MIN_INNER_RADIUS;
                            }
                            // else if (m_nFirstSize > ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.getMax()
                            // + MIN_INNER_RADIUS) {
                            // m_nFirstSize = ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.getMax()
                            // + MIN_INNER_RADIUS;
                            // }
                            // ((ActivityStyleEmptiness)
                            // mContext.get()).emptinessSize.setProgress(m_nFirstSize
                            // - MIN_INNER_RADIUS);
                            resizeAngle(x1, y1, x2, y2, fOrignalAngle, fOrignalRealAngle);
                        }
                        setInRadius(m_nFirstSize, newX - m_ptHitLast.x, newY - m_ptHitLast.y, true,
                                false);
                        mListener.onSizeChanged(m_nFirstSize);
                        m_ptHitLast.set(newX, newY);
                        isMultiMoving = false;
                        isOneDown = false;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:// 如果为多点触控，则认为是缩放模式
                    // MTDebug.Print("MTXX", "ACTION_POINTER_DOWN");
                    // x1 = (int) VerifyMothod.getX(event, 0);
                    // y1 = (int) VerifyMothod.getY(event, 0);
                    // x2 = (int) VerifyMothod.getX(event, 1);
                    // y2 = (int) VerifyMothod.getY(event, 1);
                    isAlphaing = false;
                    alphaValue = 255;
                    x1 = (int) event.getX(0);
                    y1 = (int) event.getY(0);
                    x2 = (int) event.getX(1);
                    y2 = (int) event.getY(1);
                    if (!isMultiDown && isPtInRect(x1, y1) || isPtInRect(x2, y2)) {
                        n = (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                        if (nWeakKind == 1) {
                            fOrignalAngle = getAngle(x1, y1, x2, y2);
                            fOrignalRealAngle = nLineAngle;
                        }
                        nOriginal = n;
                        isMultiDown = true;
                        nOriginalInnerRadius = getInnerRadius();
                        m_ptHitFirst.x = (x1 + x2) / 2;
                        m_ptHitFirst.y = (y1 + y2) / 2;
                        m_ptHitLast.set(m_ptHitFirst.x, m_ptHitFirst.y);
                        // m_nFirstSize = ((ActivityStyleEmptiness)
                        // mContext.get()).emptinessRange.getProgress()
                        // + MIN_INNER_RADIUS;
                        showPic(0, 0, true, true);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:// 多点触控Up事件
                    // MTDebug.Print("MTXX",
                    // "ACTION_POINTER_UP isMultiDown="+isMultiDown);
                    nOriginal = 0;
                    if (isMultiDown) {
                        nCicleAlpha = 255;
                        // MTDebug.Print("MTXX",
                        // "ACTION_UP___2222__isOneDown="+isOneDown);

                        // ((ActivityStyleEmptiness)
                        // mContext.get()).startReweak(isMultiDown);
                    }
                    showPic(0, 0, false, true);
                    isMultiDown = false;
                    isDown = false;
                    break;
            }

            // MTDebug.Print("MTXX", "ACTION__________________________end");
        } catch (Exception e) {
            isMultiMoving = false;
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public void setOnSizeChangeLisenter(Listener listener) {
        mListener = listener;
    }
    
    public void isDown(boolean down) {
        isDown = down;
    }
    
    public void onSave() {
    
    	isSaving = true;
        m_tool.ok();
        MyData.getBeautyControl().pushImage();
        isSaving = false;
    }

    
    public void onCancel() {
    	isAlphaing = false;//pr 544524 from chen.xiang@tct-nj.com
    	handler.removeMessages(ACTION_ANIMATION);//pr 544524 from chen.xiang@tct-nj.com
        m_tool.cancel();
    }
    /**
     * 防止刷新时变黑
     */
    public void refresh() {
        if (bmpBack != null && !bmpBack.isRecycled()) {
            invalidate();
        }
    }
    
   private Handler handler = new Handler(){
       @Override
    public void handleMessage(Message msg) {
    	   if(msg.what == ACTION_ANIMATION){//pr 544524 from chen.xiang@tct-nj.com
    		   alphaValue -= 50;
               if(alphaValue < 0)
               {
                   alphaValue = 0;
                   isAlphaing = false;
               }
               reWeak(true);
               invalidate();
    	   }
    }
   };
   
 //yaogang.hao for PR 547506
   
   public boolean isAnimation()
   {
       return isReakOver;
   }
   public boolean isSaving()
   {
       return isSaving;
   }
}
