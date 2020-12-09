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
    private ImageView sc_edit_canvasView;
    private FrameLayout buttonGroup;

    private Button cropButton, filterButton;
    private SC_ButtonFunction buttonFunction;

    private int ViewRange;






    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_edit_activity);

        sc_edit_imageView = findViewById(R.id.sc_edit_imageview);
        sc_edit_canvasView = findViewById(R.id.sc_edit_canvas);

        buttonGroup = findViewById(R.id.sc_edit_buttongroup);

        buttonFunction = new SC_ButtonFunction(buttonGroup, getApplicationContext(), sc_edit_imageView, sc_edit_canvasView);

        cropButton = (Button) findViewById(R.id.edit_crop);
        filterButton = findViewById(R.id.edit_filter);

        setButtonsClickListener();

    }


    private void setButtonsClickListener(){

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //buttonFunction.changeButtonTarget(cropButton);
                buttonFunction.cropButtonClick();
            }
        });



        filterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
               // buttonFunction.changeButtonTarget(filterButton);
                buttonFunction.filterButtonClick();
            }
        });



    }



    @Override//실행되었을때
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent(); //화면간 전환할 때 어떤 화면에서 데이터를 받아오면서 사용
        if(intent.getStringExtra("from").equals("gallery")) {
            Bitmap bitmap = null;
            Bitmap resultBitmap = null;
            ViewRange = SC_MainActivity.ImageWidth / 30 / 2;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                                getApplicationContext().getContentResolver(),
                                SC_MainActivity.galleryUri
                );
                resultBitmap = resizeBitmapImage(bitmap, SC_MainActivity.ImageWidth - ViewRange * 2, SC_MainActivity.ImageHeight - ViewRange * 2);
                buttonFunction.setmBitmap(resultBitmap);
                sc_edit_imageView.setImageBitmap(resultBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(intent.getStringExtra("from").equals("camera")) {

            ViewRange = SC_MainActivity.ImageWidth / 30 / 2;

            Bitmap resultBitmap
                    = resizeBitmapImage(SC_MainActivity.capturedBitmap, SC_MainActivity.ImageWidth - ViewRange * 2, SC_MainActivity.ImageHeight - ViewRange * 2);

            buttonFunction.setmBitmap(resultBitmap);
            sc_edit_imageView.setImageBitmap(resultBitmap);
        }
    }

    private Bitmap resizeBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

}
