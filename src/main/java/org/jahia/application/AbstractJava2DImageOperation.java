package org.jahia.application;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Abstract Java2D common image operation implementations
 */
public abstract class AbstractJava2DImageOperation extends AbstractImageOperation {

    public boolean isAvailable() {
        return true;
    }

    protected Graphics2D getGraphics2D(BufferedImage dest) {
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        return graphics2D;
    }

    protected void saveImageToFile(BufferedImage dest, String destFile) throws IOException {
        String fileExtension = FilenameUtils.getExtension(destFile).toLowerCase();
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersBySuffix(fileExtension);
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(new File(destFile));
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
            imageOutputStream.close();
        } else {
            System.err.println("Couldn't find a writer for extension : " + fileExtension);
        }
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[] {
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
                ResizeType.ASPECT_FILL,
                ResizeType.ASPECT_FIT
        } ;
    }
}
