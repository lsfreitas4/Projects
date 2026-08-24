package com.auction.ledger.config;

/**
 * Configuration loaded from environment variables with sane defaults.
 * Override via Docker Compose environment: section or .env file.
 */
public class LedgerConfig {

    // ── Network ─────────────────────────────────────────────────────────────
    public final int    p2pPort;          // port this node listens for peer connections
    public final String peers;            // comma-separated host:port of seed peers

    // ── Identity ─────────────────────────────────────────────────────────────
    public final String keystorePath;     // path to persisted EC key pair (PEM)
    public final String nodeId;           // derived from public key hash at runtime

    // ── Ledger storage ───────────────────────────────────────────────────────
    public final String dataDir;          // root directory for block files and indexes

    // ── Consensus (PoW) ──────────────────────────────────────────────────────
    public final int    difficulty;       // number of leading zero bits required
    public final int    blockSizeLimit;   // max transactions per block
    public final long   miningEnabled;    // 1 = this node mines, 0 = full-node only

    // ── Mempool ──────────────────────────────────────────────────────────────
    public final int    mempoolMaxSize;   // max pending transactions
    public final long   txExpirySeconds;  // drop unconfirmed txs older than this

    // ── REST API (for auction-api integration) ───────────────────────────────
    public final int    apiPort;

    private LedgerConfig() {
        p2pPort       = intEnv("LEDGER_P2P_PORT",    7000);
        peers         = env("LEDGER_PEERS",           "");
        keystorePath  = env("LEDGER_KEYSTORE_PATH",   "/data/keystore/node.pem");
        dataDir       = env("LEDGER_DATA_DIR",        "/data/ledger");
        difficulty    = intEnv("LEDGER_DIFFICULTY",   5);   // 20 leading zero bits (~1 s on modern CPU)
        blockSizeLimit= intEnv("LEDGER_BLOCK_SIZE",   50);
        miningEnabled = longEnv("LEDGER_MINING",      1L);
        mempoolMaxSize= intEnv("LEDGER_MEMPOOL_MAX",  1000);
        txExpirySeconds= longEnv("LEDGER_TX_EXPIRY",  3600L);
        apiPort       = intEnv("LEDGER_API_PORT",     8080);
        nodeId        = "";  // filled in at runtime from key
    }

    private static final LedgerConfig INSTANCE = new LedgerConfig();
    public static LedgerConfig get() { return INSTANCE; }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : def;
    }

    private static int intEnv(String key, int def) {
        try { return Integer.parseInt(env(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }

    private static long longEnv(String key, long def) {
        try { return Long.parseLong(env(key, String.valueOf(def))); }
        catch (NumberFormatException e) { return def; }
    }
}