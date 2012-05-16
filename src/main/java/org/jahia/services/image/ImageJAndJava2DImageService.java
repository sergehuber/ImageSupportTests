package org.jahia.services.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * An image service implementation that combines the Java2D and ImageJ service implementations
 * to expand the supported file formats (such as TIFF)
 */
public class ImageJAndJava2DImageService extends Java2DProgressiveBilinearImageService {

    private ImageJImageService imageJImageService = new ImageJImageService();
    private static ImageJAndJava2DImageService instance = new ImageJAndJava2DImageService();

    private static final Logger logger = LoggerFactory.getLogger(ImageJAndJava2DImageService.class);

    protected ImageJAndJava2DImageService() {
    }

    public void init() {
    }

    public static ImageJAndJava2DImageService getInstance() {
        return instance;
    }

    public String getImplementationName() {
        return "ImageJAndJava2D";
    }

    @Override
    public Image getImage(File sourceFile) throws IOException {
        if (super.canRead(sourceFile)) {
            return super.getImage(sourceFile);
        } else {
            return imageJImageService.getImage(sourceFile);
        }
    }

    @Override
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        if (iw instanceof BufferImage) {
            return super.createThumb(iw, outputFile, size, square);
        } else {
            return imageJImageService.createThumb(iw, outputFile, size, square);
        }
    }

    @Override
    public int getHeight(Image i) {
        if (i instanceof BufferImage) {
            return super.getHeight(i);
        } else {
            return imageJImageService.getHeight(i);
        }
    }

    @Override
    public int getWidth(Image i) {
        if (i instanceof BufferImage) {
            return super.getWidth(i);
        } else {
            return imageJImageService.getWidth(i);
        }
    }

    @Override
    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        if (image instanceof BufferImage) {
            return super.cropImage(image, outputFile, top, left, width, height);
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageService.cropImage(image, outputFile, top, left, width, height);
        }
    }

    @Override
    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        if (image instanceof BufferImage) {
            return super.rotateImage(image, outputFile, clockwise);
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageService.rotateImage(image, outputFile, clockwise);
        }
    }

    @Override
    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        if (image instanceof BufferImage) {
            return super.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        } else {
            logger.info("Using ImageJ code for file " + image.getPath() + "...");
            return imageJImageService.resizeImage(image, outputFile, newWidth, newHeight, resizeType);
        }
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) {
        return super.resizeImage(image, width, height, resizeType);
    }
}
