package com.example.switchcamera.SCActivity;

import android.content.Context;
import android.graphics.Bitmap;//20201203오후2시49분 추가
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.switchcamera.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SC_ButtonFunction {

    private                 FrameLayout                 buttonGroup;
    private                 Button                      targetButton;
    private                 Context                     mContext;
    //20201203오후 3시 추가
    static {
        System.loadLibrary("native-lib");//native_lib를 사용하기 위해서
    }
    static {
        System.loadLibrary("OpenCLDriver");//OpenCLDriver를 사용하기 위해서
    }
    //여기까지

    public SC_ButtonFunction(FrameLayout buttonGroup, Context mContext){
        this.buttonGroup = buttonGroup;
        this.mContext = mContext;
    }

    public void changeButtonTarget(Button changedButton){
        this.targetButton = changedButton;
    }


    // 버튼 기능 구현
    public void cropButtonClick() {

        View menu = buttonGroup.getChildAt(0);
        menu.setVisibility(View.INVISIBLE);

        LayoutInflater inflater
                = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;

        View v = inflater.inflate(R.layout.sc_edit_crop, null);
        v.setLayoutParams(params);

        // 여기에 버튼에 대한 모든 기능을 구현한 클래스를 만들 것. 임시로 닫는 버튼만 만들어서 테스트

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
    //필터 버튼에 대한 동작
    public void FilterButtonClick(){

    }
    //20201203필터 부분 작업 오후 2시 시작
    //혹시나 작동안될시 주석처리
    public native Bitmap GrayFilterClickWithOpenCL(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을  OpenCLDriver.c로 넘겨주려 한다.
                                                                  //연산을 마치면 해당 비트맵을 받아온다.
    public native Bitmap BlurFilterClickWithOpenCL(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을 OpenCLDriver.c로 넘겨주려 한다.
                                                                  //연산을 마치면 해당 비트맵을 받아온다.
    public native void CannyFilterClickWithOpenCV(Bitmap bitmap);//여기서 비트맵은 우리가 사진을 찍었을 때 찍힌 비트맵을 OpenCV연산을 하기 위해
                                                                    //native-lib.cpp로 보내준다 받는 형태는 void








}
