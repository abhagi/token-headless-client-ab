package com.bakkenbaeck.token.model.local;

public class SendState {

    public @interface State {}

    public static final int STATE_SENDING = 0;
    public static final int STATE_SENT = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_RECEIVED = 3;
    public static final int STATE_LOCAL_ONLY = 4;
}
