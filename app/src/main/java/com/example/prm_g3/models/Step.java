package com.example.prm_g3.models;

public class Step {
    public int step_number;
    public String instruction;
    public String image_url;
    public int sync_status = 0;

    public Step() {} // Bắt buộc cho Firebase

    public Step(int step_number, String instruction) {
        this.step_number = step_number;
        this.instruction = instruction;
        this.image_url = "";
    }
}

