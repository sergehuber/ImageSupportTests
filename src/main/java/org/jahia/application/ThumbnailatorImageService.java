package org.jahia.application;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.resizers.Resizers;

import org.apache.commons.io.FilenameUtils;

/**
 * Thumbnailator default image operation implementation
 */
public class ThumbnailatorImageService extends AbstractImageService {
    
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
        };
    }

    public Image getImage(File sourceFile) throws IOException {
        if (!canRead(sourceFile) || !canWrite(sourceFile)) {
            return null;
        }
        return new ImageMagickImage(sourceFile, sourceFile.getPath());
    }

    protected boolean canRead(File sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersBySuffix(sourceFileExtension.toLowerCase());
        if (imageReaderIterator.hasNext()) {
            return true;
        } else {
            System.err.println("Image reading for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return false;
        }
    }

    protected boolean canWrite(File sourceFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getPath());
        Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersBySuffix(sourceFileExtension.toLowerCase());
        if (imageWriterIterator.hasNext()) {
            return true;
        } else {
            System.err.println("Image writing for file " + sourceFile + " is not supported by this implementation (" + this.getClass().getName() + ")");
            return false;
        }
    }

    public boolean resize(Image image, File outputFile, int newWidth, int newHeight, AbstractImageService.ResizeType resizeType) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;
        boolean conserveAspectRatio = true;
        if (ResizeType.SCALE_TO_FILL.equals(resizeType)) {
            conserveAspectRatio = false;
        }
        Builder<File> builder = Thumbnails.of(imageMagickImage.getFile());
        if (outputQuality != null) {
            builder.outputQuality(outputQuality);
        }
        if (resizer != null) {
            builder.resizer(resizer);
        }
        
        builder
                .size(newWidth, newHeight)
                .keepAspectRatio(conserveAspectRatio)
                .toFile(outputFile);
        return true;
    }

    public boolean crop(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;

        Thumbnails.of(imageMagickImage.getFile())
                .outputQuality(1.0f)
                .sourceRegion(left, top, width, height)
                .scale(1.0)
                .toFile(outputFile);
        return true;
    }

    public boolean rotate(Image image, File outputFile, boolean clockwise) throws IOException {
        ImageMagickImage imageMagickImage = (ImageMagickImage) image;

        try {
            Thumbnails.of(imageMagickImage.getFile())
                    .outputQuality(1.0f)
                    .rotate(clockwise ? 90 : -90)
                    .scale(1.0)
                    .toFile(outputFile);
        } catch (Exception e) {
            System.err.println("Error while generating image for " + imageMagickImage.getFile() + ": " + e.getLocalizedMessage() + "(" + this.getClass().getName() + ")");
            return false;
        }
        return true;
    }
}
