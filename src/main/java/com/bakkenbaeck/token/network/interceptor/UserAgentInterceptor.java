package com.bakkenbaeck.token.network.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor() {
        this.userAgent = "Token Headless Client - 1.0";
    }
    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request original = chain.request();
        final Request request = original.newBuilder()
                .header("User-Agent", this.userAgent)
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
