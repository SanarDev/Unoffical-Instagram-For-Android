package com.idirect.app.manager;

public class PlayProperties {

    public PlayProperties(boolean isPlay, String currentPlayId,String previousPlayId) {
        this.isPlay = isPlay;
        this.currentPlayId = currentPlayId;
        this.previousPlayId = previousPlayId;
    }

    private boolean isPlay;
    private String currentPlayId;
    private String previousPlayId;

    public String getPreviousPlayId() {
        return previousPlayId;
    }

    public void setPreviousPlayId(String previousPlayId) {
        this.previousPlayId = previousPlayId;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public String getCurrentPlayId() {
        return currentPlayId;
    }

    public void setCurrentPlayId(String currentPlayId) {
        this.currentPlayId = currentPlayId;
    }
}
