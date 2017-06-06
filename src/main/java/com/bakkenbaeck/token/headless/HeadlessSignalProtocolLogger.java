package com.bakkenbaeck.token.headless;

import org.whispersystems.libsignal.logging.SignalProtocolLogger;

public class HeadlessSignalProtocolLogger implements SignalProtocolLogger {
    @Override
    public void log(int priority, String tag, String message) {
        if (priority >= SignalProtocolLogger.ERROR) {
            System.out.println(tag + "-" + String.valueOf(priority) + ": " + message);
        }
    }
}
