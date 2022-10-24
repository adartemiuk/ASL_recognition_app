package com.example.signlanguagereader;

import static com.example.signlanguagereader.utils.BitmapUtils.flipBitmap;
import static com.example.signlanguagereader.utils.BitmapUtils.resize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.signlanguagereader.utils.FileUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraLauncher {

    public static int numOfFrame;
    private static long start;
    private static long now;
    private final Context context;
    private final Classification classification;
    private final ClassificationActivity activity;
    private PreviewView previewView;
    private TextView predictionSummary;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Button imageCaptureButton;
    private ImageAnalysis imageAnalysis;
    private Timer timer;
    private boolean isAnalyzerRunning = false;

    public CameraLauncher(Classification classification, ClassificationActivity activity, Context context) {
        numOfFrame = 0;
        this.classification = classification;
        this.activity = activity;
        this.context = context;
    }

    public void setPredictionSummary(TextView predictionSummary) {
        this.predictionSummary = predictionSummary;
    }

    public void setImageCaptureButton(Button imageCaptureButton) {
        this.imageCaptureButton = imageCaptureButton;
    }

    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
    }

    public ProcessCameraProvider getCameraProvider() {
        return cameraProvider;
    }

    private void launchVideoClassification() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();
                imageAnalysis.setAnalyzer(Executors.newCachedThreadPool(), imageProxy -> {
                    if (classification.isPredictAllGestures() && !isAnalyzerRunning) {
                        displayNewGestureInfo();
                        isAnalyzerRunning = true;
                        start = System.currentTimeMillis();
                    }
                    @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
                    assert image != null;
                    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
                    bitmap = flipBitmap(bitmap, 90);
                    try {
                        classification.classifyGesture(bitmap);
                        predictionSummary.setText(classification.getOutputSummary().getClassificationSummary(context));
                        now = System.currentTimeMillis();
                        if ((int) ((now - start) / 1000) >= classification.getAutoSaveInterval()) {
                            FileUtils.saveResultsToFile(context, classification);
                            if (Classification.indexOfGesture == FileUtils.loadLabelList(context).size() - 1) {
                                imageAnalysis.clearAnalyzer();
                                activity.findViewById(R.id.stop_acquisition_btn).callOnClick();
                            }
                            if (classification.isPredictAllGestures()) {
                                Classification.indexOfGesture++;
                                displayNewGestureInfo();
                            }
                            start = System.currentTimeMillis();
                        }
                        numOfFrame++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageProxy.close();
                });
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.bindToLifecycle(activity, cameraSelector, imageAnalysis, preview);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void launchImageClassification() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));

        imageCaptureButton.setOnClickListener((view) -> captureImage(activity));
    }

    private void captureImage(ClassificationActivity activity) {
        if (imageCapture == null) {
            return;
        }
        try {
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(FileUtils.createOutputImageFile(context)).build();
            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(context),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Uri imgUri = outputFileResults.getSavedUri();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imgUri);
                                bitmap = flipBitmap(bitmap, 90);
                                activity.setContentView(R.layout.image_classification_activity);
                                ImageView imagePreview = activity.findViewById(R.id.image_preview);
                                imagePreview.setImageBitmap(resize(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2));
                                activity.setBitmap(bitmap);
                                activity.setUpPredictButton();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException error) {
                            Log.d("ERROR", "Image capture has failed");
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launch() {
        timer = new Timer();
        timer.start();
        if (classification.getClassificationType().equals(ClassificationType.IMAGE)) {
            launchImageClassification();
        } else if (classification.getClassificationType().equals(ClassificationType.VIDEO)) {
            launchVideoClassification();
        }
    }

    private void displayNewGestureInfo() {
        classification.setGesture(FileUtils.loadLabelList(context).get(Classification.indexOfGesture));
        timer.count();
        synchronized (imageAnalysis) {
            try {
                imageAnalysis.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class Timer extends Thread {
        private CountDownTimer timer;

        @Override
        public void run() {
            Looper.prepare();
            timer = new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    predictionSummary.setText(String.format(activity.getString(R.string.pause_prediction_info), classification.getGesture(), millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    predictionSummary.setText(String.format(activity.getString(R.string.resume_prediction_info), classification.getGesture()));
                    synchronized (imageAnalysis) {
                        imageAnalysis.notify();
                    }
                }
            };
            Looper.loop();
        }

        public void count() {
            timer.start();
        }

    }

}
