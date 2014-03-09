package io.github.vickychijwani.giantbomb.api;

class GiantBombException extends Exception {

    /**
     * Constructs a new {@link GiantBombException} that includes the current stack trace.
     */
    public GiantBombException() { }

    /**
     * Constructs a new {@link GiantBombException} with the current stack trace and the specified
     * detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public GiantBombException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@link GiantBombException} with the current stack trace, the specified
     * detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable     the cause of this exception.
     */
    public GiantBombException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@link GiantBombException} with the current stack trace and the specified
     * cause.
     *
     * @param throwable the cause of this exception.
     */
    public GiantBombException(Throwable throwable) {
        super(throwable);
    }

}
