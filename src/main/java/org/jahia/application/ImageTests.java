package org.jahia.application;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main image test class
 */
public class ImageTests {

    public final static ImageOperation[] imageOperationImpls = {
        new Java2DLinearImageOperation(),
        new Java2DBicubicImageOperation(),
        new ThumnailatorImageOperation(),
        new ThumbnailatorHQImageOperation(),
        new Im4JavaImageOperation(),
        new ImageJAndJava2DImageOperation()
    };

    public static final AbstractImageOperation.ResizeType[] allResizeTypes = {
            AbstractImageOperation.ResizeType.SCALE_TO_FILL,
            AbstractImageOperation.ResizeType.ADJUST_SIZE,
            AbstractImageOperation.ResizeType.ASPECT_FILL,
            AbstractImageOperation.ResizeType.ASPECT_FIT
    };

    public List<ImageOperation> availableImageOperations = new ArrayList<ImageOperation>();

    public ImageTests() {
        for (ImageOperation imageOperation : imageOperationImpls) {
            if (imageOperation.isAvailable()) {
                availableImageOperations.add(imageOperation);
            }
        }
    }

    public void warmup(String originalFile, int newWidth, int newHeight, int nbWarmupLoops) throws IOException {
        // first result is discarded, as VM needs time to heat up.
        System.out.println("Warming up Java VM with "+nbWarmupLoops+" warmup loops...");
        for (int i=0; i < nbWarmupLoops; i++) {
            for (ImageOperation imageOperation : availableImageOperations) {
                imageOperation.resize(originalFile, newWidth, newHeight, AbstractImageOperation.ResizeType.SCALE_TO_FILL);
            }
        }
    }

    public void runResize(String originalFile, int imageWidth, int imageHeight, int nbLoops, AbstractImageOperation.ResizeType resizeType) throws IOException {
        System.out.println("Testing and benchmarking image resizing for "+originalFile+" ("+nbLoops+" loops each, resizing to " + imageWidth + "x" + imageHeight + " with resize type = "+resizeType+")...");

        for (ImageOperation imageOperation : availableImageOperations) {
            List<AbstractImageOperation.ResizeType> supportedResizeTypes = Arrays.asList(imageOperation.getSupportedResizeTypes());
            if (!supportedResizeTypes.contains(resizeType)) {
                continue;
            }
            long accumTime = 0;
            for (int i=0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                imageOperation.resize(originalFile, imageWidth, imageHeight, resizeType);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double)nbLoops);
            System.out.println("Accumulated time for "+imageOperation.getImplementationName()+"=" + accumTime + "ms, average=" + averageTime + "ms");
        }
    }

    public void runCrop(String originalFile, int left, int top, int width, int height, int nbLoops) throws IOException {
        System.out.println("Testing and benchmarking image cropping for "+originalFile+" ("+nbLoops+" loops each, cropping from "+left+","+top+" to size " + width + "x" + height +")...");

        for (ImageOperation imageOperation : availableImageOperations) {
            long accumTime = 0;
            for (int i=0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                imageOperation.crop(originalFile, left, top, width, height);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double)nbLoops);
            System.out.println("Accumulated time for "+imageOperation.getImplementationName()+"=" + accumTime + "ms, average=" + averageTime + "ms");
        }
    }

    public void runRotate(String originalFile, int nbLoops) throws IOException {
        System.out.println("Testing and benchmarking image rotating for "+originalFile+" ("+nbLoops+" loops each, rotating counter clockwise)...");

        for (ImageOperation imageOperation : availableImageOperations) {
            long accumTime = 0;
            for (int i=0; i < nbLoops; i++) {
                long startTime = System.currentTimeMillis();
                imageOperation.rotate(originalFile, false);
                imageOperation.rotate(originalFile, true);
                long operationTotalTime = System.currentTimeMillis() - startTime;
                accumTime += operationTotalTime;
            }
            double averageTime = accumTime / ((double)nbLoops);
            System.out.println("Accumulated time for "+imageOperation.getImplementationName()+"=" + accumTime + "ms, average=" + averageTime + "ms");
        }
    }

    public static void main(String[] args) throws IOException {

        int imageWidth = 144;
        int imageHeight = 144;
        int nbLoops = 100;
        int nbWarmupLoops = 100;

        if (args == null || args.length == 0) {
            System.out.println("Syntax : ImageSupportTests FILE_OR_DIRECTORY [NB_LOOPS] [NB_WARMUP_LOOPS] [WIDTH] [HEIGHT]");
            Runtime.getRuntime().exit(1);
            return;
        }

        if (args.length > 1) {
            nbLoops = Integer.parseInt(args[1]);
        }

        if (args.length > 2) {
            nbWarmupLoops = Integer.parseInt(args[2]);
        }

        if (args.length == 4) {
            imageWidth = Integer.parseInt(args[3]);
            imageHeight = Integer.parseInt(args[4]);
        }

        String[] imageReaderMIMETypes = ImageIO.getReaderMIMETypes();
        StringBuilder readMimeTypes = new StringBuilder();
        for (int i=0 ; i < imageReaderMIMETypes.length; i++) {
            readMimeTypes.append(imageReaderMIMETypes[i]);
            if (i < imageReaderMIMETypes.length-1) {
                readMimeTypes.append(",");
            }
        }
        String[] imageWriterMIMETypes = ImageIO.getWriterMIMETypes();
        StringBuilder writeMimeTypes = new StringBuilder();
        for (int i=0 ; i < imageWriterMIMETypes.length; i++) {
            writeMimeTypes.append(imageWriterMIMETypes[i]);
            if (i < imageWriterMIMETypes.length-1) {
                writeMimeTypes.append(",");
            }
        }
        System.out.println("VM supported read image types = " + readMimeTypes);
        System.out.println("VM supported write image types = " + writeMimeTypes);

        ImageTests imageTests = new ImageTests();
        File sourceFile = new File(args[0]);

        if (sourceFile.isFile()) {
            imageTests.warmup(args[0], imageWidth, imageHeight, nbWarmupLoops);
            for (AbstractImageOperation.ResizeType resizeType : allResizeTypes) {
                imageTests.runResize(args[0], imageWidth, imageHeight, nbLoops, resizeType);
            }
            imageTests.runCrop(args[0], 10, 10, 100, 100, nbLoops);
            imageTests.runRotate(args[0], nbLoops);
        } else {
            File[] directoryFiles = sourceFile.listFiles();
            for (File directoryFile : directoryFiles) {
                if (directoryFile.getName().startsWith(".") || directoryFile.isDirectory()) {
                    continue;
                }
                for (AbstractImageOperation.ResizeType resizeType : allResizeTypes) {
                    imageTests.runResize(directoryFile.getPath(), imageWidth, imageHeight, 1, resizeType);
                }
                imageTests.runCrop(directoryFile.getPath(), 10, 10, 100, 100, 1);
                imageTests.runRotate(directoryFile.getPath(), 1);
            }
        }
    }

}
