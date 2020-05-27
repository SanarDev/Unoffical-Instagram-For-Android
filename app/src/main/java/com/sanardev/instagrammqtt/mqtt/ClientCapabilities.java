package com.sanardev.instagrammqtt.mqtt;

public class ClientCapabilities {

    public static int ACKNOWLEDGED_DELIVERY = 0;
    public static int PROCESSING_LASTACTIVE_PRESENCEINFO = 1;
    public static int EXACT_KEEPALIVE = 2;
    public static int REQUIRES_JSON_UNICODE_ESCAPES = 3;
    public static int DELTA_SENT_MESSAGE_ENABLED = 4;
    public static int USE_ENUM_TOPIC = 5;
    public static int SUPPRESS_GETDIFF_IN_CONNECT = 6;
    public static int USE_THRIFT_FOR_INBOX = 7;
    public static int USE_SEND_PINGRESP = 8;
    public static int REQUIRE_REPLAY_PROTECTION = 9;
    public static int DATA_SAVING_MODE = 10;
    public static int TYPING_OFF_WHEN_SENDING_MESSAGE = 11;
    public static int PERMISSION_USER_AUTH_CODE = 12;
    public static int FBNS_EXPLICIT_DELIVERY_ACK = 13;
    public static int IS_LARGE_PAYLOAD_SUPPORTED = 14;

    public static int DEFAULT_SET = 0
        | 1 << ACKNOWLEDGED_DELIVERY
        | 1 << PROCESSING_LASTACTIVE_PRESENCEINFO
        | 1 << EXACT_KEEPALIVE
        | 0 << REQUIRES_JSON_UNICODE_ESCAPES
        | 1 << DELTA_SENT_MESSAGE_ENABLED
        | 1 << USE_ENUM_TOPIC
        | 0 << SUPPRESS_GETDIFF_IN_CONNECT
        | 1 << USE_THRIFT_FOR_INBOX
        | 0 << USE_SEND_PINGRESP
        | 0 << REQUIRE_REPLAY_PROTECTION
        | 0 << DATA_SAVING_MODE
        | 0 << TYPING_OFF_WHEN_SENDING_MESSAGE
        | 0 << PERMISSION_USER_AUTH_CODE
        | 0 << FBNS_EXPLICIT_DELIVERY_ACK
        | 0 << IS_LARGE_PAYLOAD_SUPPORTED;

}
