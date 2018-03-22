package com.fuckmyclassic.fel;

import com.fuckmyclassic.usb.AwUsbRequest;
import com.fuckmyclassic.usb.AwUsbResponse;

import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent a connected USB device in FEL mode.
 * Ported from Hakchi2.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelDevice {

    /** The path to the fes1.bin file */
    public static final String FES1_PATH = "uboot/fes1.bin";

    /** The path to the uboot.bin file */
    public static final String UBOOT_PATH = "uboot/ubootSD.bin";

    /** The claimed USB FEL interface */
    private final UsbInterface iface;

    /** The pipe to read from the USB device */
    private final UsbPipe inPipe;

    /** The pipe to write to the USB device */
    private final UsbPipe outPipe;

    /** The binary for fes1.bin */
    private byte[] fes1Bin;

    /** The binary for uboot.bin */
    private byte[] ubootBin;

    /** Says whether or not the DRAM is initialized */
    private boolean dramInitialized;

    /** Uboot command offset */
    private int commandOffset;

    /**
     * Constructs a new FEL device.
     * @param iface The USB interface (must not be null)
     * @param inEndpoint The in endpoint address
     * @param outEndpoint The out endpoint address
     */
    public FelDevice(final UsbInterface iface, final byte inEndpoint, final byte outEndpoint) {
        if (iface == null) {
            throw new IllegalArgumentException("iface must not be null");
        }

        this.iface = iface;
        this.dramInitialized = false;
        this.inPipe = iface.getUsbEndpoint(inEndpoint).getUsbPipe();
        this.outPipe = iface.getUsbEndpoint(outEndpoint).getUsbPipe();
    }

    /**
     * Opens the FEL device. When you are finished communicating with the device
     * then you should call the {@link #close()} method.
     * @throws UsbException When device could not be opened
     */
    public void open() throws UsbException {
        this.iface.claim();
        this.inPipe.open();
        this.outPipe.open();
    }

    /**
     * Closes the FEL device.
     * @throws UsbException When device could not be closed.
     */
    public void close() throws UsbException {
        this.inPipe.close();
        this.outPipe.close();
        this.iface.release();
    }

    /**
     * Loads the fes1 and uboot into memory.
     * @throws IOException
     */
    public void loadFes1AndUboot() throws IOException, URISyntaxException {
        // once we open the device, we need to initialize the system
        // so we can actually dump the kernel
        final Path fes1Path =  Paths.get(ClassLoader.getSystemResource(FES1_PATH).toURI());
        final Path ubootPath = Paths.get(ClassLoader.getSystemResource(UBOOT_PATH).toURI());

        if (!Files.exists(fes1Path)) {
            throw new FileNotFoundException(FES1_PATH + " not found");
        } else if(!Files.exists(ubootPath)) {
            throw new FileNotFoundException(UBOOT_PATH + " not found");
        }

        this.setFes1Bin(Files.readAllBytes(fes1Path));
        this.setUbootBin(Files.readAllBytes(ubootPath));
    }

    /**
     * Initializes the system DRAM once fes1 and uboot have been loaded.
     * @return Whether or not the DRAM was initialized
     * @throws UsbException
     */
    public boolean initializeDRAM() throws UsbException {
        if (dramInitialized) {
            return true;
        }

        if (this.fes1Bin == null ||
                this.fes1Bin.length < FelConstants.FES1_TEST_SIZE) {
            throw new FelException("Can't initialize DRAM, incorrect fes1.bin");
        }

        final byte[] buf = readDeviceMemory((FelConstants.FES1_BASE_M + this.fes1Bin.length - FelConstants.FES1_TEST_SIZE),
                FelConstants.FES1_TEST_SIZE);
        final byte[] buf2 =  new byte[FelConstants.FES1_TEST_SIZE];
        System.arraycopy(this.fes1Bin, this.fes1Bin.length - buf.length, buf2, 0, FelConstants.FES1_TEST_SIZE);

        // check to see if we've already initialized DRAM
        if (Arrays.equals(buf, buf2)) {
            dramInitialized = true;
            return true;
        }

        writeDeviceMemory(FelConstants.FES1_BASE_M, this.fes1Bin);
        execute(FelConstants.FES1_BASE_M);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dramInitialized = true;
        return true;
    }

    /**
     * Runs the given uboot command
     * @param command The uboot command to run
     * @param noReturn Whether or not to check for FEL connectivity afterward
     * @throws FelException
     * @throws UsbException
     * @throws InterruptedException
     */
    public void runUbootCmd(String command, boolean noReturn) throws FelException, UsbException, InterruptedException {
        if (this.commandOffset <= 0) {
            throw new RuntimeException("Invalid uboot binary, boot command not found");
        }

        final int testSize = 0x20;
        if (this.ubootBin == null || this.ubootBin.length < testSize) {
            throw new FelException("Can't init uboot, incorrect uboot binary");
        }

        final byte[] buf = readDeviceMemory(FelConstants.UBOOT_BASE_M, testSize);
        final byte[] buf2 = Arrays.copyOf(this.ubootBin, testSize);

        if (!buf.equals(buf2)){
            writeDeviceMemory(FelConstants.UBOOT_BASE_M, this.ubootBin);
        }

        final byte[] cmdBuff = String.format("%s\0", command).getBytes(StandardCharsets.US_ASCII);
        writeDeviceMemory(FelConstants.UBOOT_BASE_M + this.commandOffset, cmdBuff);
        execute(FelConstants.UBOOT_BASE_M);

        if (noReturn) {
            return;
        }

        this.close();

        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);
            // callback?.Invoke(CurrentAction.RunningCommand, command);
        }

        int errorCount = 0;
        while (true) {
            if (getFirstConnectedConsole() == null) {
                errorCount++;

                if (errorCount >= 10) {
                    this.close();
                    throw new FelException("No answer from device");
                }

                Thread.sleep(2000);
            } else {
                break;
            }
        }
    }

    /**
     * Writes data directly to the FEL device's memory.
     * @param address The address to write to
     * @param buffer The data to write to memory
     * @throws UsbException
     */
    public void writeDeviceMemory(int address, byte[] buffer) throws UsbException {
        if (address >= FelConstants.DRAM_BASE) {
            initializeDRAM();
        }

        int length = buffer.length;
        if (length != (length & ~3)) {
            length = (length + 3) & ~3;
            final byte[] newBuffer = new byte[length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        int pos = 0;
        while (pos < buffer.length) {
            final byte[] buf = new byte[Math.min(buffer.length - pos, FelConstants.MAX_BULK_SIZE)];
            System.arraycopy(buffer, pos, buf, 0, buf.length);
            felRequest(FelStandardRequest.RequestType.FEL_DOWNLOAD, address + pos, buf.length);
            felWrite(buf);
            final FelStatusResponse status = new FelStatusResponse(felRead(8));

            if (status.getState() != 0) {
                throw new FelException("FEL write error");
            }

            pos += buf.length;
        }
    }

    /**
     * Read data directly from the FEL device's memory.
     * @param address The address to read from.
     * @param length The length of data to read
     * @return A byte array representing the data at the given address of the given length
     * @throws UsbException
     */
    public byte[] readDeviceMemory(int address, int length) throws UsbException {
        if (address > FelConstants.DRAM_BASE) {
            initializeDRAM();
        }

        length = (length + 3) & ~3;
        final List<Byte> result = new ArrayList<>();

        while (length > 0) {
            int l = Math.min(length, FelConstants.MAX_BULK_SIZE);
            felRequest(FelStandardRequest.RequestType.FEL_UPLOAD, address, l);
            final byte[] r = felRead(l);

            for (byte b : r) {
                result.add(b);
            }

            final FelStatusResponse status = new FelStatusResponse(felRead(8));
            if (status.getState() != 0) {
                throw new FelException("FEL read error");
            }

            length -= l;
            address += l;
        }

        final byte[] data = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            data[i] = result.get(i);
        }

        return data;
    }

    /**
     * Send a FEL request to the device
     * @param command The command to send
     * @param address The address to set on the command
     * @param length The length to set on the command
     * @throws UsbException
     */
    public void felRequest(final FelStandardRequest.RequestType command, final int address, final int length)
            throws UsbException {
        final FelMessage req = new FelMessage();
        req.setCommand(command);
        req.setAddress(address);
        req.setLength(length);
        felWrite(req.getData());
    }

    /**
     * Writes data to the FEL device.
     * @param buffer The data to write.
     * @throws UsbException
     */
    public void felWrite(final byte[] buffer) throws UsbException {
        final AwUsbRequest req = new AwUsbRequest();
        req.setCommand(AwUsbRequest.RequestType.AW_USB_WRITE);
        req.setLen((short)buffer.length);
        writeToUSB(req.getData());
        writeToUSB(buffer);
        final AwUsbResponse resp = new AwUsbResponse(readFromUSB(13));

        if (resp.getCswStatus() != 0) {
            throw new FelException("FEL write error");
        }
    }

    /**
     * Reads data from the FEL device.
     * @param length The length of data to read.
     * @return A byte array of the given length containing data from the FEL device.
     * @throws UsbException
     */
    public byte[] felRead(final int length) throws UsbException {
        final AwUsbRequest req = new AwUsbRequest();
        req.setCommand(AwUsbRequest.RequestType.AW_USB_READ);
        req.setLen(length);
        writeToUSB(req.getData());

        byte[] result = readFromUSB(length);
        final AwUsbResponse resp = new AwUsbResponse(readFromUSB(13));

        if (resp.getCswStatus() != 0) {
            throw new FelException("FEL read error");
        }

        return result;
    }

    /**
     * Sends data directly to the USB interface for the FEL device.
     * @param buffer The data to send to the USB interface.
     * @throws UsbException
     */
    public void writeToUSB(final byte[] buffer) throws UsbException {
        int sent = this.outPipe.syncSubmit(buffer);
        System.out.println(String.format("FEL -> {%d} bytes of {%d} requested bytes written", sent, buffer.length));

        if (sent < buffer.length) {
            throw new FelException("Can't write to USB");
        }
    }

    /**
     * Reads data directly from the USB interface.
     * @param length The length of data to read from the USB device.
     * @return A byte array of the given length containing data from the USB interface.
     * @throws UsbException
     */
    public byte[] readFromUSB(final int length) throws UsbException {
        final byte[] result = new byte[length];
        int received = this.inPipe.syncSubmit(result);

        System.out.println(String.format("FEL <- {%d} bytes of {%d} requested bytes received", received, length));

        if (received < length) {
            throw new FelException("Can't read from USB");
        }

        return result;
    }

    /**
     * Executes the code at the given address.
     * @param address The address to jump to.
     * @throws UsbException
     */
    public void execute(int address) throws UsbException {
        felRequest(FelStandardRequest.RequestType.FEL_RUN, address, 0);
        final FelStatusResponse status = new FelStatusResponse(felRead(8));

        if (status.getState() != 0) {
            throw new FelException("FEL execution error");
        }
    }

    /**
     * Gets the FES1 binary data.
     * @return The FES1 binary data
     */
    public byte[] getFes1Bin() {
        return fes1Bin;
    }

    /**
     * Sets the FES1 binary data
     * @param fes1Bin The FES1 binary data
     */
    public void setFes1Bin(byte[] fes1Bin) {
        this.fes1Bin = fes1Bin;
    }

    /**
     * Gets the uboot binary data
     * @return The uboot binary data
     */
    public byte[] getUbootBin() {
        return ubootBin;
    }

    /**
     * Sets the uboot binary data
     * @param ubootBin The uboot binary data
     */
    public void setUbootBin(byte[] ubootBin) throws UnsupportedEncodingException {
        this.ubootBin = ubootBin;

        // find the command offset in the uboot binary
        final String prefix = "bootcmd=";
        for (int i = 0; i < ubootBin.length - prefix.length(); i++) {
            if (new String(ubootBin, i, prefix.length(), StandardCharsets.US_ASCII).equals(prefix)) {
                this.commandOffset = i + prefix.length();
                break;
            }
        }
    }

    /**
     * Returns the first connected NES/SNES Mini console. Throws
     * a FelException if no console is connected.
     * @throws UsbException
     */
    public static FelDevice getFirstConnectedConsole() throws UsbException {
        // Find a FEL device to communicate with
        final List<FelDevice> felDevices = Fel.findConnectedConsolesInFelMode();

        if (felDevices.isEmpty()) {
            return null;
        } else {
            return felDevices.get(0);
        }
    }
}
