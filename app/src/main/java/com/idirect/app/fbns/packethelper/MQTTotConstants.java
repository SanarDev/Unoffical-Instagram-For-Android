package com.idirect.app.fbns.packethelper;

public class MQTTotConstants {

    public class MQTToTConnectionData{
        public static final short CLIENT_IDENTIFIER = 1;
        public static final short WILL_TOPIC = 2;
        public static final short WILL_MESSAGE = 3;
        public static final short CLIENT_INFO = 4;
        public static final short PASSWORD = 5;

        // POLYFILL
        public static final short UNKNOWN = 5;
        public static final short GET_DIFFS_REQUESTS = 6;
        public static final short ZERO_RATING_TOKEN_HASH = 9;
        public static final short APP_SPECIAL_INFO = 10;
    }

    public class MQTTotConnectionClientInfo{
        public static final short USER_ID = 1;
        public static final short USER_AGENT = 2;
        public static final short CLIENT_CAPABILITIES = 3;
        public static final short END_POINT_CAPABILITIES = 4;
        public static final short PUBLISH_FORMAT = 5;
        public static final short NO_AUTOMATIC_FOREGROUND = 6;
        public static final short MAKE_USER_AVAILABLE_IN_FOREGROUND = 7;
        public static final short DEVICE_ID = 8;
        public static final short IS_INITIALLY_FOREGROUND = 9;
        public static final short NETWORK_TYPE = 10;
        public static final short NETWORK_SUBTYPE = 11;
        public static final short CLIENT_MQTT_SESSION_ID = 12;
        public static final short CLIENT_IP_ADDRESS = 13;
        public static final short SUBSCRIBE_TOPICS = 14;
        public static final short CLIENT_TYPE = 15;
        public static final short APP_ID = 16;
        public static final short OVERRIDE_NECTAR_LOGGING = 17;
        public static final short CONNECT_TOKEN_HASH = 18;
        public static final short REGION_PREFERENCE = 19;
        public static final short DEVICE_SECRET = 20;
        public static final short CLIENT_STACK = 21;
        public static final short FBNS_CONNECTION_KEY = 22;
        public static final short FBNS_CONNECTION_SECRET = 23;
        public static final short FBNS_DEVICE_ID = 24;
        public static final short FBNS_DEVICE_SECRET = 25;
        public static final short ANOTHER_UNKNOWN = 26;
    }

    public class ForegroundStateConfig{
        public static final short IN_FOREGROUND_APP = 1;
        public static final short IN_FOREGROUND_DEVICE = 2;
        public static final short KEEP_ALIVE_TIME_OUT = 3;
        public static final short SUBSCRIBE_TOPICS = 4;
        public static final short SUBSCRIBE_GENERIC_TOPICS = 5;
        public static final short UNSUBSCRIBE_TOPICS = 6;
        public static final short UNSUBSCRIBE_GENERIC_TOPICS = 7;
        public static final short REQUEST_ID = 8;
    }

}
