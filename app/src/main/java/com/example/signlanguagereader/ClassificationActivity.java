package com.example.signlanguagereader;

import static com.example.signlanguagereader.utils.BitmapUtils.flipBitmap;
import static com.example.signlanguagereader.utils.BitmapUtils.resize;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;

import com.example.signlanguagereader.utils.FileUtils;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class ClassificationActivity extends AppCompatActivity {

    private ImageView imagePreview;

    private volatile TextView predictionSummary;
    private int buttonId;
    private Bitmap bitmap;
    private final ActivityResultLauncher<Intent> galleryLauncher = setUpGalleryLauncher();
    private Classification classification;
    private CameraLauncher cameraLauncher;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!OpenCVLoader.initDebug()) {

        }
        super.onCreate(savedInstanceState);
        buttonId = getIntent().getIntExtra("buttonType", 0);

        int autoSaveInterval = getIntent().getIntExtra("autoSaveInterval", 20);

        int recognitionMode = getIntent().getIntExtra("recognitionMode", 0);
        boolean predictAllGestures = recognitionMode == R.id.radio_all_gestures_recognition;

        int modelId = getIntent().getIntExtra("modelType", 0);
        Models selectedModel = Models.valueOfId(modelId);

        int acceleratorId = getIntent().getIntExtra("acceleratorType", 0);
        Accelerators selectedAccelerator = Accelerators.valueOfId(acceleratorId);

        int numOfThreads = getIntent().getIntExtra("numOfThreads", 0) + 1;

        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ClassificationActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        }

        String logMessage = String.format(getString(R.string.start_activity_info), buttonId, selectedModel.toString(), selectedAccelerator, numOfThreads);
        Log.d("INFO", logMessage);

        classification = new Classification(selectedModel, selectedAccelerator, numOfThreads, autoSaveInterval, predictAllGestures, getApplicationContext());
        cameraLauncher = new CameraLauncher(classification, this, getApplicationContext());
        switch (buttonId) {
            case R.id.recon_from_image_btn:
                setContentView(R.layout.camera_classification_activity);
                classification.setClassificationType(ClassificationType.IMAGE);
                cameraLauncher.setImageCaptureButton(findViewById(R.id.capture_btn));
                cameraLauncher.setPreviewView(findViewById(R.id.camera_previewView));
                cameraLauncher.launch();
                break;
            case R.id.recon_from_gallery_btn:
                setContentView(R.layout.image_classification_activity);
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                galleryLauncher.launch(gallery);
                setUpPredictButton();
                break;
            case R.id.recon_from_video_btn:
                Log.d("INFO", "Start acquisition");
                setContentView(R.layout.video_classification_activity);
                classification.setClassificationType(ClassificationType.VIDEO);
                cameraLauncher.setPreviewView(findViewById(R.id.previewView));
                cameraLauncher.setPredictionSummary(findViewById(R.id.predictionSummary));
                Button stopAcquisitionButton = findViewById(R.id.stop_acquisition_btn);
                stopAcquisitionButton.setOnClickListener(view -> {
                    ProcessCameraProvider cameraProvider = cameraLauncher.getCameraProvider();
                    if (cameraProvider != null) {
                        if (Classification.indexOfGesture == FileUtils.loadLabelList(getApplicationContext()).size()) {
                            FileUtils.saveResultsToFile(getApplicationContext(), classification);
                        }
                        cameraProvider.unbindAll();
                        Log.d("INFO", "Stop acquisition");
                        Intent switchIntent = new Intent(this, ClassificationConfigurationActivity.class);
                        switchIntent.putExtra("buttonType", buttonId);
                        startActivity(switchIntent);
                    }
                });
                cameraLauncher.launch();
                break;
        }
    }

    private ActivityResultLauncher<Intent> setUpGalleryLauncher() {
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                Uri imgUri = result.getData().getData();
                try {
                    imagePreview = findViewById(R.id.image_preview);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                    bitmap = flipBitmap(bitmap, 90);
                    imagePreview.setImageBitmap(resize(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void setUpPredictButton() {
        Button predictButton = findViewById(R.id.predict_btn);
        predictButton.setOnClickListener(view -> {
            predictionSummary = findViewById(R.id.prediction_summary);
            classification.handlePredictionTriggeredByButton(bitmap);
            imagePreview = findViewById(R.id.image_preview);
            imagePreview.setImageBitmap(bitmap);
            String summary = classification.getOutputSummary().getClassificationSummary(getApplicationContext());
            predictionSummary.setText(summary);
        });
    }
}
