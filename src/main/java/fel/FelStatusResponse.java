package com.shinkansen.fel;

/**
 * Class to represent a FEL status response.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelStatusResponse {

    private short mark = (short)0xFFFF;
    private short tag = 0;
    private byte state;

    public FelStatusResponse() {
    }

    public FelStatusResponse(byte[] data) {
        this.mark = (short)(data[0] | (data[1] * 0x100));
        this.tag = (short)(data[2] | (data[3] * 0x100));
        this.state = data[4];
    }

    public byte[] getData() {
        final byte[] data = new byte[8];
        data[0] = (byte)(this.mark & 0xFF); // mark
        data[1] = (byte)((this.mark >> 8) & 0xFF); // mark
        data[2] = (byte)(this.tag & 0xFF); // tag
        data[3] = (byte)((this.tag >> 8) & 0xFF); // tag
        data[4] = this.state;
        return data;
    }

    public short getMark() {
        return mark;
    }

    public void setMark(short mark) {
        this.mark = mark;
    }

    public short getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }
}
