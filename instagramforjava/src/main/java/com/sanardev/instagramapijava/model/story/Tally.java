package com.sanardev.instagramapijava.model.story;

import com.google.gson.annotations.SerializedName;

public class Tally {

    @SerializedName("text")
    private String text;
    @SerializedName("font_size")
    private float fontSize;
    @SerializedName("count")
    private int count;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
