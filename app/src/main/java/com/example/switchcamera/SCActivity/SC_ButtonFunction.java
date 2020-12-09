package com.example.switchcamera.SCActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.switchcamera.R;

import androidx.appcompat.app.AppCompatActivity;

public class SC_ButtonFunction extends AppCompatActivity {


    private                 SC_ImageEditActivity        sc_imageEditActivity;

    private                 FrameLayout                 buttonGroup;
    private                 Button                      targetButton;
    private                 Context                     mContext;
    private                 LayoutInflater              inflater;
    private                 ImageView                   mImageView;

    private                 Bitmap                      mBitmap;
    private                 Uri                         galleryUri;
    private                 ImageView                   canvasView;

    private                 int                         currTop, currBottom, currLeft, currRight, ViewRange, TouchRange, CanvasWidth, CanvasHeight;
    private                 int                         prevTop, prevBottom, prevLeft, prevRight;
    private                 String                      DrawStatus;


    private                 Paint[]                     paints;
    private                 Paint[]                     paintsForBackground;
    private                 String[]                    backgroundPath;
    private                 int[]                       backgroundIds;

    private                 Canvas                      canvas;
    private                 Bitmap                      cropBitmap;
    private                 Bitmap                      backgroundBitmap;





    public SC_ButtonFunction(FrameLayout buttonGroup, Context mContext, ImageView mImageView, ImageView canvasView,
                             SC_ImageEditActivity sc_imageEditActivity){
        this.sc_imageEditActivity = sc_imageEditActivity;
        this.buttonGroup = buttonGroup;
        this.mContext = mContext;
        this.mImageView = mImageView;
        this.canvasView = canvasView;

        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        makePaints();
        makeBackgroundPaint();
        setBackgroundPath();
    }

    public void setmBitmap(Bitmap bitmap){ this.mBitmap = bitmap; }




    // 여기서부터 버튼 기능 구현


    // CROP 기능 관련 영역..............................................................................
    public void cropButtonClick() {

        View v = inflater.inflate(R.layout.sc_edit_crop, null);

        commonButtonClick(v);
        setCanvas(0, 0);
        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);

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


        ImageButton done = v.findViewById(R.id.edit_rotate_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BitmapDrawable bd = (BitmapDrawable)(mImageView.getDrawable());
                Bitmap res = bd.getBitmap();

                int ll = currLeft, tt = currTop, rl = currRight - currLeft, bt  = currBottom - currTop;
                Bitmap resultBitmap
                        = Bitmap.createBitmap(res, ll + ViewRange, tt + ViewRange,
                        rl - ViewRange * 2, bt - ViewRange * 2);

                int rHeight, rWidth;
                if(rl < bt){
                    rHeight =  SC_MainActivity.fImageHeight - ViewRange * 2;
                    rWidth = (SC_MainActivity.fImageHeight - ViewRange * 2) / (bt - ViewRange * 2) * (rl - ViewRange * 2);
                }
                else if(rl > bt){
                    rWidth = SC_MainActivity.ImageWidth - ViewRange * 2;
                    rHeight = (SC_MainActivity.ImageWidth - ViewRange * 2) / (rl - ViewRange * 2) * (bt - ViewRange * 2);
                }
                else{
                    rWidth = SC_MainActivity.ImageWidth - ViewRange * 2;
                    rHeight = SC_MainActivity.ImageWidth - ViewRange * 2;
                }

                resultBitmap = resizeBitmapImage(resultBitmap, rWidth, rHeight);
                mImageView.setImageBitmap(resultBitmap);
                SC_MainActivity.ImageWidth = rWidth;
                SC_MainActivity.ImageHeight = rHeight;
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);

