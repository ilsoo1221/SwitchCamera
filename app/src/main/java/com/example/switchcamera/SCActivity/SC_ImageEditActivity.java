package com.example.switchcamera.SCActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;

import com.example.switchcamera.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SC_ImageEditActivity extends AppCompatActivity {

    private ImageView sc_edit_imageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_edit_activity);

        sc_edit_imageView = findViewById(R.id.sc_edit_imageview);
    }

    @Override
    protected void onStart() {
        super.onStart();

        sc_edit_imageView.setImageURI(SC_MainActivity.galleryUri);
    }


}
