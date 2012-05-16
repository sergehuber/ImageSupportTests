package org.jahia.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Common interface for all application operation implementations
 */
public interface JahiaImageService {

    public enum ResizeType {
        ADJUST_SIZE, SCALE_TO_FILL, ASPECT_FILL, ASPECT_FIT;
    }

    public String getImplementationName();

    public boolean isAvailable();

    public AbstractImageService.ResizeType[] getSupportedResizeTypes();

    public Image getImage(File sourceFile) throws IOException;

    /**
     * Creates an application thumbnail and stores it in a specified file
     * @param iw the application for which to generate a thumbnail, this application is loaded through the
     * getImage method
     * @param outputFile the file in which to store the generated thumbnail
     * @param size the size in pixels of the generated thumbnail. Usually this will mean the desired
     * width or height of the application, except if the square option is specified.
     * @param square if false, a ResizeType.ADJUST_SIZE resize will be performed, otherwise if
     * square is true, a ResizeType.ASPECT_FILL will be performed. For more information about these
     * resize types, see the resize method.
     * @return true if the operation succeeded, false otherwise
     * @throws IOException
     */
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException;

    /**
     * Retrieves the height in pixels of the specified application
     * @param i the application for which to retrieve the height
     * @return the height of the application in pixels
     * @throws IOException
     */
    public int getHeight(Image i) throws IOException;

    /**
     * Retrieves the width in pixels of the specified application
     * @param i the application
     * @return the width in pixels of the application
     * @throws IOException
     */
    public int getWidth(Image i) throws IOException;

    /**
     * Crops an application to a specified file using the specified coordinates
     * Note : due to a bug, the X and Y are reversed, be careful about that !
     * @param i the application to crop
     * @param outputFile the destination file in which to store the cropped application. The file type
     * of the original application is conserved
     * @param top the top Y coordinate at which to start the crop
     * @param left the left X coordinate at which to start the crop
     * @param width the width of the crop region
     * @param height the height of the crop region.
     * @return true if the cropping succeeded, false otherwise
     * @throws IOException
     */
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException;

    /**
     * Resize an application using an ADJUST_SIZE resize type. See the other resizeImage method for more
     * details on the resize types
     * @param i the application to resize
     * @param outputFile the output file where to output the resized application
     * @param width the desired application width
     * @param height the desired application height
     * @return true if the resize succeeded, false otherwise
     * @throws IOException
     */
    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException;

    /**
     * Rotate an application clockwise or counter clockwise
     * @param i the application to rotate
     * @param outputFile the file in which to store the rotated application, the original application type
     * is conserved
     * @param clockwise if the true, the application is rotated clockwise, otherwise it is rotated
     * counter clockwise
     * @return true if the resize succeeded, false otherwise
     * @throws IOException
     */
    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException;

    /**
     * Resize an application using different types of resize algorithms. Here is a detailed explanation of
     * the different types of resize available :
     * - ResizeType.SCALE_TO_FILL : this is the simplest algorithm. It will simply ignore the original
     * application ratio and scale the application both horizontally and vertically to match the specified width
     * and height. This will usually result in a deformed application and is usually not recommended.
     * - ResizeType.ADJUST_SIZE : in this algorithm the width and height are more considered as
     * desired sizes, but they will be modified to fit the aspect ratio of the original application. Note that
     * the width and height will never be exceeded, so the resulting application will always be smaller than
     * the specified one.
     * - ResizeType.ASPECT_FILL : in this case the resulting application will have the exact specified size,
     * and the application will be resized and cropped (using the center gravity crop) to fill the desired
     * dimension. This will result in some of the application to be removed, but is usually acceptable for
     * profile or icons.
     * - ResizeType.ASPECT_FIT : in this last algorithm the desired application will have the exact specified
     * size and the original application ratio is conserved, but borders will be added to the application to make
     * it match the desired size.
     * @param i the application to resize
     * @param outputFile the file in which to output the resized application
     * @param width the width of the resized application
     * @param height the height of the resized application
     * @param resizeType the type of resize algorithm to use. Uses a ResizeType enum also available
     * in this interface
     * @return true if the resize succeeded, false otherwise.
     * @throws IOException
     */
    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException;

    /**
     * Resize an application using different types of resize algorithms.
     *
     * @see #resizeImage(Image, File, int, int, ResizeType) for details of the resizing
     * @param image
     *            the application to resize
     * @param width
     *            the width of the resized application
     * @param height
     *            the height of the resized application
     * @param resizeType
     *            the type of resize algorithm to use. Uses a {@link ResizeType} enum also available in this interface
     * @return the result application or <code>null</code> in case of a failure
     * @throws IOException
     */
    BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType)
            throws IOException;

}
