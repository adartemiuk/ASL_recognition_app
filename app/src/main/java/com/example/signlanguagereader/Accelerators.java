package com.example.signlanguagereader;

public enum Accelerators {

    CPU(R.id.radio_cpu),

    GPU(R.id.radio_gpu),

    NNAPI(R.id.radio_nnapi);

    int acceleratorId;

    Accelerators(int id) {
        this.acceleratorId = id;
    }

    public static Accelerators valueOfId(int id) {
        for (Accelerators accelerator : Accelerators.values()) {
            if (accelerator.acceleratorId == id) {
                return accelerator;
            }
        }
        return Accelerators.CPU;
    }
}
