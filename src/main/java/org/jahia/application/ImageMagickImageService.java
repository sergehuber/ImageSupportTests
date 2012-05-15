package org.jahia.application;

import org.im4java.core.*;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Im4Java image operations implementation
 */
public class ImageMagickImageService extends AbstractJahiaImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMagickImageService.class);

    private static final Pattern GEOMETRY_PATTERN = Pattern.compile("[x+]");

    public String getImplementationName() {
        return "Im4Java";
    }

    public boolean isAvailable() {

        ProcessStarter.setGlobalSearchPath("/usr/bin:/usr/local/bin:/opt/local/bin");

        // create command
        ConvertCmd cmd = new ConvertCmd();

        try {
            String convertCmdPath = cmd.searchForCmd(cmd.getCommand().get(0), ProcessStarter.getGlobalSearchPath());
            return true;
        } catch (FileNotFoundException fnfe) {
            logger.info(getImplementationName() + " is not available on this system.");
        } catch (IOException e) {
        }

        return false;
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[]{
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
                ResizeType.ASPECT_FILL,
                ResizeType.ASPECT_FIT
        };
    }

    private File getFile(Image i) {
        return ((ImageMagickImage) i).getFile();
    }

    public Image getImage(File sourceFile) {
        return new ImageMagickImage(sourceFile, sourceFile.getPath());
    }

    public int getHeight(Image i) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) i;
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(GEOMETRY_PATTERN.split(imageInfo.getProperty("Geometry"))[1]);
        } catch (InfoException e) {
            logger.error("Error retrieving image " + imageMagickImage.getPath() + " height: " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error retrieving image " + imageMagickImage.getPath() + " height", e);
            }
            return -1;
        }
    }

    public int getWidth(Image i) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) i;
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(GEOMETRY_PATTERN.split(imageInfo.getProperty("Geometry"))[0]);
        } catch (InfoException e) {
            logger.error("Error retrieving image " + imageMagickImage.getPath() + " weight: " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error retrieving image " + imageMagickImage.getPath() + " weight", e);
            }
            return -1;
        }
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, AbstractJahiaImageService.ResizeType resizeType) throws IOException {

        ProcessStarter.setGlobalSearchPath("/usr/bin:/usr/local/bin:/opt/local/bin");

        // create command
        ConvertCmd cmd = new ConvertCmd();

        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(image.getPath());

        setupIMResize(op, newWidth, newHeight, resizeType);

        op.addImage(outputFile.getPath());

        try {
            // logger.info("Running ImageMagic command: convert " + op);
            cmd.run(op);
            return true;
        } catch (CommandException ce) {
            if (ce.getCause() instanceof FileNotFoundException) {
                logger.error("Seems ImageMagick is not available (" + ce.getLocalizedMessage() + ").");
            } else {
                logger.error("Error executing ImageMagick command", ce);
            }
        } catch (InterruptedException ie) {
            logger.error("Error executing ImageMagick command", ie);
        } catch (IM4JavaException ije) {
            logger.error("Error executing ImageMagick command", ije);
        }
        return false;
    }

    public boolean cropImage(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(image.getPath());
            op.background("none");
            op.crop(width, height, left, top);
            op.p_repage();
            op.addImage(outputFile.getPath());

            // logger.info("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(image.getPath());
            op.rotate(clockwise ? 90. : -90.);
            op.addImage(outputFile.getPath());

            // logger.info("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void setupIMResize(IMOperation op, int width, int height, ResizeType resizeType) {
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            op.resize(width,height);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            op.resize(width,height,"^");
            op.gravity("center");
            op.crop(width,height,0,0);
            op.p_repage();
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            op.resize(width,height);
            op.gravity("center");
            op.background("none");
            op.extent(width,height);
        } else {
            op.resize(width,height,"!");
        }
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int height,
                                     ResizeType resizeType) throws IOException {
        try {
        IMOperation op = new IMOperation();
        op.addImage();                        // input

        setupIMResize(op, width, height, resizeType);

        op.addImage("png:-");                 // output: stdout

        // set up command
        ConvertCmd convert = new ConvertCmd();
        Stream2BufferedImage s2b = new Stream2BufferedImage();
        convert.setOutputConsumer(s2b);

        // run command and extract BufferedImage from OutputConsumer
        convert.run(op, image);
        BufferedImage img = s2b.getImage();
            return img;
        } catch (Exception e) {
            logger.error("Error resizing image : " + e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error resizing image ", e);
            }
            return null;
        }
    }

}
