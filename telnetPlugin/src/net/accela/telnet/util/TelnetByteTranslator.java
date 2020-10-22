package net.accela.telnet.util;

import net.accela.telnet.server.TelnetSequence;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static net.accela.telnet.util.TelnetBytes.*;

public class TelnetByteTranslator {
    public static String byteToString(Byte by){
        if(by == null) return "JAVA_NULL";

        String plaintext;
        switch (by){
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
    public static String bytesToString(Byte[] bytes){
        if(bytes == null) return "";

        StringBuilder sb = new StringBuilder();
        for (Byte by:bytes) {
            sb.append(byteToString(by)).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    @NotNull
    public static String bytesToString(TelnetSequence telnetSequence){
        // Allocate a buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(telnetSequence.getByteSequence().length);
        Byte[] bytesToBeTranslated = new Byte[]{telnetSequence.getCommandByte(), telnetSequence.getOptionByte()};

        // Compose a string from it
        StringBuilder sb = new StringBuilder();
        sb.append(bytesToString(bytesToBeTranslated));
        // If there are arguments these should be treated as literal bytes,
        // rather than telnet commands or options
        if(telnetSequence.getArgumentBytes() != null){
            sb.append(", ");
            for (Byte by:telnetSequence.getArgumentBytes()) {
                sb.append(by).append(" ");
            }
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}
