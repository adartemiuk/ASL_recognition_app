package com.example.signlanguagereader;

import static com.example.signlanguagereader.utils.BitmapUtils.resize;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;

public class Preprocessor {

    private static final int TARGET_INPUT_SIZE = 224;

    public TensorImage processImage(Bitmap bitmap, Models model) {
        bitmap = resize(bitmap, TARGET_INPUT_SIZE, TARGET_INPUT_SIZE);
        proceedHandSegmentation(bitmap);
        TensorImage inputTensorImage = new TensorImage(DataType.FLOAT32);
        inputTensorImage.load(bitmap);
        ImageProcessor imageProcessor;
        if (model.rgbColorMode) {
            imageProcessor = new ImageProcessor.Builder().build();
        } else {
            imageProcessor = new ImageProcessor.Builder().add(new TransformToGrayscaleOp()).build();
        }
        imageProcessor.process(inputTensorImage);
        return inputTensorImage;
    }

    public void proceedHandSegmentation(Bitmap bitmap) {
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Mat mask3 = new Mat();
        Mat mask4 = new Mat();
        Mat mask5 = new Mat();

        Mat matRGB = new Mat();
        Mat matHSV = new Mat();

        Mat result_1 = new Mat();
        Mat result_2 = new Mat();
        Mat result_3 = new Mat();
        Mat result_4 = new Mat();
        Mat result_5 = new Mat();
        Mat result = new Mat();

        // darker skin tones
        Scalar lower_range_1 = new Scalar(0, 100, 0);
        Scalar upper_range_1 = new Scalar(30, 255, 100);

        Scalar lower_range_2 = new Scalar(0, 30, 90);
        Scalar upper_range_2 = new Scalar(30, 255, 255);
        // light skin tones
        Scalar lower_range_3 = new Scalar(160, 75, 0);
        Scalar upper_range_3 = new Scalar(180, 255, 255);

        // very light skin tones
        Scalar lower_range_4 = new Scalar(0, 10, 165);
        Scalar upper_range_4 = new Scalar(40, 255, 255);

        Scalar lower_range_5 = new Scalar(160, 0, 150);
        Scalar upper_range_5 = new Scalar(180, 255, 255);


        Utils.bitmapToMat(bitmap, matRGB);
        Imgproc.cvtColor(matRGB, matRGB, Imgproc.COLOR_RGBA2RGB);

        Imgproc.cvtColor(matRGB, matHSV, Imgproc.COLOR_RGB2HSV);
        Core.inRange(matHSV, lower_range_1, upper_range_1, mask1);
        Core.inRange(matHSV, lower_range_2, upper_range_2, mask2);
        Core.inRange(matHSV, lower_range_3, upper_range_3, mask3);
        Core.inRange(matHSV, lower_range_4, upper_range_4, mask4);
        Core.inRange(matHSV, lower_range_5, upper_range_5, mask5);

        Core.bitwise_and(matRGB, matRGB, result_1, mask1);
        Core.bitwise_and(matRGB, matRGB, result_2, mask2);
        Core.bitwise_and(matRGB, matRGB, result_3, mask3);
        Core.bitwise_and(matRGB, matRGB, result_4, mask4);
        Core.bitwise_and(matRGB, matRGB, result_5, mask5);
        Core.bitwise_or(result_1, result_2, result);
        Core.bitwise_or(result, result_3, result);
        Core.bitwise_or(result, result_4, result);
        Core.bitwise_or(result, result_5, result);

        Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(5, 5)));
        Imgproc.GaussianBlur(result, result, new Size(5, 5), 2);

        Utils.matToBitmap(result, bitmap);
    }
}
