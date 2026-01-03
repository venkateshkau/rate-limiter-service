package com.vk.ratelimiter;

public class Constants {
    private Constants() {}
    public static final int PORT;
    public static final String HOST;
    static {
        PORT = AppConfig.getInt("server.port", 8080);
        if(PORT < 1 || PORT > 65535) { throw new RuntimeException("Invalid port number " + PORT + ". Valid range is 1..65535"); }
        HOST = AppConfig.getString("server.host", "0.0.0.0");
    }
}
