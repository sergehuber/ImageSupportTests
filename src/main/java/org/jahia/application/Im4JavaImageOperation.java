package org.jahia.application;

import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Im4Java image operations implementation
 */
public class Im4JavaImageOperation extends AbstractImageOperation {

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
            System.out.println(getImplementationName() + " is not available on this system.");
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

    public Image getImage(File sourceFile) {
        return new ImageMagickImage(sourceFile, sourceFile.getPath());
    }

    public boolean resize(Image image, File outputFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {

        ProcessStarter.setGlobalSearchPath("/usr/bin:/usr/local/bin:/opt/local/bin");

        // create command
        ConvertCmd cmd = new ConvertCmd();

        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(image.getPath());

        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            op.resize(newWidth, newHeight);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            op.resize(newWidth, newHeight, "^");
            op.gravity("center");
            op.crop(newWidth, newHeight, 0, 0);
            op.p_repage();
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            op.resize(newWidth, newHeight);
            op.gravity("center");
            op.background("none");
            op.extent(newWidth, newHeight);
        } else {
            op.resize(newWidth, newHeight, "!");
        }

        op.addImage(outputFile.getPath());

        try {
            // System.out.println("Running ImageMagic command: convert " + op);
            cmd.run(op);
            return true;
        } catch (CommandException ce) {
            if (ce.getCause() instanceof FileNotFoundException) {
                System.err.println("Seems ImageMagick is not available (" + ce.getLocalizedMessage() + ").");
            } else {
                ce.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IM4JavaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public boolean crop(Image image, File outputFile, int left, int top, int width, int height) throws IOException {
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

            // System.out.println("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean rotate(Image image, File outputFile, boolean clockwise) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(image.getPath());
            op.rotate(clockwise ? 90. : -90.);
            op.addImage(outputFile.getPath());

            // System.out.println("Running ImageMagic command: convert " + op);
            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
