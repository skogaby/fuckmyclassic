package com.fuckmyclassic.boot;

import com.fuckmyclassic.network.SshConnection;
import com.jcraft.jsch.JSchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.usb.UsbException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to assist with kernel flashing operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class KernelFlasher {

    /** The path to the memboot.img prebaked kernel we memboot into for flashing */
    public static final String BOOT_IMG_PATH = "uboot/memboot.img";

    /**
     * The SSH connection to the console.
     */
    private final SshConnection sshConnection;

    /**
     * Constructor.
     * @param sshConnection
     */
    @Autowired
    public KernelFlasher(final SshConnection sshConnection) {
        this.sshConnection = sshConnection;
    }

    public void flashCustomKernel() throws UsbException, URISyntaxException, JSchException, IOException {
        System.out.println("----- Flashing the custom kernel to the console -----");
        System.out.println("First, membooting into the prebaked kernel image...");

        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        final MembootHelper membootHelper = new MembootHelper();

        if (!membootHelper.membootKernelImage(bootImgPath)) {
            return;
        }

        System.out.println("Waiting until network connection is detected...");
        int retries = 0;

        while (retries <= 10) {
            try {
                Thread.sleep(3000);
                retries++;
                this.sshConnection.connect();
                break;
            } catch (JSchException e) {
                System.out.println("No SSH connection available yet...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (this.sshConnection.isConnected()) {
            System.out.println("Connected to SSH successfully! Initiating kernel installation...");
        } else {
            System.out.println("Something went wrong, couldn't SSH to console after membooting...");
            return;
        }
    }
}
