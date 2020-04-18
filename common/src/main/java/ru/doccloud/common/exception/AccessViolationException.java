package ru.doccloud.common.exception;

/**
 * @author Andrey Kadnikov
 */
public class AccessViolationException extends RuntimeException {

    public AccessViolationException(String message) {
        super(message);
    }
}
