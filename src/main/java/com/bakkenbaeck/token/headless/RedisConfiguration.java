package com.bakkenbaeck.token.headless;

import java.net.URI;

public final class RedisConfiguration {
    private String host;
    private int port;
    private int timeout;
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnvKey(String envKey) {
        setUri(System.getenv(envKey));
    }

    public void setUri(String rawUri) {
        URI uri = URI.create(rawUri);
        if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
            setHost(uri.getHost());
            setPort(uri.getPort());
            if (uri.getUserInfo() != null && uri.getUserInfo().contains(":")) {
                setPassword(uri.getUserInfo().split(":", 2)[1]);
            }
        }
    }

    public String getUri() {
        if (password != null) {
            return "redis://h:"+password+"@"+host+":"+port;
        } else {
            return "redis://"+host+":"+port;
        }
    }
}