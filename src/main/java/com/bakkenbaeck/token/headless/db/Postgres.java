package com.bakkenbaeck.token.headless.db;

import com.bakkenbaeck.token.headless.PostgresConfiguration;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.sql.*;

public class Postgres implements Store {
    private final ObjectMapper jsonProcessor = new ObjectMapper();
    private Connection conn;
    PostgresConfiguration config;

    public Postgres(PostgresConfiguration config) {
        this.config = config;
        jsonProcessor.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); // disable autodetect
        jsonProcessor.enable(SerializationFeature.INDENT_OUTPUT); // for pretty print, you can disable it.
        jsonProcessor.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);
        jsonProcessor.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jsonProcessor.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        jsonProcessor.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    }

    public boolean connect() throws SQLException, ClassNotFoundException {
        if (config.getJdbcUrl().isEmpty() || config.getUsername().isEmpty()) {
            throw new SQLException("Database credentials missing");
        }

        Class.forName("org.postgresql.Driver");
        this.conn = DriverManager.getConnection(
                this.config.getJdbcUrl(),
                this.config.getUsername(),
                this.config.getPassword());
        return true;
    }

    public JsonNode load(String eth_address) {
        String json = "{}";
        JsonNode rootNode = null;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM signal_store WHERE eth_address = ?");
            st.setString(1, eth_address);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                json = rs.getString("data");
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rootNode = jsonProcessor.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rootNode;
    }

    public void save(String eth_address, JsonNode rootNode) {
        try {
            String json = jsonProcessor.writeValueAsString(rootNode);
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(json);
            PreparedStatement st = null;
            if (exists(eth_address)) {
                st = conn.prepareStatement("UPDATE signal_store SET data=? WHERE eth_address=?");
            } else {
                st = conn.prepareStatement("INSERT INTO signal_store (data, eth_address) VALUES (?,?)");

            }
            st.setObject(1, jsonObject);
            st.setString(2, eth_address);
            int rc = st.executeUpdate();
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String eth_address) {
        boolean result = false;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT eth_address FROM signal_store WHERE eth_address = ?");
            st.setString(1, eth_address);
            ResultSet rs = st.executeQuery();
            if (rs.next()){
                result = true;
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}
