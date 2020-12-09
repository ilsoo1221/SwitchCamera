package com.example.switchcamera.SCActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.switchcamera.R;

public class SC_ButtonFunction {

    private                 FrameLayout                 buttonGroup;
    private                 Button                      targetButton;
    private                 Context                     mContext;
    private                 LayoutInflater              inflater;
    private                 ImageView                   mImageView;

    private                 Bitmap                      mBitmap;
    private                 ImageView                   canvasView;

    private                 int                         currTop, currBottom, currLeft, currRight, ViewRange, TouchRange, CanvasWidth, CanvasHeight;
    private                 int                         prevTop, prevBottom, prevLeft, prevRight;
    private                 String                      DrawStatus;


    private                 Paint[]                     paints;
    private                 Canvas                      canvas;
    private                 Bitmap                      cropBitmap;


    public SC_ButtonFunction(FrameLayout buttonGroup, Context mContext, ImageView mImageView, ImageView canvasView){
        this.buttonGroup = buttonGroup;
        this.mContext = mContext;
        this.mImageView = mImageView;
        this.canvasView = canvasView;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);





    }



    public void changeButtonTarget(Button changedButton){
        this.targetButton = changedButton;
    }
    public void setmBitmap(Bitmap bitmap){ this.mBitmap = bitmap; }




    // 여기서부터 버튼 기능 구현


    // CROP 기능 관련 영역..............................................................................
    public void cropButtonClick() {

        View v = inflater.inflate(R.layout.sc_edit_crop, null);
        commonButtonClick(v);

        Button cropFreeButton = v.findViewById(R.id.crop_free);
        cropFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCanvas(0, 0);
                crop_FreeClick();
            }
        });
        Button crop2_3Button = v.findViewById(R.id.crop_2_3);
        crop2_3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCanvas(2, 3);
                crop_NoneFreeClick(2, 3);
            }
        });


        ImageButton done = v.findViewById(R.id.edit_crop_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bd = (BitmapDrawable)(mImageView.getDrawable());
                Bitmap res = bd.getBitmap();
                System.out.println(res.getWidth() + " , " + res.getHeight());
                int ll = currLeft, tt = currTop, rl = currRight - currLeft, bt  = currBottom - currTop;
                System.out.println(ll + " , " + tt + " , " + rl + " , " + bt);
                Bitmap resultBitmap
                        = Bitmap.createBitmap(res, ll + ViewRange, tt + ViewRange, rl - ViewRange * 2, bt - ViewRange * 2);

                mImageView.setImageBitmap(resizeBitmapImage(resultBitmap,
                        SC_MainActivity.ImageWidth - ViewRange * 2,
                        SC_MainActivity.ImageHeight - ViewRange * 2)
                );
            }
        });

        ImageButton close = v.findViewById(R.id.edit_crop_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
            }
        });
    }

    // CROP 기능을 위한 선택 영역 표현
    private void makePaints(){

        Paint clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        Xfermode xmode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        clearPaint.setXfermode(xmode);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(100);

        Paint StrokePaint = new Paint();
        StrokePaint.setStyle(Paint.Style.STROKE);
        StrokePaint.setStrokeCap(Paint.Cap.ROUND);
        StrokePaint.setColor(Color.WHITE);
        StrokePaint.setStrokeWidth(3);

        Paint FillPaint = new Paint();
        FillPaint.setStyle(Paint.Style.FILL);
        FillPaint.setColor(Color.TRANSPARENT);
        Xfermode FillMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        FillPaint.setXfermode(FillMode);

        Paint circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.WHITE);

        paints = new Paint[]{
                clearPaint, backgroundPaint, StrokePaint, FillPaint, circlePaint
        };
    }

    private void setCanvas(int width, int height){

        ViewRange = SC_MainActivity.ImageWidth / 30 / 2;
        TouchRange = SC_MainActivity.ImageWidth / 30 * 2;

        DrawStatus = "none";

        makePaints();

        CanvasWidth = SC_MainActivity.ImageWidth;
        CanvasHeight = SC_MainActivity.ImageHeight;

        int ImgWidth = CanvasWidth - ViewRange * 2;
        int ImgHeight = CanvasHeight - ViewRange * 2;

        int tlX, tlY;
        int trX, trY;
        int blX, blY;
        int brX, brY;
        if(width == 0 && height == 0){
            tlX = ViewRange; tlY = ViewRange;
            trX = CanvasWidth - ViewRange; trY = ViewRange;
            blX = ViewRange; blY = CanvasHeight - ViewRange;
            brX = CanvasWidth - ViewRange; brY = CanvasHeight - ViewRange;
        }
        // 가로가 꽉 찰 때
        else if(ImgWidth / width < ImgHeight / height){

            int half = ImgHeight / 2;
            int h = ImgWidth / width * height;
            tlX = ViewRange; tlY = half - h / 2;
            trX = CanvasWidth - ViewRange; trY = half - h / 2;
            blX = ViewRange; blY = half + h / 2;
            brX = CanvasWidth - ViewRange; brY = half + h / 2;
        }
        // 세로가 꽉 찰 때
        else if(ImgWidth / width > ImgHeight / height){

            int half = ImgWidth / 2;
            int w = ImgHeight / height * width;
            tlX = half - w / 2; tlY = ViewRange;
            trX = half + w / 2; trY = ViewRange;
            blX = half - w / 2; blY = CanvasHeight - ViewRange;
            brX = half + w / 2; brY = CanvasHeight - ViewRange;
        }
        // 비율이 같을 때
        else{
            tlX = ViewRange; tlY = ViewRange;
            trX = CanvasWidth - ViewRange; trY = ViewRange;
            blX = ViewRange; blY = CanvasHeight - ViewRange;
            brX = CanvasWidth - ViewRange; brY = CanvasHeight - ViewRange;
        }
        currTop = tlY; currLeft = tlX; currBottom = brY; currRight = brX;

        cropBitmap = Bitmap.createBitmap(CanvasWidth, CanvasHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(cropBitmap);
        canvas.drawColor(0);

        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[1]);

        canvas.drawRect(tlX, tlY, brX, brY, paints[2]);
        canvas.drawRect(tlX, tlY, brX, brY, paints[3]);
        canvas.drawCircle(tlX, tlY, ViewRange, paints[4]);
        canvas.drawCircle(trX, trY, ViewRange, paints[4]);
        canvas.drawCircle(blX, blY, ViewRange, paints[4]);
        canvas.drawCircle(brX, brY, ViewRange, paints[4]);
        canvasView.setImageBitmap(cropBitmap);
    }

    private void crop_FreeClick(){
        canvasView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if(x >= 0 && y >= 0 && x <= SC_MainActivity.ImageWidth && y <= SC_MainActivity.ImageHeight) {

                        // 왼쪽 위 모서리 찍을 때
                        if (x >= currLeft - TouchRange && x <= currLeft + TouchRange && y >= currTop - TouchRange && y <= currTop + TouchRange) {
                            DrawStatus = "LeftTop";
                        }
                        // 오른쪽 위 모서리 찍을 때
                        else if (x >= currRight - TouchRange && x <= currRight + TouchRange && y >= currTop - TouchRange && y <= currTop + TouchRange) {
                            DrawStatus = "RightTop";
                        }
                        // 왼쪽 아래 모서리 찍을 때
                        else if (x >= currLeft - TouchRange && x <= currLeft + TouchRange && y >= currBottom - TouchRange && y <= currBottom + TouchRange) {
                            DrawStatus = "LeftBottom";
                        }
                        // 오른쪽 아래 모서리 찍을 때
                        else if (x >= currRight - TouchRange && x <= currRight + TouchRange && y >= currBottom - TouchRange && y <= currBottom + TouchRange) {
                            DrawStatus = "RightBottom";
                        }
                        // 사각형 안쪽 찍을 때
                        else if (y > currTop && y < currBottom && x > currLeft && x < currRight) {
                            DrawStatus = "inner";
                        } else {
                            DrawStatus = "outer";
                        }
                    }
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if(x < ViewRange) x = ViewRange;
                    if(x > CanvasWidth - ViewRange) x = CanvasWidth - ViewRange;
                    if(y < ViewRange) y = ViewRange;
                    if(y > CanvasHeight - ViewRange) y = CanvasHeight - ViewRange;

                    if (!DrawStatus.equals("inner") && !DrawStatus.equals("outer")) {

                        if (DrawStatus.equals("LeftTop")) {
                            currTop = y;
                            currLeft = x;
                        } else if (DrawStatus.equals("RightTop")) {
                            currRight = x;
                            currTop = y;
                        } else if (DrawStatus.equals("LeftBottom")) {
                            currLeft = x;
                            currBottom = y;
                        } else if (DrawStatus.equals("RightBottom")) {
                            currRight = x;
                            currBottom = y;
                        }

                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[1]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[2]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[3]);

                        canvas.drawCircle(currLeft, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currLeft, currBottom, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currBottom, ViewRange, paints[4]);

                        canvasView.invalidate();
                        canvasView.setImageBitmap(cropBitmap);
                    }

                }
                return true;
            }
        });
    }

    int prevX = 0, prevY = 0;
    private void crop_NoneFreeClick(final int width, final int height){

        canvasView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    prevX = x; prevY  = y;

                    if(x >= 0 && y >= 0 && x <= SC_MainActivity.ImageWidth && y <= SC_MainActivity.ImageHeight) {

                        // 왼쪽 위 모서리 찍을 때
                        if (x >= currLeft - TouchRange && x <= currLeft + TouchRange && y >= currTop - TouchRange && y <= currTop + TouchRange) {
                            DrawStatus = "LeftTop";
                        }
                        // 오른쪽 위 모서리 찍을 때
                        else if (x >= currRight - TouchRange && x <= currRight + TouchRange && y >= currTop - TouchRange && y <= currTop + TouchRange) {
                            DrawStatus = "RightTop";
                        }
                        // 왼쪽 아래 모서리 찍을 때
                        else if (x >= currLeft - TouchRange && x <= currLeft + TouchRange && y >= currBottom - TouchRange && y <= currBottom + TouchRange) {
                            DrawStatus = "LeftBottom";
                        }
                        // 오른쪽 아래 모서리 찍을 때
                        else if (x >= currRight - TouchRange && x <= currRight + TouchRange && y >= currBottom - TouchRange && y <= currBottom + TouchRange) {
                            DrawStatus = "RightBottom";
                        }
                        // 사각형 안쪽 찍을 때
                        else if (y > currTop && y < currBottom && x > currLeft && x < currRight) {
                            DrawStatus = "inner";
                        } else {
                            DrawStatus = "outer";
                        }
                    }
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if (!DrawStatus.equals("inner") && !DrawStatus.equals("outer")) {

                        if (DrawStatus.equals("LeftTop")) {
                            currLeft = currLeft + (x - prevX);
                            currTop = currBottom - (currRight - currLeft) * height / width;
                            if(currLeft < ViewRange || currTop < ViewRange || currLeft > CanvasWidth || currTop > CanvasHeight){
                                currLeft = prevLeft; currTop = prevTop;
                            }
                            prevLeft = currLeft; prevTop = currTop;
                        } else if (DrawStatus.equals("RightTop")) {
                            currRight = currRight + (x - prevX);
                            currTop = currBottom - (currRight - currLeft) * height / width;
                            if(currRight < ViewRange || currTop < ViewRange || currRight > CanvasWidth || currTop > CanvasHeight){
                                currRight = prevRight; currTop = prevTop;
                            }
                            prevRight = currRight; prevTop = currTop;
                        } else if (DrawStatus.equals("LeftBottom")) {
                            currLeft = currLeft + (x - prevX);
                            currBottom = currTop + (currRight - currLeft) * height / width;
                            if(currLeft < ViewRange || currBottom < ViewRange || currLeft > CanvasWidth || currBottom > CanvasHeight){
                                currLeft = prevLeft; currBottom = prevBottom;
                            }
                            prevLeft = currLeft; prevBottom = currBottom;
                        } else if (DrawStatus.equals("RightBottom")) {
                            currRight = currRight + (x - prevX);
                            currBottom = currTop + (currRight - currLeft) * height / width;
                            if(currRight < ViewRange || currBottom < ViewRange || currRight > CanvasWidth || currBottom > CanvasHeight){
                                currRight = prevRight; currBottom = prevBottom;
                            }
                            prevRight = currRight; prevBottom = currBottom;
                        }

                        prevX = x; prevY = y;

                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[1]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[2]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[3]);

                        canvas.drawCircle(currLeft, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currLeft, currBottom, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currBottom, ViewRange, paints[4]);

                        canvasView.invalidate();
                        canvasView.setImageBitmap(cropBitmap);
                    }


                }
                return true;
            }
        });
    }
    //..............................................................................................


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




    private Bitmap resizeBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }
}
