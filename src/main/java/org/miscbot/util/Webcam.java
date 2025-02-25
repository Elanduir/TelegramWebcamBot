package org.miscbot.util;

import java.io.File;
import java.util.List;

public class Webcam {
    private String title;
    private int viewCount;
    private int webcamId;
    private String status;
    private String lastUpdatedOn;
    private Images images;
    private File currentImage;

    public Webcam(){}

    public File getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(File currentImage) {
        this.currentImage = currentImage;
    }

    public boolean cleanup() {
        return this.currentImage.delete();
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getWebcamId() {
        return webcamId;
    }

    public String getStatus() {
        return status;
    }

    public String getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWebcamId(int webcamId) {
        this.webcamId = webcamId;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title + " " + webcamId + " " + status;
    }

}