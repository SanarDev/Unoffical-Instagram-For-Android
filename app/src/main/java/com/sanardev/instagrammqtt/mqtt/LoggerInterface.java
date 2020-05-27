package com.sanardev.instagrammqtt.mqtt;

import java.lang.reflect.Array;

public interface LoggerInterface {

    public void emergency(String message, Array context);
    public void emergency(String message);

    public void alert(String message, Array context);
    public void alert(String message);

    public void critical(String message, Array context);
    public void critical(String message);

    public void error(String message, Array context);
    public void error(String message);

    public void warning(String message, Array context);
    public void warning(String message);

    public void notice(String message, Array context);
    public void notice(String message);

    public void info(String message, Array context);
    public void info(String message);

    public void debug(String message, Array $context);
    public void debug(String message);

    public void log(Object level, String message, Object context);
    public void log(Object level, String message);
}
