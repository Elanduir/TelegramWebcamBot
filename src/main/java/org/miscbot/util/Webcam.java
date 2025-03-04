package org.miscbot.util;

import java.io.File;

public class Webcam {
    private String title;
    private int viewCount;
    private int webcamId;
    private String status;
    private String lastUpdatedOn;
    private Images images;
    private File currentImage;
    private Location originalLocation;
    private Urls urls;

    public String getWebcamUrlDetail() {
        return getUrls().getDetail();
    }

    public String getWebcamUrlProvider() {
        return getUrls().getProvider();
    }

    public Urls getUrls() {
        return urls;
    }

    public void setUrls(Urls urls) {
        this.urls = urls;
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }

    public void setOriginalLocation(Location originalLocation) {
        this.originalLocation = originalLocation;
    }

    public String getCurrentPreview() {
        return getImages().getCurrent().getPreview();
    }

    public String getDaylightPreview() {
        return getImages().getDaylight().getPreview();
    }

    public File getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(File currentImage) {
        this.currentImage = currentImage;
    }

    public boolean cleanup() {
        if(this.currentImage != null) {
            return this.currentImage.delete();
        }
        return true;
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

    public static class Urls {
        private String detail;
        private String edit;
        private String provider;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getEdit() {
            return edit;
        }

        public void setEdit(String edit) {
            this.edit = edit;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }

}