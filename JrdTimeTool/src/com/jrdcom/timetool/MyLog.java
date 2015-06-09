/* File Name:MyLog.java
 * Version:V1.0
 * Author:jingjiang.yu
 * Date:2011-7-23 01:40:06PM
 * CopyRight (c) 2011, TCL Communication All Rights Reserved.
 */

package com.jrdcom.timetool;

import android.util.Log;
import com.android.deskclock.R;

/**
 * @author jingjiang.yu
 * @since V 1.0
 * @Date 2011-7-23 01:40:06PM
 */
public class MyLog {

	private static final boolean IS_DEBUG = false;

	public static final String TAG = "TimeTool";

	public static void debug(String message, Class<?> currClass) {
		if (IS_DEBUG) {
			StringBuilder msgBuilder = new StringBuilder();
			msgBuilder.append("<");
			msgBuilder.append(currClass.getName());
			msgBuilder.append("> ");
			msgBuilder.append(message);
			Log.d(TAG, msgBuilder.toString());
		}
	}

	public static void warn(String message, Class<?> currClass) {
		StringBuilder msgBuilder = new StringBuilder();
		msgBuilder.append("<");
		msgBuilder.append(currClass.getName());
		msgBuilder.append("> ");
		msgBuilder.append(message);

		Log.w(TAG, msgBuilder.toString());
	}

	public static void warn(String message, Throwable tr, Class<?> currClass) {
		StringBuilder msgBuilder = new StringBuilder();
		msgBuilder.append("<");
		msgBuilder.append(currClass.getName());
		msgBuilder.append("> ");
		msgBuilder.append(message);

		Log.w(TAG, msgBuilder.toString(), tr);
	}

	public static void error(String message, Class<?> currClass) {
		StringBuilder msgBuilder = new StringBuilder();
		msgBuilder.append("<");
		msgBuilder.append(currClass.getName());
		msgBuilder.append("> ");
		msgBuilder.append(message);

		Log.e(TAG, msgBuilder.toString());
	}

	public static void error(String message, Throwable tr, Class<?> currClass) {
		StringBuilder msgBuilder = new StringBuilder();
		msgBuilder.append("<");
		msgBuilder.append(currClass.getName());
		msgBuilder.append("> ");
		msgBuilder.append(message);

		Log.e(TAG, msgBuilder.toString(), tr);
	}

    public static void i(String message) {
//      Log.i(TAG, message);
    }
    public static void e(String message) {
      Log.i(TAG, message);
    }
	
	

}
