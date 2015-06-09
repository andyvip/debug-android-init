/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/************************************************************************************************************/
/*                                                                                            Date : 11/2013*/
/*                                   PRESENTATION                                                           */
/*                     Copyright (c) 2012 JRD Communications, Inc.                                          */
/************************************************************************************************************/
/*                                                                                                          */
/*           This material is company confidential, cannot be reproduced in any                             */
/*           form without the written permission of JRD Communications, Inc.                                */
/*                                                                                                          */
/*==========================================================================================================*/
/*   Author :                                                                                               */
/*   Role :                                                                                                 */
/*   Reference documents :                                                                                  */
/*==========================================================================================================*/
/* Comments :                                                                                               */
/*     file    :../packages/apps/Gallery2/src/com/android/gallery3d/app/BorderImageView.java                */
/*     Labels  :                                                                                            */
/*==========================================================================================================*/
/* Modifications   (month/day/year)                                                                         */
/*==========================================================================================================*/
/* date    | author       |FeatureID                             |modification                              */
/*=========|==============|======================================|==========================================*/
/*         |              |                                      |                                          */
/*==========================================================================================================*/
/* Problems Report(PR/CR)                                                                                   */
/*==========================================================================================================*/
/* date    | author       | PR #                    |                                                       */
/*=========|==============|=========================|=======================================================*/
/*11/26/13 | Li Zhao |PR559020-Li-Zhao|The video screenshot have a white line when dragging the progress bar*/
/*==========================================================================================================*/

package com.jrdcom.android.gallery3d.app;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;


public class BorderImageView extends ImageView {

	public BorderImageView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public BorderImageView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 画边框
		Rect rect1 = getRect(canvas);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(3);
		paint.setStyle(Paint.Style.STROKE);

		// 画边框
		canvas.drawRect(rect1, paint);
		
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2);

		 //画一条竖线,模拟右边的阴影
		canvas.drawLine(rect1.right, rect1.top + 1, rect1.right,
				rect1.bottom+2, paint);
		// 画一条横线,模拟下边的阴影
		canvas.drawLine(rect1.left+1, rect1.bottom, rect1.right+2,
				rect1.bottom, paint);
		
		/*// 画一条竖线,模拟右边的阴影
		canvas.drawLine(rect1.right + 2, rect1.top + 3, rect1.right + 2,
				rect1.bottom + 3, paint);
		// 画一条横线,模拟下边的阴影
		canvas.drawLine(rect1.left + 3, rect1.bottom + 2, rect1.right + 3,
				rect1.bottom + 2, paint);*/
	}

	Rect getRect(Canvas canvas) {
		//PR559020-Li-Zhao begin
//		Rect rect = canvas.getClipBounds();
		Rect rect = new Rect(0, 0, getWidth(), getHeight());
		//PR559020-Li-Zhao end
		rect.bottom -= getPaddingBottom();
		rect.right -= getPaddingRight();
		rect.left += getPaddingLeft();
		rect.top += getPaddingTop();
		return rect;
	}
}

//imageView.setPadding(3, 3, 5, 5);

