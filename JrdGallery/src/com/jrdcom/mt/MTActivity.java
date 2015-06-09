package com.jrdcom.mt;

import com.jrdcom.mt.util.MyData;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

public abstract class MTActivity extends Activity {
	protected Resources res;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initDevData();
		res = getResources();
	}

	protected void initDevData() {
		if (MyData.nScreenW == 0 || MyData.nScreenH == 0 || MyData.nDensity == 0) {
			Display display = getWindowManager().getDefaultDisplay();
			DisplayMetrics dm = new DisplayMetrics();
			display.getMetrics(dm);
			MyData.nScreenW = dm.widthPixels;
			MyData.nScreenH = dm.heightPixels;
			MyData.nDensity = dm.density;
			MyData.nBmpDstW = MyData.nScreenW;
			MyData.nBmpDstH = MyData.nScreenH - 100;
			if (MyData.nBmpDstW < MyData.nOutPutWidth && MyData.nBmpDstH < MyData.nOutPutHeight) {
				MyData.nBmpDstW = MyData.nOutPutWidth;
				MyData.nBmpDstH = MyData.nOutPutHeight;
			}
		}
	}

    // PR 489413 jipu.xiong@tcl.com begin
    private static MTPauseLisener mMTPauseLisener;
    protected boolean isClicked = false;

    public interface MTPauseLisener {
        public void doOnPause();
    }

    public static void setMTPauseLisener(MTPauseLisener listener) {
        mMTPauseLisener = listener;
    }

    @Override
    public void onPause() {
        super.onPause();

//        if (mMTPauseLisener != null && !isClicked) {
//            mMTPauseLisener.doOnPause();
//        }
//        isClicked = false;
    }
    // PR 489413 jipu.xiong@tcl.com end

}
