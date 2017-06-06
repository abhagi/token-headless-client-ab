package com.bakkenbaeck.token.headless.signal;

import org.whispersystems.signalservice.api.push.TrustStore;

import java.io.InputStream;

class WhisperTrustStore implements TrustStore {

    private final String trustStoreName;

    public WhisperTrustStore(String trustStoreName) {
        this.trustStoreName = trustStoreName;
    }

    @Override
    public InputStream getKeyStoreInputStream() {
        return WhisperTrustStore.class.getResourceAsStream(trustStoreName);
    }

    @Override
    public String getKeyStorePassword() {
        return "whisper";
    }
}
