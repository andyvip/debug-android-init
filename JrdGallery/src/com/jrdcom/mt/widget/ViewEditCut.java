
package com.jrdcom.mt.widget;

import android.content.Context;
import android.graphics.*;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.jrdcom.mt.core.ToolCut;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.android.gallery3d.R;

//PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start 
import android.util.Log;
//PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end 

//PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start
import android.widget.Toast;
//PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 end


public class ViewEditCut extends View {
    public int MIN_WIDTH = (int) (8 * MyData.nDensity);
    public int MIN_FINAL_WIDTH = (int) (80 * MyData.nDensity);
    public final int FINAL_MIN_WIDTH = 5;
    public int nPosX = 0;// 图片的偏移
    public int nPosY = 0;

    public int nWidth;// 屏幕宽度
    public int nHeight;// 屏幕高度
    public Rect rectSelect = new Rect();// 裁剪区域
    public Rect rectSelectTotal = new Rect();// 相对于原始图片的裁剪区域
    public Rect rectSlider[] = new Rect[4];// 拖动的直角
    public int nLineWidth;
    public int nSliderW;
    public int nSliderH;
    public Bitmap bmpBack;
    private int mSrcWidth;
    private int mSrcHeight;
    public int nViewWidth = 0;
    public int nViewHeight = 0;
    public int nSrcPosX = 0;// 初始偏移
    public int nSrcPosY = 0;// 初始偏移
    public float fScale = 1.0f; // 缩放倍数
    public float fSrcScale = 1.0f;
    public float fCurScale = 1.0f;// 裁剪引起的比例变化

    public Bitmap bmpDot;
    public Bitmap bmpDotSelect;
    public int nCurPressedDot = -1;// 当前选中的点
    public int nType = 0;// =1 1:1,=2 3:2,=3 4:3,=4 2:3,=5 3:4,=6 16:9,=7 ?:?
    public boolean mIsNeedSave;// 是否有裁剪需要保存
    public boolean mIsPreCut = true;// 是否预裁剪
    private boolean isOutoffMin = false;// 是否小于最小的限定宽高
    private boolean isOkCut = false;

    private ToolCut m_tool;

    //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start
    public boolean mIsNotFit = false;
    Context mContext;
    //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 end


    public ViewEditCut(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;    //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10
        nWidth = MyData.nScreenW;
        nHeight = MyData.nScreenH;

        mIsNeedSave = false;// 初始需要保存
        nLineWidth = 4;

        bmpDot = BitmapFactory.decodeResource(getResources(), R.drawable.img_cut_dot);
        nSliderW = bmpDot.getWidth();
        nSliderH = bmpDot.getHeight();

        bmpDotSelect = BitmapFactory.decodeResource(getResources(), R.drawable.img_cut_dot);

        rectSlider[0] = new Rect();
        rectSlider[1] = new Rect();
        rectSlider[2] = new Rect();
        rectSlider[3] = new Rect();
        nType = 7;// 默认 ？：？

        m_tool = new ToolCut();
        // m_tool.init( MyData.mOptMiddle.getJNI() );
        m_tool.init(MyData.getJNI());
    }

    //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start
    Toast mNotSupportToast = null;
    //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start

