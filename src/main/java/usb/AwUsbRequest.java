package com.shinkansen.usb;

import com.shinkansen.fel.FelParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent an Allwinner USB request for a device in FEL mode.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class AwUsbRequest {

    /**
     * Request type enum for Allwinner USB requests
     */
    public enum RequestType {
        AW_USB_READ((short)0x11),
        AW_USB_WRITE((short)0x12);

        private static Map<Short, RequestType> enumMap = new HashMap<>();

        static {
            for (RequestType requestType : RequestType.values()) {
                enumMap.put(requestType.value, requestType);
            }
        }

        private short value;

        RequestType(final short value) {
            this.value = value;
        }

        public short getValue() {
            return this.value;
        }

        public static RequestType getRequestType(final int value) {
            if (!enumMap.containsKey(value)) {
                throw new IllegalArgumentException("Given value is not a valid RequestType");
            }

            return enumMap.get(value);
        }
    }

    private int tag = 0;
    private int len;
    private RequestType command;
    private byte commandLen = 0x0C;

    public AwUsbRequest() {

    }

    public AwUsbRequest(final byte[] data) {
        if (data[0] != 'A' ||
                data[1] != 'W' ||
                data[2] != 'U' ||
                data[3] != 'C') {
            throw new FelParseException("Unable to find magic bytes in the provided data");
        }

        this.tag = (data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000));
        this.len = (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        this.commandLen = data[15];
        this.command = RequestType.getRequestType(data[16]);
    }

    public byte[] getData() {
        final byte[] data = new byte[32];
        data[0] = 'A';
        data[1] = 'W';
        data[2] = 'U';
        data[3] = 'C';
        data[4] = (byte)(this.tag & 0xFF); // tag
        data[5] = (byte)((this.tag >> 8) & 0xFF); // tag
        data[6] = (byte)((this.tag >> 16) & 0xFF); // tag
        data[7] = (byte)((this.tag >> 24) & 0xFF); // tag
        data[8] = (byte)(this.len & 0xFF); // len
        data[9] = (byte)((this.len >> 8) & 0xFF); // len
        data[10] = (byte)((this.len >> 16) & 0xFF); // len
        data[11] = (byte)((this.len >> 24) & 0xFF); // len
        data[12] = 0; // reserved1
        data[13] = 0; // reserved1
        data[14] = 0; // reserved2
        data[15] = this.commandLen; // cmd_len
        data[16] = (byte)(this.command.getValue());
        data[17] = 0; // reserved3
        data[18] = (byte)(this.len & 0xFF); // len
        data[19] = (byte)((this.len >> 8) & 0xFF); // len
        data[20] = (byte)((this.len >> 16) & 0xFF); // len
        data[21] = (byte)((this.len >> 24) & 0xFF); // len

        return data;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public RequestType getCommand() {
        return command;
    }

    public void setCommand(RequestType command) {
        this.command = command;
    }

    public byte getCommandLen() {
        return commandLen;
    }

    public void setCommandLen(byte commandLen) {
        this.commandLen = commandLen;
    }
}
