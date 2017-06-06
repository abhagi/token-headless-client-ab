package com.bakkenbaeck.token.network;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.network.interceptor.LoggingInterceptor;
import com.bakkenbaeck.token.network.interceptor.SigningInterceptor;
import com.bakkenbaeck.token.network.interceptor.UserAgentInterceptor;
import com.squareup.moshi.Moshi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class IdService {

    private final IdInterface idInterface;
    private final OkHttpClient.Builder client;
    private final HDWallet wallet;
    public IdInterface getApi() {
        return this.idInterface;
    }

    public IdService(HDWallet wallet, String baseUrl) {
        this.wallet = wallet;

        //final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();

        addUserAgentHeader();
        addSigningInterceptor();
        addLogging();

        final Moshi moshi = new Moshi.Builder().build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                //.addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.idInterface = retrofit.create(IdInterface.class);
    }

    private void addUserAgentHeader() {
        this.client.addInterceptor(new UserAgentInterceptor());
    }

    private void addSigningInterceptor() {
        this.client.addInterceptor(new SigningInterceptor(this.wallet));
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.client.addInterceptor(interceptor);
    }
}
