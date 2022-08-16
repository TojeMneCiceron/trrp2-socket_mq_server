package ru.psu.movs.trrp.socketmq.server;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class CacheServer {
    public static void main(String[] args) throws IOException, TimeoutException {
        CacheServerService service = new CacheServerService();
        CacheServerController controller = new CacheServerController(service);
        controller.run();
    }
}
