package com.bakkenbaeck.token.headless.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class HeadlessRPCRequest {

    @JsonProperty
    private String jsonrpc;

    @JsonProperty
    private String method;

    @JsonProperty
    private HashMap<String, String> params;

    @JsonProperty
    private String id;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public HeadlessRPCRequest() {}

}