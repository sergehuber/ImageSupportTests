package org.jahia.application;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.Resizers;
import org.apache.commons.io.FilenameUtils;
import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: 03.05.12
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class ImageTests {


    public static void main(String[] args) throws IOException, InterruptedException, IM4JavaException {

        int imageWidth = 160;
        int imageHeight = 160;
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

        // first result is discarded, as VM needs time to heat up.
        System.out.println("Warming up Java VM with "+nbWarmupLoops+" warmup loops...");
        for (int i=0; i < nbWarmupLoops; i++) {
            generateWithThumbnailatorDefault(args[0], imageWidth, imageHeight);
            generateWithThumbnailatorHQ(args[0], imageWidth, imageHeight);
            generateWithJava2DBicubic(args[0], imageWidth, imageHeight);
        }

        System.out.println("Testing and benchmarking image generation ("+nbLoops+" loops each, resizing to " + imageWidth + "x" + imageHeight + ")...");

        long accumTime = 0;
        for (int i=0; i < nbLoops; i++) {
            accumTime += generateWithThumbnailatorDefault(args[0], imageWidth, imageHeight);
        }
        double averageTime = accumTime / ((double)nbLoops);
        System.out.println("Accumulated time for Thumbnailator default quality=" + accumTime + "ms, average=" + averageTime + "ms");

        accumTime = 0;
        for (int i=0; i < nbLoops; i++) {
            accumTime += generateWithThumbnailatorHQ(args[0], imageWidth, imageHeight);
        }
        averageTime = accumTime / ((double)nbLoops);
        System.out.println("Accumulated time for Thumbnailator high quality   =" + accumTime + "ms, average=" + averageTime + "ms");

        accumTime = 0;
        for (int i=0; i < nbLoops; i++) {
            accumTime += generateWithJava2DLinear(args[0], imageWidth, imageHeight);
        }
        averageTime = accumTime / ((double)nbLoops);
        System.out.println("Accumulated time for Java2D linear interpolation  =" + accumTime + "ms, average=" + averageTime + "ms");

        accumTime = 0;
        for (int i=0; i < nbLoops; i++) {
            accumTime += generateWithJava2DBicubic(args[0], imageWidth, imageHeight);
        }
        averageTime = accumTime / ((double)nbLoops);
        System.out.println("Accumulated time for Java2D bicubic interpolation =" + accumTime + "ms, average=" + averageTime + "ms");

        accumTime = 0;
        for (int i=0; i < nbLoops; i++) {
            accumTime += generateWithIm4Java(args[0], imageWidth, imageHeight);
            if (accumTime < 0) {
                break;
            }
        }
        if (accumTime > 0) {
            averageTime = accumTime / ((double)nbLoops);
            System.out.println("Accumulated time for Im4Java                      =" + accumTime + "ms, average=" + averageTime + "ms");
        }
    }

    public static long generateWithJava2DLinear(String originalFile, int newWidth, int newHeight) throws IOException {

        long startTime = System.currentTimeMillis();
        String destFile = getDestFileName(originalFile, "Java2DLinear", newWidth, newHeight);

        // Read image to scale
        BufferedImage originalImage = ImageIO.read(new File(originalFile));

        BufferedImage dest = new BufferedImage(newWidth, newHeight,
        BufferedImage.TYPE_INT_ARGB);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        // Save destination image
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersBySuffix(FilenameUtils.getExtension(originalFile));
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(new File(destFile));
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return totalTime;
    }

    public static long generateWithJava2DBicubic(String originalFile, int newWidth, int newHeight) throws IOException {

        long startTime = System.currentTimeMillis();
        String destFile = getDestFileName(originalFile, "Java2DBicubic", newWidth, newHeight);

        // Read image to scale
        BufferedImage originalImage = ImageIO.read(new File(originalFile));
        BufferedImage dest = new BufferedImage(newWidth, newHeight,
        BufferedImage.TYPE_INT_ARGB);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        // Save destination image
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersBySuffix(FilenameUtils.getExtension(originalFile));
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(new File(destFile));
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return totalTime;
    }

    private static String getDestFileName(String originalFile, String classifier, int newWidth, int newHeight) {
        String originalFileBaseName = FilenameUtils.getBaseName(originalFile);
        return originalFileBaseName + "." + classifier + "." + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + "." + FilenameUtils.getExtension(originalFile);
    }

    public static long generateWithThumbnailatorHQ(String originalFile, int newWidth, int newHeight) throws IOException {
        long startTime = System.currentTimeMillis();
        String destFile = getDestFileName(originalFile, "ThumbnailatorHQ", newWidth, newHeight);
        Thumbnails.of(new File(originalFile))
                .outputQuality(1.0f)
                .resizer(Resizers.BICUBIC)
                .size(newWidth, newHeight)
                .keepAspectRatio(false)
                .toFile(new File(destFile));
        long totalTime = System.currentTimeMillis() - startTime;
        return totalTime;
    }

    public static long generateWithThumbnailatorDefault(String originalFile, int newWidth, int newHeight) throws IOException {
        long startTime = System.currentTimeMillis();
        String destFile = getDestFileName(originalFile, "Thumbnailator", newWidth, newHeight);
        Thumbnails.of(new File(originalFile))
                .size(newWidth, newHeight)
                .keepAspectRatio(false)
                .toFile(new File(destFile));
        long totalTime = System.currentTimeMillis() - startTime;
        return totalTime;
    }

    public static long generateWithIm4Java(String originalFile, int newWidth, int newHeight) throws IM4JavaException, InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        String destFile = getDestFileName(originalFile, "Im4Java", newWidth, newHeight);

        ProcessStarter.setGlobalSearchPath("/usr/bin:/usr/local/bin:/opt/local/bin");

        // create command
        ConvertCmd cmd = new ConvertCmd();

        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(originalFile);

        if (false) {
            op.resize(newWidth,newWidth,"^");
            op.gravity("center");
            op.crop(newWidth,newWidth,0,0);
        } else {
            op.resize(newWidth,newHeight);
        }

        op.addImage(destFile);

        try {
            cmd.run(op);
            long totalTime = System.currentTimeMillis() - startTime;
            return totalTime;
        } catch (CommandException ce) {
            if (ce.getCause() instanceof FileNotFoundException) {
                System.err.println("Seems ImageMagick is not available ("+ce.getLocalizedMessage()+").");
            } else {
                ce.printStackTrace();
            }
        }
        return -1;
    }
}
