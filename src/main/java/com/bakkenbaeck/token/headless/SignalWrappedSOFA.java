package com.bakkenbaeck.token.headless;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignalWrappedSOFA {

    @JsonProperty
    private String sofa;

    @JsonProperty
    private String recipient;

    @JsonProperty
    private String sender;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSofa() {
        return sofa;
    }

    public void setSofa(String sofa) {
        this.sofa = sofa;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public SignalWrappedSOFA() {}

    public SignalWrappedSOFA(String sofa, String recipient, String sender) {
        this.sofa = sofa;
        this.recipient = recipient;
        this.sender = sender;
    }

}