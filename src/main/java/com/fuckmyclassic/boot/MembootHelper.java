package com.fuckmyclassic.boot;

import com.fuckmyclassic.fel.FelConstants;
import com.fuckmyclassic.fel.FelDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Class to manage booting a console from memory by uploading
 * specific kernel images to boot.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MembootHelper {

    static Logger LOG = LogManager.getLogger(MembootHelper.class.getName());

    /**
     * Memboots the given kernel image on a connected NES/SNES Mini in FEL mode.
     * @param bootImagePath The Path that points to the boot image file.
     * @return Whether or not the image was booted successfully.
     * @throws UsbException
     */
    public boolean membootKernelImage(final Path bootImagePath) throws UsbException {
        LOG.debug(String.format("Membooting kernel image located at %s", bootImagePath.toString()));

        final FelDevice device = FelDevice.getFirstConnectedConsole();
        if (device == null) {
            LOG.warn("No NES/SNES Minis in FEL mode detected");
            return false;
        }

        try {
            device.open();

            LOG.debug("Loading fes1 and uboot...");
            device.loadFes1AndUboot();
            LOG.debug("Done loading fes1 and uboot.");

            LOG.debug("Initializing DRAM...");
            device.initializeDRAM();
            LOG.debug("Done initializing DRAM.");

            byte[] kernelData = Files.readAllBytes(bootImagePath);
            int size = KernelHelper.calculateKernelSize(kernelData);
            LOG.trace(String.format("Calculated kernel size: %d", size));

            if (size > kernelData.length ||
                    size > FelConstants.TRANSFER_MAX_SIZE) {
                LOG.error("Invalid kernel size for memboot kernel image");
                return false;
            }

            size = (size + FelConstants.SECTOR_SIZE - 1) / FelConstants.SECTOR_SIZE;
            size *= FelConstants.SECTOR_SIZE;
            LOG.trace(String.format("Padded kernel size: %d", size));

            if (kernelData.length != size) {
                kernelData = Arrays.copyOf(kernelData, size);
            }

            LOG.debug("Uploading kernel image to FEL device...");
            device.writeDeviceMemory(FelConstants.TRANSFER_BASE_M, kernelData);
            LOG.debug("Done uploading kernel image.");

            LOG.debug("Executing the boot command...");
            final String bootCommand = String.format("boota %08X", FelConstants.TRANSFER_BASE_M);
            device.runUbootCmd(bootCommand, true);
            LOG.debug("Done membooting the kernel image.");

            return true;
        } catch (Exception e) {
            LOG.error(String.format("Exception occurred during memboot: %s%n%s",
                    e.getMessage(), e.getStackTrace()));
            return false;
        } finally {
            if (device != null) {
                device.close();
            }
        }
    }
}
