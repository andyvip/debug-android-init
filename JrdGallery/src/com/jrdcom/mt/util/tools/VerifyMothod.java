
package com.mt.util.tools;

import android.view.MotionEvent;

public class VerifyMothod {

    public static float getX(MotionEvent event, int loc)
    {
        return event.getX(loc);
    }

    public static float getY(MotionEvent event, int loc)
    {
        return event.getY(loc);
    }
}
