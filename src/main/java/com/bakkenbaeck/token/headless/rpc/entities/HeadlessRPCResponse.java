package com.bakkenbaeck.token.headless.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HeadlessRPCResponse {

    @JsonProperty
    private String jsonrpc = "2.0";

    @JsonProperty
    private String id;

    @JsonProperty
    private HeadlessRPCResult result;

    @JsonProperty
    private HeadlessRPCError error;



    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HeadlessRPCResult getResult() {
        return result;
    }

    public void setResult(HeadlessRPCResult result) {
        this.result = result;
    }

    public HeadlessRPCError getError() {
        return error;
    }

    public void setError(HeadlessRPCError error) {
        this.error = error;
    }

    public HeadlessRPCResponse() {}

    public HeadlessRPCResponse(String id, HeadlessRPCResult result, HeadlessRPCError error) {
        this.jsonrpc = "2.0";
        this.id = id;
        this.result = result;
        this.error = error;

    }
}