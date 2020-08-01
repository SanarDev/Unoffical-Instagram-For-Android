package com.idirect.app.datasource.model.subscribers;

import java.util.UUID;

public class GraphQLSubBaseOptions {

    private String subscriptionId = UUID.randomUUID().toString();
    private boolean clientLogged;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public boolean isClientLogged() {
        return clientLogged;
    }

    public void setClientLogged(boolean clientLogged) {
        this.clientLogged = clientLogged;
    }
}