                sc_imageEditActivity.prevList.add(resultBitmap);
                sc_imageEditActivity.prevListIdx = sc_imageEditActivity.prevList.size();
            }
        });

        ImageButton close = v.findViewById(R.id.edit_rotate_close);
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

        ViewRange = SC_MainActivity.fImageWidth / 30 / 2;
        TouchRange = SC_MainActivity.fImageWidth / 30 * 2;

        DrawStatus = "none";

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

                    System.out.println(x + ", " + y);
                    System.out.println(currLeft - TouchRange);
                    System.out.println(currBottom - TouchRange);

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
                        System.out.println(DrawStatus);
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

                    // x, y는 현재 손가락이 찍는 좌표를 가져오는 것.
                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if(x < ViewRange) x = ViewRange;
                    if(x > CanvasWidth - ViewRange) x = CanvasWidth - ViewRange;
                    if(y < ViewRange) y = ViewRange;
                    if(y > CanvasHeight - ViewRange) y = CanvasHeight - ViewRange;


                    // inner 또는 outer를 찍고 있는 것이 아니라면 = 모서리 부분을 터치하고 있는 것이라면
                    if (!DrawStatus.equals("inner") && !DrawStatus.equals("outer")) {

                        // 일단 선택영역이 2 : 3 비율을 유지해서 크기가 변한다고 가정하자.
                        // 여기서부터는 어차피 어떤 모서리를 찍냐에 따라 값만 달라지는 거니까 코드 동작 방식은 똑같고, 동작 원리에 대해 설명하자면 :
                        // 일단 위에서 x, y 좌표는 현재 손가락이 찍는 좌표라고 했지? prevX와 prevY는 손가락이 움직이기 바로 직전의 좌표를 저장하기 위한 변수야.
                        // currLeft, currTop, ccurRight, currBottom 모서리의 좌표야.
                        // 선택 영역은 비율이 일정하게 줄었다가 늘어났다가 하기 때문에 손가락 좌표와는 조금 다른 위치로 움직이겠지? 가령 손가락을 대각선으로 움직이는 게 아니라
                        // 약간 직선 같이 움직여도 선택 영역은 비율에 따라 줄었다가 늘었다가 하잖아? 즉 손가락이 어떻게 움직이던 간에 모서리들은 각각 비율을 유지하면서
                        // 움직여야 하니까 별개의 좌표를 가져야 한단 말이지.
                        // 따라서 손가락이 prevX -> x 만큼 움직일 때 currLeft (즉 왼쪽 모서리의 x좌표)의 좌표도 prevX -> x 로 움직인 거리만큼 움직이게 해주는 거야.
                        // 모서리의 x좌표가 x -> prevX 만큼 이동했으니까 y 좌표도 x -> prevX 가 움직인만큼 비율을 유지해서 움직여야 겠지?
                        // 즉 prevX -> x : prevY -> y = 2 : 3 이 되는거야.
                        // currTop = currBottom - (currRight - currLeft) * height / width; 이 코드를 분석해보자.

                        // 좌, 우항을 움직여서 식을 정리해보면
                        // currTop + (currRight - currLeft) * height / width = currBottom
                        // (currRight - currLeft) * height / width = currBottom - currTop
                        // (currRight - currLeft) * height = (currBottom - currTop) * width
                        // currRight - currLeft : currBottom - currTop = width : height
                        // 여기서 currRight가 우측 모서리 x 좌표, currLeft가 좌측 모서리 x 좌표, currBottom이 맨 아래 y좌표, currTop이 맨 위의 y좌표
                        // 이해하기 쉽게 그림으로 표현하자면

                        //       (currLeft, currTop)                      (currRight, currTop)
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        //     (currLeft, currBottom)                      (currRight, currBottom)

                        // 의 모양을 가지는 것인데, 위의 공식
                        // currRight - currLeft : currBottom - currTop = width : height 이것에 의해
                        // 오른쪽 - 왼쪽 : 아래쪽 - 위쪽이 너비 (여기서는 2) : 높이 (여기서는 3) 의 비율로 유지가 되는거야.
                        // 즉 식을 정리하기 전의 식인 currTop = currBottom - (currRight - currLeft) * height / width;
                        // 이 공식에서 currRight ~ currLeft의 길이와 currTop ~ currBottom과의 길이가  2:3 의 비율이 유지되게 해주는 것이야.
                        // 아래도 동작원리는 같아.
                        // 여기서 이제 부호가 문제인데, 부호는 머리가 꼬여서 그냥 대충 어림짐작하고 이상한 것만 하나씩 바꿔주면서 비율대로 움직이게끔 한거라...
                        // 어차피 '비율' 에 의미가 있는 것이라 오른쪽에서 왼쪽을 빼든, 왼쪽에서 오른쪽을 빼든 큰 의미는 없고, 너비와 높이가 같이 줄어드는지,
                        // 또는 같이 늘어나는지만 제대로 맞는다면 큰 상관이 없어.

                        // none-free는 이걸로 됐는데, 그렇다면 free는 다르게 움직이지 않느냐?
                        // 한다면 어차피 free 영역은 모서리가 손가락 좌표에 그대로 따라가기 때문에 현재 손가락 좌표를 모서리 좌표로 설정하면 되기 때문에
                        // 특별한 연산이 필요 없어.

                        // 여튼 이렇게 각 모서리의 좌표를 구해서 밑에 canvas.drawRect에 해당 좌표를 때려박아서 선택영역을 갱신해주면서 그려주면 되는 것
                        // 모르는 거 있으면 바로 물어봐줘

                        if (DrawStatus.equals("LeftTop")) {
                            currLeft = currLeft + (x - prevX);
                            currTop = currBottom - (currRight - currLeft) * height / width;
                            // 각 구역의 해당 if문은 선택영역이 캔버스 밖으로 빠져나갈 때 모서리의 동그라미가 잘리는 현상 때문에 넣은 것.
                            // 이전 모서리 좌표를 prev...좌표 에 담고 curr... 좌표가 밖으로 나갔을 때 prev...좌표로 다시 되돌린다.
                            if(currLeft < ViewRange || currTop < ViewRange || currLeft > CanvasWidth - ViewRange || currTop > CanvasHeight - ViewRange){
                                currLeft = prevLeft; currTop = prevTop;
                            }
                            if(currTop != currBottom - (currRight - currLeft) * height / width){
                                currLeft = prevLeft; currTop = prevTop;
                            }
                            prevLeft = currLeft; prevTop = currTop;
                        } else if (DrawStatus.equals("RightTop")) {
                            currRight = currRight + (x - prevX);
                            currTop = currBottom - (currRight - currLeft) * height / width;
                            if(currRight < ViewRange || currTop < ViewRange || currRight > CanvasWidth - ViewRange || currTop > CanvasHeight - ViewRange){
                                currRight = prevRight; currTop = prevTop;
                            }
                            if(currTop != currBottom - (currRight - currLeft) * height / width){
                                currRight = prevRight; currTop = prevTop;
                            }
                            prevRight = currRight; prevTop = currTop;
                        } else if (DrawStatus.equals("LeftBottom")) {
                            currLeft = currLeft + (x - prevX);
                            currBottom = currTop + (currRight - currLeft) * height / width;
                            if(currLeft < ViewRange || currBottom < ViewRange || currLeft > CanvasWidth - ViewRange || currBottom > CanvasHeight - ViewRange){
                                currLeft = prevLeft; currBottom = prevBottom;
                            }
                            if(currTop != currBottom - (currRight - currLeft) * height / width){
                                currLeft = prevLeft; currBottom = prevBottom;
                            }
                            prevLeft = currLeft; prevBottom = currBottom;
                        } else if (DrawStatus.equals("RightBottom")) {
                            currRight = currRight + (x - prevX);
                            currBottom = currTop + (currRight - currLeft) * height / width;
                            if(currRight < ViewRange || currBottom < ViewRange || currRight > CanvasWidth - ViewRange || currBottom > CanvasHeight - ViewRange){
                                currRight = prevRight; currBottom = prevBottom;
                            }
                            if(currTop != currBottom - (currRight - currLeft) * height / width){
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






    // 합성 기능 관련 영역..............................................................................

    // background (SC_ImageEditActivity의 sc_edit_imageview)에 불러온 배경을 깔기 전에 합성할 이미지를 BGbitmap에 저장
    public                  Bitmap                  BGBitmap;
    public                  int                     returnOriginalWidth;
    public                  int                     returnOriginalHeight;
    // 백그라운드 버튼 클릭
    public void backgroundButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_background, null);
        commonButtonClick(v);

        BGBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();

        mImageView.setImageBitmap(null);
        setCanvas();

        ImageButton done = v.findViewById(R.id.edit_background_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canvas.drawRect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight, paintsForBackground[0]);
                Bitmap b = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
                canvas.drawBitmap(b, null, new Rect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight), null);
                canvas.drawBitmap(BGBitmap, null, new Rect(currLeft, currTop, currRight, currBottom), null);

                canvasView.setImageBitmap(backgroundBitmap);

                Bitmap resultBitmap = ((BitmapDrawable)canvasView.getDrawable()).getBitmap();
                canvasView.setImageBitmap(null);
                mImageView.setImageBitmap(resultBitmap);

                sc_imageEditActivity.prevList.add(resultBitmap);
                sc_imageEditActivity.prevListIdx = sc_imageEditActivity.prevList.size();

                SC_MainActivity.ImageWidth = SC_MainActivity.fImageWidth;
                SC_MainActivity.ImageHeight = SC_MainActivity.fImageHeight;

                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
            }
        });

        // 닫기 버튼 클릭
        ImageButton close = v.findViewById(R.id.edit_background_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                canvasView.setImageBitmap(null);
                mImageView.setImageBitmap(BGBitmap);
            }
        });
        // background 선택 버튼 클릭
        LinearLayout buttons = v.findViewById(R.id.background_buttons_layout);
        for(int i = 0; i < buttons.getChildCount(); i++){

            FrameLayout frameLayout = (FrameLayout) buttons.getChildAt(i);
            ImageButton button = (ImageButton) frameLayout.getChildAt(0);
            // 맨 처음 버튼 (갤러리에서 가져오기 버튼) 클릭
            if(i == 0){
                Bitmap b = getThumbnail();
                Bitmap resultBitmap = resizeBitmapImage(b, 128, 128);
                button.setImageBitmap(resultBitmap);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sc_imageEditActivity.accessGallery();
                    }
                });
            }
            else{
                Drawable drawable = button.getDrawable();
                Bitmap b = ((BitmapDrawable)drawable).getBitmap();
                Bitmap resultBitmap = resizeBitmapImage(b, 128, 128);
                button.setImageBitmap(resultBitmap);

                final int idx = i - 1;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Drawable d
                                = sc_imageEditActivity.getApplicationContext().getResources().getDrawable(backgroundIds[idx]);
                        Bitmap b = ((BitmapDrawable)d).getBitmap();
                        b = resizeBitmapImage(b, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight);
                        mImageView.setImageBitmap(b);
                    }
                });
            }
        }
    }

    // "갤러리에서 배경 가져오기" 버튼에 미리 표시될 섬네일
    private Bitmap getThumbnail(){

        // 커서로 디바이스에 있는 Image 형식 데이터들 전부 훑어서 가져오기
        Cursor cursor
                = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        // 데이터가 존재한다면 맨 처음 데이터의 id를 가져와서 섬네일로 변환 후 리턴
        if (cursor != null && cursor.moveToFirst())
        {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                    id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        }

        cursor.close();
        return null;
    }

    public void setCanvas(){

        int centerX, centerY;
        centerX = SC_MainActivity.fImageWidth / 2;
        centerY = SC_MainActivity.fImageHeight / 2;

        ViewRange = SC_MainActivity.fImageWidth / 30 / 2;
        TouchRange = SC_MainActivity.fImageWidth / 30 * 2;

        int bitmapWidth = BGBitmap.getWidth() * 3 / 4;
        int bitmapHeight = BGBitmap.getHeight() * 3 / 4;

        int tlX, tlY, trX, trY, blX, blY, brX, brY;
        tlX = centerX - bitmapWidth / 2; tlY = centerY - bitmapHeight / 2;
        trX = centerX + bitmapWidth / 2; trY = centerY - bitmapHeight / 2;
        blX = centerX - bitmapWidth / 2; blY = centerY + bitmapHeight / 2;
        brX = centerX + bitmapWidth / 2; brY = centerY + bitmapHeight / 2;

        backgroundBitmap
                = Bitmap.createBitmap(SC_MainActivity.fImageWidth - ViewRange, SC_MainActivity.fImageHeight - ViewRange, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(backgroundBitmap);
        canvas.drawColor(0);

        canvas.drawBitmap(BGBitmap, null, new Rect(tlX, tlY, brX, brY), null);
        canvas.drawRect(tlX, tlY, brX, brY, paintsForBackground[1]);
        canvas.drawCircle(tlX, tlY, ViewRange, paintsForBackground[3]);
        canvas.drawCircle(trX, trY, ViewRange, paintsForBackground[3]);
        canvas.drawCircle(blX, blY, ViewRange, paintsForBackground[3]);
        canvas.drawCircle(brX, brY, ViewRange, paintsForBackground[3]);

        canvasView.setImageBitmap(backgroundBitmap);

        currTop = tlY; currLeft = tlX; currBottom = brY; currRight = brX;
        canvasView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if(x >= 0 && y >= 0 && x <= SC_MainActivity.fImageWidth && y <= SC_MainActivity.fImageHeight) {

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
                        prevX = x; prevY = y;
                    }
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    if(x < ViewRange) x = ViewRange;
                    if(x > SC_MainActivity.fImageWidth - ViewRange) x = SC_MainActivity.fImageWidth - ViewRange;
                    if(y < ViewRange) y = ViewRange;
                    if(y > SC_MainActivity.fImageHeight - ViewRange) y = SC_MainActivity.fImageHeight - ViewRange;

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

                        canvas.drawRect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight, paintsForBackground[0]);
                        canvas.drawBitmap(BGBitmap, null, new Rect(currLeft, currTop, currRight, currBottom), null);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paintsForBackground[1]);
                        canvas.drawCircle(currLeft, currTop, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currRight, currTop, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currLeft, currBottom, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currRight, currBottom, ViewRange, paintsForBackground[3]);

                        canvasView.invalidate();
                        canvasView.setImageBitmap(backgroundBitmap);
                    }
                    else if(DrawStatus.equals("inner")){

                        currLeft = currLeft + x - prevX; currRight = currRight + x - prevX;
                        currTop = currTop + y - prevY;  currBottom = currBottom + y - prevY;
                        if(currLeft < ViewRange || currLeft > SC_MainActivity.fImageWidth - ViewRange
                                || currRight < ViewRange || currRight > SC_MainActivity.fImageWidth - ViewRange){
                            currLeft = currLeft - (x - prevX); currRight = currRight - (x - prevX);
                        }
                        if(currTop < ViewRange || currTop > SC_MainActivity.fImageHeight - ViewRange
                                || currBottom < ViewRange || currBottom > SC_MainActivity.fImageHeight - ViewRange){
                            currTop = currTop - (y - prevY); currBottom = currBottom - (y - prevY);
                        }

                        canvas.drawRect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight, paintsForBackground[0]);
                        canvas.drawBitmap(BGBitmap, null, new Rect(currLeft, currTop, currRight, currBottom), null);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paintsForBackground[1]);
                        canvas.drawCircle(currLeft, currTop, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currRight, currTop, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currLeft, currBottom, ViewRange, paintsForBackground[3]);
                        canvas.drawCircle(currRight, currBottom, ViewRange, paintsForBackground[3]);

                        canvasView.invalidate();
                        canvasView.setImageBitmap(backgroundBitmap);
                        prevX = x; prevY = y;
                    }

                }
                return false;
            }
        });
    }

    private void setBackgroundPath(){

        backgroundIds = new int[]{
                R.drawable.spring, R.drawable.flower, R.drawable.frame
        };
    }

    // 배경 합성을 위한 페인트
    private void makeBackgroundPaint(){

        Paint clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        Xfermode xmode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        clearPaint.setXfermode(xmode);

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

        paintsForBackground = new Paint[]{
                clearPaint, StrokePaint, FillPaint, circlePaint
        };
    }
    //.............................................................................................






    // 회전 기능 관련 영역..............................................................................
    // 회전은 opencv로 하지 않으면 상당히 메모리를 많이 먹게 되어 연속적으로 한다면 앱이 꺼짐.
    // seekBar를 이용해서 하는 건 포기

    public Bitmap rotateBitmap;
    public Bitmap originalRotateBitmap;
    public void rotateButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_rotate, null);
        commonButtonClick(v);

