package org.jahia.application;

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;

/**
 * A combination of ImageJ and Java2D implementations, to expand the supported formats of Java2D.
 */
public class ImageJAndJava2DImageOperation extends Java2DBicubicImageOperation {

    public String getImplementationName() {
        return "ImageJAndJava2D";
    }

    @Override
    protected boolean canRead(String sourceFile) {
        return true;
    }

    @Override
    public boolean resize(String originalFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        if (super.canRead(originalFile)) {
            return super.resize(originalFile, newWidth, newHeight, resizeType);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            System.out.println("Using ImageJ code for file " + originalFile + "...");
            String destFile = getDestFileName(originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + resizeType);
            ImagePlus ip = null;
            Opener op = new Opener();
            int fileType = op.getFileType(originalFile);
            ip = op.openImage(originalFile);

            ImageProcessor processor = ip.getProcessor();

            int originalWidth = ip.getWidth();
            int originalHeight = ip.getHeight();
            ResizeCoords resizeCoords = getResizeCoords(resizeType, originalWidth, originalHeight, newWidth, newHeight);

            if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
                processor.setInterpolationMethod(ImageProcessor.BICUBIC);
                processor = processor.resize(newWidth, newHeight, true);
            } else if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
                newWidth = resizeCoords.getTargetWidth();
                newHeight = resizeCoords.getTargetHeight();
                processor.setInterpolationMethod(ImageProcessor.BICUBIC);
                processor = processor.resize(newWidth, newHeight, true);
            } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
                processor.setRoi(resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(), resizeCoords.getSourceWidth(), resizeCoords.getSourceHeight());
                processor = processor.crop();
                processor.setInterpolationMethod(ImageProcessor.BICUBIC);
                processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
            } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
                processor.setInterpolationMethod(ImageProcessor.BICUBIC);
                processor = processor.resize(resizeCoords.getTargetWidth(), resizeCoords.getTargetHeight(), true);
                ImageProcessor newProcessor = processor.createProcessor(newWidth, newHeight);
                newProcessor.copyBits(processor, resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(), Blitter.ADD);
                processor = newProcessor;
            }
            ip.setProcessor(null, processor);

            return save(fileType, ip, new File(destFile));
        }
    }

    @Override
    public boolean crop(String originalFile, int left, int top, int width, int height) throws IOException {
        if (super.canRead(originalFile)) {
            return super.crop(originalFile, left, top, width, height);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            System.out.println("Using ImageJ code for file " + originalFile + "...");
            String destFile = getDestFileName(originalFile, "cropTo" + Integer.toString(width) + "x" + Integer.toString(height));
            ImagePlus ip = null;
            Opener op = new Opener();
            int fileType = op.getFileType(originalFile);
            ip = op.openImage(originalFile);
            ImageProcessor processor = ip.getProcessor();

            processor.setRoi(left, top, width, height);
            processor = processor.crop();
            ip.setProcessor(null, processor);

            return save(fileType, ip, new File(destFile));

        }
    }

    @Override
    public boolean rotate(String originalFile, boolean clockwise) throws IOException {
        if (super.canRead(originalFile)) {
            return super.rotate(originalFile, clockwise);    //To change body of overridden methods use File | Settings | File Templates.
        } else {
            System.out.println("Using ImageJ code for file " + originalFile + "...");
            String direction = clockwise ? "Clockwise" : "Counterclockwise";

            String destFile = getDestFileName(originalFile, "rotate" + direction);

            ImagePlus ip = null;
            Opener op = new Opener();
            int fileType = op.getFileType(originalFile);
            ip = op.openImage(originalFile);
            ImageProcessor processor = ip.getProcessor();
            if (clockwise) {
                processor = processor.rotateRight();
            } else {
                processor = processor.rotateLeft();
            }
            ip.setProcessor(null, processor);

            return save(fileType, ip, new File(destFile));

        }
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
                PlugIn p =null;
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
