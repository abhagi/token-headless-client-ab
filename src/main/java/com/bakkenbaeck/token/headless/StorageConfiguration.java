package com.bakkenbaeck.token.headless;

public class StorageConfiguration {

    private PostgresConfiguration postgres;
    private SqliteConfiguration sqlite;

    public PostgresConfiguration getPostgres() {
        return postgres;
    }

    public void setPostgres(PostgresConfiguration postgres) {
        this.postgres = postgres;
    }

    public SqliteConfiguration getSqlite() {
        return sqlite;
    }

    public void setSqlite(SqliteConfiguration sqlite) {
        this.sqlite = sqlite;
    }

    public void setEnvKey(String envKey) {
        String url = System.getenv(envKey);
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            this.postgres = new PostgresConfiguration();
            this.postgres.setUrl(url);
        } else if (url.startsWith("sqlite://")) {
            this.sqlite = new SqliteConfiguration();
            this.sqlite.setFile(url);
        }
    }

    public StorageConfiguration() { }
}
