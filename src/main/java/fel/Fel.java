package com.shinkansen.fel;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbEndpointDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to help with top-level FEL mode interactions.
 * Derived heavily from the ADB demo found in
 * the usb4java examples.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class Fel {

    /** The USB vendor ID for NES/SNES Mini systems */
    private static final short USB_VENDOR_ID = (short) 0x1F3A;

    /** The USB product ID for NES/SNES Mini systems */
    private static final short USB_PRODUCT_ID = (short) 0xEFE8;

    /**
     * Returns the list of all available FEL devices.
     * @return The list of available FEL devices.
     * @throws UsbException When USB communication failed.
     */
    public static List<FelDevice> findDevices() throws UsbException {
        UsbServices services = UsbHostManager.getUsbServices();
        List<FelDevice> usbDevices = new ArrayList<>();
        findDevicesHelper(services.getRootUsbHub(), usbDevices);

        return usbDevices;
    }

    /**
     * Recursively scans the specified USB hub for FEL devices and puts them
     * into the list.
     * @param hub The USB hub to scan recursively.
     * @param devices The list where to add found devices.
     */
    private static void findDevicesHelper(final UsbHub hub, final List<FelDevice> devices) {
        for (UsbDevice usbDevice: (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            if (usbDevice.isUsbHub()) {
                findDevicesHelper((UsbHub) usbDevice, devices);
            }
            else {
                checkDevice(usbDevice, devices);
            }
        }
    }

    /**
     * Checks if the specified USB device is a FEL device and adds it to the
     * list if it is.
     * @param usbDevice The USB device to check.
     * @param felDevices The list of FEL devices to add the USB device to when it is a FEL device.
     */
    private static void checkDevice(final UsbDevice usbDevice, final List<FelDevice> felDevices) {
        final UsbDeviceDescriptor deviceDesc = usbDevice.getUsbDeviceDescriptor();

        // Ignore devices with the wrong VID/PID
        if (!isUsbDeviceNintendoSystem(deviceDesc)) {
            return;
        }

        // Check interfaces of device
        UsbConfiguration config = usbDevice.getActiveUsbConfiguration();
        for (UsbInterface iface: (List<UsbInterface>) config.getUsbInterfaces()) {
            List<UsbEndpoint> endpoints = iface.getUsbEndpoints();

            // Ignore interface if it does not have two endpoints
            if (endpoints.size() != 2) {
                continue;
            }

            UsbEndpointDescriptor ed1 = endpoints.get(0).getUsbEndpointDescriptor();
            UsbEndpointDescriptor ed2 = endpoints.get(1).getUsbEndpointDescriptor();

            // Ignore interface if endpoints are not bulk endpoints
            if (((ed1.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0) ||
                    ((ed2.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0)) {
                continue;
            }

            // Determine which endpoint is in and which is out. If both
            // endpoints are in or out then ignore the interface
            byte a1 = ed1.bEndpointAddress();
            byte a2 = ed2.bEndpointAddress();
            byte in, out;

            if (((a1 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                    ((a2 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
                in = a1;
                out = a2;
            } else if (((a2 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                    ((a1 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
                out = a1;
                in = a2;
            }
            else {
                continue;
            }

            // Create ADB device and add it to the list
            FelDevice adbDevice = new FelDevice(iface, in, out);
            felDevices.add(adbDevice);
        }
    }

    /**
     * Says whether or not the given USB device is a Nintendo Classic system
     * connected in FEL mode.
     * @param deviceDescriptor The USB device to query
     * @return Whether or not the given device is a Nintendo Classic in FEL mode
     */
    private static boolean isUsbDeviceNintendoSystem(final UsbDeviceDescriptor deviceDescriptor) {
        return (deviceDescriptor.idVendor() == USB_VENDOR_ID) &&
                (deviceDescriptor.idProduct() == USB_PRODUCT_ID);
    }
}
