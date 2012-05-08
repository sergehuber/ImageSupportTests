package org.jahia.application;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.Resizers;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Thumbnailator high quality image operation implementation
 */
public class ThumbnailatorHQImageOperation extends AbstractImageOperation {

    public String getImplementationName() {
        return "ThumbnailatorHQ";
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

    protected boolean canRead(String sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile);
        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersBySuffix(sourceFileExtension.toLowerCase());
        if (imageReaderIterator.hasNext()) {
            return true;
        } else {
            System.err.println("Image format for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return false;
        }
    }

    public boolean resize(String originalFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {
        if (!canRead(originalFile)) {
            return false;
        }
        String destFile = getDestFileName(originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + resizeType);
        boolean conserveAspectRatio = true;
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            conserveAspectRatio = false;
        }
        Thumbnails.of(new File(originalFile))
                .outputQuality(1.0f)
                .resizer(Resizers.BICUBIC)
                .size(newWidth, newHeight)
                .keepAspectRatio(conserveAspectRatio)
                .toFile(new File(destFile));
        return true;
    }

    public boolean crop(String sourceFile, int left, int top, int width, int height) throws IOException {
        if (!canRead(sourceFile)) {
            return false;
        }
        String destFile = getDestFileName(sourceFile, "cropTo" + Integer.toString(width) + "x" + Integer.toString(height));

        Thumbnails.of(new File(sourceFile))
                .outputQuality(1.0f)
                .antialiasing(Antialiasing.ON)
                .resizer(Resizers.BICUBIC)
                .sourceRegion(left, top, width, height)
                .scale(1.0)
                .toFile(new File(destFile));
        return true;
    }

    public boolean rotate(String sourceFile, boolean clockwise) throws IOException {
        if (!canRead(sourceFile)) {
            return false;
        }
        String direction = clockwise ? "Clockwise" : "Counterclockwise";
        String destFile = getDestFileName(sourceFile, "rotate" + direction);

        try {
            Thumbnails.of(new File(sourceFile))
                    .outputQuality(1.0f)
                    .antialiasing(Antialiasing.ON)
                    .resizer(Resizers.PROGRESSIVE)
                    .rotate(clockwise ? 90 : -90)
                    .scale(1.0)
                    .toFile(new File(destFile));
        } catch (Exception e) {
            System.err.println("Error while generating image for " + sourceFile + ": " + e.getLocalizedMessage() + "(" + this.getClass().getName() + ")");
            return false;
        }
        return true;
    }
}
