package com.fuckmyclassic.usb;

import com.fuckmyclassic.fel.FelParseException;

/**
 * Class to represent an Allwinner USB response from a device in FEL mode.
 * Ported from Hakchi2.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class AwUsbResponse {

    private int tag;
    private int residue;
    private byte cswStatus;

    public AwUsbResponse() {

    }

    public AwUsbResponse(byte[] data) {
        if (data[0] != 'A' ||
                data[1] != 'W' ||
                data[2] != 'U' ||
                data[3] != 'S') {
            throw new FelParseException("Unable to find magic bytes in the provided data");
        }

        this.tag = (data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000));
        this.residue = (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        this.cswStatus = data[12];
    }

    public byte[] getData() {
        final byte[] data = new byte[13];
        data[0] = 'A';
        data[1] = 'W';
        data[2] = 'U';
        data[3] = 'S';
        data[4] = (byte)(this.tag & 0xFF); // tag
        data[5] = (byte)((this.tag >> 8) & 0xFF); // tag
        data[6] = (byte)((this.tag >> 16) & 0xFF); // tag
        data[7] = (byte)((this.tag >> 24) & 0xFF); // tag
        data[8] = (byte)(this.residue & 0xFF); // residue
        data[9] = (byte)((this.residue >> 8) & 0xFF); // residue
        data[10] = (byte)((this.residue >> 16) & 0xFF); // residue
        data[11] = (byte)((this.residue >> 24) & 0xFF); // residue
        data[12] = this.cswStatus; // csw_status

        return data;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getResidue() {
        return residue;
    }

    public void setResidue(int residue) {
        this.residue = residue;
    }

    public byte getCswStatus() {
        return cswStatus;
    }

    public void setCswStatus(byte cswStatus) {
        this.cswStatus = cswStatus;
    }
}
