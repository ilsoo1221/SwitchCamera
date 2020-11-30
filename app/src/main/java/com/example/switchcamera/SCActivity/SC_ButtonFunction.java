package com.example.switchcamera.SCActivity;

import android.content.Context;
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

    public SC_ButtonFunction(FrameLayout buttonGroup, Context mContext){
        this.buttonGroup = buttonGroup;
        this.mContext = mContext;
    }

    public void changeButtonTarget(Button changedButton){
        this.targetButton = changedButton;
    }


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

        //
        buttonGroup.addView(v);
    }

    //testestas
    public void GrayFilterClick(){

    }


}
