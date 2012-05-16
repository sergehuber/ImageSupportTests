package org.jahia.services.image;

import net.coobird.thumbnailator.resizers.Resizers;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main application test class
 */
public class ImageTests {

    private static final Logger logger = LoggerFactory.getLogger(ImageTests.class);

    public final static JahiaImageService[] IMAGE_SERVICE_IMPLs = {
            new Java2DLinearImageService(),
            new Java2DBicubicImageService(),
            new Java2DProgressiveBilinearImageService(),
            new ImageJImageService(),
            new ImageJAndJava2DImageService(),
            new ThumbnailatorImageService(),
            new ThumbnailatorImageService("ThumbnailatorHQ", 1.0f, null),
            new ThumbnailatorImageService("ThumbnailatorProgressiveBilinear", null, Resizers.PROGRESSIVE),
            new ImageMagickImageService(),
            new JavaImageScalingImageService()
    };

    public static final AbstractImageService.ResizeType[] allResizeTypes = {
            AbstractImageService.ResizeType.SCALE_TO_FILL,
            AbstractImageService.ResizeType.ADJUST_SIZE,
            AbstractImageService.ResizeType.ASPECT_FILL,
            AbstractImageService.ResizeType.ASPECT_FIT
    };

    public List<JahiaImageService> availableImageServices = new ArrayList<JahiaImageService>();

    private int longestServiceName = -1;

    public ImageTests() {
        for (JahiaImageService jahiaImageService : IMAGE_SERVICE_IMPLs) {
            if (jahiaImageService.isAvailable()) {
                availableImageServices.add(jahiaImageService);
                if (jahiaImageService.getImplementationName().length() > longestServiceName) {
                    longestServiceName = jahiaImageService.getImplementationName().length();
                }
            }
        }
    }

    public void warmup(File originalFile, int newWidth, int newHeight, int nbWarmupLoops) throws IOException {
        // first result is discarded, as VM needs time to heat up.
        logger.info("Warming up Java VM with " + nbWarmupLoops + " warmup loops...");
        for (int i = 0; i < nbWarmupLoops; i++) {
            for (JahiaImageService jahiaImageService : availableImageServices) {
                File destFile = getDestFile(jahiaImageService.getImplementationName(), originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + JahiaImageService.ResizeType.SCALE_TO_FILL);
                Image image = jahiaImageService.getImage(originalFile);
                if (image == null) {
                    continue;
                }
                jahiaImageService.resizeImage(image, destFile, newWidth, newHeight, JahiaImageService.ResizeType.SCALE_TO_FILL);
            }
        }
    }

