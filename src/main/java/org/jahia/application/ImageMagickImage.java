package org.jahia.application;

import java.io.File;

/**
 * ImageMagick image
 */
public class ImageMagickImage implements Image {

    private File file;
    private String path;

    public ImageMagickImage(File file, String path) {
        this.file = file;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

}
