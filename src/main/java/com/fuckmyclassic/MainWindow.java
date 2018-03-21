package com.fuckmyclassic;

import com.fuckmyclassic.boot.KernelHelper;
import com.fuckmyclassic.fel.FelConstants;
import com.fuckmyclassic.fel.FelDevice;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.usb.UsbException;
import java.awt.image.Kernel;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class MainWindow {

    /** The path to the boot.img file */
    public static final String BOOT_IMG_PATH = "bootimg/boot.img";

    /** The path to the kernel.hmod file */
    public static final String KERNEL_HMOD_PATH = "bootimg/kernel.hmod";

    public JPanel mainPanel;
    private JButton membootBootImgButton;
    private JButton flashTheCustomKernelButton;

    public MainWindow() {
        membootBootImgButton.addActionListener(e -> {
            try {
                handleMembootClick();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        flashTheCustomKernelButton.addActionListener(e -> handleFlashCustomKernelClick());
    }

    private void handleMembootClick() throws UsbException, IOException, URISyntaxException, InterruptedException {
        System.out.println("Membooting the boot.img file");

        final FelDevice device = FelDevice.getFirstConnectedConsole();

        if (device == null) {
            System.out.println("No NES/SNES Minis in FEL mode detected");
            return;
        }

        device.open();

        try {
            System.out.println("Loading fes1 and uboot...");
            device.loadFes1AndUboot();
            System.out.println("Done loading fes1 and uboot. Initializing DRAM...");
            device.initializeDRAM();
            System.out.println("Done initializing DRAM.");

            final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
            byte[] kernelData = Files.readAllBytes(bootImgPath);
            int size = KernelHelper.calculateKernelSize(kernelData);

            if (size > kernelData.length ||
                    size > FelConstants.TRANSFER_MAX_SIZE) {
                throw new RuntimeException("Invalid kernel size for boot.img");
            }

            size = (size + FelConstants.SECTOR_SIZE - 1) / FelConstants.SECTOR_SIZE;
            size *= FelConstants.SECTOR_SIZE;

            if (kernelData.length != size) {
                kernelData = Arrays.copyOf(kernelData, size);
            }

            System.out.println("Uploading boot.img...");
            device.writeDeviceMemory(FelConstants.TRANSFER_BASE_M, kernelData);

            System.out.println("Done uploading boot.img. Running boot.img...");
            final String bootCommand = String.format("boota %08X", FelConstants.TRANSFER_BASE_M);
            device.runUbootCmd(bootCommand, true);
            System.out.println("Done membooting boot.img");
        } finally {
            device.close();
        }
    }

    private void handleFlashCustomKernelClick() {
        System.out.println("Flashing the custom kernel");
    }
}
