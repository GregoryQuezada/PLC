package edu.ufl.cise.plc.runtime;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageOpsComparison {

    public static boolean equals(BufferedImage image0, BufferedImage image1) {
        int[] pixels0 = image0.getRGB(0,0,image0.getWidth(), image0.getHeight(), null,0,image0.getWidth());
        int[] pixels1 = image1.getRGB(0,0,image1.getWidth(), image1.getHeight(), null,0,image1.getWidth());
        return Arrays.equals(pixels0, pixels1);
    }

}