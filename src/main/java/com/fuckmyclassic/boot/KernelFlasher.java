package com.fuckmyclassic.boot;

import com.fuckmyclassic.network.SshConnection;
import com.jcraft.jsch.JSchException;

import javax.usb.UsbException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to assist with kernel flashing operations.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class KernelFlasher {

    /** The path to the memboot.img prebaked kernel we memboot into for flashing */
    public static final String BOOT_IMG_PATH = "uboot/memboot.img";


    public void flashCustomKernel() throws UsbException, URISyntaxException, JSchException, IOException {
        System.out.println("----- Flashing the custom kernel to the console -----");
        System.out.println("First, membooting into the prebaked kernel image...");

        final Path bootImgPath = Paths.get(ClassLoader.getSystemResource(BOOT_IMG_PATH).toURI());
        final MembootHelper membootHelper = new MembootHelper();

        if (!membootHelper.membootKernelImage(bootImgPath)) {
            return;
        }

        System.out.println("Waiting until network connection is detected...");

        final SshConnection connection = new SshConnection();
        int seconds = 0;

        while (seconds <= 10) {
            try {
                Thread.sleep(1000);
                seconds++;
                connection.connect();
                break;
            } catch (JSchException e) {
                System.out.println("No SSH connection available yet...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (connection.isConnected()) {
            System.out.println("Connected to SSH successfully! Initiating kernel installation...");
        } else {
            System.out.println("Something went wrong, couldn't SSH to console after membooting...");
            return;
        }
    }
}
