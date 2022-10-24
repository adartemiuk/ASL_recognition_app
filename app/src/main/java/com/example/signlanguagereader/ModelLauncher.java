package com.example.signlanguagereader;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.model.Model;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class ModelLauncher {

    public Interpreter launchModel(Models selectedModel, Accelerators selectedAccelerator, int numOfThreads, Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if (selectedAccelerator == Accelerators.GPU) {
            Log.d("INFO", "Using GPU");
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            delegateOptions.setPrecisionLossAllowed(false);
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
        } else if (selectedAccelerator == Accelerators.NNAPI && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.d("INFO", "Using NN API");
            NnApiDelegate nnApiDelegate = new NnApiDelegate();
            options.addDelegate(nnApiDelegate);
        } else {
            Log.d("INFO", "Using CPU");
            options.setNumThreads(numOfThreads);
        }

        Model model = Model.createModel(context, assignModel(selectedModel));
        MappedByteBuffer modelData = model.getData();

        return new Interpreter(modelData, options);
    }

    private String assignModel(Models selectedModel) {
        switch (selectedModel) {
            case VGG_19:
                return "cnn_model_vgg_19.tflite";
            case INCEPTION_V3:
                return "cnn_model_inception_v3.tflite";
            case MODEL_WITH_DECOMPOSED_LAYERS:
                return "cnn_model_decomposed.tflite";
            case QUANTIZED_MODEL_QAT:
                return "cnn_model_qat.tflite";
            case QUANTIZED_MODEL_PTQ:
                return "cnn_model_ptq.tflite";
            case KNOWLEDGE_DISTILLATION_MODEL:
                return "cnn_model_from_knowledge_distillation.tflite";
            default:
                return "cnn_model_base.tflite";
        }
    }
}
