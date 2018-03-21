package com.fuckmyclassic.fel;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent a FEL request.
 * Ported from Hakchi2.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class FelStandardRequest {

    /**
     * Enum to represent a FEL mode request type.
     */
    public enum RequestType {
        FEL_VERIFY_DEVICE((short)0x1), // (Read length 32 => FelVerifyDeviceResponse)
        FEL_SWITCH_ROLE((short)0x2),
        FEL_IS_READY((short)0x3), // (Read length 8)
        FEL_GET_CMD_SET_VER((short)0x4),
        FEL_DISCONNECT((short)0x10),
        FEL_DOWNLOAD((short)0x101), // (Write data to the device)
        FEL_RUN((short)0x102), // (Execute code)
        FEL_UPLOAD((short)0x103); // (Read data from the device)

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

    private RequestType command;
    private short tag;

    public FelStandardRequest(final byte[] data) {
        this.command = RequestType.getRequestType(data[0] | (data[1] * 0x100));
        this.tag = (short)(data[2] | (data[3] * 0x100));
    }

    public byte[] getData() {
        final byte[] data = new byte[16];
        data[0] = (byte)(command.getValue() & 0xFF);
        data[1] = (byte)((command.getValue() >> 8) & 0xFF);
        data[2] = (byte)(tag & 0xFF);
        data[3] = (byte)((tag >> 8) & 0xFF);
        return data;
    }
}
