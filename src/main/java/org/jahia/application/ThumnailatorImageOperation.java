package org.jahia.application;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 05.05.12
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class ThumnailatorImageOperation extends AbstractImageOperation {
    public String getImplementationName() {
        return "Thumbnailator";
    }

    public boolean isAvailable() {
        return true;
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
