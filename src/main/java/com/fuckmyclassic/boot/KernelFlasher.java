package com.fuckmyclassic.boot;

import com.fuckmyclassic.network.SshConnection;
import com.jcraft.jsch.JSchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to assist with kernel flashing operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class KernelFlasher {

    static Logger LOG = LogManager.getLogger(KernelFlasher.class.getName());

    /** The path to the memboot.img prebaked kernel we memboot into for flashing */
    public static final String BOOT_IMG_PATH = "uboot/memboot.img";

    /**
     * The SSH connection to the console.
     */
    private final SshConnection sshConnection;

    /**
     * The helper for memboot operations.
     */
    private final MembootHelper membootHelper;

    /**
     * Constructor.
     * @param sshConnection
     */
    @Autowired
    public KernelFlasher(final SshConnection sshConnection, final MembootHelper membootHelper) {
        this.sshConnection = sshConnection;
        this.membootHelper = membootHelper;
    }

    public void flashCustomKernel() throws UsbException, URISyntaxException, InterruptedException {
        LOG.info("Flashing the custom kernel to the console");
        LOG.debug("First, membooting into the prebaked kernel image");

        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        if (!this.membootHelper.membootKernelImage(bootImgPath)) {
            return;
        }

        Thread.sleep(10000);
        LOG.debug("Waiting until network connection is detected");
        boolean connected = false;

        while (!connected) {
            try {
                Thread.sleep(3000);
                this.sshConnection.connect();
                connected = true;
            } catch (JSchException e) {
                LOG.debug("No SSH connection available yet");
            }
        }

        if (this.sshConnection.isConnected()) {
            LOG.debug("Connected to SSH successfully! Initiating kernel installation");
        } else {
            LOG.error("Something went wrong, couldn't SSH to console after membooting");
            return;
        }
    }
}
