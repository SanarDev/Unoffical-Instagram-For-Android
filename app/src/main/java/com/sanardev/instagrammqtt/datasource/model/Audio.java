package com.sanardev.instagrammqtt.datasource.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Audio {


    @SerializedName("audio_src")
    @Expose
    private String audioSrc;
    @SerializedName("duration")
    @Expose
    private Long duration;
    @SerializedName("waveform_data")
    @Expose
    private List<Double> waveformData = null;
    @SerializedName("waveform_sampling_frequency_hz")
    @Expose
    private Long waveformSamplingFrequencyHz;

    public String getAudioSrc() {
        return audioSrc;
    }

    public void setAudioSrc(String audioSrc) {
        this.audioSrc = audioSrc;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public List<Double> getWaveformData() {
        return waveformData;
    }

    public void setWaveformData(List<Double> waveformData) {
        this.waveformData = waveformData;
    }

    public Long getWaveformSamplingFrequencyHz() {
        return waveformSamplingFrequencyHz;
    }

    public void setWaveformSamplingFrequencyHz(Long waveformSamplingFrequencyHz) {
        this.waveformSamplingFrequencyHz = waveformSamplingFrequencyHz;
    }

}