    public void runResize(File originalFile, int imageWidth, int imageHeight, int nbLoops, AbstractImageService.ResizeType resizeType) throws IOException {
        logger.info("Testing and benchmarking application resizing for " + originalFile + " (" + nbLoops + " loops each, resizing to " + imageWidth + "x" + imageHeight + " with resize type = " + resizeType + ")...");

        for (JahiaImageService jahiaImageService : availableImageServices) {
            List<AbstractImageService.ResizeType> supportedResizeTypes = Arrays.asList(jahiaImageService.getSupportedResizeTypes());
            if (!supportedResizeTypes.contains(resizeType)) {
                continue;
            }
            long accumTime = 0;
            for (int i = 0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                Image image = jahiaImageService.getImage(originalFile);
                if (image == null) {
                    continue;
                }
                File destFile = getDestFile(jahiaImageService.getImplementationName(), originalFile, "resizeTo" + Integer.toString(imageWidth) + "x" + Integer.toString(imageHeight) + resizeType);
                jahiaImageService.resizeImage(image, destFile, imageWidth, imageHeight, resizeType);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double) nbLoops);
            logger.info("Total time for " + getPaddedImplementationName(jahiaImageService) + "=" +
                    getLeftPaddedString(Double.toString(accumTime), 7) +
                    "ms, average=" + getLeftPaddedString(Double.toString(averageTime),6) + "ms");
        }
    }

    public void runCrop(File originalFile, int left, int top, int width, int height, int nbLoops) throws IOException {
        logger.info("Testing and benchmarking application cropping for " + originalFile + " (" + nbLoops + " loops each, cropping from " + left + "," + top + " to size " + width + "x" + height + ")...");

        for (JahiaImageService jahiaImageService : availableImageServices) {
            long accumTime = 0;
            for (int i = 0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                Image image = jahiaImageService.getImage(originalFile);
                if (image == null) {
                    continue;
                }
                File destFile = getDestFile(jahiaImageService.getImplementationName(), originalFile, "cropTo" + Integer.toString(width) + "x" + Integer.toString(height));
                jahiaImageService.cropImage(image, destFile, left, top, width, height);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double) nbLoops);
            logger.info("Total time for " + getPaddedImplementationName(jahiaImageService) + "=" +
                    getLeftPaddedString(Double.toString(accumTime), 7) +
                    "ms, average=" + getLeftPaddedString(Double.toString(averageTime),6) + "ms");
        }
    }

    public void runRotate(File originalFile, int nbLoops) throws IOException {
        logger.info("Testing and benchmarking application rotating for " + originalFile + " (" + nbLoops + " loops each, rotating counter clockwise)...");

        for (JahiaImageService jahiaImageService : availableImageServices) {
            long accumTime = 0;
            for (int i = 0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                Image image = jahiaImageService.getImage(originalFile);
                if (image == null) {
                    continue;
                }
                File destFile = getDestFile(jahiaImageService.getImplementationName(), originalFile, "rotateCounterClockwise");
                jahiaImageService.rotateImage(image, destFile, false);
                destFile = getDestFile(jahiaImageService.getImplementationName(), originalFile, "rotateClockwise");
                jahiaImageService.rotateImage(image, destFile, true);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double) nbLoops);
            logger.info("Total time for " + getPaddedImplementationName(jahiaImageService) + "=" +
                    getLeftPaddedString(Double.toString(accumTime), 7) +
                    "ms, average=" + getLeftPaddedString(Double.toString(averageTime),6) + "ms");
        }
    }

    public static void main(String[] args) throws IOException {

        int imageWidth = 144;
        int imageHeight = 144;
        int nbLoops = 100;
        int nbWarmupLoops = 100;

        // Set up a simple configuration that logs on the console.
        DOMConfigurator.configure(ImageTests.class.getResource("/log4j.xml"));

        if (args == null || args.length == 0) {
            logger.info("Syntax : ImageSupportTests FILE_OR_DIRECTORY [NB_LOOPS] [NB_WARMUP_LOOPS] [WIDTH] [HEIGHT]");
            Runtime.getRuntime().exit(1);
            return;
        }

        if (args.length > 1) {
            nbLoops = Integer.parseInt(args[1]);
        }

        if (args.length > 2) {
            nbWarmupLoops = Integer.parseInt(args[2]);
        }

        if (args.length == 5) {
            imageWidth = Integer.parseInt(args[3]);
            imageHeight = Integer.parseInt(args[4]);
        }

        outputVMSupportedImageTypes();

        ImageTests imageTests = new ImageTests();
        File sourceFile = new File(args[0]);

        if (sourceFile.isFile() && sourceFile.canRead()) {
            imageTests.warmup(sourceFile, imageWidth, imageHeight, nbWarmupLoops);
            for (AbstractImageService.ResizeType resizeType : allResizeTypes) {
                imageTests.runResize(sourceFile, imageWidth, imageHeight, nbLoops, resizeType);
            }
            imageTests.runCrop(sourceFile, 10, 10, 100, 100, nbLoops);
            imageTests.runRotate(sourceFile, nbLoops);
        } else {
            if (!sourceFile.isDirectory()) {
                logger.error("Expected directory but found invalid file instead " + sourceFile);
                return;
            }
            File[] directoryFiles = sourceFile.listFiles();
            for (File directoryFile : directoryFiles) {
                if (directoryFile.getName().startsWith(".") || directoryFile.isDirectory()) {
                    continue;
                }
                if (directoryFile.isFile() && directoryFile.canRead()) {
                    for (AbstractImageService.ResizeType resizeType : allResizeTypes) {
                        imageTests.runResize(directoryFile, imageWidth, imageHeight, 1, resizeType);
                    }
                    imageTests.runCrop(directoryFile, 10, 10, 100, 100, 1);
                    imageTests.runRotate(directoryFile, 1);
                }
            }
        }
    }

    private static void outputVMSupportedImageTypes() {
        String[] imageReaderMIMETypes = ImageIO.getReaderMIMETypes();
        StringBuilder readMimeTypes = new StringBuilder();
        for (int i = 0; i < imageReaderMIMETypes.length; i++) {
            readMimeTypes.append(imageReaderMIMETypes[i]);
            if (i < imageReaderMIMETypes.length - 1) {
                readMimeTypes.append(",");
            }
        }
        String[] imageWriterMIMETypes = ImageIO.getWriterMIMETypes();
        StringBuilder writeMimeTypes = new StringBuilder();
        for (int i = 0; i < imageWriterMIMETypes.length; i++) {
            writeMimeTypes.append(imageWriterMIMETypes[i]);
            if (i < imageWriterMIMETypes.length - 1) {
                writeMimeTypes.append(",");
            }
        }
        logger.info("VM supported read application types = " + readMimeTypes);
        logger.info("VM supported write application types = " + writeMimeTypes);
    }

    private File getDestFile(String implementationName, File originalFile, String operationName) {
        String originalFileBaseName = FilenameUtils.getBaseName(originalFile.getPath());
        String originalFilePath = FilenameUtils.getPath(originalFile.getPath());
        String newPath = FilenameUtils.getPath(originalFile.getPath()) + "generatedImages";
        if (originalFilePath == null || "".equals(originalFilePath)) {
            newPath = "generatedImages";
        }
        new File(newPath).mkdirs();
        File destFile = new File(newPath + File.separator + originalFileBaseName + "." + implementationName + "." + operationName + "." + FilenameUtils.getExtension(originalFile.getPath()));
        // logger.info("Using destFile=" + destFile);
        return destFile;
    }

    private String getPaddedImplementationName(JahiaImageService imageService) {
        if (imageService.getImplementationName().length() > longestServiceName) {
            return imageService.getImplementationName();
        }
        StringBuilder result = new StringBuilder(imageService.getImplementationName());
        for (int i=0; i < longestServiceName - imageService.getImplementationName().length(); i++) {
            result.append(" ");
        }
        return result.toString();
    }

    private String getLeftPaddedString(String stringToPad, int totalLength) {
        if (stringToPad.length() >= totalLength) {
            return stringToPad;
        }
        StringBuilder result = new StringBuilder();
        for (int i=0; i < totalLength - stringToPad.length(); i++) {
            result.append(" ");
        }
        result.append(stringToPad);
        return result.toString();
    }
}
