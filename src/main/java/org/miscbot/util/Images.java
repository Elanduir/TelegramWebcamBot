package org.miscbot.util;

public class Images {
    ImageUrls imageUrls;
    Sizes sizes;
    ImageUrls daylight;

    public ImageUrls getCurrent() {
        return imageUrls;
    }

    public void setCurrent(ImageUrls imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Sizes getSizes() {
        return sizes;
    }

    public void setSizes(Sizes sizes) {
        this.sizes = sizes;
    }

    public ImageUrls getDaylight() {
        return daylight;
    }

    public void setDaylight(ImageUrls daylight) {
        this.daylight = daylight;
    }

    public static class ImageUrls {
        String icon;
        String thumbnail;
        String preview;

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }
    }

    public static class Sizes {
        Size icon;
        Size thumbnail;
        Size preview;

        public Size getIcon() {
            return icon;
        }

        public void setIcon(Size icon) {
            this.icon = icon;
        }

        public Size getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(Size thumbnail) {
            this.thumbnail = thumbnail;
        }

        public Size getPreview() {
            return preview;
        }

        public void setPreview(Size preview) {
            this.preview = preview;
        }

        public static class Size {
            int width;
            int height;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }
        }
    }
}
