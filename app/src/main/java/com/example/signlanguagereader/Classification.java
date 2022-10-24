package com.example.signlanguagereader;

import static com.example.signlanguagereader.utils.BitmapUtils.resize;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Classification {

    public static int indexOfGesture;

    private final OutputSummary outputSummary;

    private final Preprocessor preprocessor;
    private final Accelerators selectedAccelerator;
    private final int numOfThreads;
    private final Models selectedModel;
    private final List<String> acquisitionResults = new ArrayList<>();
    private final boolean predictAllGestures;
    private final Context context;
    private final int autoSaveInterval;
    private Interpreter interpreter;
    private String gesture;
    private ClassificationType classificationType;

    public Classification(Models selectedModel, Accelerators selectedAccelerator, int numOfThreads, int autoSaveInterval, boolean predictAllGestures, Context context) {
        indexOfGesture = 0;
        this.selectedModel = selectedModel;
        this.selectedAccelerator = selectedAccelerator;
        this.numOfThreads = numOfThreads;
        this.autoSaveInterval = autoSaveInterval;
        this.predictAllGestures = predictAllGestures;
        this.context = context;
        this.preprocessor = new Preprocessor();
        setUpInterpreter();
        this.outputSummary = new OutputSummary(context);
    }

    public Models getSelectedModel() {
        return selectedModel;
    }

    private void setUpInterpreter() {
        try {
            ModelLauncher launcher = new ModelLauncher();
            interpreter = launcher.launchModel(selectedModel, selectedAccelerator, numOfThreads, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OutputSummary getOutputSummary() {
        return outputSummary;
    }

    public List<String> getAcquisitionResults() {
        return acquisitionResults;
    }

    public ClassificationType getClassificationType() {
        return classificationType;
    }

    public void setClassificationType(ClassificationType classificationType) {
        this.classificationType = classificationType;
    }

    public int getNumOfThreads() {
        return numOfThreads;
    }

    public Accelerators getSelectedAccelerator() {
        return selectedAccelerator;
    }

    public boolean isPredictAllGestures() {
        return predictAllGestures;
    }

    public String getGesture() {
        return this.gesture;
    }

    public void setGesture(String gesture) {
        this.gesture = gesture;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public void classifyGesture(Bitmap bitmap) throws IOException {
        //PREPROCESS
        long startPreprocessNano = System.nanoTime();
        TensorImage input = preprocessor.processImage(bitmap, selectedModel);
        long stopPreprocessNano = System.nanoTime();
        Log.d("INFO", "Time of image preprocessing " + (TimeUnit.MILLISECONDS.convert((stopPreprocessNano - startPreprocessNano), TimeUnit.NANOSECONDS)));

        //SETUP MODEL AND INPUTS
        FloatBuffer inputBuf = FloatBuffer.allocate(interpreter.getInputTensor(0).numElements());
        FloatBuffer outputBuf = FloatBuffer.allocate(interpreter.getOutputTensor(0).numElements());
        inputBuf.put(input.getBuffer().asFloatBuffer());

        //INFERENCE
        interpreter.run(inputBuf.rewind(), outputBuf);

        //OUTPUT DATA
        outputSummary.outputArrayToList(outputBuf.array());
        outputSummary.setInferenceTime((TimeUnit.MILLISECONDS.convert(interpreter.getLastNativeInferenceDurationNanoseconds(), TimeUnit.NANOSECONDS)));
        outputSummary.setNumOfFrame(CameraLauncher.numOfFrame);

        //PRINT RESULTS
        acquisitionResults.add(outputSummary.printClassificationResultInCSVFormat(context));
        outputSummary.logClassificationResult(context);
    }

    public void handlePredictionTriggeredByButton(Bitmap bitmap) {
        try {
            Log.d("INFO", "Start acquisition");
            classifyGesture(bitmap);
            Log.d("INFO", "Stop acquisition");
            resize(bitmap, 224, 224);
            preprocessor.proceedHandSegmentation(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
