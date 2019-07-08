package org.utplsql.api.exception;

/**
 * Created by Pavel Kaplya on 08.03.2019.
 */
public class UtplsqlConfigurationException extends Exception {
    public UtplsqlConfigurationException(String message) {
        super(message);
    }

    public UtplsqlConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
