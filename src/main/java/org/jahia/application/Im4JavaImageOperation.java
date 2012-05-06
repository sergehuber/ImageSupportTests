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
                ResizeType.SCALE_TO_FILL
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

        if (false) {
            op.resize(newWidth,newWidth,"^");
            op.gravity("center");
            op.crop(newWidth,newWidth,0,0);
        } else {
            if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
                op.resize(newWidth,newHeight);
            } else {
                op.resize(newWidth,newHeight,"!");
            }
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
}
