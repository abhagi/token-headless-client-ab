package com.bakkenbaeck.token.headless.signal;

import com.bakkenbaeck.token.crypto.HDWallet;
import okhttp3.*;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.internal.push.AccountAttributes;
import org.whispersystems.signalservice.internal.push.SignalServiceUrl;
import org.whispersystems.signalservice.internal.util.DynamicCredentialsProvider;
import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class TokenSignalServiceAccountManager {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final DynamicCredentialsProvider credentialsProvider;

    public TokenSignalServiceAccountManager(SignalServiceUrl[] urls,
                                       String user, String password, int deviceId,
                                       String userAgent)
    {
        this.credentialsProvider = new DynamicCredentialsProvider(user, password, null, deviceId);
    }

    public void createEthereumAccount(HDWallet wallet, SignalServiceUrl serviceUrl, String signalingKey, int registrationId, boolean voice, boolean fetchesMessages) throws IOException {
        AccountAttributes accountAttributes = new AccountAttributes(signalingKey, registrationId, voice, fetchesMessages);
        String json = JsonUtil.toJson(accountAttributes);
        String url = serviceUrl.getUrl()+"/v1/accounts/";
        final String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        final String idAddress = wallet.getOwnerAddress();

        String verb = "PUT";
        String path = "/v1/accounts/";

        final Keccak.DigestKeccak keccak = new Keccak.Digest256();
        keccak.update(json.getBytes());
        byte[] hash = keccak.digest();
        byte[] encodedHashBytes = Base64.encodeBytesToBytes(hash);
        String encodedHash = new String(encodedHashBytes);
        String payload = verb+"\n"+path+"\n"+timestamp+"\n"+encodedHash;
        final String signature = wallet.signIdentity(payload);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(JSON, json))
                .header("Authorization", getAuthorizationHeader())
                .header("Token-Timestamp", timestamp)
                .header("Token-Signature", signature)
                .header("Token-ID-Address", idAddress)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
    }

    private String getAuthorizationHeader() {
        try {
            if(credentialsProvider.getDeviceId() == SignalServiceAddress.DEFAULT_DEVICE_ID) {
                return "Basic " + org.whispersystems.signalservice.internal.util.Base64.encodeBytes((credentialsProvider.getUser() + ":" + credentialsProvider.getPassword()).getBytes("UTF-8"));
            } else {
                return "Basic " + org.whispersystems.signalservice.internal.util.Base64.encodeBytes((credentialsProvider.getUser() + "." + credentialsProvider.getDeviceId() + ":" + credentialsProvider.getPassword()).getBytes("UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
