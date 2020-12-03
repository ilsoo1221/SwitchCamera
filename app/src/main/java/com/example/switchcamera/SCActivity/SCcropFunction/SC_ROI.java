package com.example.switchcamera.SCActivity.SCcropFunction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;

public class SC_ROI extends View {

    private                 Context                 mContext;
    private                 ImageView               mImageView;


    private                 int                     LTx, LTy, RBx, RBy;


    private                 Canvas                  canvas;
    private                 Paint                   region;

    public SC_ROI(Context mContext, ImageView mImageView){
        super(mContext);
        this.mContext = mContext;
        this.mImageView = mImageView;

        init();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    private void init(){

        int cWidth = mImageView.getWidth();
        int cHeight = mImageView.getHeight();




        region = new Paint();
        region.setColor(Color.BLUE);
        region.setAlpha(100);
    }

    public void drawRoi(){

    }
}
