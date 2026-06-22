package com.mrammor.create_rvb;

import net.minecraft.util.StringRepresentable;

public enum FurnacePart implements StringRepresentable {
    FRONT_LEFT("front_left"),         FRONT_MID("front_mid"),         FRONT_RIGHT("front_right"),
    MID_LEFT("mid_left"),             CENTER("center"),               MID_RIGHT("mid_right"),
    MID_CENTER_LEFT("mid_center_left"),                               MID_CENTER_RIGHT("mid_center_right"), // Наши новые центры боковушек
    BACK_LEFT("back_left"),           BACK_MID("back_mid"),           BACK_RIGHT("back_right");

    private final String name;

    FurnacePart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}