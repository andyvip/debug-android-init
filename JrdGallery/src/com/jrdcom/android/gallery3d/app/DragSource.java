package com.jrdcom.android.gallery3d.app;

import  android.view.View;

public class DragSource {

    public static  int drag_p_x;
    public static  int drag_p_y;
    public static  int drag_p_left;
    public static  int drag_p_top;
    public static int drag_drop_group_index  = -1;
    public static int scrolledX;
    public static int scrolledY;
    public static int drag_height = 80;
    public static int drag_margin_pixel = 30;
    
    public static int group_position ;
    public static int child_position;
    public static  View drag_view;
    public static boolean isClosed = false;
    

}
