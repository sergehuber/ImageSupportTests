package org.jahia.application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

/**
 * Progressive Bilinear implementation of the Java 2D image operation.
 * <p/>
 * This algorithm comes from http://code.google.com/p/thumbnailator/, itself used from the
 * example code from the resizing technique
 * discussed in <em>Chapter 4: Images</em> of
 * <a href="http://filthyrichclients.org">Filthy Rich Clients</a>
 * by Chet Haase and Romain Guy.
 */
public class Java2DProgressiveBilinearImageService extends Java2DBicubicImageService {
    @Override
    public String getImplementationName() {
        return "Java2DProgressiveBilinear";    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected Graphics2D getGraphics2D(BufferedImage dest) {
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        if (dest.getColorModel() instanceof IndexColorModel) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            IndexColorModel indexColorModel = (IndexColorModel) dest.getColorModel();
            int transparentPixelIndex = indexColorModel.getTransparentPixel();
            if (transparentPixelIndex > -1) {
                int transparentRGB = indexColorModel.getRGB(transparentPixelIndex);
                Color transparentColor = new Color(transparentRGB, true);
                graphics2D.setBackground(transparentColor);
                graphics2D.setColor(transparentColor);
                graphics2D.setPaint(transparentColor);
                graphics2D.fillRect(0, 0, dest.getWidth(), dest.getHeight());
            }
        } else {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
        }
        return graphics2D;
    }

    @Override
    public boolean resize(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {

        BufferedImage originalImage = ((ImageJImage) image).getOriginalImage();

        ResizeCoords resizeCoords = getResizeCoords(resizeType, originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            newWidth = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }

        int currentWidth = resizeCoords.getSourceWidth();
        int currentHeight = resizeCoords.getSourceHeight();
        int targetWidth = resizeCoords.getTargetWidth();
        int targetHeight = resizeCoords.getTargetHeight();

        BufferedImage dest = getDestImage(newWidth, newHeight, originalImage);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest);

        // If multi-step downscaling is not required, perform one-step.
        if ((newWidth * 2 >= resizeCoords.getSourceWidth()) && (newHeight * 2 >= resizeCoords.getSourceHeight())) {
            graphics2D.drawImage(originalImage,
                    resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                    resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                    resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                    resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                    null);
            graphics2D.dispose();
            // Save destination image
            saveImageToFile(dest, outputFile);
            return true;
        }

        // Temporary image used for in-place resizing of image.
        BufferedImage tempImage = new BufferedImage(
                currentWidth,
                currentHeight,
                dest.getType()
        );

        Graphics2D g = getGraphics2D(tempImage);
        g.setComposite(AlphaComposite.Src);

        /*
         * Determine the size of the first resize step should be.
         * 1) Beginning from the target size
         * 2) Increase each dimension by 2
         * 3) Until reaching the original size
         */
        int startWidth = resizeCoords.getTargetWidth();
        int startHeight = resizeCoords.getTargetHeight();

        while (startWidth < currentWidth && startHeight < currentHeight) {
            startWidth *= 2;
            startHeight *= 2;
        }

        currentWidth = startWidth / 2;
        currentHeight = startHeight / 2;

        // Perform first resize step.
        g.drawImage(originalImage, 0, 0, currentWidth, currentHeight,
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);

        // Perform an in-place progressive bilinear resize.
        while ((currentWidth >= targetWidth * 2) && (currentHeight >= targetHeight * 2)) {
            currentWidth /= 2;
            currentHeight /= 2;

            if (currentWidth < targetWidth) {
                currentWidth = targetWidth;
            }
            if (currentHeight < targetHeight) {
                currentHeight = targetHeight;
            }

            g.drawImage(
                    tempImage,
                    0, 0, currentWidth, currentHeight,
                    0, 0, currentWidth * 2, currentHeight * 2,
                    null
            );
        }

        g.dispose();

        // Draw the resized image onto the destination image.
        graphics2D.drawImage(tempImage, resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(), targetWidth, targetHeight, 0, 0, currentWidth, currentHeight, null);
        graphics2D.dispose();
        // Save destination image
        saveImageToFile(dest, outputFile);
        return true;
    }
}
