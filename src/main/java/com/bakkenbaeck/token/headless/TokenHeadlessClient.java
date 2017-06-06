package com.bakkenbaeck.token.headless;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.headless.db.Store;
import com.bakkenbaeck.token.headless.db.Postgres;
import com.bakkenbaeck.token.headless.db.Sqlite;
import com.bakkenbaeck.token.headless.rpc.HeadlessRPC;
import com.bakkenbaeck.token.headless.signal.Manager;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.UserDetails;
import com.bakkenbaeck.token.network.IdService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.whispersystems.libsignal.logging.SignalProtocolLoggerProvider;
import org.yaml.snakeyaml.Yaml;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import retrofit2.Response;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

class TokenHeadlessClient {
    private static TokenHeadlessClientConfiguration config;
    private static Logger logger = Logger.getLogger(TokenHeadlessClient.class.toString());

    public static void main(String[] args) throws Exception {

        // -- config
        String configPath = "config.yml";
        if( args.length == 1 ) {
            configPath = args[0];
        }
        Yaml yaml = new Yaml();
        try( InputStream in = Files.newInputStream( Paths.get( configPath ) ) ) {
            config = yaml.loadAs(in, TokenHeadlessClientConfiguration.class);
            System.out.println( config.toString() );
        } catch (Exception e) {
            System.out.println( "Error parsing configuration - " + e.getMessage() );
            return;
        }

        SignalProtocolLoggerProvider.setProvider(new HeadlessSignalProtocolLogger());

        Flyway flyway = null;
        Store db = null;

        StorageConfiguration storageConfig = config.getStorage();

        if (storageConfig.getSqlite() != null) {
            SqliteConfiguration sqliteconfig = storageConfig.getSqlite();
            // -- migrations
            // TODO: figure out why this doesn't work!
            // flyway = new Flyway();
            // flyway.setDataSource(sqliteconfig.getJdbcUrl(), "", "");
            // flyway.setLocations("db/migration/sqlite");
            // flyway.migrate();

            db = new Sqlite(sqliteconfig);
            ((Sqlite)db).connect();
            // XXX: remove this is the flyway migration issue is solved
            ((Sqlite)db).executeResourceScript("db/migration/sqlite/V1__Initial_model.sql");
        } else if (storageConfig.getPostgres() != null) {
            PostgresConfiguration pgconfig = storageConfig.getPostgres();
            while(flyway == null || db == null) {
                try {
                    // -- migrations
                    flyway = new Flyway();
                    flyway.setDataSource(pgconfig.getJdbcUrl(), pgconfig.getUsername(), pgconfig.getPassword());
                    flyway.setLocations("db/migration/psql");
                    flyway.migrate();

                    // -- Postgres
                    db = new Postgres(pgconfig);
                    ((Postgres)db).connect();
                } catch (FlywaySqlException e) {
                    System.out.println("Could not connect to Postgres - retrying");
                    Thread.sleep(2000);
                }
            }
        }

        // Workaround for BKS truststore
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);

        String seed = config.getSeed();
        // validate seed
        if (seed == null || !Pattern.compile("^[a-z ]+$").matcher(seed).matches()) {
            System.out.println("Invalid Mnemonic Seed value");
            return;
        }
        HDWallet wallet = new HDWallet().init(seed);
        System.out.println("ID Address: " + wallet.getOwnerAddress());
        System.out.println("Payment Address: " + wallet.getPaymentAddress());

        //final boolean voice = false;
        String settingsPath = config.getStore();
        String trustStoreName = ("development".equals(config.getStage())) ? "heroku.store" : "token.store";
        Manager m = new Manager(wallet.getOwnerAddress(), settingsPath, config.getServer(), db, trustStoreName);


