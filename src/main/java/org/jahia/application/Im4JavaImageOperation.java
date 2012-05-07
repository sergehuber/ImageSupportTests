package org.jahia.application;

import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

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
        return new ResizeType[] {
                ResizeType.ADJUST_SIZE,
                ResizeType.SCALE_TO_FILL,
                ResizeType.ASPECT_FILL,
                ResizeType.ASPECT_FIT
        };
    }

    public boolean resize(String originalFile, int newWidth, int newHeight, AbstractImageOperation.ResizeType resizeType) throws IOException {
        String destFile = getDestFileName(originalFile, "resizeTo" + Integer.toString(newWidth) + "x" + Integer.toString(newHeight) + resizeType);

        ProcessStarter.setGlobalSearchPath("/usr/bin:/usr/local/bin:/opt/local/bin");

        // create command
        ConvertCmd cmd = new ConvertCmd();

        // create the operation, add images and operators/options
        IMOperation op = new IMOperation();
        op.addImage(originalFile);

        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            op.resize(newWidth,newHeight);
        } else if (ResizeType.ASPECT_FILL.equals(resizeType)) {
            op.resize(newWidth,newHeight,"^");
            op.gravity("center");
            op.crop(newWidth,newHeight,0,0);
        } else if (ResizeType.ASPECT_FIT.equals(resizeType)) {
            op.resize(newWidth,newHeight);
            op.gravity("center");
            op.background("none");
            op.extent(newWidth,newHeight);
        } else {
            op.resize(newWidth,newHeight,"!");
        }

        op.addImage(destFile);

        try {
            cmd.run(op);
            return true;
        } catch (CommandException ce) {
            if (ce.getCause() instanceof FileNotFoundException) {
                System.err.println("Seems ImageMagick is not available ("+ce.getLocalizedMessage()+").");
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

    public boolean crop(String originalFile, int left, int top, int width, int height) throws IOException {
        String destFile = getDestFileName(originalFile, "cropTo" + Integer.toString(width) + "x" + Integer.toString(height));
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(originalFile);
            op.background("none");
            op.crop(width, height, left, top);
            op.addImage(destFile);

            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean rotate(String originalFile, boolean clockwise) throws IOException {
        String direction = clockwise ? "Clockwise" : "Counterclockwise";

        String destFile = getDestFileName(originalFile, "rotate" + direction);
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(originalFile);
            op.rotate(clockwise ? 90. : -90.);
            op.addImage(destFile);

            cmd.run(op);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
