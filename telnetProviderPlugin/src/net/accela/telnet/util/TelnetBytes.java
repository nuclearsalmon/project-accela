package net.accela.telnet.util;

import org.jetbrains.annotations.NotNull;

/**
 * Telnet commands, based off of RFC854.
 */
@SuppressWarnings("unused")
// TODO: 12/8/20 document this
public final class TelnetBytes {
    public static final byte NUL = (byte) 0x0;
    public static final byte TRANSMIT_BINARY = (byte) 0x0;
    public static final byte ECHO = (byte) 0x1;
    public static final byte RECONNECTION = (byte) 0x2;
    public static final byte SUPPRESS_GA = (byte) 0x3;
    public static final byte APPROX_MESSAGE_SIZE = (byte) 0x4;
    public static final byte STATUS = (byte) 0x5;
    public static final byte TIMING_MARK = (byte) 0x6;
    public static final byte REMOTE_CONTROLLED_TRANS_AND_ECHO = (byte) 0x7;
    public static final byte OUTPUT_LINE_WIDTH = (byte) 0x8;
    public static final byte OUTPUT_PAGE_SIZE = (byte) 0x9;
    public static final byte OUTPUT_CARRIAGE_RETURN_DISPOSITION = (byte) 0xA;
    public static final byte OUTPUT_HORIZONTAL_TABSTOPS = (byte) 0xB;
    public static final byte OUTPUT_HORIZONTAL_TAB_DISPOSITION = (byte) 0xC;
    public static final byte OUTPUT_FORMFEED_DISPOSITION = (byte) 0xD;
    public static final byte VERTICAL_TABSTOPS = (byte) 0xE;
    public static final byte OUTPUT_VERTICAL_TAB_DISPOSITION = (byte) 0xF;
    public static final byte OUTPUT_LINEFEED_DISPOSITION = (byte) 0x10;
    public static final byte EXTENDED_ASCII = (byte) 0x11;
    public static final byte LOGOUT = (byte) 0x12;
    public static final byte BYTE_MACRO = (byte) 0x13;
    public static final byte DATA_ENTRY_TERMINAL = (byte) 0x14;
    public static final byte SUPDUP = (byte) 0x15;
    public static final byte SUPDUP_OUTPUT = (byte) 0x16;
    public static final byte SEND_LOCATION = (byte) 0x17;
    public static final byte TERMINAL_TYPE = (byte) 0x18;
    public static final byte END_OF_RECORD = (byte) 0x19;
    public static final byte TACACS_USER_IDENTIFICATION = (byte) 0x1A;
    public static final byte OUTPUT_MARKING = (byte) 0x1B;
    public static final byte TERMINAL_LOCATION_NUMBER = (byte) 0x1C;
    public static final byte TELNET_3270_REGIME = (byte) 0x1D;
    public static final byte X3_PAD = (byte) 0x1E;
    /**
     * Window size
     */
    public static final byte NAWS = (byte) 0x1F;
    public static final byte TERMINAL_SPEED = (byte) 0x20;
    public static final byte REMOTE_FLOW_CONTROL = (byte) 0x21;
    public static final byte LINEMODE = (byte) 0x22;
    public static final byte X_DISPLAY_LOCATION = (byte) 0x23;
    public static final byte ENVIRONMENT = (byte) 0x24;
    public static final byte AUTHENTICATION = (byte) 0x25;
    public static final byte ENCRYPTION_OPTION = (byte) 0x26;
    public static final byte NEW_ENVIRONMENT = (byte) 0x27;
    public static final byte TN3270E = (byte) 0x28;
    public static final byte XAUTH = (byte) 0x29;
    public static final byte CHARSET = (byte) 0x2A;
    public static final byte REMOTE_SERIAL_PORT = (byte) 0x2B;
    public static final byte COM_PORT_CONTROL = (byte) 0x2C;
    public static final byte SUPPRESS_LOCAL_ECHO = (byte) 0x2D;
    public static final byte START_TLS = (byte) 0x2E;
    public static final byte KERMIT = (byte) 0x2F;
    public static final byte SEND_URL = (byte) 0x30;
    public static final byte FORWARD_X = (byte) 0x31;
    // 50 - 137 are undefined
    public static final byte TELOPT_PRAGMA_LOGON = (byte) 0x8A;
    public static final byte TELOPT_SSPI_LOGON = (byte) 0x8B;
    public static final byte TELOPT_PRAGMA_HEARTBEAT = (byte) 0x8C;
    // 141 - 239 are undefined

