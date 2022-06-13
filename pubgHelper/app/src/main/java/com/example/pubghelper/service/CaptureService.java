package com.example.pubghelper.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pubghelper.R;
import com.example.pubghelper.adapter.FloatAdapter;
import com.example.pubghelper.utils.CvUtil;
import com.example.pubghelper.utils.LOCATION;
import com.example.pubghelper.utils.ScreenUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CaptureService extends Service {
    String TAG = "CaptureService2";
    private MediaProjection mediaProjection; // 声明一个媒体投影对象
    private ImageReader mImageReader; // 声明一个图像读取器对象
    VirtualDisplay virtualDisplay;
    WindowManager mWindowManager;
    View rootView;
    int height=0,width=0;
    float DPI=0;
    RecyclerView recyclerView;
    FloatAdapter floatAdapter;
    List<FloatAdapter.DataBean> dataBeans;
    Handler myHandler;
    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler{
        public MyHandler() {
            super();
        }
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void handleMessage(@NonNull Message msg) {
            FloatAdapter.DataBean dataBean = (FloatAdapter.DataBean) msg.obj;
            dataBeans.add(dataBean);
            floatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(dataBeans.size()-1);
            super.handleMessage(msg);
        }
    }

    public static Intent getIntent(Activity activity,int CODE,Intent data) {
        Intent intent = new Intent(activity, CaptureService.class);
        int[] size = ScreenUtil.getSize(activity);
        intent.putExtra("windowHeight", size[1]);
        intent.putExtra("windowWidth", size[0]);
        intent.putExtra("windowDPI", ScreenUtil.getDPI(activity));
        intent.putExtra("data", data);
        intent.putExtra("CODE", CODE);
        return intent;
    }


    // 把Image对象转换成位图对象
    public static Bitmap getBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        return bitmap;
    }


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (intent != null&&height<1) {
            height = intent.getIntExtra("windowHeight", 1);
            width = intent.getIntExtra("windowWidth", 1);
            DPI = intent.getFloatExtra("windowDPI", 0);
            System.out.println(height+" "+width+" "+DPI);
            int mResultCode = intent.getIntExtra("code", -1);
            notification();
            // 根据屏幕宽高创建一个新的图像读取器
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 4);
            //setOnClickListener
            initListener();
            Intent mResultData = intent.getParcelableExtra("data");
            //mResultData = intent.getSelector();
            //    private static final String TAG = "CaptureService";
            // 声明一个媒体投影管理器对象
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mediaProjection = mediaProjectionManager.getMediaProjection(mResultCode, Objects.requireNonNull(mResultData));
            createVirtualDisplay();
        }
        return Service.START_NOT_STICKY;
    }


    @Nullable
    @Override
    //返回一个Binder用于通信，需要一个获取Service的方法
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        myHandler = new MyHandler();
        rootView= LayoutInflater.from(this).inflate(R.layout.float_capture, null);
        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(rootView, params);
        recyclerView = rootView.findViewById(R.id.float_recyclerview);
        dataBeans = new LinkedList<>();
        floatAdapter = new FloatAdapter(dataBeans, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(floatAdapter);
        //adding an touchListener to make drag movement of the floating widget
        rootView.findViewById(R.id.float_root).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(rootView, params);
                        return true;
                }
                return false;
            }
        });
    }
    long time=0;
    CvUtil.ResultGetter resultGetter = new CvUtil.ResultGetter();
    @SuppressLint("NotifyDataSetChanged")
    private void initListener() {
        mImageReader.setOnImageAvailableListener(reader -> {
            if ((System.currentTimeMillis() - time > 80)) {
                time = System.currentTimeMillis();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Image img = reader.acquireLatestImage();
                        FloatAdapter.DataBean process = null;
                        if (img != null) {
                            process = resultGetter.process(CaptureService.getBitmap(img), new LOCATION(), 500);
                            if (process != null) {
                                System.out.println(process.doubles[0]+" "+process.doubles[1]);
                                Message msg = new Message();
                                msg.obj = process;
                                myHandler.sendMessage(msg);
                            }
                            img.close();
                        }
                    }
                }).start();
            } else {
                Image image= reader.acquireLatestImage();
                if (image != null) {
                    image.close();
                }
            }

        },null);

    }




    private void createVirtualDisplay() {
        //虚拟屏幕通过MediaProjection获取，传入一系列传过来的参数
        //可能创建时会出错，捕获异常
        try {
            // 声明一个虚拟显示层对象
            virtualDisplay = mediaProjection.createVirtualDisplay("VirtualScreen", width, height, (int) DPI,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,"virtualDisplay创建录屏异常，请退出重试！",Toast.LENGTH_SHORT).show();
        }
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (rootView != null){
            try {
                mWindowManager.removeView(rootView);
            } catch (Exception ignore) { }
        }
        if (mImageReader != null) {
            mImageReader.close();
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        

    }



    public void notification() {
        //Call Start foreground with notification
        String NOTIFICATION_TICKER = "ssa0";
        String NOTIFICATION_CHANNEL_ID = "ssa1";
        String NOTIFICATION_CHANNEL_NAME = "ssa2";
        String NOTIFICATION_CHANNEL_DESC = "ssa3";
        int NOTIFICATION_ID = 333;
        Intent notificationIntent = new Intent(this, CaptureService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Starting Service")
                .setContentText("Starting monitoring service")
                .setTicker(NOTIFICATION_TICKER)
                .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(NOTIFICATION_CHANNEL_DESC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        startForeground(NOTIFICATION_ID, notification); //必须使用此方法显示通知，不能使用notificationManager.notify，否则还是会报上面的错误
    }

}
