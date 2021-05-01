package net.accela.prisma.file;

public class InvalidSauceException extends RuntimeException {

    public InvalidSauceException(String reason) {
        super(reason);
    }

    public InvalidSauceException(Exception ex) {
        super(ex);
    }
}
