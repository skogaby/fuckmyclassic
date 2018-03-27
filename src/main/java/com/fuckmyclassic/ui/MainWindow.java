package com.fuckmyclassic.ui;

import com.fuckmyclassic.boot.KernelFlasher;
import com.fuckmyclassic.boot.MembootHelper;
import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.usb.UsbException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Binding class for the main window's form.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class MainWindow {

    /** The path to the boot.img file I'm using for testing */
    public static final String BOOT_IMG_PATH = "uboot/memboot.img";

    public JPanel mainPanel;
    private JComboBox cmbGamesCollection;
    private JButton btnStructureOptions;
    private JTree treeGameCollection;

    private final KernelFlasher kernelFlasher;

    @Autowired
    public MainWindow(final KernelFlasher kernelFlasher) {
        this.kernelFlasher = kernelFlasher;

        btnStructureOptions.addActionListener(a -> {
            try {
                handleFlashCustomKernelClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleMembootClick() throws UsbException, URISyntaxException {
        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        final MembootHelper membootHelper = new MembootHelper();
        membootHelper.membootKernelImage(bootImgPath);
    }

    private void handleFlashCustomKernelClick() throws UsbException, JSchException, URISyntaxException, IOException {
        this.kernelFlasher.flashCustomKernel();
    }
}
