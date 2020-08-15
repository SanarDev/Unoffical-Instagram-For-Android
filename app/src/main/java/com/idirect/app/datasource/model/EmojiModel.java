package com.idirect.app.datasource.model;

public class EmojiModel {

    public EmojiModel(String emoji, String tag) {
        this.emoji = emoji;
        this.tag = tag;
    }

    private String emoji;
    private String tag;

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
