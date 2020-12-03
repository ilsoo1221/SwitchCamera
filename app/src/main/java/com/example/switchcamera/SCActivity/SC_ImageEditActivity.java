package com.example.switchcamera.SCActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.switchcamera.R;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SC_ImageEditActivity extends AppCompatActivity {

    private ImageView sc_edit_imageView;
    private FrameLayout buttonGroup;

    private Button cropButton, filterButton;
    private SC_ButtonFunction buttonFunction;


    private                 ImageView                   testView;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_edit_activity);

        sc_edit_imageView = findViewById(R.id.sc_edit_imageview);
        buttonGroup = findViewById(R.id.sc_edit_buttongroup);

        buttonFunction = new SC_ButtonFunction(buttonGroup, getApplicationContext(), sc_edit_imageView);

        cropButton = (Button) findViewById(R.id.edit_crop);
        filterButton = findViewById(R.id.edit_filter);

        setButtonsClickListener();


        Bitmap bitmap = Bitmap.createBitmap(SC_MainActivity.ImageWidth, SC_MainActivity.ImageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAlpha(100);
        canvas.drawRect(10, 10, 100, 100, paint);


        testView = findViewById(R.id.sc_edit_canvas);
        testView.setImageBitmap(bitmap);
    }


    private void setButtonsClickListener(){

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonFunction.changeButtonTarget(cropButton);
                buttonFunction.cropButtonClick();
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                System.out.println("filter click");
                buttonFunction.filterButtonClick();
            }
        });



    }



    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if(intent.getStringExtra("from").equals("gallery")) {
            Bitmap bitmap = null;
            Bitmap resultBitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                                getApplicationContext().getContentResolver(),
                                SC_MainActivity.galleryUri
                );
                resultBitmap = resizeBitmapImage(bitmap, SC_MainActivity.ImageWidth, SC_MainActivity.ImageHeight);
                buttonFunction.setmBitmap(resultBitmap);
                sc_edit_imageView.setImageBitmap(resultBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(intent.getStringExtra("from").equals("camera")) {
            Bitmap resultBitmap
                    = resizeBitmapImage(SC_MainActivity.capturedBitmap, SC_MainActivity.ImageWidth, SC_MainActivity.ImageHeight);
            buttonFunction.setmBitmap(resultBitmap);
            sc_edit_imageView.setImageBitmap(resultBitmap);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            System.out.println("result : " + sc_edit_imageView.getWidth() + " , " + sc_edit_imageView.getHeight());
            Bitmap bitmap = ((BitmapDrawable) sc_edit_imageView.getDrawable()).getBitmap();
            System.out.println("result : " + bitmap.getWidth() + " , " + bitmap.getHeight());
        }

    }



    private Bitmap resizeBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

}
