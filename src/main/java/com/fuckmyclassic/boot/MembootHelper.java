package com.fuckmyclassic.boot;

import com.fuckmyclassic.fel.FelConstants;
import com.fuckmyclassic.fel.FelDevice;

import javax.usb.UsbException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Class to manage booting a console from memory by uploading
 * specific kernel images to boot.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class MembootHelper {

    /**
     * Memboots the given kernel image on a connected NES/SNES Mini in FEL mode.
     * @param bootImagePath The Path that points to the boot image file.
     * @return Whether or not the image was booted successfully.
     * @throws UsbException
     */
    public boolean membootKernelImage(final Path bootImagePath) throws UsbException {
        System.out.println(String.format("Membooting kernel image located at %s", bootImagePath.toString()));

        final FelDevice device = FelDevice.getFirstConnectedConsole();
        if (device == null) {
            System.out.println("No NES/SNES Minis in FEL mode detected");
            return false;
        }

        try {
            device.open();

            System.out.println("Loading fes1 and uboot...");
            device.loadFes1AndUboot();
            System.out.println("Done loading fes1 and uboot.");

            System.out.println("Initializing DRAM...");
            device.initializeDRAM();
            System.out.println("Done initializing DRAM.");

            byte[] kernelData = Files.readAllBytes(bootImagePath);
            int size = KernelHelper.calculateKernelSize(kernelData);
            System.out.println(String.format("Calculated kernel size: %d", size));

            if (size > kernelData.length ||
                    size > FelConstants.TRANSFER_MAX_SIZE) {
                System.out.println("[ERROR] Invalid kernel size for memboot kernel image");
                return false;
            }

            size = (size + FelConstants.SECTOR_SIZE - 1) / FelConstants.SECTOR_SIZE;
            size *= FelConstants.SECTOR_SIZE;
            System.out.println(String.format("Padded kernel size: %d", size));

            if (kernelData.length != size) {
                kernelData = Arrays.copyOf(kernelData, size);
            }

            System.out.println("Uploading kernel image to FEL device...");
            device.writeDeviceMemory(FelConstants.TRANSFER_BASE_M, kernelData);
            System.out.println("Done uploading kernel image.");

            System.out.println("Executing the boot command...");
            final String bootCommand = String.format("boota %08X", FelConstants.TRANSFER_BASE_M);
            device.runUbootCmd(bootCommand, true);
            System.out.println("Done membooting the kernel image.");

            return true;
        } catch (Exception e) {
            System.out.println(String.format("[ERROR] Exception occurred during memboot: %s%n%s",
                    e.getMessage(), e.getStackTrace()));
            return false;
        } finally {
            if (device != null) {
                device.close();
            }
        }
    }
}
