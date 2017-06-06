package com.bakkenbaeck.token.headless;


public final class TokenHeadlessClientConfiguration {
    private String server;
    private String token_ethereum_service_url;
    private String token_id_service_url;
    private String seed;
    private String store;
    private String stage;
    private String username;
    private String name;
    private String avatar;
    private RedisConfiguration redis;
    private StorageConfiguration storage;

    private String stripQuotes(String input) {
        if (input != null && ((input.startsWith("\"") && input.endsWith("\"")) || (input.startsWith("'") && input.endsWith("'")))) {
            input = input.substring(1, input.length() - 1);
        }
        return input;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getSeed() {
        if (this.seed == null) {
            String seed = System.getenv("TOKEN_APP_SEED");
            this.seed = stripQuotes(seed);
        }
        return this.seed;
    }

    public void setSeed(String seed) {
        this.seed = stripQuotes(seed);
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getStage() {
        return (stage != null) ? stage : System.getenv("STAGE");
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getUsername() {
        if (this.username == null) {
            String username = System.getenv("TOKEN_APP_USERNAME");
            this.username = stripQuotes(username);
        }
        return this.username;
    }

    public void setUsername(String username) {
        this.username = stripQuotes(username);
    }

    public String getName() {
        if (this.name == null) {
            String name = System.getenv("TOKEN_APP_NAME");
            this.name = stripQuotes(name);
        }
        return this.name;
    }

    public void setName(String name) {
        this.name = stripQuotes(name);
    }

    public String getAvatar() {
        return (avatar != null) ? avatar : System.getenv("TOKEN_APP_AVATAR");
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public RedisConfiguration getRedis() {
        return redis;
    }

    public void setRedis(RedisConfiguration redis) {
        this.redis = redis;
    }

    public StorageConfiguration getStorage() {
        return storage;
    }

    public void setStorage(StorageConfiguration storage) {
        this.storage = storage;
    }

    public String getToken_ethereum_service_url() {
        return token_ethereum_service_url;
    }

    public void setToken_ethereum_service_url(String token_ethereum_service_url) {
        this.token_ethereum_service_url = token_ethereum_service_url;
    }

    public String getToken_id_service_url() {
        return token_id_service_url;
    }

    public void setToken_id_service_url(String token_id_service_url) {
        this.token_id_service_url = token_id_service_url;
    }
}
