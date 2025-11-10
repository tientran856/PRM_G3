package com.example.prm_g3.models;

public class Ingredient {
    public String name;
    public String quantity;
    public int sync_status = 0;

    public Ingredient() {} // Bắt buộc cho Firebase

    public Ingredient(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}

