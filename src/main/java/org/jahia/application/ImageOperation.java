package org.jahia.application;

import java.io.IOException;

/**
 * Common interface for all image operation implementations
 */
public interface ImageOperation {

    public enum ResizeType {
        ADJUST_SIZE, SCALE_TO_FILL, ASPECT_FILL, ASPECT_FIT;
    }

    public String getImplementationName();

    public boolean isAvailable();

    public AbstractImageOperation.ResizeType[] getSupportedResizeTypes();

    public boolean resize(String sourceFile, int width, int height, ResizeType resizeType) throws IOException;

    public boolean crop(String sourceFile, int left, int top, int width, int height) throws IOException;

    public boolean rotate(String sourceFile, boolean clockwise) throws IOException;

}
