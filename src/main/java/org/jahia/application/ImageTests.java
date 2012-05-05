package org.jahia.application;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 03.05.12
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class ImageTests {

    public static ImageOperation[] imageOperationImpls = {
        new Java2DLinearImageOperation(),
        new Java2DBicubicImageOperation(),
        new ThumnailatorImageOperation(),
        new ThumbnailatorHQImageOperation(),
        new Im4JavaImageOperation()
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
        System.out.println("Testing and benchmarking image generation ("+nbLoops+" loops each, resizing to " + imageWidth + "x" + imageHeight + " with resize type = "+resizeType+")...");

        for (ImageOperation imageOperation : availableImageOperations) {
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

    public static void main(String[] args) throws IOException {

        int imageWidth = 144;
        int imageHeight = 144;
        int nbLoops = 100;
        int nbWarmupLoops = 100;

        if (args == null || args.length == 0) {
            System.out.println("Syntax : ImageSupportTests IMAGE_NAME [NB_LOOPS] [NB_WARMUP_LOOPS] [WIDTH] [HEIGHT]");
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
        imageTests.warmup(args[0], imageWidth, imageHeight, nbWarmupLoops);
        imageTests.runResize(args[0], imageWidth, imageHeight, nbLoops, AbstractImageOperation.ResizeType.SCALE_TO_FILL);
        imageTests.runResize(args[0], imageWidth, imageHeight, nbLoops, AbstractImageOperation.ResizeType.ADJUST_SIZE);
        imageTests.runResize(args[0], imageWidth, imageHeight, nbLoops, AbstractImageOperation.ResizeType.ASPECT_FILL);
        imageTests.runResize(args[0], imageWidth, imageHeight, nbLoops, AbstractImageOperation.ResizeType.ASPECT_FIT);
    }

}
