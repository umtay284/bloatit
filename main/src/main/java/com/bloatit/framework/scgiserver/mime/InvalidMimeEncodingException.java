package com.bloatit.framework.scgiserver.mime;

public class InvalidMimeEncodingException extends Exception {
    private static final long serialVersionUID = -1100275740713977221L;

    public InvalidMimeEncodingException(String string) {
        super(string);
    }
}
