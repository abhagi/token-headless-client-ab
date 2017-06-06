package com.bakkenbaeck.token.headless.rpc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class HeadlessRPCError {
    @JsonProperty
    private Integer code;

    @JsonProperty
    private String message;

    @JsonProperty
    private HashMap<String, String> data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public HeadlessRPCError() {}
}