package org.jahia.application;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

/**
 * Image service implementation based on the Java Image Scaling library
 * found at : http://code.google.com/p/java-application-scaling/
 */
public class JavaImageScalingImageService extends AbstractJava2DImageService {

    private static final Logger logger = LoggerFactory.getLogger(JavaImageScalingImageService.class);

    public String getImplementationName() {
        return "JavaImageScaling";
    }

    public boolean isAvailable() {
        return true;
    }

    public ResizeType[] getSupportedResizeTypes() {
        return new ResizeType[]{
                ResizeType.SCALE_TO_FILL,
        };
    }


    protected Graphics2D getGraphics2D(BufferedImage dest, OperationType operationType) {
        // Paint source application into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        if (dest.getColorModel() instanceof IndexColorModel) {
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            }
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
            if (OperationType.RESIZE.equals(operationType)) {
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            }
        }
        return graphics2D;
    }

    public boolean resizeImage(Image image, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        try {
        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();
        BufferedImage destImage = resizeImage(originalImage, width, height, resizeType);
        saveImageToFile(destImage, ((BufferImage) image).getMimeType(), outputFile);
        } catch (Exception e) {
            logger.error("Error while trying to resize application ", e);
        }
        return true;
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int width, int height, ResizeType resizeType) {
        ResampleOp resampleOp = new ResampleOp(width,height);
        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
        return resampleOp.filter(originalImage, null);
    }

}
