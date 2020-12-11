package com.example.switchcamera.SCActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.switchcamera.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SC_ImageEditActivity extends AppCompatActivity {

    private ImageView sc_edit_imageView;
    private ImageView sc_edit_canvasView;
    private FrameLayout buttonGroup;

    private Button cropButton, filterButton, backgroundButton, rotateButton;
    private ImageButton undoButton, redoButton, doneButton;
    private SC_ButtonFunction buttonFunction;
    public SeekBar rotateSeekBar;
    public TextView rotateSeekBarText;

    private int ViewRange;

    public LinkedList<Bitmap> prevList = new LinkedList<>();
    public int prevListIdx = 0;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_edit_activity);

        sc_edit_imageView = findViewById(R.id.sc_edit_imageview);
        sc_edit_canvasView = findViewById(R.id.sc_edit_canvas);

        buttonGroup = findViewById(R.id.sc_edit_buttongroup);

        buttonFunction = new SC_ButtonFunction(buttonGroup, getApplicationContext(), sc_edit_imageView, sc_edit_canvasView,
        this);

        redoButton = findViewById(R.id.sc_edit_redo);
        undoButton = findViewById(R.id.sc_edit_undo);
        doneButton = findViewById(R.id.sc_edit_done);

        cropButton = (Button) findViewById(R.id.edit_crop);
        filterButton = findViewById(R.id.edit_filter);
        backgroundButton = findViewById(R.id.edit_background);
        rotateButton = findViewById(R.id.edit_rotate);

        setButtonsClickListener();

    }


    // 각 버튼 클릭 기능 모아둔 함수
    private void setButtonsClickListener(){

        // 재실행 버튼 클릭. 리스트 인덱스의 뒤쪽으로 이동한다.
        redoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(prevList.size() > 0 && prevListIdx < prevList.size() - 1){
                    prevListIdx++;
                    SC_MainActivity.ImageWidth = prevList.get(prevListIdx).getWidth();
                    SC_MainActivity.ImageHeight = prevList.get(prevListIdx).getHeight();
                    sc_edit_imageView.setImageBitmap(prevList.get(prevListIdx));
                }
            }
        });

        // 되돌리기 버튼 클릭. 리스트 인덱스의 앞쪽으로 이동한다.
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(prevList.size() > 0 && prevListIdx > 0){
                    prevListIdx--;
                    SC_MainActivity.ImageWidth = prevList.get(prevListIdx).getWidth();
                    SC_MainActivity.ImageHeight = prevList.get(prevListIdx).getHeight();
                    sc_edit_imageView.setImageBitmap(null);
                    sc_edit_imageView.setImageBitmap(prevList.get(prevListIdx));
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveImage();
                    Intent intent = new Intent(SC_ImageEditActivity.this, SC_MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonFunction.cropButtonClick();
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                buttonFunction.filterButtonClick();
            }
        });

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonFunction.backgroundButtonClick();
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonFunction.rotateButtonClick();
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        // 일단 사진 찍은거나 갤러리에서 가져온 이미지를 리사이징해 이미지 뷰에 붙이기
        if(sc_edit_imageView.getDrawable() == null) {
            Intent intent = getIntent();
            // 갤러리에서 가져온 이미지를 넘겨주는 인텐트라면-
            if (intent.getStringExtra("from").equals("gallery")) {
                Bitmap bitmap = null;
                Bitmap resultBitmap = null;
                ViewRange = SC_MainActivity.fImageWidth / 30 / 2;

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getApplicationContext().getContentResolver(),
                            SC_MainActivity.galleryUri
                    );
                    resultBitmap = resizeBitmapImage(bitmap, SC_MainActivity.ImageWidth - ViewRange * 2, SC_MainActivity.ImageHeight - ViewRange * 2);
                    buttonFunction.setmBitmap(resultBitmap);
                    sc_edit_imageView.setImageBitmap(resultBitmap);
                    prevList.add(resultBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 카메라로 찍은 이미지를 넘겨주는 인텐트라면-
            } else if (intent.getStringExtra("from").equals("camera")) {

                ViewRange = SC_MainActivity.fImageWidth / 30 / 2;

                Bitmap resultBitmap
                        = resizeBitmapImage(SC_MainActivity.capturedBitmap, SC_MainActivity.ImageWidth - ViewRange * 2, SC_MainActivity.ImageHeight - ViewRange * 2);

                buttonFunction.setmBitmap(resultBitmap);
                sc_edit_imageView.setImageBitmap(resultBitmap);
                prevList.add(resultBitmap);
            }
        }
    }

    // 배경으로 쓸 이미지를 찾으러 갤러리 접근하기. 인텐트로 갤러리에 접근하고 밑에 있는 onActivityResult 메소드에서 찾은 이미지를 메인 이미지뷰에 보여주게끔 만듬.
    public void accessGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // background를 깔고 합성하기 위한 intent 전달
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // gallery에서 가져온 uri를 bitmap으로 변환하고 리사이징
            Uri galleryUri = data.getData();
            try {
                Bitmap resultBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), galleryUri);
                resultBitmap = resizeBitmapImage(resultBitmap, SC_MainActivity.fImageWidth, SC_MainActivity.fImageHeight);
                sc_edit_imageView.setImageBitmap(resultBitmap);

                buttonFunction.setCanvas();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap resizeBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    // 편집한 이미지 저장
    private void saveImage() throws Exception {

        if (ActivityCompat.checkSelfPermission(SC_ImageEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            long time = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            Date dd = new Date(time);
            String timeString = format.format(dd);

            String url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator + "SwitchCamera";

            File file = new File(url);
            file.mkdir();

            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String fileName = timeString + ".jpg";
                File ImageFile = new File(url, fileName);

                FileOutputStream out = new FileOutputStream(ImageFile);
                Bitmap resultImage = ((BitmapDrawable) sc_edit_imageView.getDrawable()).getBitmap();
                resultImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

                out.flush();
                out.close();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + url + File.separator + fileName)));

                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();

            }
        }
    }

}
