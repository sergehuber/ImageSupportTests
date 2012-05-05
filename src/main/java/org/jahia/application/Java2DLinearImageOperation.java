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
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 05.05.12
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class Java2DLinearImageOperation extends AbstractJava2DImageOperation {

    public String getImplementationName() {
        return "Java2DLinear";
    }

    public boolean resize(String originalFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {

        // Read image to scale
        BufferedImage originalImage = ImageIO.read(new File(originalFile));

        ResizeCoords resizeCoords = getResizeCoords(resizeType, originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            newWidth = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }
        String destFile = getDestFileName(originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + resizeType);

        BufferedImage dest = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D graphics2D = getGraphics2D(dest);

        graphics2D.drawImage(originalImage,
                resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);
        graphics2D.dispose();

        // Save destination image
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersBySuffix(FilenameUtils.getExtension(originalFile));
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(new File(destFile));
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
        }

        return true;
    }
}
