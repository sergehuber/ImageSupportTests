package org.jahia.services.image;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Abstract Java2D common application operation implementations
 */
public abstract class AbstractJava2DImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJava2DImageService.class);

    public boolean isAvailable() {
        return true;
    }

    public enum OperationType {
        RESIZE, CROP, ROTATE
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[]{
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
                ResizeType.ASPECT_FILL,
                ResizeType.ASPECT_FIT
        };
    }

    private static Detector detector = new DefaultDetector();

    public Image getImage(File sourceFile) throws IOException {
        if (!canRead(sourceFile)) {
            logger.error("Image reading for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return null;
        }
        // Read application to scale
        try {
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, sourceFile.getName());
            String type = null;
            String mediaType = null;
            BufferedInputStream targetInputStream = null;
            try {
                targetInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                MediaType detectedType = detector.detect(targetInputStream, metadata);
                type = detectedType.getType();
                mediaType = detectedType.toString();
                logger.debug("Detected media type=" + mediaType);
            } finally {
                IOUtils.closeQuietly(targetInputStream);
            }

            BufferedImage originalImage = ImageIO.read(sourceFile);
            return new BufferImage(sourceFile.getPath(), originalImage, mediaType);
        } catch (IOException ioe) {
            logger.error("Image reading for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return null;
        }
    }

    public int getHeight(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getHeight();
    }

    public int getWidth(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getWidth();
    }

    public boolean cropImage(Image image, File outputFile, int left, int top, int width, int height) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        int clippingWidth = width;
        if (left + clippingWidth > originalImage.getWidth()) {
            clippingWidth = originalImage.getWidth() - left;
        }
        int clippingHeight = height;
        if (top + clippingHeight > originalImage.getHeight()) {
            clippingHeight = originalImage.getHeight() - top;
        }
        BufferedImage clipping = getDestImage(clippingWidth, clippingHeight, originalImage);
        Graphics2D area = getGraphics2D(clipping, OperationType.CROP);
        area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth,
                top + clippingHeight, null);
        area.dispose();

        // Save destination application
        saveImageToFile(clipping, ((BufferImage)image).getMimeType(), outputFile);

        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = getDestImage(originalImage.getHeight(), originalImage.getWidth(), originalImage);
        // Paint source application into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.ROTATE);

        double angle = Math.toRadians(clockwise ? 90 : -90);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        graphics2D.translate((neww - w) / 2, (newh - h) / 2);
        graphics2D.rotate(angle, w / 2, h / 2);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
        } else {
            graphics2D.drawImage(originalImage, 0, 0, null);
        }

        // Save destination application
        saveImageToFile(dest, ((BufferImage)image).getMimeType(), outputFile);
        return true;
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, AbstractImageService.ResizeType resizeType) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = resizeImage(originalImage, newWidth, newHeight, resizeType);

        // Save destination application
        saveImageToFile(dest, ((BufferImage)image).getMimeType(), outputFile);

        return true;
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int newHeight, ResizeType resizeType) {
        ResizeCoords resizeCoords = getResizeCoords(resizeType, image.getWidth(), image.getHeight(), width, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }

        BufferedImage dest = getDestImage(width, newHeight, image);

        // Paint source application into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.RESIZE);

        graphics2D.drawImage(image,
                resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);
        graphics2D.dispose();
        return dest;
    }

    protected abstract Graphics2D getGraphics2D(BufferedImage bufferedImage, OperationType operationType);

    protected boolean canRead(File sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersBySuffix(sourceFileExtension.toLowerCase());
        if (imageReaderIterator.hasNext()) {
            return true;
        } else {
            return false;
        }
    }

    protected BufferedImage getDestImage(int newWidth, int newHeight, BufferedImage originalImage) {
        BufferedImage dest;
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            // dest = new BufferedImage(newWidth, newHeight, originalImage.getType(), (IndexColorModel) originalImage.getColorModel());
            dest = new BufferedImage(originalImage.getColorModel(), originalImage.getColorModel().createCompatibleWritableRaster(newWidth, newHeight), false, new Hashtable<Object, Object>());
        } else {
            dest = new BufferedImage(newWidth, newHeight, originalImage.getType());
        }
        return dest;
    }

    protected void saveImageToFile(BufferedImage dest, String mimeType, File destFile) throws IOException {
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersByMIMEType(mimeType);
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(destFile);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
            imageOutputStream.close();
        } else {
            logger.warn("Couldn't find a writer for mime type : " + mimeType + "(" + this.getClass().getName() + ")");
        }
    }

}
