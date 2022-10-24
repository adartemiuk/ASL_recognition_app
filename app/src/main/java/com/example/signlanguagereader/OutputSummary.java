package com.example.signlanguagereader;

import android.content.Context;
import android.util.Log;

import com.example.signlanguagereader.utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutputSummary {

    private final List<String> labels;

    private List<Float> output;

    private long inferenceTime;
    private int numOfFrame;

    public OutputSummary(Context context) {
        this.labels = FileUtils.loadLabelList(context);
    }

    public void setInferenceTime(long inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    public void setNumOfFrame(int numOfFrame) {
        this.numOfFrame = numOfFrame;
    }

    public String printClassificationResultInCSVFormat(Context context) {
        return String.format(context.getString(R.string.classification_results), numOfFrame, this.getPredictedLabel(), this.getPredictionProbability() * 100, inferenceTime);
    }

    public void logClassificationResult(Context context) {
        String logMessage = String.format(context.getString(R.string.log_message), numOfFrame, this.getClassificationSummary(context), inferenceTime);
        Log.d("INFO", logMessage);
    }

    public String getClassificationSummary(Context context) {
        float accuracy = getPredictionProbability();
        String label = getPredictedLabel();
        StringBuilder result = new StringBuilder();
        if (accuracy >= 0.7 && !label.equals("Nothing"))
            result.append(String.format(context.getString(R.string.classification_summary), label, accuracy * 100));
        else
            result.append("No gesture detected");
        return result.toString();
    }

    public String getPredictedLabel() {
        Float value = getPredictionProbability();
        int index = output.indexOf(value);
        return labels.get(index);
    }

    public Float getPredictionProbability() {
        return Collections.max(output);
    }

    public void outputArrayToList(float[] array) {
        output = new ArrayList<>();
        for (float f : array) {
            output.add(f);
        }
    }
}
