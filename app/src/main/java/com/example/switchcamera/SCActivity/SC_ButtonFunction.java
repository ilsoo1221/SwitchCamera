package com.example.switchcamera.SCActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.example.switchcamera.SCActivity.SCcropFunction.SC_ROI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SC_ButtonFunction {

    private                 FrameLayout                 buttonGroup;
    private                 Button                      targetButton;
    private                 Context                     mContext;
    private                 LayoutInflater              inflater;
    private                 ImageView                   mImageView;

    private                 Bitmap                      mBitmap;


    public SC_ButtonFunction(FrameLayout buttonGroup, Context mContext, ImageView mImageView){
        this.buttonGroup = buttonGroup;
        this.mContext = mContext;
        this.mImageView = mImageView;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void changeButtonTarget(Button changedButton){
        this.targetButton = changedButton;
    }
    public void setmBitmap(Bitmap bitmap){ this.mBitmap = bitmap; }


    public void cropButtonClick() {

        View v = inflater.inflate(R.layout.sc_edit_crop, null);
        commonButtonClick(v);

        SC_ROI test = new SC_ROI(mContext, mImageView);



    }


    public void rotateButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_rotate, null);
        commonButtonClick(v);
    }

    public void filterButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_filter, null);
        commonButtonClick(v);

        Button cannyFilterButton = v.findViewById(R.id.filter_canny);
        cannyFilterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CannyFilterClick(mBitmap);
                mImageView.setImageBitmap(mBitmap);
            }
        });

    }

    public void tuneButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_tune, null);
        commonButtonClick(v);
    }

    public void backgroundButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_background, null);
        commonButtonClick(v);
    }


    public void CannyFilterClick(Bitmap bitmap){
        if(bitmap != null) {
            CannyFilterClickWithOpenCV(bitmap);
        }

    }




    // 편집 스타일의 공통 기능인 닫기, 완료 기능 구현
    public void commonButtonClick(View v){

        View menu = buttonGroup.getChildAt(0);
        menu.setVisibility(View.INVISIBLE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;


        v.setLayoutParams(params);

        ImageButton close = v.findViewById(R.id.edit_crop_close);
        ImageButton done = v.findViewById(R.id.edit_crop_done);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
            }
        });

        buttonGroup.addView(v);
    }



    public void GrayFilterClick(){

    }

    //20201203필터 부분 작업 오후 2시 시작
    //혹시나 작동안될시 주석처리
    //public native Bitmap GrayFilterClickWithOpenCL(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을  OpenCLDriver.c로 넘겨주려 한다.
    //연산을 마치면 해당 비트맵을 받아온다.
    //public native Bitmap BlurFilterClickWithOpenCL(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을 OpenCLDriver.c로 넘겨주려 한다.
    //연산을 마치면 해당 비트맵을 받아온다.
    public native void CannyFilterClickWithOpenCV(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을 OpenCV연산을 하기 위해
    //native-lib.cpp로 보내준다 받는 형태는 void




}
