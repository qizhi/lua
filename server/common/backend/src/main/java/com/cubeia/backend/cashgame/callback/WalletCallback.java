package com.cubeia.backend.cashgame.callback;

public interface WalletCallback {

    void requestSucceeded(Object response);

    void requestFailed(Object response);
}
