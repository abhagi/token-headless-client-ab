package com.bakkenbaeck.token.headless;

import java.net.URI;
import java.net.URISyntaxException;

public class SqliteConfiguration {
    private String file;
    private String jdbcUrl;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        // strip sqlite url header
        if (file.startsWith("sqlite://")) {
            file = file.substring(9);
            // match sqlalchemy absolute pathing
            if (file.startsWith("/")) {
                file = file.substring(1);
            }
        }
        this.file = file;
        this.jdbcUrl = "jdbc:sqlite:" + file;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setEnvKey(String envKey) {
        setFile(System.getenv(envKey));
    }

    public SqliteConfiguration() {}
}
