package net.sf.buildbox.releasator.ng;

public class ScmException extends RuntimeException {
    public ScmException(Throwable cause) {
        super(cause);
    }

    public ScmException(String message) {
        super(message);
    }

    public ScmException(String message, Throwable cause) {
        super(message, cause);
    }
}
