package com.fuckmyclassic.boot;

import java.nio.charset.StandardCharsets;

/**
 * Class to help with miscellaneous tasks related to kernel operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class KernelHelper {

    /**
     * Calculates the kernel size so we know if it's too big to transfer over FEL.
     * @param kernel The kernel boot image.
     * @return The size calculated.
     */
    public static int calculateKernelSize(final byte[] kernel) throws RuntimeException {
        if (!(new String(kernel, 0, 8, StandardCharsets.US_ASCII).equals("ANDROID!"))) {
            throw new RuntimeException("Invalid kernel header");
        }

        int kernel_size = Integer.reverseBytes((kernel[8] << 24) + (kernel[9] << 16) + (kernel[10] << 8) + kernel[11]);
        int ramdisk_size = Integer.reverseBytes((kernel[16] << 24) + (kernel[17] << 16) + (kernel[18] << 8) + kernel[19]);
        int second_size = Integer.reverseBytes((kernel[24] << 24) + (kernel[25] << 16) + (kernel[26] << 8) + kernel[27]);
        int page_size = Integer.reverseBytes((kernel[36] << 24) + (kernel[37] << 16) + (kernel[38] << 8) + kernel[39]);
        int dt_size = Integer.reverseBytes((kernel[40] << 24) + (kernel[41] << 16) + (kernel[42] << 8) + kernel[43]);
        int pages = 1;

        pages += (kernel_size + page_size - 1) / page_size;
        pages += (ramdisk_size + page_size - 1) /  page_size;
        pages += (second_size + page_size - 1) /  page_size;
        pages += (dt_size + page_size - 1) /  page_size;

        return pages * page_size;
    }
}
