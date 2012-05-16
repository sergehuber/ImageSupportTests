package org.jahia.application;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Java2D linear interpolation application operation implementation
 */
public class Java2DLinearImageService extends AbstractJava2DImageService {

    public String getImplementationName() {
        return "Java2DLinear";
    }

    protected Graphics2D getGraphics2D(BufferedImage dest, OperationType operationType) {
        // Paint source application into the destination, scaling as needed
        Graphics2D graphics2D = dest.createGraphics();
        if (OperationType.RESIZE.equals(operationType)) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        return graphics2D;
    }

}
