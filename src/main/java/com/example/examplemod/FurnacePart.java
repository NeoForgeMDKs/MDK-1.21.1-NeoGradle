package com.example.examplemod;

import net.minecraft.util.StringRepresentable;

public enum FurnacePart implements StringRepresentable {
    FRONT_LEFT("front_left"),   FRONT_MID("front_mid"),   FRONT_RIGHT("front_right"),
    MID_LEFT("mid_left"),       CENTER("center"),         MID_RIGHT("mid_right"),
    BACK_LEFT("back_left"),     BACK_MID("back_mid"),     BACK_RIGHT("back_right");

    private final String name;

    FurnacePart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}