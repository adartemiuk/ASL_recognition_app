package com.example.signlanguagereader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button cameraButton, galleryButton, videoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = findViewById(R.id.recon_from_image_btn);
        cameraButton.setOnClickListener(view -> switchToClassificationConfigurationActivity(cameraButton.getId()));

        galleryButton = findViewById(R.id.recon_from_gallery_btn);
        galleryButton.setOnClickListener(view -> switchToClassificationConfigurationActivity(galleryButton.getId()));

        videoButton = findViewById(R.id.recon_from_video_btn);
        videoButton.setOnClickListener(view -> switchToClassificationConfigurationActivity(videoButton.getId()));
    }

    private void switchToClassificationConfigurationActivity(int buttonId) {
        Intent switchIntent = new Intent(this, ClassificationConfigurationActivity.class);
        switchIntent.putExtra("buttonType", buttonId);
        startActivity(switchIntent);
    }
}