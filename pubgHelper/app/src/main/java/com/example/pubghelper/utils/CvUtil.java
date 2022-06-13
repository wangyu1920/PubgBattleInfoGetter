package com.example.pubghelper.utils;

import android.graphics.Bitmap;

import com.example.pubghelper.adapter.FloatAdapter;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CvUtil {
    public static int[] getPixelNum(Mat src,int[] rect) {
        int[] result = new int[]{0,0,0,0};
        Mat dst = src.clone();
        Imgproc.rectangle(dst,new Point(0,0),new Point(dst.cols(),dst.rows()),new Scalar(255,0,0),-1);
        int cols = src.cols();
        int rows = src.rows();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                double[] doubles = src.get(j, i);
                if ((i > rect[0] && i < rect[2]) && (j > rect[1] && j < rect[3])) {

                    if (doubles[0] == 0) {//黑色点
                        result[2] += 1;
                    } else {
                        result[3] += 1;
                    }
                } else {
                    if (doubles[0] == 0) {//黑色点
                        result[0] += 1;
                    } else {
                        result[1] += 1;
                    }
                }

            }
        }
        return result;
    }

    public static int[] getPixelNum(Mat src) {
        int[] result = new int[]{0,0,0,0};
        Mat dst = src.clone();
        Imgproc.rectangle(dst,new Point(0,0),new Point(dst.cols(),dst.rows()),new Scalar(255,0,0),-1);
        int cols = src.cols();
        int rows = src.rows();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                double[] doubles = src.get(j, i);

                    if (doubles[0] == 0) {//黑色点
                        result[0] += 1;
                    } else {
                        result[1] += 1;
                    }


            }
        }
        return result;
    }

    public static Mat autoProc(Mat src,LOCATION location) {
        //剪裁
        Rect rect = new Rect(location.RECT[0],location.RECT[1], location.getWidth(), location.getHeight());
        Mat dst = new Mat(src, rect);
        //HSV色彩识别
        Imgproc.cvtColor(dst,dst,Imgproc.COLOR_BGR2HSV);
        Core.inRange(dst,new Scalar(0,0,170),new Scalar(255,20,255),dst);
        //闭运算
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,10));
        Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, kernel);
        return dst;
    }

    public static double[] getScores(Bitmap bitmap, LOCATION location) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap,src);
        //旋转
        //Imgproc.warpAffine(src, src, Imgproc.getRotationMatrix2D(new Point(src.width() >> 1, src.height() >> 1), 90, 1.0), new Size(src.height(),src.width()));
        Mat dst = autoProc(src, location);
        int x0=location.RECT2[0]-location.RECT[0];
        int y0=location.RECT2[1]-location.RECT[1]+3;
        int x1=dst.cols();
        int y1=location.RECT2[3]-location.RECT[1]+1;
        int[] result = getPixelNum(dst, new int[]{x0, y0, x1, y1});
        double score1 = (double) result[0] / ((double)(result[0]) + (double) result[1]);
        double score2 = (double) result[2] / ((double)(result[2]) + (double) result[3]);
        return new double[]{score1, score2};
    }

    public static double getWhiteRate(Mat H1,Mat H2) {
        Mat comp = new Mat();
        Core.bitwise_xor(H1, H2, comp);
        int[] result1=getPixelNum(comp);
        double v = result1[1] / ((double) result1[0] + result1[1]);
        comp.release();
        return v;
    }

    public static class ResultGetter {
        Mat lastResult;
        Mat last=null;
        boolean lastSame=false;
        boolean lastReturn=false;
        long lastReturnTrueTime=0;
        public ResultGetter() {
        }


        public FloatAdapter.DataBean process(Bitmap bitmap, LOCATION location , int width) {
            Mat toSave = new Mat();
            Mat src = new Mat();
            Utils.bitmapToMat(bitmap,src);
            //剪裁
            Rect rect = new Rect(location.RECT[0],location.RECT[1], location.getWidth(), location.getHeight());
            Mat dst = new Mat(src, rect);

            //HSV色彩识别
            Imgproc.cvtColor(dst,dst,Imgproc.COLOR_BGR2HSV);
            Core.inRange(dst,new Scalar(0,0,180),new Scalar(255,15,255),toSave);
            if (last != null) {
                double v = getWhiteRate(last, toSave);
                if (v < 0.05) {//相同
                    if (lastSame) {
                        //闭运算
                        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,10));
                        Imgproc.morphologyEx(toSave, dst, Imgproc.MORPH_CLOSE, kernel);
                        //定义采样区
                        int x0=location.RECT2[0]-location.RECT[0];
                        int y0=location.RECT2[1]-location.RECT[1]+3;
                        int x1=dst.cols();
                        int y1=location.RECT2[3]-location.RECT[1]+1;
                        //计算黑白像素个数
                        int[] result = getPixelNum(dst, new int[]{x0, y0, x1, y1});
                        double score1 = (double) result[0] / ((double)(result[0]) + (double) result[1]);
                        double score2 = (double) result[2] / ((double)(result[2]) + (double) result[3]);
                        if (score1 > 0.7 && score2 > 0.2 && score2 < 0.58) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastReturnTrueTime > 3990) {
                                lastResult = toSave;
                                lastReturn = true;
                                lastReturnTrueTime = currentTime;
                                dst.release();
                                return getReturn(src,location,width,score1,score2);
                            } else {
                                if (lastReturn) {
                                    src.release();
                                    dst.release();
                                    return null;
                                } else {
                                    dst.release();
                                    lastReturnTrueTime = currentTime;
                                    lastResult = toSave;
                                    lastReturn = true;
                                    return getReturn(src,location,width, score1, score2);
                                }
                            }

                        } else {

                            src.release();
                            dst.release();
                            last = toSave;
                            lastReturn = false;
                            return null;
                        }
                    } else {
                        lastSame = true;
                        last = toSave;
                        lastReturn = false;
                        return null;
                    }
                } else {
                    lastSame = false;
                    last = toSave;
                    lastReturn = false;
                    return null;
                }
            } else {
                lastSame = false;
                last = toSave;
                lastReturn = false;
                return null;
            }

        }

        private FloatAdapter.DataBean getReturn(Mat src, LOCATION location, int width, double score1, double score2) {
            //剪裁
            Rect rect = new Rect(location.RECT[0],location.RECT[1],width, location.getHeight());
            Mat dst = new Mat(src, rect);
            Bitmap bitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, bitmap);
            dst.release();
            src.release();
            return new FloatAdapter.DataBean(bitmap, new double[]{score1, score2});

        }
    }

}
