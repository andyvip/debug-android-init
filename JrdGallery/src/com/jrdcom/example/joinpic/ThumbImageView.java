package com.jrdcom.example.joinpic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import com.jrdcom.android.gallery3d.R;

public class ThumbImageView extends ImageView {
	private static Paint painta;
	private static Paint paintb;
	private static Drawable drawable = null;
	private Rect rect = new Rect();
	private AlphaAnimation alphaAnimation;
	private boolean f = false;
	private boolean g = true;

	public ThumbImageView(Context paramContext) {
		super(paramContext);
		creatPaint();
	}

	public ThumbImageView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		creatPaint();
	}

	public ThumbImageView(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		creatPaint();
	}

	private void creatPaint() {
		painta = new Paint(3);
		painta.setColor(-1);
		painta.setStrokeWidth(10.0F);
		painta.setStyle(Paint.Style.STROKE);
		paintb = new Paint(3);
		paintb.setColor(-1);
		paintb.setStyle(Paint.Style.FILL);
		paintb.setAlpha(100);
		this.alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
		this.alphaAnimation.setDuration(200L);
	}

	public boolean a() {
		return this.g;
	}

	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		getDrawingRect(this.rect);
		Rect localRect1 = this.rect;
		localRect1.left = (1 + localRect1.left);
		Rect localRect2 = this.rect;
		localRect2.top = (1 + localRect2.top);
		Rect localRect3 = this.rect;
		localRect3.right = (-1 + localRect3.right);
		Rect localRect4 = this.rect;
		localRect4.bottom = (-1 + localRect4.bottom);
		paramCanvas.drawRect(this.rect, painta);
		if ((this.f) && (!this.g)) {
			paramCanvas.drawRect(this.rect, paintb);
			int i = drawable.getMinimumWidth();
			int j = drawable.getMinimumHeight();
			this.rect.left = (this.rect.right - i);
			this.rect.top = (this.rect.bottom - j);
			drawable.setBounds(this.rect);
			drawable.draw(paramCanvas);
		}
	}

	public void setChecked(boolean paramBoolean) {
		this.f = paramBoolean;
		invalidate();
	}

	public void setImageBitmap(Bitmap paramBitmap) {
		super.setImageBitmap(paramBitmap);
		this.g = false;
		clearAnimation();
	}

	public void setImageBitmapWithAlphaAni(Bitmap paramBitmap) {
		super.setImageBitmap(paramBitmap);
		this.g = false;
		startAnimation(this.alphaAnimation);
	}

	public void setImageResource(int paramInt) {
		super.setImageResource(paramInt);
		clearAnimation();
		this.g = true;
	}

}
