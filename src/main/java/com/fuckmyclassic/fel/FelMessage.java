package com.fuckmyclassic.fel;

/**
 * Class to represent a message to a FEL device.
 * Ported from Hakchi2.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelMessage {

    private FelStandardRequest.RequestType command;
    private short tag;
    private int address;
    private int length;
    private int flags;

    public FelMessage() {

    }

    public FelMessage(final byte[] data) {
        this.command = FelStandardRequest.RequestType.getRequestType(data[0] | (data[1] * 0x100));
        this.tag = (short)(data[2] | (data[3] * 0x100));
        this.address = (data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000));
        this.length = (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        this.flags = (data[12] | (data[13] * 0x100) | (data[14] * 0x10000) | (data[15] * 0x1000000));
    }

    public byte[] getData() {
        final byte[] data = new byte[16];
        data[0] = (byte)(command.getValue() & 0xFF); // mark
        data[1] = (byte)((command.getValue() >> 8) & 0xFF); // mark
        data[2] = (byte)(tag & 0xFF); // tag
        data[3] = (byte)((tag >> 8) & 0xFF); // tag
        data[4] = (byte)(address & 0xFF); // address
        data[5] = (byte)((address >> 8) & 0xFF); // address
        data[6] = (byte)((address >> 16) & 0xFF); // address
        data[7] = (byte)((address >> 24) & 0xFF); // address
        data[8] = (byte)(length & 0xFF); // len
        data[9] = (byte)((length >> 8) & 0xFF); // len
        data[10] = (byte)((length >> 16) & 0xFF); // len
        data[11] = (byte)((length >> 24) & 0xFF); // len
        data[12] = (byte)(flags & 0xFF); // flags
        data[13] = (byte)((flags >> 8) & 0xFF); // flags
        data[14] = (byte)((flags >> 16) & 0xFF); // flags
        data[15] = (byte)((flags >> 24) & 0xFF); // flags

        return data;
    }

    public FelStandardRequest.RequestType getCommand() {
        return command;
    }

    public void setCommand(FelStandardRequest.RequestType command) {
        this.command = command;
    }

    public short getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
