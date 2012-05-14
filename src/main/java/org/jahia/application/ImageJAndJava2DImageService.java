package org.jahia.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A combination of ImageJ and Java2D implementations, to expand the supported formats of Java2D.
 */
public class ImageJAndJava2DImageService extends Java2DProgressiveBilinearImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageJAndJava2DImageService.class);

    private ImageJImageService imageJImageOperation = new ImageJImageService();

    public String getImplementationName() {
        return "ImageJAndJava2D";
    }

    @Override
    public Image getImage(File sourceFile) throws IOException {
        if (super.canRead(sourceFile)) {
            return super.getImage(sourceFile);
        } else {
            return imageJImageOperation.getImage(sourceFile);
        }
    }

    public int getHeight(Image i) {
        if (i instanceof BufferImage) {
            return super.getHeight(i);
        } else {
            return imageJImageOperation.getHeight(i);
        }
    }

    public int getWidth(Image i) {
        if (i instanceof BufferImage) {
            return super.getWidth(i);
        } else {
            return imageJImageOperation.getWidth(i);
        }
    }

    @Override
    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        if (image instanceof BufferImage) {
            return super.resizeImage(image, outputFile, newWidth, newHeight, resizeType);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageOperation.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        }
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) {
        return super.resizeImage(image, width, height, resizeType);
    }

    @Override
    public boolean cropImage(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
        if (image instanceof BufferImage) {
            return super.cropImage(image, outputFile, left, top, width, height);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageOperation.cropImage(image, outputFile, left, top, width, height);
        }
    }

    @Override
    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        if (image instanceof BufferImage) {
            return super.rotateImage(image, outputFile, clockwise);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageOperation.rotateImage(image, outputFile, clockwise);
        }
    }

}
