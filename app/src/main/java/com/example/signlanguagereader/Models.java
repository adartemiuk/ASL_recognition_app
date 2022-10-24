package com.example.signlanguagereader;

public enum Models {

    BASE_MODEL(R.id.radio_base_model, false),

    VGG_19(R.id.radio_vgg, true),

    INCEPTION_V3(R.id.radio_inception, true),

    MODEL_WITH_DECOMPOSED_LAYERS(R.id.radio_optimized_model_decomposition, false),

    QUANTIZED_MODEL_QAT(R.id.radio_model_quantized_qat, false),

    QUANTIZED_MODEL_PTQ(R.id.radio_model_quantized_ptq, false),

    KNOWLEDGE_DISTILLATION_MODEL(R.id.radio_model_knowledge_distillation, false);

    int modelId;

    boolean rgbColorMode;

    Models(int id, boolean rgbColorMode) {
        this.modelId = id;
        this.rgbColorMode = rgbColorMode;
    }

    public static Models valueOfId(int id) {
        for (Models accelerator : Models.values()) {
            if (accelerator.modelId == id) {
                return accelerator;
            }
        }
        return Models.BASE_MODEL;
    }
}