        // -- redis
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, config.getRedis().getHost(), config.getRedis().getPort(), config.getRedis().getTimeout(), config.getRedis().getPassword());

        // -- eth service
        EthService ethService = new EthService(wallet, config.getToken_ethereum_service_url());

        // -- id service
        IdService idService = new IdService(wallet, config.getToken_id_service_url());

        final String username = config.getUsername();
        if (username == null || !Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,59}$").matcher(username).matches()) {
            System.out.println("Invalid Username: usernames must be at least 3 characters, must start with a letter (a-z), and contain only letters (a-z) numbers (0-9) and underscores (_).");
            return;
        }

        if (config.getUsername() != null) {
            UserDetails userDetails = new UserDetails();
            userDetails.setUsername(config.getUsername());
            userDetails.setPayment_address(wallet.getPaymentAddress());
            userDetails.setIs_app(true);
            if (config.getName() != null) {
                userDetails.setName(config.getName());
            } else {
                userDetails.setName(config.getUsername());
            }
            String avatar = config.getAvatar();
            Boolean try_upload_avatar = false;
            // if the variable points to a URL
            if (avatar != null) {
                // make sure the URI is valid
                try {
                    URI check = URI.create(avatar);
                    if (check.getScheme() != null && (check.getScheme() == "http" || check.getScheme() == "https")) {
                        userDetails.setAvatar(avatar);
                    } else {
                        // TODO: if scheme == "file", change `avatar` to match
                        try_upload_avatar = true;
                    }
                } catch (IllegalArgumentException e) {
                    try_upload_avatar = true;
                }
            }

            Response<User> getResponse = idService.getApi().getUser(wallet.getOwnerAddress()).execute();
            final long ts = idService.getApi().getTimestamp().execute().body().get();
            final long ts_shift = System.currentTimeMillis() / 1000 - ts;
            Response<User> res;
            if (getResponse.code() == 404) {
                res = idService.getApi().registerUser(userDetails, System.currentTimeMillis() / 1000 + ts_shift).execute();
            } else {
                res = idService.getApi().updateUser(wallet.getOwnerAddress(), userDetails, System.currentTimeMillis() / 1000 + ts_shift).execute();
            }

            if (res.isSuccessful()) {
                System.out.println("Registered with ID service as '"+config.getUsername()+"'");
                if (try_upload_avatar) {
                    // TODO: this is a bit brute forcy, would be nice
                    // to not have to do this everytime the bot starts
                    final File file = new File(avatar);
                    if (file.exists()) {
                        final String mimeType = URLConnection.guessContentTypeFromName(file.getName());
                        MediaType mediaType;
                        if (mimeType != null) {
                            mediaType = MediaType.parse(mimeType);
                            final RequestBody requestFile = RequestBody.create(mediaType, file);
                            final MultipartBody.Part body = MultipartBody.Part.createFormData("Profile-Image-Upload", file.getName(), requestFile);
                            Response<User> upload_res = idService.getApi().uploadFile(body, System.currentTimeMillis() / 1000 + ts_shift).execute();
                            if (upload_res.code() == 200) {
                                System.out.println("Successfully updated avatar");
                            } else {
                                System.out.println("WARNING: Failed to updated avatar!");
                            }
                        } else {
                            System.out.println("WARNING: Invalid avatar mimetype");
                        }

                    } else {
                        System.out.println("WARNING: unable to process avatar '" + avatar + "'. Not a valid URL or File");
                    }
                }
            } else {
                logger.log(Level.SEVERE, "Failed to register with ID service: "+res.code()+" - "+res.errorBody().string());
                // abort if registration fails
                return;
            }
        }

        // -- rpc
        HeadlessRPC rpc = new HeadlessRPC(jedisPool, wallet.getOwnerAddress(), ethService);


        final RedisSubscriber subscriber = new RedisSubscriber(rpc, m);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Jedis subscriberJedis = new Jedis(config.getRedis().getUri());
                try {
                    subscriberJedis.subscribe(subscriber, wallet.getOwnerAddress(), wallet.getOwnerAddress()+"_rpc_request");
                    System.out.println("Subscription ended.");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Subscribing failed.", e);
                }
            }
        }).start();

        // -- signal
        if (m.userExists()) {
            try {
                m.init();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading state file \"" + m.getFileName() + "\"");
                return;
            }
        }

        //register
        if (!m.userHasKeys()) {
            m.createNewIdentity();
            try {
                m.createEthereumAccount(wallet);
            } catch (IOException e) {
                System.err.println("Verify error: " + e.getMessage());
                return;
            }
        }


        if (!m.isRegistered()) {
            System.err.println("User is not registered.");
            return;
        }
        double timeout = 5;
        boolean returnOnTimeout = false;
        boolean ignoreAttachments = true;
        try {
            m.receiveMessages((long) (timeout * 1000), TimeUnit.MILLISECONDS, returnOnTimeout, ignoreAttachments, new ReceiveMessageHandler(m, jedisPool, wallet.getOwnerAddress()));
        } catch (IOException e) {
            System.err.println("Error while receiving messages: " + e.getMessage());
            return;
        } catch (AssertionError e) {
            handleAssertionError(e);
            return;
        }


        // -- cleanup
        jedisPool.destroy();

    }

    private static void handleAssertionError(AssertionError e) {
        System.err.println("Failed to send/receive message (Assertion): " + e.getMessage());
        e.printStackTrace();
        System.err.println("If you use an Oracle JRE please check if you have unlimited strength crypto enabled, see README");
    }


}
