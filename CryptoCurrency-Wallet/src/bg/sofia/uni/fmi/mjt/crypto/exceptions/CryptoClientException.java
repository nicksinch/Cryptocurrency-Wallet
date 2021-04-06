package bg.sofia.uni.fmi.mjt.crypto.exceptions;

public class CryptoClientException extends RuntimeException{
    public CryptoClientException(String msg) {
        super(msg);
    }

    public CryptoClientException(String msg, Exception e) {
        super(msg, e);
    }
}
