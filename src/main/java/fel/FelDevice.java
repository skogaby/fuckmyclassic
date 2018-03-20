package com.shinkansen.fel;

import com.shinkansen.usb.AwUsbRequest;
import com.shinkansen.usb.AwUsbResponse;

import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent a connected USB device in FEL mode.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelDevice {

    /** The claimed USB FEL interface */
    private final UsbInterface iface;

    /** The in endpoint address. */
    private final byte inEndpoint;

    /** The out endpoint address. */
    private final byte outEndpoint;

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
        this.inEndpoint = inEndpoint;
        this.outEndpoint = outEndpoint;
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

    private void writeDeviceMemory(int address, byte[] buffer) throws UsbException {
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

    private byte[] readDeviceMemory(int address, int length) throws UsbException {
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

    private void felRequest(final FelStandardRequest.RequestType command, final int address, final int length)
            throws UsbException {
        final FelMessage req = new FelMessage();
        req.setCommand(command);
        req.setAddress(address);
        req.setLength(length);
        felWrite(req.getData());
    }

    private void felWrite(final byte[] buffer) throws UsbException {
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

    private byte[] felRead(final int length) throws UsbException {
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

    private void writeToUSB(final byte[] buffer) throws UsbException {
        int sent = this.outPipe.syncSubmit(buffer);
        System.out.println(String.format("FEL -> {%d} bytes of {%d} requested bytes written", sent, buffer.length));

        if (sent < buffer.length) {
            throw new FelException("Can't write to USB");
        }
    }

    private byte[] readFromUSB(final int length) throws UsbException {
        final byte[] result = new byte[length];
        int received = this.inPipe.syncSubmit(result);

        System.out.println(String.format("FEL <- {%d} bytes of {%d} requested bytes received", received, length));

        if (received < length) {
            throw new FelException("Can't read from USB");
        }

        return result;
    }

    public void execute(int address) throws UsbException {
        felRequest(FelStandardRequest.RequestType.FEL_RUN, address, 0);
        final FelStatusResponse status = new FelStatusResponse(felRead(8));

        if (status.getState() != 0) {
            throw new FelException("FEL run error");
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
    public void setUbootBin(byte[] ubootBin) {
        this.ubootBin = ubootBin;
    }
}
