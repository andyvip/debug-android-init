/******************************************************************************************************************/
/*                                                                         Date : 08/2013  */
/*                            PRESENTATION                                                 */
/*              Copyright (c) 2010 JRD Communications, Inc.                                */
/******************************************************************************************************************/
/*                                                                                                                */
/*    This material is company confidential, cannot be reproduced in any                   */
/*    form without the written permission of JRD Communications, Inc.                      */
/*                                                                                                                */
/*================================================================================================================*/
/*   Author :                                                                              */
/*   Role :                                                                                */
/*   Reference documents :                                                                 */
/*================================================================================================================*/
/* Comments :                                                                              */
/*     file    :packages/apps/JrdGallery/src/com/jrdcom/mt/mtxx/controls/DialogWait.java              */
/*     Labels  :                                                                           */
/*================================================================================================================*/
/* Modifications   (month/day/year)                                                        */
/*================================================================================================================*/
/* date    | author       |FeatureID                |modification                          */
/*============|==============|=========================|==========================================================*/
/*08/06/13 | zhangcheng |PR498772-zhangcheng-001 |Pop up gallery force close when tap home key during loanding images. */
/*============|==============|=========================|==========================================================*/
package com.jrdcom.mt.mtxx.controls;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;
import com.jrdcom.android.gallery3d.filtershow.PauseListener;

/**
 * 等待对话框
 * 
 * @author notepaper
 * 
 */

public abstract class DialogWait implements PauseListener {
	private WeakReference<Context> mContext;
	private String mTitle;
	private String mContent;
	//add by zhangcheng for PR498772 begin	
	private ProgressDialog mProgressDialog;
	//add by zhangcheng for PR498772 end

	public DialogWait(Context context, String title, String content) {
		mContext = new WeakReference<Context>(context);
		mTitle = title;
		mContent = content;
		//add by zhangcheng for PR498772 begin
		((OnSetPauseListener)context).setPauseListener(this);
		//add by zhangcheng for PR498772 end
	}
	
	public DialogWait(Context context) {
		mContext = new WeakReference<Context>(context);
	}

	//add by zhangcheng for PR498772 begin
	public void onPause(){
		if((mProgressDialog != null) && mProgressDialog.isShowing()) mProgressDialog.dismiss();
	}
	//add by zhangcheng for PR498772 end

	/**
	 * 处理当前的事
	 */
	public abstract void process();
	
	//modify by zhangcheng for PR498772 begin
	public void run() {
		mProgressDialog = ProgressDialog.show(mContext.get(), mTitle, mContent, true);
		new Thread() {
			//ProgressDialog myDialog = ProgressDialog.show(mContext.get(), mTitle, mContent, true);

			public void run() {
				try {
					process();
				} catch (Exception e) {
				} finally {
             				if(mProgressDialog.isShowing())	mProgressDialog.dismiss();
				}
			}
		}.start();
		
	}
	//modify by zhangcheng for PR498772 end

	//小的ProgressDialog展示
	public void show(){
		new Thread(){
			
			Dialog dialog = Progress();
			@Override
			public void run() {
				try {
					process();
				} catch (Exception e) {
				} finally {
					dialog.dismiss();
				}
			}
			
		}.start();
		
	}
	
	private Dialog Progress(){
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
		ProgressBar progressBar = new ProgressBar(mContext.get());
		builder.setMessage("处理中");
		builder.setView(progressBar);
		dialog = builder.create();
		dialog.show();
		return dialog;
		
	}
	
	
}
