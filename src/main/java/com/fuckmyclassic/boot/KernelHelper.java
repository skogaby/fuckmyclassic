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

        int kernel_size = ((kernel[11] << 24) + (kernel[10] << 16) + (kernel[9] << 8) + kernel[8]);
        int ramdisk_size = ((kernel[19] << 24) + (kernel[18] << 16) + (kernel[17] << 8) + kernel[16]);
        int second_size = ((kernel[27] << 24) + (kernel[26] << 16) + (kernel[25] << 8) + kernel[24]);
        int page_size = ((kernel[39] << 24) + (kernel[38] << 16) + (kernel[37] << 8) + kernel[36]);
        int dt_size = ((kernel[43] << 24) + (kernel[42] << 16) + (kernel[41] << 8) + kernel[40]);
        int pages = 1;

        pages += (kernel_size + page_size - 1) / page_size;
        pages += (ramdisk_size + page_size - 1) /  page_size;
        pages += (second_size + page_size - 1) /  page_size;
        pages += (dt_size + page_size - 1) /  page_size;

        return pages * page_size;
    }
}
