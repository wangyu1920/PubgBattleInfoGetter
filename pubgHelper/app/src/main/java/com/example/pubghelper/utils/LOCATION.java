package com.example.pubghelper.utils;

public class LOCATION {
    public LOCATION() {
        RECT = new int[]{80, 376, 250, 414};
        RECT2 = new int[]{104, 381, 250, 408};
    }
    //淘汰信息图案范围，左上角坐标和右下角坐标
    public int[] RECT;
    //内部文字范围
    public int[] RECT2;
    public int getWidth(){
        return RECT[2]-RECT[0];
    }
    public int getWidth2(){
        return RECT2[2]-RECT2[0];
    }
    public int getHeight() {
        return RECT[3]-RECT[1];
    }
    public int getHeight2() {
        return RECT2[3]-RECT2[1];
    }
}
