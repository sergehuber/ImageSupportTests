package org.jahia.application;

import java.io.IOException;

/**
 * Common interface for all image operation implementations
 */
public interface ImageOperation {

    public String getImplementationName();

    public boolean isAvailable();

    public AbstractImageOperation.ResizeType[] getSupportedResizeTypes();

    public boolean resize(String sourceFile, int width, int height, AbstractImageOperation.ResizeType resizeType) throws IOException;

}
