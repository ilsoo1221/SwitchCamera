package com.example.switchcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.switchcamera.SCInterface.JNIListener;

public class MainActivity extends AppCompatActivity implements JNIListener {
    TextView tv;
    String str = "";
    JNIDriver mDriver;
    boolean mThreadRun = true;
    private static final String TAG = "CamTestActivity";

    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView capturedImageHolder;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_main_activity);


        mDriver = new JNIDriver();
        mDriver.setListener(this);
        if(mDriver.open("/dev/sm9s5422_interrupt")<0){
            Toast.makeText(MainActivity.this,"Driver Open Failed", Toast.LENGTH_SHORT).show();
        }
        capturedImageHolder = (ImageView) findViewById(R.id.captured_image);

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(180);

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }


    @Override
    protected void onPause(){
        mDriver.close();
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            BitmapDrawable tmp = (BitmapDrawable) capturedImageHolder.getDrawable();
            switch(msg.arg1){
                case 1:
                    capturedImageHolder.setImageBitmap(grayImage(tmp.getBitmap()));
                    break;
                case 2:
                    capturedImageHolder.setImageBitmap(rotateImage(tmp.getBitmap()));
                    break;
                case 3:
                    capturedImageHolder.setImageBitmap(resizeBitmap(tmp.getBitmap()));
                    break;
                case 4:
                    break;
                case 5:
                    mCamera.takePicture(null, null, pictureCallback);
                    break;
            }
        }
    };
    @Override
    protected  void onResume(){
        super.onResume();
    }
    @Override
    public void onReceive(int val){
        Message text = Message.obtain();
        text.arg1 = val;
        handler.sendMessage(text);
    }
    private void releaseMediaRecorder() {
        mCamera.lock();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {

        }
        return c;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1001 :

                getCameraInstance();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public Bitmap grayImage(Bitmap src){
        int w = src.getWidth();
        int h = src.getHeight();
        int a,r,g,b;
        int pixel;
        for(int x = 0; x < w; x++){
            for(int y = 0; y < h; y++){
                pixel=src.getPixel(x,y);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);
                int gr = (int)(r*0.299+g*0.587+b*0.114);
                src.setPixel(x,y,Color.argb(a,gr,gr,gr));
            }
        }
        return src;
    }
    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println(data);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int w = bitmap.getWidth() / 2;
            int h = bitmap.getHeight() / 2;
            Matrix mtx = new Matrix();
            mtx.postRotate(180);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);

            if (bitmap == null) {
                Toast.makeText(MainActivity.this, "Captured image is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            capturedImageHolder.setImageBitmap(scaleDownBitmapImage(rotatedBitmap, 600, 600));

        }
    };

    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }
    public Bitmap rotateImage(Bitmap src){
        Matrix mtx1 = new Matrix();
        mtx1.postRotate(90);
        return  Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), mtx1, true);
    }
    public Bitmap resizeBitmap(Bitmap src){
        Matrix mtx1 = new Matrix();
        return  Bitmap.createBitmap(src, 0, 0, src.getWidth()/2, src.getHeight()/2, mtx1, true);
    }

}