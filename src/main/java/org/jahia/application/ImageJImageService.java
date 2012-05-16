package org.jahia.application;

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ImageJ application operation implementation
 */
public class ImageJImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageJImageService.class);

    public String getImplementationName() {
        return "ImageJ";
    }

    public boolean isAvailable() {
        return true;
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[]{
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
                ResizeType.ASPECT_FILL,
                ResizeType.ASPECT_FIT
        };
    }

    public Image getImage(File sourceFile) throws IOException {
        ImagePlus ip = null;
        Opener op = new Opener();
        int fileType = op.getFileType(sourceFile.getPath());
        ip = op.openImage(sourceFile.getPath());
        if (ip == null) {
            logger.error("Couldn't open file " + sourceFile + " with ImageJ !");
            return null;
        }
        return new ImageJImage(sourceFile.getPath(), ip, fileType);
    }

    public int getHeight(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getHeight();
        }
        return -1;
    }

    public int getWidth(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getWidth();
        }
        return -1;
    }

    public boolean cropImage(Image image, File outputFile, int left, int top, int width, int height) throws IOException {

        ImageJImage imageJImage = (ImageJImage) image;
        ImagePlus ip = imageJImage.getImagePlus();

        ImageProcessor processor = ip.getProcessor();

        processor.setRoi(left, top, width, height);
        processor = processor.crop();
        ip.setProcessor(null, processor);

        return save(imageJImage.getImageType(), ip, outputFile);

    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {

        ImageJImage imageJImage = (ImageJImage) image;
        ImagePlus ip = imageJImage.getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        if (clockwise) {
            processor = processor.rotateRight();
        } else {
            processor = processor.rotateLeft();
        }
        ip.setProcessor(null, processor);

        return save(imageJImage.getImageType(), ip, outputFile);

    }

    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {

        ImageJImage imageJImage = (ImageJImage) i;

        ImagePlus ip = imageJImage.getImagePlus();

        resizeImage(ip, width, height, resizeType);

        return save(imageJImage.getImageType(), ip, outputFile);
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height,
            ResizeType resizeType) throws IOException {
        ImagePlus ip = new ImagePlus(null, image);

        resizeImage(ip, width, height, resizeType);

        return ip.getBufferedImage();
    }

    protected void resizeImage(ImagePlus ip, int width, int height, ResizeType resizeType) throws IOException {
        ImageProcessor processor = ip.getProcessor();

        int originalWidth = ip.getWidth();
        int originalHeight = ip.getHeight();
        ResizeCoords resizeCoords = getResizeCoords(resizeType, originalWidth, originalHeight, width, height);

        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(width, height, true);
        } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            height = resizeCoords.getTargetHeight();
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(width, height, true);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            processor.setRoi(resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(), resizeCoords.getSourceWidth(), resizeCoords.getSourceHeight());
            processor = processor.crop();
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            processor.setInterpolationMethod(ImageProcessor.BICUBIC);
            processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
            ImageProcessor newProcessor = processor.createProcessor(width, height);
            newProcessor.copyBits(processor, resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(), Blitter.ADD);
            processor = newProcessor;
        }
        ip.setProcessor(null, processor);
    }

    public static boolean save(int type, ImagePlus ip, File outputFile) {
        switch (type) {
            case Opener.TIFF:
                return new FileSaver(ip).saveAsTiff(outputFile.getPath());
            case Opener.GIF:
                return new FileSaver(ip).saveAsGif(outputFile.getPath());
            case Opener.JPEG:
                return new FileSaver(ip).saveAsJpeg(outputFile.getPath());
            case Opener.TEXT:
                return new FileSaver(ip).saveAsText(outputFile.getPath());
            case Opener.LUT:
                return new FileSaver(ip).saveAsLut(outputFile.getPath());
            case Opener.ZIP:
                return new FileSaver(ip).saveAsZip(outputFile.getPath());
            case Opener.BMP:
                return new FileSaver(ip).saveAsBmp(outputFile.getPath());
            case Opener.PNG:
                ImagePlus tempImage = WindowManager.getTempCurrentImage();
                WindowManager.setTempCurrentImage(ip);
                PlugIn p = null;
                try {
                    p = (PlugIn) Class.forName("ij.plugin.PNG_Writer").newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p.run(outputFile.getPath());
                WindowManager.setTempCurrentImage(tempImage);
                return true;
            case Opener.PGM:
                return new FileSaver(ip).saveAsPgm(outputFile.getPath());
        }
        return false;
    }

}
