package org.jahia.application;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

/**
 * Thumbnailator default image operation implementation
 */
public class ThumnailatorImageOperation extends AbstractImageOperation {
    public String getImplementationName() {
        return "Thumbnailator";
    }

    public boolean isAvailable() {
        return true;
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[] {
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
        } ;
    }

    public boolean resize(String originalFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {
        String destFile = getDestFileName(originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + resizeType);
        boolean conserveAspectRatio = true;
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            conserveAspectRatio = false;
        }
        Thumbnails.of(new File(originalFile))
                .size(newWidth, newHeight)
                .keepAspectRatio(conserveAspectRatio)
                .toFile(new File(destFile));
        return true;
    }
}
