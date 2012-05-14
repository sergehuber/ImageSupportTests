package org.jahia.application;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.resizers.Resizers;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Thumbnailator default image operation implementation
 */
public class ThumbnailatorImageService extends AbstractJahiaImageService {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailatorImageService.class);

    private String name = "Thumbnailator";

    private Float outputQuality;

    private Resizers resizer;

    public ThumbnailatorImageService() {
        super();
    }

    public ThumbnailatorImageService(String name, Float outputQuality, Resizers resizer) {
        this();
        this.name = name;
        this.outputQuality = outputQuality;
        this.resizer = resizer;
    }

    public String getImplementationName() {
        return name;
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
        if (!canRead(sourceFile) || !canWrite(sourceFile)) {
            return null;
        }
        return new ImageMagickImage(sourceFile, sourceFile.getPath());
    }

    public int getHeight(Image i) throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getWidth(Image i) throws IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected boolean canRead(File sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersBySuffix(sourceFileExtension.toLowerCase());
        if (imageReaderIterator.hasNext()) {
            return true;
        } else {
            logger.error("Image reading for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return false;
        }
    }

    protected boolean canWrite(File sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
        Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersBySuffix(sourceFileExtension.toLowerCase());
        if (imageWriterIterator.hasNext()) {
            return true;
        } else {
            logger.error("Image writing for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return false;
        }
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, AbstractJahiaImageService.ResizeType resizeType) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;
        boolean conserveAspectRatio = true;
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            conserveAspectRatio = false;
        }
        try {
            Builder<File> builder = Thumbnails.of(imageMagickImage.getFile());
            if (outputQuality != null) {
                builder.outputQuality(outputQuality);
            }
            if (resizer != null) {
                builder.resizer(resizer);
            }
            if (ResizeType.ASPECT_FILL.equals(resizeType)) {
                Thumbnails.of(imageMagickImage.getFile())
                        .crop(Positions.CENTER)
                        .size(newWidth, newHeight)
                        .toFile(outputFile);
            } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
                Thumbnails.of(imageMagickImage.getFile())
                        .size(newWidth, newHeight)
                        .addFilter(new Canvas(newWidth, newHeight, Positions.CENTER))
                        .toFile(outputFile);
            } else {
                builder
                        .size(newWidth, newHeight)
                        .keepAspectRatio(conserveAspectRatio)
                        .toFile(outputFile);
            }
        } catch (IOException ioe) {
            logger.error("Error trying to resize file " + imageMagickImage.getFile() + ": " + ioe.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight, ResizeType resizeType) throws IOException {
        boolean conserveAspectRatio = true;
        BufferedImage resultImage = null;
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            conserveAspectRatio = false;
        }
        try {
            Builder<BufferedImage> builder = Thumbnails.of(image);
            if (outputQuality != null) {
                builder.outputQuality(outputQuality);
            }
            if (resizer != null) {
                builder.resizer(resizer);
            }
            if (ResizeType.ASPECT_FILL.equals(resizeType)) {
                resultImage = builder
                        .crop(Positions.CENTER)
                        .size(newWidth, newHeight)
                        .asBufferedImage();
            } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
                resultImage = builder
                        .size(newWidth, newHeight)
                        .addFilter(new Canvas(newWidth, newHeight, Positions.CENTER))
                        .asBufferedImage();
            } else {
                resultImage = builder
                        .size(newWidth, newHeight)
                        .keepAspectRatio(conserveAspectRatio)
                        .asBufferedImage();
            }
        } catch (IOException ioe) {
            logger.error("Error trying to resize image: " + ioe.getLocalizedMessage());
            return null;
        }
        return resultImage;
    }

    public boolean cropImage(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;

        try {
            Thumbnails.of(imageMagickImage.getFile())
                    .outputQuality(1.0f)
                    .sourceRegion(left, top, width, height)
                    .scale(1.0)
                    .toFile(outputFile);
        } catch (IOException ioe) {
            logger.error("Error trying to crop file " + imageMagickImage.getFile() + ": " + ioe.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;

        try {
            Thumbnails.of(imageMagickImage.getFile())
                    .outputQuality(1.0f)
                    .rotate(clockwise ? 90 : -90)
                    .scale(1.0)
                    .toFile(outputFile);
        } catch (Exception e) {
            logger.error("Error while generating image for " + imageMagickImage.getFile() + ": " + e.getLocalizedMessage() + "(" + this.getClass().getName() + ")");
            return false;
        }
        return true;
    }
}
