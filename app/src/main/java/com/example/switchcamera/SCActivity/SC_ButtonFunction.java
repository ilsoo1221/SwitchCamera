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




    // 생성자
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

    // 메인 비트맵 가져오기
    public void setmBitmap(Bitmap bitmap){ this.mBitmap = bitmap; }




    // 여기서부터 버튼 기능 구현


    // CROP 기능 관련 영역..............................................................................
    public void cropButtonClick() {

        // 인플레이터로 뷰 동적 생성
        View v = inflater.inflate(R.layout.sc_edit_crop, null);

        // 버튼 공통 디자인 대략적으로 잡기
        commonButtonClick(v);

        // crop 선택 영역을 위한 캔버스 초기화
        setCanvas(0, 0);
        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);

        // free 버튼 클릭
        Button cropFreeButton = v.findViewById(R.id.crop_free);
        cropFreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCanvas(0, 0);
                crop_FreeClick();
            }
        });

        // 2:3 버튼 클릭
        Button crop2_3Button = v.findViewById(R.id.crop_2_3);
        crop2_3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCanvas(2, 3);
                crop_NoneFreeClick(2, 3);
            }
        });

        // 완료 버튼 클릭
        ImageButton done = v.findViewById(R.id.edit_crop_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 현재 이미지뷰에 있는 이미지를 비트맵으로 가져오기
                BitmapDrawable bd = (BitmapDrawable)(mImageView.getDrawable());
                Bitmap res = bd.getBitmap();

                // ll : 왼쪽 x 좌표, tt : 위쪽 y좌표, rl : 너비, bt : 높이
                int ll = currLeft, tt = currTop, rl = currRight - currLeft, bt  = currBottom - currTop;

                // 가져온 비트맵의 크기를 대략적으로 초기화해준다.
                Bitmap resultBitmap
                        = Bitmap.createBitmap(res, ll + ViewRange, tt + ViewRange,
                        rl - ViewRange * 2, bt - ViewRange * 2);

                // 가로, 세로 비율에 따라 비트맵의 너비와 높이를 적절히 세팅하여 최대한 이미지가 크게 만들어준다.
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

                // 새로운 너비와 높이로 비트맵 리사이징 후 이미지뷰에 넣기
                resultBitmap = resizeBitmapImage(resultBitmap, rWidth, rHeight);
                mImageView.setImageBitmap(resultBitmap);
                SC_MainActivity.ImageWidth = rWidth;
                SC_MainActivity.ImageHeight = rHeight;

                //crop 뷰 닫고 다시 edit 뷰로 돌아오고 선택 영역 지우기
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);

                // 이전 작업 되돌리기, 앞으로 가기 기능을 위해 list에 현재 비트맵을 넣어준다.
                sc_imageEditActivity.prevList.add(resultBitmap);
                sc_imageEditActivity.prevListIdx = sc_imageEditActivity.prevList.size();
            }
        });

        // 닫기 버튼 클릭.
        ImageButton close = v.findViewById(R.id.edit_crop_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // crop 뷰를 닫고 edit 뷰로 되돌아온다. 선택 영역도 제거
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
                canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
            }
        });
    }

    // CROP 기능을 위한 선택 영역 표현
    private void makePaints(){

        // 선택영역 제거 및 지나간 흔적 지우기 위한 페인트
        Paint clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        Xfermode xmode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        clearPaint.setXfermode(xmode);

        //흐릿한 효과를 위한 페인트
        Paint backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(100);

        // 선택영역 테두리
        Paint StrokePaint = new Paint();
        StrokePaint.setStyle(Paint.Style.STROKE);
        StrokePaint.setStrokeCap(Paint.Cap.ROUND);
        StrokePaint.setColor(Color.WHITE);
        StrokePaint.setStrokeWidth(3);

        // 선택 영역 안쪽 흐릿한 효과 없애기 위한 페인트
        Paint FillPaint = new Paint();
        FillPaint.setStyle(Paint.Style.FILL);
        FillPaint.setColor(Color.TRANSPARENT);
        Xfermode FillMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        FillPaint.setXfermode(FillMode);

        // 네개의 동그란 모서리
        Paint circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.WHITE);

        paints = new Paint[]{
                clearPaint, backgroundPaint, StrokePaint, FillPaint, circlePaint
        };
    }

    // 캔버스 그리기
    private void setCanvas(int width, int height){

        // 동그란 모서리가 안짤리게 선택 영역을 캔버스 크기에서 조금 작게 만들기 위한 ViewRange
        // 동그라미가 작으면 터치가 안될 수 있으니 터치 영역을 조금 크게 만들기 위한 touchRange
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
        // 선택 영역의 너비 비율이 좀 더 클 때 리사이징
        else if(ImgWidth / width < ImgHeight / height){

            int half = ImgHeight / 2;
            int h = ImgWidth / width * height;
            tlX = ViewRange; tlY = half - h / 2;
            trX = CanvasWidth - ViewRange; trY = half - h / 2;
            blX = ViewRange; blY = half + h / 2;
            brX = CanvasWidth - ViewRange; brY = half + h / 2;
        }
        // 선택 영역의 높이 비율이 좀 더 클 때 리사이징
        else if(ImgWidth / width > ImgHeight / height){

            int half = ImgWidth / 2;
            int w = ImgHeight / height * width;
            tlX = half - w / 2; tlY = ViewRange;
            trX = half + w / 2; trY = ViewRange;
            blX = half - w / 2; blY = CanvasHeight - ViewRange;
            brX = half + w / 2; brY = CanvasHeight - ViewRange;
        }
        // 너비와 높이의 비율이 같을 때 리사이징
        else{
            tlX = ViewRange; tlY = ViewRange;
            trX = CanvasWidth - ViewRange; trY = ViewRange;
            blX = ViewRange; blY = CanvasHeight - ViewRange;
            brX = CanvasWidth - ViewRange; brY = CanvasHeight - ViewRange;
        }

        //초기 좌표 설정. curr 좌표들은 터치할 때마다 계속 변경된다.
        currTop = tlY; currLeft = tlX; currBottom = brY; currRight = brX;

        // 캔버스를 그리기 위한 비트맵. 색은 투명으로 만든다.(canvas.drawColor(0))
        cropBitmap = Bitmap.createBitmap(CanvasWidth, CanvasHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(cropBitmap);
        canvas.drawColor(0);

        // 일단 캔버스 깨끗히 지우고 흐릿한 효과 주기
        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[1]);

        // 선택 영역 그리기
        canvas.drawRect(tlX, tlY, brX, brY, paints[2]);
        canvas.drawRect(tlX, tlY, brX, brY, paints[3]);
        canvas.drawCircle(tlX, tlY, ViewRange, paints[4]);
        canvas.drawCircle(trX, trY, ViewRange, paints[4]);
        canvas.drawCircle(blX, blY, ViewRange, paints[4]);
        canvas.drawCircle(brX, brY, ViewRange, paints[4]);
        canvasView.setImageBitmap(cropBitmap);
    }

    // 선택 영역 자유 변형 클릭
    private void crop_FreeClick(){
        canvasView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    // 현재 터치하고 있는 좌표 받아오기
                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    // 이미지 안쪽을 터치하고 있을 때 작동
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

                // 터치한 후 손가락을 움직일 때
                if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    // 일단 좌표를 받아온 후
                    int x = (int)motionEvent.getX();
                    int y = (int)motionEvent.getY();

                    //누르고 있는 좌표가 캔버스 밖이라면 터치하는 좌표가 안쪽 끝으로 되게끔 터치하고 있는 좌표 강제 재설정
                    if(x < ViewRange) x = ViewRange;
                    if(x > CanvasWidth - ViewRange) x = CanvasWidth - ViewRange;
                    if(y < ViewRange) y = ViewRange;
                    if(y > CanvasHeight - ViewRange) y = CanvasHeight - ViewRange;

                    // 터치하는 위치에 따라 curr 좌표가 바뀌면서
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

                        // 선택 영역 다시 그리기. 캔버스 안 지우면 뒤에 이전 선택 영역이 계속 남아서 움직일 때마다 캔버스 완전 지우고 선택 영역 그리고를
                        // 반복한다. (애니메이션 만들 때 그림 여러 장을 연속적으로 움직이는 거라 생각하면 이해하기 조금 편함)
                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[0]);
                        canvas.drawRect(0, 0, CanvasWidth, CanvasHeight, paints[1]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[2]);
                        canvas.drawRect(currLeft, currTop, currRight, currBottom, paints[3]);

                        canvas.drawCircle(currLeft, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currLeft, currBottom, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currTop, ViewRange, paints[4]);
                        canvas.drawCircle(currRight, currBottom, ViewRange, paints[4]);

                        // 캔버스 변경 알리는 건데, 동작하는지는 테스트 따로 안해봤고 일단 냅둠
                        canvasView.invalidate();
                        canvasView.setImageBitmap(cropBitmap);
                    }

                }
                return true;
            }
        });
    }

    // 비율 일정 변형 부분인데 위에랑 다른 부분은 똑같고 주석 긴 부분만 대충 이해하면 될 듯
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
    // 백그라운드 버튼 클릭
    public void backgroundButtonClick(){

        // 인플레이터로 뷰 동적 생성 및 commonButtonClick에서 뷰 디자인 대략적으로 구성
        View v = inflater.inflate(R.layout.sc_edit_background, null);
        commonButtonClick(v);

        // 합성할 이미지 가져온 후 배경이 들어갈 이미지 뷰를 일단 깨끗하게 지우기. 이후 합성할 이미지의 크기 조절 영역을 위한 캔버스 초기화
        BGBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        mImageView.setImageBitmap(null);
        setCanvas();

        // 완료 버튼 클릭
        ImageButton done = v.findViewById(R.id.edit_background_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 일단 캔버스에 작업한 내용이 남아있지 않게 깨끗하게 지우고
                // 배경으로 쓸 이미지 (b)와 합성할 이미지를 해당 캔버스에 같이 붙여버린다.
                // 이 때 new Rect를 이용해서 배경과 합성할 이미지의 위치까지 제대로 세팅되게 만들어준다.
                canvas.drawRect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight, paintsForBackground[0]);
                Bitmap b = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
                canvas.drawBitmap(b, null, new Rect(0, 0, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight), null);
                canvas.drawBitmap(BGBitmap, null, new Rect(currLeft, currTop, currRight, currBottom), null);

                // canvas는 backgroundBitmap이라는 도화지를 깔아놓고, 해당 도화지에 위에서 drawBitmap으로 이미지를 합성시킨 것.
                // 작업한 후에는 캔버스뷰에 해당 합성이미지를 넘겨준다.
                canvasView.setImageBitmap(backgroundBitmap);

                // 이제 캔버스뷰에 있는 합성 이미지를 결과를 출력하기 위한 mImageView에 최종적으로 띄워주고, 캔버스뷰는 다시 깨끗하게 지워준다.
                Bitmap resultBitmap = ((BitmapDrawable)canvasView.getDrawable()).getBitmap();
                canvasView.setImageBitmap(null);
                mImageView.setImageBitmap(resultBitmap);

                // 되돌리기 및 재실행 기능을 위해 합성 이미지를 prevList에 넣어준다
                sc_imageEditActivity.prevList.add(resultBitmap);
                sc_imageEditActivity.prevListIdx = sc_imageEditActivity.prevList.size();

                SC_MainActivity.ImageWidth = SC_MainActivity.fImageWidth;
                SC_MainActivity.ImageHeight = SC_MainActivity.fImageHeight;

                // 배경합성 버튼 뷰를 끄고 edit 뷰로 되돌아오기
                buttonGroup.removeViewAt(1);
                buttonGroup.getChildAt(0).setVisibility(View.VISIBLE);
            }
        });

        // 닫기 버튼 클릭
        ImageButton close = v.findViewById(R.id.edit_background_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 캔버스에서 작업하던거 다 지우고 원래 이미지를 이미지뷰에 띄우고 편집 뷰로 되돌아오기
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

                // 해당 버튼을 누르면 갤러리로 접근해서 맨 처음의 이미지를 썸네일로 띄워주게 됨
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sc_imageEditActivity.accessGallery();
                    }
                });
            }
            // 나머지 미리 넣어둔 배경 가져오기 버튼 (128x128 사이즈로)
            else{
                Drawable drawable = button.getDrawable();
                Bitmap b = ((BitmapDrawable)drawable).getBitmap();
                Bitmap resultBitmap = resizeBitmapImage(b, 128, 128);
                button.setImageBitmap(resultBitmap);

                // backgroundIds는 drawable 폴더에 이미지의 경로 (id)를 미리 담아둔 배열. 해당 경로를 통해 R.drawable.(id)를 가져와서 배경으로 깐다.
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

    // 합성할 이미지 크기 조절 영역을 위한 캔버스 세팅.
    // 위의 선택 영역 표현과 크게 다를 게 없지만, 이미지 가운데를 터치해서 끌고 다니면 좌표를 이동시킬 수 있는 기능이 추가됨.
    // 크기 조절은 선택영역의 free와 같은 방식으로 동작
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

        // 여기서 위의 crop의 선택 영역과 페인트가 조금 다른데, crop의 선택 영역은 일단 캔버스를 지우고 선택 영역의 안은 투명하게 그럈지만,
        // 여기서는 선택 영역 안을 합성할 이미지로 그렸다는 점
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
                    // 위는 crop의 선택 영역 모서리 찍는 것과 똑같고, 여기서부터는 이미지 안쪽을 누르고 사진을 원하는 위치로 이동시키기 위한 기능
                    else if(DrawStatus.equals("inner")){

                        // curr의 좌우 좌표와 상하 좌표가 손가락이 움직이는 방향과 대략적으로 비슷하게 직선이동을 하게끔 구현
                        currLeft = currLeft + x - prevX; currRight = currRight + x - prevX;
                        currTop = currTop + y - prevY;  currBottom = currBottom + y - prevY;

                        // 이 때 상하좌우 좌표가 배경 영역 밖을 벗어난다면 벗어나지 않는 가장 끝쪽으로 고정되게 좌표 강제 재설정
                        if(currLeft < ViewRange || currLeft > SC_MainActivity.fImageWidth - ViewRange
                                || currRight < ViewRange || currRight > SC_MainActivity.fImageWidth - ViewRange){
                            currLeft = currLeft - (x - prevX); currRight = currRight - (x - prevX);
                        }
                        if(currTop < ViewRange || currTop > SC_MainActivity.fImageHeight - ViewRange
                                || currBottom < ViewRange || currBottom > SC_MainActivity.fImageHeight - ViewRange){
                            currTop = currTop - (y - prevY); currBottom = currBottom - (y - prevY);
                        }

                        // 역시 움직일 때마다 영역 깨끗하게 지우고 다시 그려주는 방식으로 표현
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

    // drawable의 경로 초기화
    private void setBackgroundPath(){

        backgroundIds = new int[]{
                R.drawable.spring, R.drawable.flower, R.drawable.frame
        };
    }

    // 배경 합성을 위한 페인트
    private void makeBackgroundPaint(){

        // 캔버스 지우기 페인트
        Paint clearPaint = new Paint();
        clearPaint.setColor(Color.TRANSPARENT);
        Xfermode xmode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        clearPaint.setXfermode(xmode);

        // 그림 크기 조절 영역 테두리 그리기
        Paint StrokePaint = new Paint();
        StrokePaint.setStyle(Paint.Style.STROKE);
        StrokePaint.setStrokeCap(Paint.Cap.ROUND);
        StrokePaint.setColor(Color.WHITE);
        StrokePaint.setStrokeWidth(3);

        // 안쪽 깨끗하게 지우기인데 이거 복붙하면서 필요없는 거 안지웠네. 아마 쓰는 부분이 없을 거 같긴한데 괜히 지웠다가 귀찮아질 수 있으니 걍 냅둬보기.
        Paint FillPaint = new Paint();
        FillPaint.setStyle(Paint.Style.FILL);
        FillPaint.setColor(Color.TRANSPARENT);
        Xfermode FillMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        FillPaint.setXfermode(FillMode);

        // 모서리 둥글게 페인트
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


    // 이 정도 코드는 아마 설명 안해도 충분히 이해할 수 있을거라 생각함. 어려운게 아니라... 모르는거 있으면 물어보고.

    public Bitmap rotateBitmap;
    public Bitmap originalRotateBitmap;
    public void rotateButtonClick(){

        View v = inflater.inflate(R.layout.sc_edit_rotate, null);
        commonButtonClick(v);


        // 이거 seekbar로 회전 각도 조절하는건데 너무 느려서 이걸로는 포기. 궁금하면 아래 주석 펼쳐보기
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
