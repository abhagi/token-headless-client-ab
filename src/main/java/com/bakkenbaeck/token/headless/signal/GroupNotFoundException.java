package com.bakkenbaeck.token.headless.signal;

//import org.freedesktop.dbus.exceptions.DBusExecutionException;

import java.util.concurrent.ExecutionException;

public class GroupNotFoundException extends ExecutionException {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(byte[] groupId) {
        super("Group not found: " + Base64.encodeBytes(groupId));
    }
}
