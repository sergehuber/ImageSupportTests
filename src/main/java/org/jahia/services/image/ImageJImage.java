package org.jahia.services.image;

import ij.ImagePlus;

/**
 * ImageJ and Java2D application
 */
public class ImageJImage implements Image {
    private String path;

    private ImagePlus ip;
    private int imageType;

    public ImageJImage(String path, ImagePlus ip, int imageType) {
        this.path = path;
        this.imageType = imageType;
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public ImagePlus getImagePlus() {
        return ip;
    }

    public int getImageType() {
        return imageType;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (ip != null) {
            ip.close();
        }
    }

}
