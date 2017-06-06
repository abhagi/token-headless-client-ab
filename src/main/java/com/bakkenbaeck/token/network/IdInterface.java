package com.bakkenbaeck.token.network;


import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.UserDetails;
import com.bakkenbaeck.token.model.network.UserSearchResults;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;

public interface IdInterface {

    @GET("/v1/timestamp")
    Call<ServerTime> getTimestamp();

    @POST("/v1/user")
    Call<User> registerUser(@Body UserDetails details,
                              @Query("timestamp") long timestamp);

    @GET("/v1/user/{id}")
    Call<User> getUser(@Path("id") String userId);

    @PUT("/v1/user/{id}")
    Call<User> updateUser(@Path("id") String userId,
                            @Body UserDetails details,
                            @Query("timestamp") long timestamp);

    @GET("/v1/search/user")
    Call<UserSearchResults> searchByUsername(@Query("query") String username);

    @Multipart
    @PUT("v1/user")
    Call<User> uploadFile(@Part MultipartBody.Part file,
                            @Query("timestamp") long timestamp);
}
