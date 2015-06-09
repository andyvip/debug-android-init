package com.jrdcom.timetool.alarm.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.R.string;
import android.app.AlarmManager;
import android.content.Context;
import android.drm.DrmManagerClient;
import android.util.Log;

public class ReflectionTool {
	public boolean isMTKDrmEnable() {
		try {
			// Class myClass = Class.forName("DrmManagerClient");
			// Method method = myClass.getMethod("isMTKDrmEnable", null);
			return (Boolean) DrmManagerClient.class.getMethod("isMTKDrmEnable")
					.invoke(null);
			// method.setAccessible(true);
			// method.invoke(myClass, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("huihui", "DrmManagerClient未找到");
			e.printStackTrace();
		}
		return false;
	}

	public boolean isDrm(String alertPath) {
		try {
			// Class myClass=Class.forName("DrmManagerClient");
			// Method method = myClass.getMethod("isDrm", null);
			return (Boolean) DrmManagerClient.class.getMethod("isDrm",
					String.class).invoke(null, alertPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public static DrmManagerClient getInstance(Context context) {

		try {
			return (DrmManagerClient) DrmManagerClient.class.getMethod(
					"getInstance", Context.class).invoke(null, context);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static boolean hasCountConstraint(DrmManagerClient dClient,
			String text) {
		try {
			return (Boolean) dClient.getClass()
					.getMethod("hasCountConstraint", String.class)
					.invoke(dClient, text);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public static void cancelPoweroffAlarm(AlarmManager am, String text) {
		try {
			am.getClass().getMethod("cancelPoweroffAlarm", String.class)
					.invoke(am, text);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
