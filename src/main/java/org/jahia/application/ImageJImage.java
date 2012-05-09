package org.jahia.application;

import ij.ImagePlus;

import java.awt.image.BufferedImage;

/**
 * ImageJ and Java2D image
 */
public class ImageJImage implements Image {
    private String path;

    private ImagePlus ip;
    private int imageType;
    private BufferedImage originalImage;
    private String mimeType;
    private boolean java2DUsed = false;

    public ImageJImage(String path, ImagePlus ip, int imageType, BufferedImage originalImage, String mimeType, boolean java2DUsed) {
        this.path = path;
        this.imageType = imageType;
        this.ip = ip;
        this.originalImage = originalImage;
        this.mimeType = mimeType;
        this.java2DUsed = java2DUsed;
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

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isJava2DUsed() {
        return java2DUsed;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (ip != null) {
            ip.close();
        }
    }

}
