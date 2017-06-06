package com.bakkenbaeck.token.network;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.adapter.BigIntegerAdapter;
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

public class BalanceService {
    private final BalanceInterface balanceInterface;
    private final OkHttpClient.Builder client;

    public BalanceService(HDWallet wallet, String baseUrl) {
        //final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory
        //        .createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();
        this.client.addInterceptor(new UserAgentInterceptor());
        this.client.addInterceptor(new SigningInterceptor(wallet));

        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        this.client.addInterceptor(interceptor);

        final Moshi moshi = new Moshi.Builder()
                                    .add(new BigIntegerAdapter())
                                    .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client.build())
                .build();
        this.balanceInterface = retrofit.create(BalanceInterface.class);
    }

    public BalanceInterface getApi() {
        return this.balanceInterface;
    }
}
