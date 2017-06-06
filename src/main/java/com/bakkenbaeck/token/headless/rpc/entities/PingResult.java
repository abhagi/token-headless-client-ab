package com.bakkenbaeck.token.headless.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PingResult extends HeadlessRPCResult {
    @JsonProperty
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PingResult() {}
    public PingResult(String message) {
        this.message = message;
    }
}