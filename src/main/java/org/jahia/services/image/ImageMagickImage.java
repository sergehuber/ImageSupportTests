package org.jahia.services.image;

import java.io.File;

/**
 * ImageMagick application
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
