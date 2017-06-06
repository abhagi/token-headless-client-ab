package com.bakkenbaeck.token.headless.signal;

//import org.freedesktop.dbus.exceptions.DBusExecutionException;

import java.util.concurrent.ExecutionException;

public class NotAGroupMemberException extends ExecutionException {

    public NotAGroupMemberException(String message) {
        super(message);
    }

    public NotAGroupMemberException(byte[] groupId, String groupName) {
        super("User is not a member in group: " + groupName + " (" + Base64.encodeBytes(groupId) + ")");
    }
}
