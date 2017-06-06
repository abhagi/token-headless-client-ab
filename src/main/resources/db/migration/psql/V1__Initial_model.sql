CREATE TABLE IF NOT EXISTS bot_sessions (
    eth_address TEXT,
    data JSON,
    PRIMARY KEY(eth_address)
);

CREATE TABLE IF NOT EXISTS signal_store (
    eth_address TEXT,
    data JSON,
    PRIMARY KEY(eth_address)
);
