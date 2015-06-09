/* Fiel Name:MyGallery.java
 * Version:V1.0
 * Author:jingjiang.yu
 * Date:2011-9-2 06:53:03PM
 * CopyRight (c) 2011, TCL Communication All Rights Reserved.
 */

package com.jrdcom.timetool.worldclock.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;


/**
 * @author jingjiang.yu
 * @since V 1.0
 */
public class MyGallery extends Gallery {

	public MyGallery(Context context) {
		super(context);
	}

	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		super.getChildStaticTransformation(child, t);

		Matrix matrix = t.getMatrix();
		Camera camera = new Camera();
		if (child.isSelected()) {

			camera.translate(-7, 25, -50);

		} else {
			camera.translate(10, 0, 50);
		}
		camera.getMatrix(matrix);
		return true;
	}

}
