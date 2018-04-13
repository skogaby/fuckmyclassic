package com.fuckmyclassic.ui.util;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Simple class to help resize images while maintain aspect ratios.
 * This is mainly used for importing box art for new games.
 */
@Component
public class ImageResizer {

    /**
     * Resizes the given image up to the given dimensions, while maintaining aspect ratio. Therefore,
     * only one dimension is guaranteed to match the given values, unless the given image is square.
     * @param image The image to resize
     * @param width The desired width of the output image
     * @param height The desired height of the output image
     * @return A resized image whose aspect ratio has been maintained and either whose width or height matches the given values
     */
    public BufferedImage resizeProportionally(final BufferedImage image, final int width, final int height) {
        // determine which direction the image is larger in so we know which dimension to
        // pass -1 to for the resizing
        final boolean wider = image.getWidth() > image.getHeight();
        final Image tmp = image.getScaledInstance(wider ? width : -1, wider ? -1 : height, Image.SCALE_SMOOTH);
        final BufferedImage resized = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    /**
     * Dump method to say whether a file is an image based on its file extension.
     * @param filename The name of the file to check
     * @return Whether or not it has an extension we consider a valid image
     */
    public static boolean isImageFile(final String filename) {
        return filename.endsWith(".png") ||
                filename.endsWith(".jpg") ||
                filename.endsWith(".bmp");
    }
}
