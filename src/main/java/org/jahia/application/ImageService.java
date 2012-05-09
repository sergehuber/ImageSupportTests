package org.jahia.application;

import java.io.File;
import java.io.IOException;

/**
 * Common interface for all image operation implementations
 */
public interface ImageService {

    public enum ResizeType {
        ADJUST_SIZE, SCALE_TO_FILL, ASPECT_FILL, ASPECT_FIT;
    }

    public String getImplementationName();

    public boolean isAvailable();

    public AbstractImageService.ResizeType[] getSupportedResizeTypes();

    public Image getImage(File sourceFile) throws IOException;

    public boolean resize(Image image, File outputFile, int width, int height, ResizeType resizeType) throws IOException;

    public boolean crop(Image image, File outputFile, int left, int top, int width, int height) throws IOException;

    public boolean rotate(Image image, File outputFile, boolean clockwise) throws IOException;

}
