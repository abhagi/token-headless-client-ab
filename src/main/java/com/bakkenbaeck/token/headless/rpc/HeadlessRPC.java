package com.bakkenbaeck.token.headless.rpc;

import com.bakkenbaeck.token.headless.EthService;
import com.bakkenbaeck.token.headless.rpc.entities.*;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.TokenError;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class HeadlessRPC {
    private JedisPool jedisPool;
    private String address;
    private EthService ethService;

    public HeadlessRPC(JedisPool jedisPool, String address, EthService ethService) {
        this.jedisPool = jedisPool;
        this.address = address;
        this.ethService = ethService;
    }

    public void handleRequest(HeadlessRPCRequest request) {
        if (request != null) {
            try {
                switch (request.getMethod()) {
                    case "ping":
                        ping(request);
                        break;
                    case "sendTransaction":
                        sendTransaction(request);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleResult(HeadlessRPCRequest request, HeadlessRPCResult result, HeadlessRPCError error) throws IOException {
        HeadlessRPCResponse response = new HeadlessRPCResponse();
        response.setId(request.getId());
        response.setResult(result);
        response.setError(error);
        ObjectMapper mapper = new ObjectMapper();
        String message = mapper.writeValueAsString(response);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(address + "_rpc_response", message);
        }
    }

    private void ping(HeadlessRPCRequest request) throws IOException {
        handleResult(request, new PingResult("Java client pong!"), null);
    }

    private void sendTransaction(HeadlessRPCRequest request) throws IOException {
        HeadlessRPCError error = new HeadlessRPCError();
        error.setMessage("Unknown ETH error");
        error.setCode(0);

        Response<UnsignedTransaction> response = ethService.createTransaction(request.getParams().get("to"), request.getParams().get("value"));
        if (!response.isSuccessful()) {
            error.setMessage(response.errorBody().string());
            error.setCode(response.code());
            handleResult(request, null, error);
        } else {
            String utx = response.body().getTransaction();
            SentTransaction tx = ethService.sendTransaction(utx);
            if (tx != null) {
                List<TokenError> errors = tx.getErrors();
                if (errors != null && errors.size() > 0) {
                    error.setMessage(errors.get(0).getMessage());
                    handleResult(request, null, error);
                } else {
                    handleResult(request, new SendTransactionResult(tx.getTxHash()), null);
                }
            } else {
                handleResult(request, null, error);
            }
        }
    }

}