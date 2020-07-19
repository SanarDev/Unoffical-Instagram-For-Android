package com.sanardev.instagrammqtt.datasource.model.event;

public class ConnectionStateEvent {

    public ConnectionStateEvent(State connection) {
        this.connection = connection;
    }

    private State connection;

    public State getConnection() {
        return connection;
    }

    public void setConnection(State connection) {
        this.connection = connection;
    }

    public enum State{
        CONNECTED,
        CONNECTING,
        NETWORK_DISCONNECTED,
        NETWORK_CONNECTION_RESET,
        CHANNEL_DISCONNECTED
    }
}
