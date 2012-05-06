package org.jahia.application;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Java2D linear interpolation image operation implementation
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
        saveImageToFile(dest, destFile);

        return true;
    }
}
