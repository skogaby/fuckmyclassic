package com.fuckmyclassic.boot;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class KernelHelper {

    /**
     * Calculates the kernel size so we know if it's too big to transfer over FEL.
     * @param kernel The kernel boot image.
     * @return The size calculated.
     */
    public static int calculateKernelSize(final byte[] kernel) throws RuntimeException, UnsupportedEncodingException {
        if (!(new String(kernel, 0, 8, StandardCharsets.US_ASCII).equals("ANDROID!"))) {
            throw new RuntimeException("Invalid kernel header");
        }

        int kernel_size = (int)(kernel[8] | (kernel[9] * 0x100) | (kernel[10] * 0x10000) | (kernel[11] * 0x1000000));
        int ramdisk_size = (int)(kernel[16] | (kernel[17] * 0x100) | (kernel[18] * 0x10000) | (kernel[19] * 0x1000000));
        int second_size = (int)(kernel[24] | (kernel[25] * 0x100) | (kernel[26] * 0x10000) | (kernel[27] * 0x1000000));
        int page_size = (int)(kernel[36] | (kernel[37] * 0x100) | (kernel[38] * 0x10000) | (kernel[39] * 0x1000000));
        int dt_size = (int)(kernel[40] | (kernel[41] * 0x100) | (kernel[42] * 0x10000) | (kernel[43] * 0x1000000));
        int pages = 1;

        pages += (kernel_size + page_size - 1) / page_size;
        pages += (ramdisk_size + page_size - 1) / page_size;
        pages += (second_size + page_size - 1) / page_size;
        pages += (dt_size + page_size - 1) / page_size;

        return pages * page_size;
    }
}
