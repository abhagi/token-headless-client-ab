package com.bakkenbaeck.token.headless.signal;

//import org.freedesktop.dbus.exceptions.DBusExecutionException;

import java.util.concurrent.ExecutionException;

public class AttachmentInvalidException extends ExecutionException {
    public AttachmentInvalidException(String message) {
        super(message);
    }

    public AttachmentInvalidException(String attachment, Exception e) {
        super(attachment + ": " + e.getMessage());
    }
}