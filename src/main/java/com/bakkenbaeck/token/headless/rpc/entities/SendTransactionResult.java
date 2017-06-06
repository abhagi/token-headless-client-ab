package com.bakkenbaeck.token.headless.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendTransactionResult extends HeadlessRPCResult {
    @JsonProperty
    private String txHash;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public SendTransactionResult() {}
    public SendTransactionResult(String txHash) {
        this.txHash = txHash;
    }
}