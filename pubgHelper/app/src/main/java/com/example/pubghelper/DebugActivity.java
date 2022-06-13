package com.example.pubghelper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pubghelper.adapter.BtnAdapter;
import com.example.pubghelper.adapter.ImgAdapter;
import com.example.pubghelper.service.CaptureService;
import com.example.pubghelper.utils.CvUtil;
import com.example.pubghelper.utils.DocumentFileUtils;
import com.example.pubghelper.utils.LOCATION;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DebugActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 33;
    private static final int REQUEST_CODE = 44;
    String TAG = "DebugActivity";
    Activity activity;
    RecyclerView recyclerView_img;
    RecyclerView recyclerView_btn;
    List<Button> buttons;
    List<Bitmap> bitmaps;
    Bitmap initialImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_main);
        Log.d(TAG, OpenCVLoader.initDebug() ? "OpenCV loaded successfully!" : "OpenCV loaded failed!");
        if (!DocumentFileUtils.isGrant$File(this)) {
            DocumentFileUtils.startForRoot$File(this,1);
        }
        initButton();
        initRecyclerview();


    }

    private void initButton() {
        buttons=new ArrayList<>();
        Button startFloat = new Button(this);
        startFloat.setText("打开悬浮窗");
        startFloat.setOnClickListener(v -> {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE);
        });
        buttons.add(startFloat);
        Button readImg = new Button(this);
        readImg.setText("读取图片");
        readImg.setOnClickListener(getListenerOfReadImg());
        buttons.add(readImg);
        Button autoProc = new Button(this);
        autoProc.setText("自动处理");
        autoProc.setOnClickListener(v -> {
            Mat src = new Mat();
            Utils.bitmapToMat(bitmaps.get(bitmaps.size() - 1), src);
            Mat dst = CvUtil.autoProc(src, new LOCATION());
            Bitmap newBitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, newBitmap);
            bitmaps.add(newBitmap);
            initRecyclerview_img();
        });
        buttons.add(autoProc);
        Button getScores = new Button(this);
        getScores.setText("计算分数");
        getScores.setOnClickListener(v -> {
            double[] scores= CvUtil.getScores(initialImg, new LOCATION());
            Toast.makeText(activity, scores[0] + " " + scores[1], Toast.LENGTH_LONG).show();
        });
        buttons.add(getScores);
        Button threshold = new Button(this);
        threshold.setText("普通二值化");
        threshold.setOnClickListener(getListenerOfThreshold());
        buttons.add(threshold);
        Button threshold2 = new Button(this);
        threshold2.setText("自动二值化");
        threshold2.setOnClickListener(getListenerOfThreshold2());
        buttons.add(threshold2);
        Button morphologyEx = new Button(this);
        morphologyEx.setText("开/闭运算");
        morphologyEx.setOnClickListener(getListenerOfMorphologyEx());
        buttons.add(morphologyEx);
        Button cut = new Button(this);
        cut.setText("剪裁");
        cut.setOnClickListener(getListenerOfCut());
        buttons.add(cut);
        Button inRange = new Button(this);
        inRange.setText("颜色检测");
        inRange.setOnClickListener(getListenerOfInRange());
        buttons.add(inRange);
        Button getPixelNum = new Button(this);
        getPixelNum.setText("计算像素点数");
        getPixelNum.setOnClickListener(v -> {
            Mat src = new Mat();
            Utils.bitmapToMat(bitmaps.get(bitmaps.size() - 1), src);
            int[] result = CvUtil.getPixelNum(src,new int[]{24,8,src.cols()-1,33});
            Mat dst = new Mat(src, new Rect(new Point(24, 8), new Point(src.cols()-1, 33)));
            Bitmap newBitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, newBitmap);
            bitmaps.add(newBitmap);
            initRecyclerview_img();
            Toast.makeText(activity,result[0]+" "+result[1]+" "+result[2]+" "+result[3],Toast.LENGTH_LONG).show();
        });
        buttons.add(getPixelNum);

    }

    private View.OnClickListener getListenerOfInRange() {
        return v -> {
            Mat src = new Mat();
            Mat dst = new Mat();
            Utils.bitmapToMat(bitmaps.get(bitmaps.size() - 1), src);
            Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2HSV);
            Core.inRange(src,new Scalar(0,0,170),new Scalar(255,20,255),dst);
            Bitmap newBitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, newBitmap);
            bitmaps.add(newBitmap);
            initRecyclerview_img();
        };
    }

    private View.OnClickListener getListenerOfCut() {
        return v -> {
            Mat src = new Mat();
            Utils.bitmapToMat(bitmaps.get(bitmaps.size()-1), src);
            Rect rect = new Rect(new Point(80, 376), new Point(438, 414));
            Mat dst = new Mat(src, rect);
            Bitmap newBitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, newBitmap);
            bitmaps.add(newBitmap);
            initialImg = newBitmap;
            initRecyclerview_img();
        };
    }

    private View.OnClickListener getListenerOfMorphologyEx() {
        return v -> {
            final int[] i = {0};
            new AlertDialog.Builder(activity)
                    .setSingleChoiceItems(new CharSequence[]{"开运算", "闭运算"}, 0, (dialog, which) -> i[0] = which)
                    .setPositiveButton("确定", (dialog, which) -> {
                        SeekBar seekBar = new SeekBar(activity);
                        seekBar.setMax(36);
                        TextView textView = new TextView(activity);
                        LinearLayout linearLayout = new LinearLayout(activity);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                        linearLayout.addView(seekBar);
                        linearLayout.addView(textView);
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                textView.setText(String.valueOf(progress));
                            }
                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {}
                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                        new AlertDialog.Builder(activity)
                                .setView(linearLayout)
                                .setPositiveButton("确定", (dialog2, which2) -> {
                                    int size = seekBar.getProgress();//卷积核大小
                                    Mat src = new Mat();
                                    Mat dst = new Mat();
                                    Utils.bitmapToMat(bitmaps.get(bitmaps.size()-1), src);
                                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(size, size));
                                    Imgproc.morphologyEx(src, dst, i[0] == 0 ? Imgproc.MORPH_OPEN : Imgproc.MORPH_CLOSE, kernel);
                                    Bitmap newBitmap = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
                                    Utils.matToBitmap(dst, newBitmap);
                                    bitmaps.add(newBitmap);
                                    initRecyclerview_img();
                                })
                                .setNegativeButton("取消", null)
                                .setTitle("请输入阈值")
                                .create().show();
                    })
                    .setNegativeButton("取消", null)
                    .setTitle("选择模式")
                    .create().show();
        };
    }

    private View.OnClickListener getListenerOfThreshold2() {
        return v -> {
            if (initialImg == null) { return; }
            EditText editText = new EditText(activity);
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            new AlertDialog.Builder(activity)
                    .setView(editText)
                    .setPositiveButton("确定", (dialog, which) -> {
                        Mat src = new Mat();
                        Mat dst = new Mat();
                        Utils.bitmapToMat(initialImg, src);
                        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
                        Imgproc.adaptiveThreshold(src, dst, 255,
                                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                Imgproc.THRESH_BINARY,
                                Integer.parseInt(editText.getText().toString()),5);

                        Bitmap newBitmap= Bitmap.createBitmap(dst.width(),dst.height(), Bitmap.Config.ARGB_8888 );
                        Utils.matToBitmap(dst, newBitmap);
                        bitmaps.add(newBitmap);
                        initRecyclerview_img();
                    })
                    .setNegativeButton("取消", null)
                    .setTitle("请输入阈值")
                    .create().show();

        };
    }

    private View.OnClickListener getListenerOfThreshold() {
        return v -> {
            if (initialImg == null) { return; }
            EditText editText = new EditText(activity);
            editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            new AlertDialog.Builder(activity)
                    .setView(editText)
                    .setPositiveButton("确定", (dialog, which) -> {
                        Mat src = new Mat();
                        Mat dst = new Mat();
                        Utils.bitmapToMat(initialImg, src);
                        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
                        Imgproc.threshold(src, dst,
                                Double.parseDouble(editText.getText().toString()),
                                255, Imgproc.THRESH_BINARY);
                        Bitmap newBitmap= Bitmap.createBitmap(dst.width(),dst.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(dst, newBitmap);
                        bitmaps.add(newBitmap);
                        initRecyclerview_img();
                    })
                    .setNegativeButton("取消", null)
                    .setTitle("请输入阈值")
                    .create().show();

        };
    }

    private View.OnClickListener getListenerOfReadImg() {
        return v -> {
            EditText editText = new EditText(activity);
            new AlertDialog.Builder(activity)
                    .setView(editText)
                    .setPositiveButton("确定", (dialog, which) -> {
                        initialImg = BitmapFactory.decodeFile(editText.getText().toString());
                        if (bitmaps == null) {
                            bitmaps = new ArrayList<>();
                        }
                        bitmaps.add(initialImg);
                        initRecyclerview_img();
                    })
                    .setNegativeButton("取消", null)
                    .setTitle("请输入路径")
                    .create().show();
        };
    }


    private void initRecyclerview() {
        initRecyclerview_img();
        initRecyclerview_btn();
    }

    private void initRecyclerview_btn() {
        recyclerView_btn = findViewById(R.id.recyclerview_btn);
        recyclerView_btn.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_btn.setAdapter(new BtnAdapter(activity, buttons));
    }

    private void initRecyclerview_img() {
        recyclerView_img = findViewById(R.id.recyclerview_img);
        recyclerView_img.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_img.setAdapter(new ImgAdapter(activity, bitmaps));
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 2);
    }


    //权限检查，连接录屏服务
    public void checkPermission() {
        //调用检查权限接口进行权限检查
        if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)){
            //如果没有权限，获取权限
            //调用请求权限接口进行权限申请
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},PERMISSION_REQUEST_CODE);
        }
    }

    //没有权限，去请求权限后，需要判断是否请求成功
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE){
            //请求码相同
            if(grantResults.length != 0 &&
                    ((grantResults[0] != PackageManager.PERMISSION_GRANTED) ||
                            (grantResults[1] != PackageManager.PERMISSION_GRANTED))){
                //如果结果都存在，但是至少一个没请求成功，弹出提示
                Toast.makeText(activity,"请同意必须的应用权限，否则无法正常使用该功能！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    //返回方法，获取返回的信息
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //首先判断请求码是否一致，结果是否ok
        System.out.println("lllldsdsdsd");
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            //录屏请求成功，使用工具MediaProjection录屏
            //从发送获得的数据和结果中获取该工具
             if (Settings.canDrawOverlays(this)) {
                 checkPermission();
                 System.out.println("dsahdhsjdshdd");
                 startForegroundService(CaptureService.getIntent(activity, requestCode, data));
            } else {
                askPermission();
            }


        }
    }
}