package com.bakkenbaeck.token.network;

import com.bakkenbaeck.token.model.network.Addresses;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import io.reactivex.Single;

public interface BalanceInterface {

    @POST("/v1/tx/skel")
    Call<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @POST("/v1/tx")
    Call<SentTransaction> sendSignedTransaction(
            @Query("timestamp") long timestamp,
            @Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Call<Balance> getBalance(@Path("id") String walletAddress);

    @GET("/v1/timestamp")
    Call<ServerTime> getTimestamp();


    @POST("/v1/register")
    Call<Void> startWatchingAddresses(
            @Query("timestamp") long timestamp,
            @Body Addresses addresses);

}
