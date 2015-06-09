package com.jrdcom.mt.mtxx.controls;

import java.lang.ref.WeakReference;

import com.jrdcom.mt.mtxx.tools.UnBackKeyDialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import com.jrdcom.android.gallery3d.R;


/**
 * 自定义ProgressDialog  
 * @author d.cc 
 *
 */
public abstract class MtprogressDialog {

	private WeakReference<Context> mContext;
	//是否可以点击back按钮
	private boolean mBackable = true;
	private String txt_progress = "处理中";//文字

	public MtprogressDialog(Context context) {
		mContext = new WeakReference<Context>(context);
		txt_progress = context.getString(R.string.progress_dialog_title);
	}
	/**
	 * @param context
	 * @param backable 是否可以点击back按钮
	 */
	public MtprogressDialog(Context context,boolean backable) {
		mContext = new WeakReference<Context>(context);
		mBackable = backable;
		txt_progress = context.getString(R.string.progress_dialog_title);
	}
	
	/**
	 * 
	 * @param context
	 * @param backable 是否可以点击back按钮
	 * @param msg 文字信息
	 */
	public MtprogressDialog(Context context,boolean backable,String msg) {
		mContext = new WeakReference<Context>(context);
		mBackable = backable;
		txt_progress = msg;
	}

	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// // TODO Auto-generated method stub
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.mtprogress_dialog_view);
	// }

	/**
	 * 处理当前的事
	 */
	public abstract void process();

	public void show() {
		new Thread() {
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

	private Dialog Progress() {
		Dialog dialog = null;
		if(mBackable){
			dialog = new Dialog(mContext.get(), R.style.progressdialog);
		}else{
			dialog = new UnBackKeyDialog(mContext.get(), R.style.progressdialog);
		}
		dialog.setContentView(R.layout.mtprogress_dialog_view);
		TextView txt = (TextView) dialog.findViewById(R.id.txt_progress);
		txt.setText(txt_progress);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		return dialog;

	}

}
