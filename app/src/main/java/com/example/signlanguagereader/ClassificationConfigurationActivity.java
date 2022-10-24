package com.example.signlanguagereader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class ClassificationConfigurationActivity extends AppCompatActivity {

    private RadioGroup radioGroupModels;

    private RadioGroup radioGroupAccelerators;

    private SeekBar seekBarThreads;

    private Button buttonProceed;

    private EditText autoSaveIntervalInput;

    private RadioGroup radioGroupRecognitionMode;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition_configuration_activity);

        radioGroupModels = findViewById(R.id.radio_group_models);
        radioGroupAccelerators = findViewById(R.id.radio_group_accelerators);

        seekBarThreads = findViewById(R.id.chooseBar);

        buttonProceed = findViewById(R.id.proceed_btn);

        autoSaveIntervalInput = findViewById(R.id.auto_save_interval_input);

        radioGroupRecognitionMode = findViewById(R.id.radio_group_recognition_mode);

        enableButtonOnClickListener();

    }

    private void enableButtonOnClickListener() {
        buttonProceed.setOnClickListener(view -> {
                    int modelId = radioGroupModels.getCheckedRadioButtonId();
                    int acceleratorId = radioGroupAccelerators.getCheckedRadioButtonId();
                    int numOfThreads = seekBarThreads.getProgress();
                    String autoSaveIntervalString = autoSaveIntervalInput.getText().toString();
                    int autoSaveInterval = autoSaveIntervalString.isEmpty() ? 20 : Integer.parseInt(autoSaveIntervalString);
                    int recognitionMode = radioGroupRecognitionMode.getCheckedRadioButtonId();
                    switchToClassificationActivity(modelId, acceleratorId, numOfThreads, autoSaveInterval, recognitionMode);
                }
        );
    }

    private void switchToClassificationActivity(int modelType, int acceleratorType, int numOfThreads, int autoSaveInterval, int recognitionMode) {
        getIntent().putExtra("modelType", modelType);
        getIntent().putExtra("acceleratorType", acceleratorType);
        getIntent().putExtra("numOfThreads", numOfThreads);
        getIntent().putExtra("autoSaveInterval", autoSaveInterval);
        getIntent().putExtra("recognitionMode", recognitionMode);
        getIntent().setClass(this, ClassificationActivity.class);
        startActivity(getIntent());
    }
}
