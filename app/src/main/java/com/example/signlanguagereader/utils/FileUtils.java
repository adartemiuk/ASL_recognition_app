package com.example.signlanguagereader.utils;

import android.content.Context;
import android.util.Log;

import com.example.signlanguagereader.Accelerators;
import com.example.signlanguagereader.Classification;
import com.example.signlanguagereader.Models;
import com.example.signlanguagereader.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtils {

    private static final String LABELS_TXT_PATH = "labels.txt";

    private static final String EMPTY_DIR = "";

    public static File createOutputImageFile(Context context) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.US);
        String timestamp = simpleDateFormat.format(new Date());

        File imagesDir = new File(String.format(context.getString(R.string.images_dir_path), context.getFilesDir().getAbsolutePath()));
        File imageFile = new File(String.format(context.getString(R.string.image_file_path), context.getFilesDir().getAbsolutePath(), timestamp));

        if (!imagesDir.exists()) {
            boolean dirCreated = imagesDir.mkdir();
            if (dirCreated) {
                Log.d("INFO", String.format(context.getString(R.string.notification_img_dir_created), context.getFilesDir().getAbsolutePath()));
            }
        }

        boolean fileCreated = imageFile.createNewFile();

        if (fileCreated) {
            Log.d("INFO", String.format(context.getString(R.string.notification_img_file_created), timestamp, context.getFilesDir().getAbsolutePath()));
        }

        return imageFile;
    }

    public static void saveResultsToFile(Context context, Classification classification) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.US);
        String timestamp = simpleDateFormat.format(new Date());
        Accelerators selectedAccelerator = classification.getSelectedAccelerator();
        Models selectedModel = classification.getSelectedModel();
        String modelPath = "/" + selectedModel.toString();
        String acceleratorPath = "/" + selectedAccelerator.toString() + (selectedAccelerator.equals(Accelerators.CPU) ? "_" + classification.getNumOfThreads() : "");
        String gesturePath = classification.getGesture() != null ? "/" + classification.getGesture() : "";
        List<String> acquisitionResults = classification.getAcquisitionResults();
        try {
            File resultsDir = new File(String.format(context.getString(R.string.results_dir_path), context.getFilesDir().getAbsolutePath(), EMPTY_DIR, EMPTY_DIR, EMPTY_DIR));
            File resultsModelDir = new File(String.format(context.getString(R.string.results_dir_path), context.getFilesDir().getAbsolutePath(), modelPath, EMPTY_DIR, EMPTY_DIR));
            File resultsAcceleratorDir = new File(String.format(context.getString(R.string.results_dir_path), context.getFilesDir().getAbsolutePath(), modelPath, acceleratorPath, EMPTY_DIR));
            File resultsGestureDir = new File(String.format(context.getString(R.string.results_dir_path), context.getFilesDir().getAbsolutePath(), modelPath, acceleratorPath, gesturePath));
            File resultsFile = new File(String.format(context.getString(R.string.results_file_path), context.getFilesDir().getAbsolutePath(), modelPath, acceleratorPath, gesturePath, timestamp));

            checkIfDirExists(resultsDir, context);
            checkIfDirExists(resultsModelDir, context);
            checkIfDirExists(resultsAcceleratorDir, context);
            checkIfDirExists(resultsGestureDir, context);

            boolean fileCreated = resultsFile.createNewFile();

            if (fileCreated) {
                Log.d("INFO", String.format(context.getString(R.string.notification_file_created), timestamp, resultsFile.getAbsolutePath()));
            }

            FileWriter fileWriter = new FileWriter(resultsFile);
            for (String result : acquisitionResults) {
                fileWriter.append(result);
                fileWriter.append("\n");
            }
            fileWriter.flush();
            fileWriter.close();
            acquisitionResults.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkIfDirExists(File dirToCreate, Context context) {
        if (!dirToCreate.exists()) {
            boolean dirCreated = dirToCreate.mkdir();
            if (dirCreated) {
                Log.d("INFO", String.format(context.getString(R.string.notification_dir_created), dirToCreate.getAbsolutePath()));
            }
        }
    }

    public static List<String> loadLabelList(Context context) {
        List<String> labelList = new ArrayList<>();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(context.getAssets().open(LABELS_TXT_PATH)));
            String line;
            while ((line = reader.readLine()) != null) {
                labelList.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return labelList;
    }
}
