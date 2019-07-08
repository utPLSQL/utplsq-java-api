package org.utplsql.api.exception;

/**
 * Created by Pavel Kaplya on 16.03.2019.
 */
public class UtplsqlException extends RuntimeException {
    public UtplsqlException() {
    }

    public UtplsqlException(String message) {
        super(message);
    }

    public UtplsqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public UtplsqlException(Throwable cause) {
        super(cause);
    }

    public UtplsqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
