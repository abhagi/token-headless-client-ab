package com.bakkenbaeck.token.headless.db;
import com.fasterxml.jackson.databind.JsonNode;

public interface Store {
    public JsonNode load(String eth_address);
    public void save(String eth_address, JsonNode rootNode);
    public boolean exists(String eth_address);
}
