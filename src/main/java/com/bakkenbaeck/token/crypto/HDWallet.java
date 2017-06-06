package com.bakkenbaeck.token.crypto;

//import android.content.Context;
//import android.content.SharedPreferences;

//import com.bakkenbaeck.token.R;

import com.tokenbrowser.crypto.hdshim.EthereumKeyChainGroup;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.tokenbrowser.exception.InvalidMasterSeedException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Arrays;

import static com.bakkenbaeck.token.crypto.util.HashUtil.sha3;

//import com.bakkenbaeck.token.util.LogUtil;
//import com.bakkenbaeck.token.view.BaseApplication;

public class HDWallet {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final String MASTER_SEED = "ms";
    //private SharedPreferences prefs;
    private ECKey identityKey;
    private ECKey paymentKey;
    private String masterSeed;

    public HDWallet init(String seed) {
        initWallet(seed);
        return this;
    }

    private HDWallet initWallet(String seed) {
        this.masterSeed = seed;
        final Wallet wallet = this.masterSeed == null
                ? generateNewWallet()
                : initFromMasterSeed(masterSeed);

        deriveKeysFromWallet(wallet);

        return this;
    }

    private Wallet generateNewWallet() {
        final Wallet wallet = new Wallet(NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
        final DeterministicSeed seed = wallet.getKeyChainSeed();
        this.masterSeed = seedToString(seed);

        return wallet;
    }

    private Wallet initFromMasterSeed(final String masterSeed) {
        try {
            final DeterministicSeed seed = getSeed(masterSeed);
            seed.check();
            return constructFromSeed(seed);
        } catch (final UnreadableWalletException | MnemonicException e) {
            throw new RuntimeException("Unable to create wallet. Seed is invalid");
        }
    }

    private Wallet constructFromSeed(final DeterministicSeed seed) {
        return new Wallet(getNetworkParameters(),  new EthereumKeyChainGroup(getNetworkParameters(), seed));
    }

    private DeterministicSeed getSeed(final String masterSeed) throws UnreadableWalletException {
        return new DeterministicSeed(masterSeed, null, "", 0);
    }

    private NetworkParameters getNetworkParameters() {
        return NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
    }

    private void deriveKeysFromWallet(final Wallet wallet) {
        try {
            deriveIdentityKey(wallet);
            deriveReceivingKey(wallet);
        } catch (final UnreadableWalletException | IOException ex) {
            throw new RuntimeException("Error deriving keys: " + ex);
        }
    }

    private void deriveIdentityKey(final Wallet wallet) throws IOException, UnreadableWalletException {
        this.identityKey = deriveKeyFromWallet(wallet, 0, KeyChain.KeyPurpose.AUTHENTICATION);
    }

    private void deriveReceivingKey(final Wallet wallet) throws IOException, UnreadableWalletException {
        this.paymentKey = deriveKeyFromWallet(wallet, 0, KeyChain.KeyPurpose.RECEIVE_FUNDS);
    }

    private ECKey deriveKeyFromWallet(final Wallet wallet, final int iteration, final KeyChain.KeyPurpose keyPurpose) throws UnreadableWalletException, IOException {
        DeterministicKey key = null;
        for (int i = 0; i <= iteration; i++) {
            key = wallet.freshKey(keyPurpose);
        }

        if (key == null) {
            throw new IOException("Unable to derive key");
        }

        return ECKey.fromPrivate(key.getPrivKey());
    }

    public String signIdentity(final String data) {
        return sign(data.getBytes(), this.identityKey);
    }

    public String signTransaction(final String data) {
        try {
            final byte[] transactionBytes = TypeConverter.StringHexToByteArray(data);
            return sign(transactionBytes, this.paymentKey);
        } catch (final Exception e) {
            System.err.println("Unable to sign transaction. " + e);
            return null;
        }
    }

    private String sign(final byte[] bytes, final ECKey key) {
        final byte[] msgHash = sha3(bytes);
        final ECKey.ECDSASignature signature = key.sign(msgHash);
        return signature.toHex();
    }

    public String getMasterSeed() {
        return this.masterSeed;
    }

    private String getPrivateKey() {
        return Hex.toHexString(this.identityKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.identityKey.getPubKey());
    }

    public String getOwnerAddress() {
        if (identityKey != null) {
            return TypeConverter.toJsonHex(this.identityKey.getAddress());
        }
        return null;
    }

    public String getPaymentAddress() {
        if (this.paymentKey != null) {
            return TypeConverter.toJsonHex(this.paymentKey.getAddress());
        }
        return null;
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getOwnerAddress();
    }

    private String seedToString(final DeterministicSeed seed) {
        final StringBuilder sb = new StringBuilder();
        for (final String word : seed.getMnemonicCode()) {
            sb.append(word).append(" ");
        }

        // Remove the extraneous space character
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