    /**
     * SE - End of subnegotiation parameters.
     */
    public static final byte SE = (byte) 0xF0;

    /**
     * NOP - No operation
     */
    public static final byte NOP = (byte) 0xF1;

    /**
     * Data Mark - The data stream portion of a Synch.
     */
    public static final byte DMA = (byte) 0xF2;

    /**
     * Break - NVT character BRK
     */
    public static final byte BRK = (byte) 0xF3;

    /**
     * Interrupt Process - The function IP.
     */
    public static final byte IP = (byte) 0xF4;

    /**
     * Abort output - The function AO.
     */
    public static final byte AO = (byte) 0xF5;

    /**
     * Are You There - The function AYT.
     */
    public static final byte AYT = (byte) 0xF6;

    /**
     * Erase character - The function EC.
     */
    public static final byte EC = (byte) 0xF7;

    /**
     * Erase line - The function EL.
     */
    public static final byte EL = (byte) 0xF8;

    /**
     * Go ahead - The GA signal
     */
    public static final byte GA = (byte) 0xF9;

    /**
     * Subnegotiation - Indicates that what follows is subnegotiation of the indicated option
     */
    public static final byte SB = (byte) 0xFA;

    /**
     * Will - Indicates the desire to begin performing,
     * or confirmation that you are now performing, the indicated option.
     */
    public static final byte WILL = (byte) 0xFB;

    /**
     * Won't - Indicates the refusal to perform,
     * or continue performing, the indicated option.
     */
    public static final byte WONT = (byte) 0xFC;

    /**
     * Do - Indicates the request that the other party perform,
     * or confirmation that you are expecting the other party to perform, the indicated option.
     */
    public static final byte DO = (byte) 0xFD;

    /**
     * Don't - Indicates the demand that the other party stop performing,
     * or confirmation that you are no longer expecting the other party to perform, the indicated option.
     */
    public static final byte DONT = (byte) 0xFE;

    /**
     * Interpret as Command - What follows will be interpreted as a command sgr.
     * IAC needs to be doubled to be sent as data rather than a command. Other options should not be doubled.
     */
    public static final byte IAC = (byte) 0xFF;

    public static String byteToString(Byte by) {
        if (by == null) return "JAVA_NULL";

        String plaintext;
        switch (by) {
            case NUL:
                plaintext = "NUL";
                break;
            case NOP:
                plaintext = "NOP";
                break;
            case DMA:
                plaintext = "DMA";
                break;
            case BRK:
                plaintext = "BRK";
                break;
            case IP:
                plaintext = "IP";
                break;
            case AYT:
                plaintext = "AYT";
                break;
            case EC:
                plaintext = "EC";
                break;
            case EL:
                plaintext = "EL";
                break;
            case GA:
                plaintext = "GA";
                break;
            case SB:
                plaintext = "SB";
                break;
            case SE:
                plaintext = "SE";
                break;
            case WILL:
                plaintext = "WILL";
                break;
            case WONT:
                plaintext = "WONT";
                break;
            case DO:
                plaintext = "DO";
                break;
            case DONT:
                plaintext = "DONT";
                break;
            case IAC:
                plaintext = "IAC";
                break;
            default:
                plaintext = String.valueOf(by);
                break;
        }
        return plaintext;
    }

    @NotNull
    public static String bytesToString(byte... bytes) {
        if (bytes == null) return "";

        StringBuilder sb = new StringBuilder();
        for (Byte by : bytes) {
            sb.append(byteToString(by)).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    @NotNull
    public static String bytesToString(Byte[] bytes) {
        if (bytes == null) return "";

        StringBuilder sb = new StringBuilder();
        for (Byte by : bytes) {
            sb.append(byteToString(by)).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }
}
