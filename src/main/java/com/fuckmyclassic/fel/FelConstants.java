package com.fuckmyclassic.fel;

/**
 * Holds constants we need during FEL operations.
 * Ported from Hakchi2.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelConstants {

    public static final int FES1_TEST_SIZE = 0x80;

    public static final int FES1_BASE_M = 0x2000;

    public static final int DRAM_BASE = 0x40000000;

    public static final int UBOOT_BASE_M = DRAM_BASE + 0x7000000;

    public static final int UBOOT_BASE_F = 0x100000;

    public static final int SECTOR_SIZE = 0x20000;

    public static final int UBOOT_MAX_SIZE_F = (SECTOR_SIZE * 0x10);

    public static final int KERNEL_BASE_F = (SECTOR_SIZE * 0x30);

    public static final int KERNEL_MAX_SIZE = (SECTOR_SIZE * 0x20);

    public static final int TRANSFER_BASE_M = (DRAM_BASE + 0x7400000);

    public static final int TRANSFER_MAX_SIZE = (SECTOR_SIZE * 0x100);

    public static final int MAX_BULK_SIZE = 0x10000;

    public static final String FASTBOOT = "efex_test";
}