//        sc_imageEditActivity.rotateSeekBar = v.findViewById(R.id.edit_seekbar);
//        sc_imageEditActivity.rotateSeekBarText = v.findViewById(R.id.edit_seekbar_text);
//
//        sc_imageEditActivity.rotateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                rotateBitmap = rotateImage(rotateBitmap, i);
//                mImageView.setImageBitmap(rotateBitmap);
//                //sc_imageEditActivity.rotateSeekBarText.setText(i);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        rotateBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        originalRotateBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();

        ImageButton done = v.findViewById(R.id.edit_rotate_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
            }
        });

        // 닫기 버튼 클릭
        ImageButton close = v.findViewById(R.id.edit_rotate_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(originalRotateBitmap);
            }
        });

        // rotate 선택 버튼 클릭
        LinearLayout buttons = v.findViewById(R.id.rotate_buttons);
        for(int i = 0; i < buttons.getChildCount(); i++){

            Button button = (Button) buttons.getChildAt(i);

            final int idx = i + 1;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rotateBitmap = rotateImage(rotateBitmap, idx * 90);
                    mImageView.setImageBitmap(rotateBitmap);
                }
            });
        }
    }

    public Bitmap rotateImage(Bitmap src, int degrees){
        Matrix mtx1 = new Matrix();
        mtx1.postRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), mtx1, true);
    }
    //..............................................................................................





    // 필터 기능 관련 영역..............................................................................

    Bitmap filterBitmap;
    public void filterButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_filter, null);
        commonButtonClick(v);

        filterBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        Bitmap originalBitmap
                = Bitmap.createScaledBitmap(filterBitmap, filterBitmap.getWidth(), filterBitmap.getHeight(), true);
        canvasView.setImageBitmap(filterBitmap);

        ImageButton done = v.findViewById(R.id.edit_filter_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap resultBitmap = ((BitmapDrawable)canvasView.getDrawable()).getBitmap();
                mImageView.setImageBitmap(resultBitmap);

                sc_imageEditActivity.prevList.add(resultBitmap);
                sc_imageEditActivity.prevListIdx = sc_imageEditActivity.prevList.size();

                canvasView.setImageBitmap(null);
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);

            }
        });

        ImageButton close = v.findViewById(R.id.edit_filter_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(originalBitmap);
                canvasView.setImageBitmap(null);
            }
        });

        Button cannyFilterButton = v.findViewById(R.id.filter_canny);
        cannyFilterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CannyFilterClick(filterBitmap);
                canvasView.setImageBitmap(filterBitmap);
            }
        });

        Button grayFilterButton = v.findViewById(R.id.filter_gray);
        grayFilterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                GrayFilterClick(filterBitmap);
                canvasView.setImageBitmap(filterBitmap);
            }
        });

        Button blurFilterButton = v.findViewById(R.id.filter_blur);
        blurFilterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                BlurFilterClick(filterBitmap);
                canvasView.setImageBitmap(filterBitmap);
            }
        });
    }

    public void CannyFilterClick(Bitmap bitmap){
        if(bitmap != null) {
            CannyFilterClickWithOpenCV(bitmap);
        }

    }

    public void GrayFilterClick(Bitmap bitmap){
        if(bitmap != null) {
            //GrayFilterClickWithOpenCL(bitmap);
        }
    }

    public void BlurFilterClick(Bitmap bitmap){
        if(bitmap != null){
            //BlurFilterClickWithOpenCL(bitmap);
        }
    }
    //..............................................................................................





    // 편집 스타일의 공통 기능인 닫기, 완료 기능 구현
    public void commonButtonClick(View v){

        View menu = buttonGroup.getChildAt(0);
        menu.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );

        buttonGroup.addView(v, params);
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
