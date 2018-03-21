package com.fuckmyclassic;

import com.fuckmyclassic.boot.MembootHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.usb.UsbException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Binding class for the main window's form.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class MainWindow {

    /** The path to the boot.img file I'm using for testing */
    public static final String BOOT_IMG_PATH = "bootimg/boot.img";

    /** The path to the kernel.hmod file I'm using for testing */
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
        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        final MembootHelper membootHelper = new MembootHelper();
        membootHelper.membootKernelImage(bootImgPath);
    }

    private void handleFlashCustomKernelClick() {
        System.out.println("Flashing the custom kernel");
    }
}