    public boolean setPic() {
        try {
            nType = 0;
            fCurScale = 1.0f;
            nViewWidth = this.getWidth();
            nViewHeight = this.getHeight();

            if (bmpBack != null && !bmpBack.isRecycled()) {
                bmpBack.recycle();
                bmpBack = null;
            }
            // if(isOkCut){
            // bmpBack = m_tool.getShowProcImage();
            // }else {
            bmpBack = m_tool.getShowOralImage();
            // }
            int w = bmpBack.getWidth();
            bmpBack = BitmapOperate.FittingWindow(bmpBack, (int) (nViewWidth), (int) (nViewHeight),
                    true);
            float scale = 1.0f * w / bmpBack.getWidth();
            fScale = 1.0f * m_tool.getRealImageSize()[0] / bmpBack.getWidth();

            if (bmpBack.getWidth() * fScale < MIN_FINAL_WIDTH * MyData.fScaleCut * scale
                    || bmpBack.getHeight() * fScale < MIN_FINAL_WIDTH * MyData.fScaleCut * scale) {
                isOutoffMin = true;
            } else {
                isOutoffMin = false;
            }
            // m_tool.setScale(scale);
            mSrcWidth = bmpBack.getWidth();
            mSrcHeight = bmpBack.getHeight();

            nSrcPosX = (nViewWidth - bmpBack.getWidth()) / 2;
            nSrcPosY = (nViewHeight - bmpBack.getHeight()) / 2;
            fSrcScale = 1.0f * m_tool.getShowImageSize()[0] / bmpBack.getWidth();

            if (bmpBack.getWidth() > bmpBack.getHeight()) {// 宽大于高
                rectSelect.set(
                        nSrcPosX + (bmpBack.getWidth() - bmpBack.getHeight()) / 2,
                        nSrcPosY,
                        nSrcPosX + (bmpBack.getWidth() - bmpBack.getHeight()) / 2
                                + bmpBack.getHeight(), nSrcPosY + bmpBack.getHeight());// 标准初始位置
            } else if (bmpBack.getWidth() < bmpBack.getHeight()) {
                rectSelect.set(
                        nSrcPosX,
                        nSrcPosY + (bmpBack.getHeight() - bmpBack.getWidth()) / 2,
                        nSrcPosX + bmpBack.getWidth(),
                        nSrcPosY + (bmpBack.getHeight() - bmpBack.getWidth()) / 2
                                + bmpBack.getWidth());// 标准初始位置
            } else {
                rectSelect.set(nSrcPosX, nSrcPosY, nSrcPosX + bmpBack.getWidth(), nSrcPosY
                        + bmpBack.getHeight());// 标准初始位置
            }

            /*
             * rectSelect.set(nSrcPosX + bmpBack.getWidth() / 4, nSrcPosY +
             * bmpBack.getHeight() / 4, nSrcPosX + bmpBack.getWidth() * 3 / 4,
             * nSrcPosY + bmpBack.getHeight() * 3 / 4);// 标准初始位置
             */
            float scaleCut = 1.0f / MyData.fScaleCut;
            if (rectSelect.width() * scaleCut < MIN_FINAL_WIDTH) {
                rectSelect.left = nSrcPosX;
                rectSelect.right = nSrcPosX + bmpBack.getWidth();
            }

            if (rectSelect.height() * scaleCut < MIN_FINAL_WIDTH) {
                rectSelect.top = nSrcPosY;
                rectSelect.bottom = nSrcPosY + bmpBack.getHeight();
            }

            //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start
            int CUR_MIN_FINAL_WIDTH = 10;
            if (!isOutoffMin) {
                CUR_MIN_FINAL_WIDTH = (int) (MIN_FINAL_WIDTH * MyData.fScaleCut);
            }
            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH || rectSelect.height() <= CUR_MIN_FINAL_WIDTH) {
                mIsNotFit = true;
                Log.w("ViewEditCut","fengke ViewEditCut mIsNotFit = " + mIsNotFit);
                if (mNotSupportToast == null) {
                    mNotSupportToast = Toast.makeText(mContext, mContext.getResources().getString(R.string.jrdgallery_scale_not_support), Toast.LENGTH_LONG);
                }
                mNotSupportToast.show();
            }
            //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 end

            rectSelectTotal.set(0, 0, bmpBack.getWidth(), bmpBack.getHeight());
            rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                    rectSelect.top - bmpDot.getHeight() / 2,
                    rectSelect.left + bmpDot.getWidth() / 2, rectSelect.top + bmpDot.getHeight()
                            / 2);
            rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                    rectSelect.top - bmpDot.getHeight() / 2,
                    rectSelect.right + bmpDot.getWidth() / 2, rectSelect.top + bmpDot.getHeight()
                            / 2);
            rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2,
                    rectSelect.bottom - bmpDot.getHeight() / 2,
                    rectSelect.left + bmpDot.getWidth() / 2, rectSelect.bottom + bmpDot.getHeight()
                            / 2);
            rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2,
                    rectSelect.bottom - bmpDot.getHeight() / 2,
                    rectSelect.right + bmpDot.getWidth() / 2,
                    rectSelect.bottom + bmpDot.getHeight() / 2);
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (nViewWidth == 0) {
                // 不能在oncreate里获取view宽高，只有触发了ondraw才能获取宽高
                setPic();
            }
            if (bmpBack != null && !bmpBack.isRecycled()) {
                canvas.drawBitmap(bmpBack, nPosX + nSrcPosX, nPosY + nSrcPosY, null);

                Paint paintMask = new Paint();
                paintMask.setStyle(Paint.Style.FILL);
                paintMask.setColor(R.color.mask);
                paintMask.setAntiAlias(true);

                Rect rectTop = new Rect(nPosX + nSrcPosX, nPosY + nSrcPosY, nPosX + nSrcPosX
                        + bmpBack.getWidth(),
                        rectSelect.top);
                Rect rectLeft = new Rect(nPosX + nSrcPosX, rectSelect.top, rectSelect.left,
                        rectSelect.bottom);
                Rect rectRight = new Rect(rectSelect.right, rectSelect.top, nPosX + nSrcPosX
                        + bmpBack.getWidth(),
                        rectSelect.bottom);
                Rect rectBottom = new Rect(nPosX + nSrcPosX, rectSelect.bottom, nPosX + nSrcPosX
                        + bmpBack.getWidth(),
                        nPosY + nSrcPosY + bmpBack.getHeight());

                // if (isOkCut) {
                // Paint paintcut = new Paint();
                // paintcut.setStyle(Paint.Style.FILL);
                // paintcut.setColor(0xff0E0E0E);
                // paintcut.setAlpha(255);
                // paintcut.setAntiAlias(true);
                // canvas.drawRect(rectTop, paintcut);
                // canvas.drawRect(rectLeft, paintcut);
                // canvas.drawRect(rectRight, paintcut);
                // canvas.drawRect(rectBottom, paintcut);
                // } else {
                canvas.drawRect(rectTop, paintMask);
                canvas.drawRect(rectLeft, paintMask);
                canvas.drawRect(rectRight, paintMask);
                canvas.drawRect(rectBottom, paintMask);
                // }

                Paint paintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
                paintWhite.setStyle(Paint.Style.FILL);
                paintWhite.setColor(0xffffffff);
                paintWhite.setAntiAlias(true);
                // 画白框
                canvas.drawRect(rectSelect.left, rectSelect.top - 2 * MyData.nDensity,
                        rectSelect.right, rectSelect.top, paintWhite);
                canvas.drawRect(rectSelect.left, rectSelect.bottom, rectSelect.right,
                        rectSelect.bottom + 2 * MyData.nDensity, paintWhite);
                canvas.drawRect(rectSelect.left - 2 * MyData.nDensity, rectSelect.top - 2
                        * MyData.nDensity, rectSelect.left,
                        rectSelect.bottom + 2 * MyData.nDensity,
                        paintWhite);
                canvas.drawRect(rectSelect.right, rectSelect.top - 2 * MyData.nDensity,
                        rectSelect.right + 2 * MyData.nDensity, rectSelect.bottom + 2
                                * MyData.nDensity,
                        paintWhite);
                // 画白框里的1/3透明线
                paintWhite.setColor(0x80ffffff);
                canvas.drawLine(rectSelect.left + rectSelect.width() / 3, rectSelect.top,
                        rectSelect.left + rectSelect.width() / 3, rectSelect.bottom, paintWhite);
                canvas.drawLine(rectSelect.left + rectSelect.width() * 2 / 3, rectSelect.top,
                        rectSelect.left
                                + rectSelect.width() * 2 / 3, rectSelect.bottom, paintWhite);
                canvas.drawLine(rectSelect.left, rectSelect.top + rectSelect.height() / 3,
                        rectSelect.right,
                        rectSelect.top + rectSelect.height() / 3, paintWhite);
                canvas.drawLine(rectSelect.left, rectSelect.top + rectSelect.height() * 2 / 3,
                        rectSelect.right,
                        rectSelect.top + rectSelect.height() * 2 / 3, paintWhite);

                // //画白框里的尺寸
                paintWhite.setColor(0xffffffff);

                String strSize = (int) (rectSelect.width() * fScale) + "*"
                        + (int) (rectSelect.height() * fScale);
                int mx = rectSelect.left + rectSelect.width() / 2;
                int my = rectSelect.top + rectSelect.height() / 2;
                mx = mx - (int) (4 * strSize.length() * MyData.nDensity);// 自适应文字居中
                my = my + (int) (2 * MyData.nDensity);
                paintWhite.setTextSize(14 * MyData.nDensity);
                canvas.drawText(strSize, mx, my, paintWhite);
                Paint paintCircle = new Paint();
                paintCircle.setAntiAlias(true);
                if (nCurPressedDot == 0) {
                    canvas.drawBitmap(bmpDotSelect, rectSlider[0].left, rectSlider[0].top,
                            paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[1].left, rectSlider[1].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[2].left, rectSlider[2].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[3].left, rectSlider[3].top, paintCircle);
                } else if (nCurPressedDot == 1) {
                    canvas.drawBitmap(bmpDot, rectSlider[0].left, rectSlider[0].top, paintCircle);
                    canvas.drawBitmap(bmpDotSelect, rectSlider[1].left, rectSlider[1].top,
                            paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[2].left, rectSlider[2].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[3].left, rectSlider[3].top, paintCircle);
                } else if (nCurPressedDot == 2) {
                    canvas.drawBitmap(bmpDot, rectSlider[0].left, rectSlider[0].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[1].left, rectSlider[1].top, paintCircle);
                    canvas.drawBitmap(bmpDotSelect, rectSlider[2].left, rectSlider[2].top,
                            paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[3].left, rectSlider[3].top, paintCircle);
                } else if (nCurPressedDot == 3) {
                    canvas.drawBitmap(bmpDot, rectSlider[0].left, rectSlider[0].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[1].left, rectSlider[1].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[2].left, rectSlider[2].top, paintCircle);
                    canvas.drawBitmap(bmpDotSelect, rectSlider[3].left, rectSlider[3].top,
                            paintCircle);
                } else {
                    canvas.drawBitmap(bmpDot, rectSlider[0].left, rectSlider[0].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[1].left, rectSlider[1].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[2].left, rectSlider[2].top, paintCircle);
                    canvas.drawBitmap(bmpDot, rectSlider[3].left, rectSlider[3].top, paintCircle);
                }
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }

    public void isOkCut(boolean okcut) {
        if (okcut) {
            isOkCut = okcut;
            preCut(true);
            
            // mIsNeedSave = false;
            // bmpDot = BitmapFactory.decodeResource(getResources(),
            // R.drawable.img_cut_dot);
            // bmpDotSelect = BitmapFactory.decodeResource(getResources(),
            // R.drawable.img_cut_dot);
            // setPic();
            // invalidate();
//            preCut(true);
//            mIsPreCut = false;
//            m_tool.ok();
//            MyData.getBeautyControl().pushImage();
//            mIsNeedSave = false;
//            nViewWidth = 0;
//            for (Rect rect : rectSlider) {
//                rect.setEmpty();
//            }
            // preCut(true);
//            invalidate();
        } else {
            isOkCut = okcut;
            invalidate();
        }
    }

    // 移动窗体，4个参数为偏移的值
    public void setPos(int left, int top, int right, int bottom) {
        rectSelect.left += left;
        rectSelect.top += top;
        rectSelect.right += right;
        rectSelect.bottom += bottom;

        invalidate();
    }

    // 点是否在区域内
    public boolean isPointInView(int x, int y) {
        try {
            if (y >= 0 && y <= this.getHeight()) {
                return true;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return false;
    }

    /**
     * 获取选中的位置 =4整个
     */
    public int getClickPart(int x, int y) {
        try {
            for (int i = 0; i < 4; i++) {
                if (rectSlider[i].contains(x, y)) {
                    return i;
                }
            }
            if (rectSelect.contains(x, y)) {
                return 4;
            }
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return -1;
    }

    // 更新拖动后的显示
    public int moveSlider(int kind, int x, int y) {
        try {
            int dx, dy;
            float scaleX = 1;
            int kx, ky;
            boolean isXMin = true;
            kx = ky = MIN_WIDTH;
            if (kind != 4) {// 局部处理，不是整体拖动
                if (nType == 1) {// =1 1:1,
                    scaleX = 1;
                    if (Math.abs(x) > Math.abs(y)) {
                        y = x;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                } else if (nType == 2) {// =2 3:2
                    ky = MIN_WIDTH;
                    kx = ky * 3 / 2;
                    isXMin = false;
                    scaleX = 3.0f / 2;
                    if (Math.abs(x) * 2 > Math.abs(y) * 3) {
                        y = x * 2 / 3;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y * 3 / 2;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                } else if (nType == 3) {// =3 4:3
                    ky = MIN_WIDTH;
                    kx = ky * 4 / 3;
                    isXMin = false;
                    scaleX = 4.0f / 3;
                    if (Math.abs(x) * 3 > Math.abs(y) * 4) {
                        y = x * 3 / 4;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y * 4 / 3;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                } else if (nType == 4) {// =4 2:3
                    kx = MIN_WIDTH;
                    ky = kx * 3 / 2;
                    isXMin = true;
                    scaleX = 2.0f / 3;
                    if (Math.abs(x) * 3 > Math.abs(y) * 2) {
                        y = x * 3 / 2;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y * 2 / 3;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                } else if (nType == 5) {// ,=5 3:4
                    kx = MIN_WIDTH;
                    ky = kx * 4 / 3;
                    isXMin = true;
                    scaleX = 3.0f / 4;
                    if (Math.abs(x) * 4 > Math.abs(y) * 3) {
                        y = x * 4 / 3;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y * 3 / 4;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                } else if (nType == 6) {// =6 16:9
                    ky = MIN_WIDTH;
                    kx = ky * 16 / 9;
                    // MTDebug.Print("kx=" + kx + " ky=" + ky);
                    isXMin = false;
                    scaleX = 16.0f / 9;
                    if (Math.abs(x) * 9 > Math.abs(y) * 16) {
                        y = x * 9 / 16;
                        if (kind == 1 || kind == 2) {
                            y *= -1;
                        }
                    } else {
                        x = y * 16 / 9;
                        if (kind == 1 || kind == 2) {
                            x *= -1;
                        }
                    }
                }
            }
            int CUR_MIN_FINAL_WIDTH = 10;
            if (!isOutoffMin) {
                CUR_MIN_FINAL_WIDTH = (int) (MIN_FINAL_WIDTH * MyData.fScaleCut);
            }
            boolean isMovePoint = false;
            switch (kind) {
                case 0:// 左上
                    dx = rectSelect.left;
                    dy = rectSelect.top;

                    if (nType == 0 || nType == 7) {// 任意比例
                        if (!(rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x > 0)) {
                            rectSelect.left += x;
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                            } else if (rectSelect.left > rectSelect.right - kx) {
                                rectSelect.left = rectSelect.right - kx;
                            }
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (!(rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y > 0)) {
                            rectSelect.top += y;
                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                            } else if (rectSelect.top > rectSelect.bottom - ky) {
                                rectSelect.top = rectSelect.bottom - ky;
                            }
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (isMovePoint) {
                            dx = rectSelect.left - dx;
                            dy = rectSelect.top - dy;
                            rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.left + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.right + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.left
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                        }
                        break;
                    }
                    if ((rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x > 0)
                            || (rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y > 0)) {
                        return -1;
                    }
                    if ((rectSelect.left <= nSrcPosX && x <= 0)
                            || (rectSelect.left >= rectSelect.right - kx && x >= 0)
                            || (rectSelect.top <= nSrcPosY && y <= 0)
                            || (rectSelect.top >= rectSelect.bottom - ky && y >= 0)) {
                        return -1;
                    }

                    rectSelect.left += x;
                    rectSelect.top += y;
                    if (isXMin) {
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH / scaleX) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH
                                        / scaleX);
                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            } else if (rectSelect.top > rectSelect.bottom - MIN_WIDTH) {
                                rectSelect.top = rectSelect.bottom - MIN_WIDTH;

                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                        }
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            } else if (rectSelect.left > rectSelect.right - MIN_WIDTH) {
                                rectSelect.left = rectSelect.right - MIN_WIDTH;
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                        }
                    } else {
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH * scaleX) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH
                                        * scaleX);
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            } else if (rectSelect.left > rectSelect.right - MIN_WIDTH) {
                                rectSelect.left = rectSelect.right - MIN_WIDTH;
                                int nn = rectSelect.left - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                        }
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            } else if (rectSelect.top > rectSelect.bottom - MIN_WIDTH) {
                                rectSelect.top = rectSelect.bottom - MIN_WIDTH;

                                int nn = rectSelect.top - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                        }
                    }

                    if (rectSelect.width() > rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                        rectSelect.left += tn;
                        rectSelect.right -= tn;
                    } else if (rectSelect.width() < rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.height() * scaleX - rectSelect.width()) / 2);
                        rectSelect.top += tn;
                        rectSelect.bottom -= tn;
                    }
                    dx = rectSelect.left - dx;
                    dy = rectSelect.top - dy;
                    rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);

                    break;
                case 1:// 右上
                    dx = rectSelect.right;
                    dy = rectSelect.top;
                    if (nType == 0 || nType == 7) {
                        if (!(rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x < 0)) {
                            rectSelect.right += x;
                            if (rectSelect.right < rectSelect.left + kx) {
                                rectSelect.right = rectSelect.left + kx;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                            }
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (!(rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y > 0)) {
                            rectSelect.top += y;

                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                            } else if (rectSelect.top > rectSelect.bottom - ky) {
                                rectSelect.top = rectSelect.bottom - ky;
                            }
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (isMovePoint) {
                            dx = rectSelect.right - dx;
                            dy = rectSelect.top - dy;

                            rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.left + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.right + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.right
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                        }
                        break;
                    }
                    if ((rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x < 0)
                            || (rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y > 0)) {
                        return -1;
                    }
                    if ((rectSelect.right <= rectSelect.left + kx && x <= 0)
                            || (rectSelect.right >= bmpBack.getWidth() + nSrcPosX && x >= 0)
                            || (rectSelect.top <= nSrcPosY && y <= 0)
                            || (rectSelect.top >= rectSelect.bottom - ky && y >= 0)) {
                        return -1;
                    }

                    rectSelect.right += x;
                    rectSelect.top += y;
                    if (isXMin) {
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH / scaleX) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH
                                        / scaleX);
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            } else if (rectSelect.top > rectSelect.bottom - MIN_WIDTH) {
                                rectSelect.top = rectSelect.bottom - MIN_WIDTH;
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                        }
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                            if (rectSelect.right < rectSelect.left + MIN_WIDTH) {
                                rectSelect.right = rectSelect.left + MIN_WIDTH;
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                        }
                    } else {
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH * scaleX) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH
                                        * scaleX);
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                            if (rectSelect.right < rectSelect.left + MIN_WIDTH) {
                                rectSelect.right = rectSelect.left + MIN_WIDTH;
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                                int nn = rectSelect.right - dx;
                                rectSelect.top = dy + y * nn / x;
                            }
                        }
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.top = (int) (rectSelect.bottom - CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                            if (rectSelect.top < nSrcPosY) {
                                rectSelect.top = nSrcPosY;
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            } else if (rectSelect.top > rectSelect.bottom - MIN_WIDTH) {
                                rectSelect.top = rectSelect.bottom - MIN_WIDTH;
                                int nn = rectSelect.top - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                        }
                    }
                    dx = rectSelect.right - dx;
                    dy = rectSelect.top - dy;
                    if (rectSelect.width() > rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                        rectSelect.left += tn;
                        rectSelect.right -= tn;
                    } else if (rectSelect.width() < rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.height() * scaleX - rectSelect.width()) / 2);
                        rectSelect.top += tn;
                        rectSelect.bottom -= tn;
                    }
                    rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);

                    break;
                case 2:// 左下
                    dx = rectSelect.left;
                    dy = rectSelect.bottom;
                    if (nType == 0 || nType == 7) {
                        if (!(rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x > 0)) {
                            rectSelect.left += x;
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                            } else if (rectSelect.left > rectSelect.right - kx) {
                                rectSelect.left = rectSelect.right - kx;
                            }
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (!(rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y < 0)) {
                            rectSelect.bottom += y;
                            if (rectSelect.bottom < rectSelect.top + ky) {
                                rectSelect.bottom = rectSelect.top + ky;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                            }
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (isMovePoint) {
                            dx = rectSelect.left - dx;
                            dy = rectSelect.bottom - dy;
                            rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.left + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.left
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                            rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.right
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                        }
                        break;
                    }
                    if ((rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x > 0)
                            || (rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y < 0)) {
                        return -1;
                    }
                    if ((rectSelect.left <= nSrcPosX && x <= 0)
                            || (rectSelect.left >= rectSelect.right - kx && x >= 0)
                            || (rectSelect.bottom <= rectSelect.top + ky && y <= 0)
                            || (rectSelect.bottom >= bmpBack.getHeight() + nSrcPosY && y >= 0)) {
                        return -1;
                    }
                    dx = rectSelect.left;
                    dy = rectSelect.bottom;
                    rectSelect.left += x;
                    rectSelect.bottom += y;
                    if (isXMin) {
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH / scaleX) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH
                                        / scaleX);
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                            if (rectSelect.bottom < rectSelect.top + MIN_WIDTH) {
                                rectSelect.bottom = rectSelect.top + MIN_WIDTH;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                        }
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            } else if (rectSelect.left > rectSelect.right - MIN_WIDTH) {
                                rectSelect.left = rectSelect.right - MIN_WIDTH;
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                        }
                    } else {
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH * scaleX) {
                                rectSelect.left = (int) (rectSelect.right - CUR_MIN_FINAL_WIDTH
                                        * scaleX);
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                            if (rectSelect.left < nSrcPosX) {
                                rectSelect.left = nSrcPosX;
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            } else if (rectSelect.left > rectSelect.right - MIN_WIDTH) {
                                rectSelect.left = rectSelect.right - MIN_WIDTH;
                                int nn = rectSelect.left - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                        }
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                            if (rectSelect.bottom < rectSelect.top + MIN_WIDTH) {
                                rectSelect.bottom = rectSelect.top + MIN_WIDTH;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.left = dx + x * nn / y;
                            }
                        }
                    }
                    dx = rectSelect.left - dx;
                    dy = rectSelect.bottom - dy;
                    if (rectSelect.width() > rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                        rectSelect.left += tn;
                        rectSelect.right -= tn;
                    } else if (rectSelect.width() < rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.height() * scaleX - rectSelect.width()) / 2);
                        rectSelect.top += tn;
                        rectSelect.bottom -= tn;
                    }
                    rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);
                    rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);

                    break;
                case 3:// 右下
                    dx = rectSelect.right;
                    dy = rectSelect.bottom;
                    if (nType == 0 || nType == 7) {
                        if (!(rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x < 0)) {
                            rectSelect.right += x;
                            if (rectSelect.right < rectSelect.left + kx) {
                                rectSelect.right = rectSelect.left + kx;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                            }
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (!(rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y < 0)) {
                            rectSelect.bottom += y;

                            if (rectSelect.bottom < rectSelect.top + ky) {
                                rectSelect.bottom = rectSelect.top + ky;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                            }
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH);
                            }
                            isMovePoint = true;
                        }
                        if (isMovePoint) {
                            dx = rectSelect.right - dx;
                            dy = rectSelect.bottom - dy;
                            rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.top - bmpDot.getHeight()
                                            / 2, rectSelect.right + bmpDot.getWidth() / 2,
                                    rectSelect.top + bmpDot.getHeight() / 2);
                            rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.left
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                            rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2,
                                    rectSelect.bottom - bmpDot.getHeight() / 2, rectSelect.right
                                            + bmpDot.getWidth() / 2,
                                    rectSelect.bottom + bmpDot.getHeight() / 2);
                        }
                        break;
                    }
                    if ((rectSelect.width() <= CUR_MIN_FINAL_WIDTH && x < 0)
                            || (rectSelect.height() <= CUR_MIN_FINAL_WIDTH && y < 0)) {
                        return -1;
                    }
                    if ((rectSelect.right <= rectSelect.left + kx && x <= 0)
                            || (rectSelect.right >= bmpBack.getWidth() + nSrcPosX && x >= 0)
                            || (rectSelect.bottom <= rectSelect.top + ky && y <= 0)
                            || (rectSelect.bottom >= bmpBack.getHeight() + nSrcPosY && y >= 0)) {
                        return -1;
                    }

                    rectSelect.right += x;
                    rectSelect.bottom += y;
                    if (isXMin) {
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH / scaleX) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH
                                        / scaleX);
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                            if (rectSelect.bottom < rectSelect.top + MIN_WIDTH) {
                                rectSelect.bottom = rectSelect.top + MIN_WIDTH;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                        }
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                            if (rectSelect.right < rectSelect.left + MIN_WIDTH) {
                                rectSelect.right = rectSelect.left + MIN_WIDTH;
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                        }
                    } else {
                        if (x != 0) {
                            if (rectSelect.width() < CUR_MIN_FINAL_WIDTH * scaleX) {
                                rectSelect.right = (int) (rectSelect.left + CUR_MIN_FINAL_WIDTH
                                        * scaleX);
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                            if (rectSelect.right < rectSelect.left + MIN_WIDTH) {
                                rectSelect.right = rectSelect.left + MIN_WIDTH;
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            } else if (rectSelect.right > bmpBack.getWidth() + nSrcPosX) {
                                rectSelect.right = bmpBack.getWidth() + nSrcPosX;
                                int nn = rectSelect.right - dx;
                                rectSelect.bottom = dy + y * nn / x;
                            }
                        }
                        if (y != 0) {
                            if (rectSelect.height() < CUR_MIN_FINAL_WIDTH) {
                                rectSelect.bottom = (int) (rectSelect.top + CUR_MIN_FINAL_WIDTH);
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                            if (rectSelect.bottom < rectSelect.top + MIN_WIDTH) {
                                rectSelect.bottom = rectSelect.top + MIN_WIDTH;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            } else if (rectSelect.bottom > bmpBack.getHeight() + nSrcPosY) {
                                rectSelect.bottom = bmpBack.getHeight() + nSrcPosY;
                                int nn = rectSelect.bottom - dy;
                                rectSelect.right = dx + x * nn / y;
                            }
                        }
                    }
                    dx = rectSelect.right - dx;
                    dy = rectSelect.bottom - dy;
                    if (rectSelect.width() > rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                        rectSelect.left += tn;
                        rectSelect.right -= tn;
                    } else if (rectSelect.width() < rectSelect.height() * scaleX) {
                        int tn = (int) ((rectSelect.height() * scaleX - rectSelect.width()) / 2);
                        rectSelect.top += tn;
                        rectSelect.bottom -= tn;
                    }
                    rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);
                    rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);
                    break;
                case 4:// 整个拖动
                    if (x < 0) {
                        if (rectSelect.left < nSrcPosX) {
                            return -1;
                        }
                    } else {
                        if (rectSelect.right > nViewWidth - nSrcPosX) {
                            return -1;
                        }
                    }
                    if (y < 0) {
                        if (rectSelect.top < nSrcPosY) {
                            return -1;
                        }
                    } else {
                        if (rectSelect.bottom > nViewHeight - nSrcPosY) {
                            return -1;
                        }
                    }
                    dx = rectSelect.left;
                    dy = rectSelect.top;
                    rectSelect.offset(x, y);
                    if (x < 0) {
                        if (rectSelect.left <= nSrcPosX) {
                            rectSelect.offset(nSrcPosX - rectSelect.left, 0);
                        }
                    } else {
                        if (rectSelect.right >= nSrcPosX + bmpBack.getWidth()) {
                            rectSelect.offset(nSrcPosX + bmpBack.getWidth() - rectSelect.right, 0);
                        }
                    }
                    if (y < 0) {
                        if (rectSelect.top <= nSrcPosY) {
                            rectSelect.offset(0, nSrcPosY - rectSelect.top);
                        }
                    } else {
                        if (rectSelect.bottom >= nSrcPosY + bmpBack.getHeight()) {
                            rectSelect
                                    .offset(0, nSrcPosY + bmpBack.getHeight() - rectSelect.bottom);
                        }
                    }
                    dx = rectSelect.left - dx;
                    dy = rectSelect.top - dy;
                    rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.top
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.top + bmpDot.getHeight() / 2);
                    rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.left + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);
                    rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                            - bmpDot.getHeight() / 2,
                            rectSelect.right + bmpDot.getWidth() / 2,
                            rectSelect.bottom + bmpDot.getHeight() / 2);

                    break;
            }
            invalidate();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return 1;
    }

    // 放大缩小
    public boolean resize(float scale, int px, int py) {
        try {
            int dx;
            int dy;
            int CUR_MIN_FINAL_WIDTH = (int) (MIN_FINAL_WIDTH * MyData.fScaleCut);
            if (scale > 1.0f) {
                if (rectSelect.width() >= bmpBack.getWidth()
                        || rectSelect.height() >= bmpBack.getHeight()) {
                    return false;
                }
                dx = (int) (rectSelect.width() * (scale - 1.0f) / 2);
                dy = (int) (rectSelect.height() * (scale - 1.0f) / 2);

                float scaleX = 1;
                // nType = 0; =1 1:1,=2 3:2,=3 4:3,=4 2:3,=5 3:4,=6 16:9,=7 ?:?
                switch (nType) {
                    case 0:
                    case 7:
                        scaleX = 0;
                        break;
                    case 1:
                        break;
                    case 2:
                        scaleX = (float) 3 / 2;
                        break;
                    case 3:
                        scaleX = (float) 4 / 3;
                        break;
                    case 4:
                        scaleX = (float) 2 / 3;
                        break;
                    case 5:
                        scaleX = (float) 3 / 4;
                        break;
                    case 6:
                        scaleX = (float) 16 / 9;
                        break;
                }

                if (rectSelect.left - dx <= nSrcPosX) {
                    dx = rectSelect.left - nSrcPosX;
                }
                if (rectSelect.right + dx >= nViewWidth - nSrcPosX) {
                    dx = nViewWidth - nSrcPosX - rectSelect.right;
                }
                if (rectSelect.top - dy <= nSrcPosY) {
                    dy = rectSelect.top - nSrcPosY;
                }
                if (rectSelect.bottom + dy >= nViewHeight - nSrcPosY) {
                    dy = nViewHeight - nSrcPosY - rectSelect.bottom;
                }
                // 没有变化，退出
                if (dx == 0 || dy == 0) {
                    return false;
                }

                rectSelect.left -= dx;
                rectSelect.right += dx;
                rectSelect.top -= dy;
                rectSelect.bottom += dy;
                if (scaleX != 0 && rectSelect.width() > rectSelect.height() * scaleX) {
                    int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                    rectSelect.left += tn;
                    rectSelect.right -= tn;
                    dx -= tn;
                } else if (scaleX != 0 && rectSelect.width() < rectSelect.height() * scaleX) {
                    int tn = (int) ((rectSelect.height() - rectSelect.width() / scaleX) / 2);
                    rectSelect.top += tn;
                    rectSelect.bottom -= tn;
                    dy -= tn;
                }

                rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);
                rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);

            } else if (scale < 1.0f) {
                dx = (int) (rectSelect.width() * (1.0f - scale) / 2);
                dy = (int) (rectSelect.height() * (1.0f - scale) / 2);

                float scaleX = 1;
                // nType = 0; =1 1:1,=2 3:2,=3 4:3,=4 2:3,=5 3:4,=6 16:9,=7 ?:?
                switch (nType) {
                    case 0:
                    case 7:
                        scaleX = 0;
                        break;
                    case 1:
                        break;
                    case 2:
                        scaleX = (float) 3 / 2;
                        break;
                    case 3:
                        scaleX = (float) 4 / 3;
                        break;
                    case 4:
                        scaleX = (float) 2 / 3;
                        break;
                    case 5:
                        scaleX = (float) 3 / 4;
                        break;
                    case 6:
                        scaleX = (float) 16 / 9;
                        break;
                }
                if (rectSelect.width() <= CUR_MIN_FINAL_WIDTH
                        || rectSelect.height() <= CUR_MIN_FINAL_WIDTH) {
                    invalidate();
                    return false;
                }
                if (scaleX <= 1 && rectSelect.width() * scale < CUR_MIN_FINAL_WIDTH) {
                    dx = (int) (rectSelect.width() - CUR_MIN_FINAL_WIDTH) / 2;
                    if (scaleX != 0)
                        dy = (int) (dx / scaleX);
                }
                if (scaleX > 1 && rectSelect.height() * scale < CUR_MIN_FINAL_WIDTH) {
                    dy = (int) (rectSelect.height() - CUR_MIN_FINAL_WIDTH) / 2;
                    if (scaleX != 0)
                        dx = (int) (dy * scaleX);
                }

                if (rectSelect.left + dx * 2 + MIN_WIDTH > rectSelect.right) {
                    dx = (rectSelect.right - rectSelect.left - MIN_WIDTH) / 2;
                    if (scaleX != 0)
                        dy = (int) (dx / scaleX);
                }
                if (rectSelect.top + dy * 2 + MIN_WIDTH > rectSelect.bottom) {
                    dy = (rectSelect.bottom - rectSelect.top - MIN_WIDTH) / 2;
                    if (scaleX != 0)
                        dx = (int) (dy * scaleX);
                }
                // 没有变化，退出
                if (dx == 0 || dy == 0) {
                    invalidate();
                    return false;
                }
                rectSelect.left += dx;
                rectSelect.right -= dx;
                rectSelect.top += dy;
                rectSelect.bottom -= dy;
                if (scaleX != 0 && rectSelect.width() > rectSelect.height() * scaleX) {
                    int tn = (int) ((rectSelect.width() - rectSelect.height() * scaleX) / 2);
                    rectSelect.left += tn;
                    rectSelect.right -= tn;
                    dx += tn;
                } else if (scaleX != 0 && rectSelect.width() < rectSelect.height() * scaleX) {
                    int tn = (int) ((rectSelect.height() - rectSelect.width() / scaleX) / 2);
                    rectSelect.top += tn;
                    rectSelect.bottom -= tn;
                    dy += tn;
                }
                rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);
                rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);
            }
            invalidate();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    private int getRealX(int x, int y) {
        if (nType == 1) {
            x = y;
        } else if (nType == 2) {
            x = 3 * y / 2;

        } else if (nType == 3) {
            x = 4 * y / 3;
        } else if (nType == 4) {
            x = 2 * y / 3;
        } else if (nType == 5) {
            x = 3 * y / 4;
        } else if (nType == 6) {
            x = 16 * y / 9;
        } else {

        }

        return x;
    }

    // 完成裁剪 返回 = 0完成裁剪，=1不用裁剪，=-1太小无法裁剪
    public int preCut(boolean isRefresh) {
        try {
            Rect rectT = new Rect();

            rectT.left = (int) (rectSelectTotal.left + (rectSelect.left - nSrcPosX));
            rectT.top = (int) (rectSelectTotal.top + (rectSelect.top - nSrcPosY));
            rectT.right = (int) (rectT.left + rectSelect.width());
            rectT.bottom = (int) (rectT.top + rectSelect.height());

            // 长度过短，不能再裁剪
            if (rectT.width() < FINAL_MIN_WIDTH || rectT.height() < FINAL_MIN_WIDTH) {
                return -1;
            }
            if (rectT.right <= rectT.left) {
                rectT.right = rectT.left + 1;
            }
            if (rectT.bottom <= rectT.top) {
                rectT.bottom = rectT.top + 1;
            }
            rectSelectTotal.set(rectT);
            // 长度过短，不能再裁剪

            // 长度相等，不用裁剪
            if (rectSelect.width() == bmpBack.getWidth()
                    && rectSelect.height() == bmpBack.getHeight()) {
                // MTDebug.Print("finishCut equal");
                return 1;
            }

//            fCurScale = ((float) bmpBack.getWidth() / rectSelectTotal.width());
            float val[] = new float[4];
            val[0] = (rectSelectTotal.left * 1.0f) / bmpBack.getWidth();
            val[1] = rectSelectTotal.right * 1.0f / bmpBack.getWidth();
            val[2] = rectSelectTotal.top * 1.0f / bmpBack.getHeight();
            val[3] = rectSelectTotal.bottom * 1.0f / bmpBack.getHeight();
        
            m_tool.procImage(val, true);

            if (isRefresh) {
                if (bmpBack != null && !bmpBack.isRecycled()) {
                    bmpBack.recycle();
                    bmpBack = null;
                }
                 bmpBack = m_tool.getShowProcImage();
                 fCurScale = ((float) bmpBack.getWidth() / rectSelectTotal.width());
//                 zoomBitmap(bmpBack,rectSelect.width(),rectSelect.height());

                nSrcPosX = (nViewWidth - bmpBack.getWidth()) / 2;
                nSrcPosY = (nViewHeight - bmpBack.getHeight()) / 2;

                rectSelect.set(nSrcPosX, nSrcPosY, nSrcPosX + bmpBack.getWidth(), nSrcPosY
                        + bmpBack.getHeight());

                rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                        rectSelect.top - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.top + bmpDot.getHeight() / 2);
                rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.left + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);
                rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2, rectSelect.bottom
                        - bmpDot.getHeight() / 2,
                        rectSelect.right + bmpDot.getWidth() / 2,
                        rectSelect.bottom + bmpDot.getHeight() / 2);
                invalidate();
            }
            mIsNeedSave = true;
            mIsPreCut = false;
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return 0;
    }
    
    public Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    // 重置
    public boolean reset() {
        mIsNeedSave = false;
        bmpDot = BitmapFactory.decodeResource(getResources(), R.drawable.img_cut_dot);
        bmpDotSelect = BitmapFactory.decodeResource(getResources(), R.drawable.img_cut_dot);
        setPic();
        invalidate();
        return true;
    }

    // 是否有操作裁剪
    public boolean isAdjusted() {
        // 长度相等，并且未裁剪过
        if (fCurScale == 1 && rectSelect.width() == mSrcWidth && rectSelect.height() == mSrcHeight) {
            // MTDebug.Print("TEMP", "finishCut equal");
            return false;
        }
        return true;
    }

    // 是否已确定裁剪，但未保存
    public boolean isNeedSave() {
        // 长度相等，并且未裁剪过
        return mIsNeedSave;
    }

    // 获取裁剪后的图片
    public boolean saveCut() {
        try {
            if (isAdjusted() && mIsPreCut) {
                preCut(false);
                mIsPreCut = false;
            }
            if (mIsNeedSave) {
                // MyData.mOptMiddle.addEffect();
                m_tool.ok();
                MyData.getBeautyControl().pushImage();
            }
            mIsNeedSave = false;
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public void setPressedDot(int id) {
        if (id >= -1 && id < 4) {
            nCurPressedDot = id;
            invalidate();
        }
    }

    // 切换比例时更新选中区域
    // =1 1:1,=2 3:2,=3 4:3,=4 2:3,=5 3:4,=6 16:9
    public boolean setRectSelcetBySize(int kind) {
        try {
            int CUR_MIN_FINAL_WIDTH = (int) (MIN_FINAL_WIDTH * MyData.fScaleCut);
            int w = bmpBack.getWidth();
            int h = bmpBack.getHeight();
            float dx, dy;
            Rect rectT = new Rect();
            switch (kind) {
                case 0:
                    break;
                case 7:
                    return true;
                case 1:// 1:1
                    if (w > h) {
                        rectT.set((w - h) / 2 + nSrcPosX + h / 4, nSrcPosY + h / 4, (w - h) / 2
                                + nSrcPosX + h * 3 / 4,
                                nSrcPosY + h * 3 / 4);
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + w / 4 + (h - w) / 2, nSrcPosX + w
                                * 3 / 4, nSrcPosY + w * 3
                                / 4 + (h - w) / 2);
                    }
                    if (w < CUR_MIN_FINAL_WIDTH || h < CUR_MIN_FINAL_WIDTH) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize1");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (rectT.width() < CUR_MIN_FINAL_WIDTH) {
                        rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH) / 2;
                        rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH) / 2;
                    }
                    if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                        rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH) / 2;
                        rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH) / 2;
                    }
                    break;
                case 2:// 3:2
                    if (w < CUR_MIN_FINAL_WIDTH * 3 / 2 || h < CUR_MIN_FINAL_WIDTH) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize2");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (w * 2 > h * 3) {
                        rectT.set(nSrcPosX + (w - h * 3 / 4) / 2, nSrcPosY + h / 4, nSrcPosX + h
                                * 3 / 4 + (w - h * 3 / 4)
                                / 2, nSrcPosY + h * 3 / 4);
                        if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                            int ty = rectT.height();
                            rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH) / 2;
                            rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH) / 2;
                            if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize3");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.height() - ty) / 2 * 3 / 2;
                            rectT.left -= vn;
                            rectT.right += vn;
                        }
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + (h - w / 3) / 2, nSrcPosX + w * 3
                                / 4, nSrcPosY + w / 3
                                + (h - w / 3) / 2);
                        if (rectT.width() < CUR_MIN_FINAL_WIDTH * 3 / 2) {
                            int tx = rectT.width();
                            rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH * 3 / 2) / 2;
                            rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH * 3 / 2) / 2;
                            if (rectT.width() < CUR_MIN_FINAL_WIDTH * 3 / 2) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize4");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.width() - tx) / 2 * 2 / 3;
                            rectT.top -= vn;
                            rectT.bottom += vn;
                        }
                    }
                    if (rectT.height() < MIN_WIDTH && MIN_WIDTH * 3 / 2 <= w) {//
                        dx = ((float) MIN_WIDTH * 3 / 2 - rectT.width()) / 2;
                        rectT.left -= dx;
                        rectT.right += dx;
                        dy = (float) (MIN_WIDTH - rectT.height()) / 2;
                        rectT.top -= dy;
                        rectT.bottom += dy;
                    }
                    break;
                case 3:// 4:3
                    if (w < CUR_MIN_FINAL_WIDTH * 4 / 3 || h < CUR_MIN_FINAL_WIDTH) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize5");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (w * 3 > h * 4) {
                        rectT.set(nSrcPosX + (w - h * 2 / 3) / 2, nSrcPosY + h / 4, nSrcPosX + h
                                * 2 / 3 + (w - h * 2 / 3)
                                / 2, nSrcPosY + h * 3 / 4);
                        if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                            int ty = rectT.height();
                            rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH) / 2;
                            rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH) / 2;
                            if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize6");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.height() - ty) / 2 * 4 / 3;
                            rectT.left -= vn;
                            rectT.right += vn;
                        }
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + (h - w * 3 / 8) / 2, nSrcPosX + w
                                * 3 / 4, nSrcPosY + w * 3
                                / 8 + (h - w * 3 / 8) / 2);
                        if (rectT.width() < CUR_MIN_FINAL_WIDTH * 4 / 3) {
                            int tx = rectT.width();
                            rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH * 4 / 3) / 2;
                            rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH * 4 / 3) / 2;
                            if (rectT.width() < CUR_MIN_FINAL_WIDTH * 4 / 3) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize7");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.width() - tx) / 2 * 3 / 4;
                            rectT.top -= vn;
                            rectT.bottom += vn;
                        }
                    }
                    if (rectT.height() < MIN_WIDTH && MIN_WIDTH * 4 / 3 <= w) {//
                        dx = ((float) MIN_WIDTH * 4 / 3 - rectT.width()) / 2;
                        rectT.left -= dx;
                        rectT.right += dx;
                        dy = (float) (MIN_WIDTH - rectT.height()) / 2;
                        rectT.top -= dy;
                        rectT.bottom += dy;
                    }
                    break;
                case 4:// 2:3
                    if (w < CUR_MIN_FINAL_WIDTH || h < CUR_MIN_FINAL_WIDTH * 3 / 2) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize8");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (w * 3 > h * 2) {
                        rectT.set(nSrcPosX + (w - h / 3) / 2, nSrcPosY + h / 4, nSrcPosX + h / 3
                                + (w - h / 3) / 2,
                                nSrcPosY + h * 3 / 4);
                        if (rectT.height() < CUR_MIN_FINAL_WIDTH * 3 / 2) {
                            int ty = rectT.height();
                            rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH * 3 / 2) / 2;
                            rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH * 3 / 2) / 2;
                            if (rectT.height() < CUR_MIN_FINAL_WIDTH * 3 / 2) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize9");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.height() - ty) / 2 * 2 / 3;
                            rectT.left -= vn;
                            rectT.right += vn;
                        }
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + (h - w * 3 / 4) / 2, nSrcPosX + w
                                * 3 / 4, nSrcPosY + w * 3
                                / 4 + (h - w * 3 / 4) / 2);
                        if (rectT.width() < CUR_MIN_FINAL_WIDTH) {
                            int tx = rectT.width();
                            rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH) / 2;
                            rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH) / 2;
                            if (rectT.width() < CUR_MIN_FINAL_WIDTH) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize10");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.width() - tx) / 2 * 3 / 2;
                            rectT.top -= vn;
                            rectT.bottom += vn;
                        }
                    }
                    if (rectT.width() < MIN_WIDTH && MIN_WIDTH * 3 / 2 <= h) {//
                        dx = (float) (MIN_WIDTH - rectT.width()) / 2;
                        rectT.left -= dx;
                        rectT.right += dx;
                        dy = ((float) MIN_WIDTH * 3 / 2 - rectT.height()) / 2;
                        rectT.top -= dy;
                        rectT.bottom += dy;
                    }
                    break;
                case 5:// 3:4
                    if (w < CUR_MIN_FINAL_WIDTH || h < CUR_MIN_FINAL_WIDTH * 4 / 3) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize11");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (w * 4 > h * 3) {
                        rectT.set(nSrcPosX + (w - h * 3 / 8) / 2, nSrcPosY + h / 4, nSrcPosX + h
                                * 3 / 8 + (w - h * 3 / 8)
                                / 2, nSrcPosY + h * 3 / 4);
                        if (rectT.height() < CUR_MIN_FINAL_WIDTH * 4 / 3) {
                            int ty = rectT.height();
                            rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH * 4 / 3) / 2;
                            rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH * 4 / 3) / 2;
                            if (rectT.height() < CUR_MIN_FINAL_WIDTH * 4 / 3) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize12");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.height() - ty) / 2 * 3 / 4;
                            rectT.left -= vn;
                            rectT.right += vn;
                        }
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + (h - w * 2 / 3) / 2, nSrcPosX + w
                                * 3 / 4, nSrcPosY + w * 2
                                / 3 + (h - w * 2 / 3) / 2);
                        if (rectT.width() < CUR_MIN_FINAL_WIDTH) {
                            int tx = rectT.width();
                            rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH) / 2;
                            rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH) / 2;
                            if (rectT.width() < CUR_MIN_FINAL_WIDTH) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize13");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.width() - tx) / 2 * 4 / 3;
                            rectT.top -= vn;
                            rectT.bottom += vn;
                        }
                    }
                    if (rectT.width() < MIN_WIDTH && MIN_WIDTH * 4 / 3 <= h) {//
                        dx = (float) (MIN_WIDTH - rectT.width()) / 2;
                        rectT.left -= dx;
                        rectT.right += dx;
                        dy = ((float) MIN_WIDTH * 4 / 3 - rectT.height()) / 2;
                        rectT.top -= dy;
                        rectT.bottom += dy;
                    }
                    break;
                case 6:// 16:9
                    if (w < CUR_MIN_FINAL_WIDTH * 16 / 9 || h < CUR_MIN_FINAL_WIDTH) {
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                        Log.w("ViewEditCut","fengke setRectSelcetBySize14");
                        //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                        return false;
                    }
                    if (w * 9 > h * 16) {
                        rectT.set(nSrcPosX + (w - h * 8 / 9) / 2, nSrcPosY + h / 4, nSrcPosX + h
                                * 8 / 9 + (w - h * 8 / 9)
                                / 2, nSrcPosY + h * 3 / 4);
                        if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                            int ty = rectT.height();
                            rectT.top = nSrcPosY + (h - CUR_MIN_FINAL_WIDTH) / 2;
                            rectT.bottom = nSrcPosY + (h + CUR_MIN_FINAL_WIDTH) / 2;
                            if (rectT.height() < CUR_MIN_FINAL_WIDTH) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize15");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.height() - ty) / 2 * 16 / 9;
                            rectT.left -= vn;
                            rectT.right += vn;
                        }
                    } else {
                        rectT.set(nSrcPosX + w / 4, nSrcPosY + (h - w * 9 / 32) / 2, nSrcPosX + w
                                * 3 / 4, nSrcPosY + w * 9
                                / 32 + (h - w * 9 / 32) / 2);
                        if (rectT.width() < CUR_MIN_FINAL_WIDTH * 16 / 9) {
                            int tx = rectT.width();
                            rectT.left = nSrcPosX + (w - CUR_MIN_FINAL_WIDTH * 16 / 9) / 2;
                            rectT.right = nSrcPosX + (w + CUR_MIN_FINAL_WIDTH * 16 / 9) / 2;
                            if (rectT.width() < CUR_MIN_FINAL_WIDTH * 16 / 9) {
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                                Log.w("ViewEditCut","fengke setRectSelcetBySize16");
                                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                                return false;
                            }
                            int vn = (rectT.width() - tx) / 2 * 9 / 16;
                            rectT.top -= vn;
                            rectT.bottom += vn;
                        }
                    }
                    if (rectT.height() < MIN_WIDTH && MIN_WIDTH * 16 / 9 <= w) {//
                        dx = ((float) MIN_WIDTH * 16 / 9 - rectT.width()) / 2;
                        rectT.left -= dx;
                        rectT.right += dx;
                        dy = (float) (MIN_WIDTH - rectT.height()) / 2;
                        rectT.top -= dy;
                        rectT.bottom += dy;
                    }
                    break;
            }
            if (rectT.width() < MIN_WIDTH || rectT.height() < MIN_WIDTH) {
                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
                Log.w("ViewEditCut","fengke setRectSelcetBySize17");
                //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end
                return false;
            }
            rectSelect.set(rectT);
            rectSlider[0].set(rectSelect.left - bmpDot.getWidth() / 2,
                    rectSelect.top - bmpDot.getHeight() / 2,
                    rectSelect.left + bmpDot.getWidth() / 2, rectSelect.top + bmpDot.getHeight()
                            / 2);
            rectSlider[1].set(rectSelect.right - bmpDot.getWidth() / 2,
                    rectSelect.top - bmpDot.getHeight() / 2,
                    rectSelect.right + bmpDot.getWidth() / 2, rectSelect.top + bmpDot.getHeight()
                            / 2);
            rectSlider[2].set(rectSelect.left - bmpDot.getWidth() / 2,
                    rectSelect.bottom - bmpDot.getHeight() / 2,
                    rectSelect.left + bmpDot.getWidth() / 2, rectSelect.bottom + bmpDot.getHeight()
                            / 2);
            rectSlider[3].set(rectSelect.right - bmpDot.getWidth() / 2,
                    rectSelect.bottom - bmpDot.getHeight() / 2,
                    rectSelect.right + bmpDot.getWidth() / 2,
                    rectSelect.bottom + bmpDot.getHeight() / 2);

            invalidate();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    public boolean Release() {
        try {
            if (bmpBack != null && !bmpBack.isRecycled()) {
                bmpBack.recycle();
                bmpBack = null;
            }
            if (bmpDot != null && !bmpDot.isRecycled()) {
                bmpDot.recycle();
                bmpDot = null;
            }
            if (bmpDotSelect != null && !bmpDotSelect.isRecycled()) {
                bmpDotSelect.recycle();
                bmpDotSelect = null;
            }
            m_tool.cancel();
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
        return true;
    }

    private int nOriginal = 0;
    public int nMouseState = -1;
    private Point ptLast = new Point();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();
        // int pointerCount = VerifyManager.getPointerCount(event);
        int pointerCount = 1;
        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                try {
                    mListener.onCutViewTouched();
                } catch (Exception e) {
                    // MTDebug.PrintError(e);
                }
                int x = (int) event.getX();
                int y = (int) event.getY();
                nMouseState = getClickPart(x, y);
                if (nMouseState >= 0) {
                    ptLast.set(x, y);
                    setPressedDot(nMouseState);
                }

            }
            if (action == MotionEvent.ACTION_MOVE) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 start
                if (mIsNotFit == true) {
                    return true;
                }
                //PR865418 modify photoedit not fitsize crop picture error by fengke at 2014.12.10 end
                if (nMouseState >= 0 && isPointInView(x, y)) {
                    moveSlider(nMouseState, x - ptLast.x, y - ptLast.y);
                    ptLast.set(x, y);
                }
            } else if (action == MotionEvent.ACTION_UP) {
                if (nMouseState >= 0) {
                    setPressedDot(-1);
                }
                nMouseState = -1;
                nOriginal = 0;
            }
        } else if (pointerCount == 2) {
            // int x1 = (int) (VerifyMothod.getX(event, 0));
            // int y1 = (int) (VerifyMothod.getY(event, 0) - getHeight());
            // int x2 = (int) (VerifyMothod.getX(event, 1));
            // int y2 = (int) (VerifyMothod.getY(event, 1) - getHeight());
            // int n = (int) (Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1
            // - y2)));
            // if (nOriginal == 0) {
            // nOriginal = n;
            // } else {
            // float scale = 1.0f * (1.0f + 1.0f * (n - nOriginal) / 400);
            // resize(scale, 0, 0);
            // nOriginal = n;
            // }
        }
        // return super.onTouchEvent(event);
        return true;
    }

    public OnViewEditCutTouchListener mListener = null;

    public interface OnViewEditCutTouchListener {
        public void onCutViewTouched();
    }

    public void setHost(Fragment fg) {
        try {
            mListener = (OnViewEditCutTouchListener) fg;
        } catch (Exception e) {
            // MTDebug.PrintError(e);
        }
    }
}
