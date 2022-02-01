package xyz.kyngs.hostify.monitor.common;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class HostifyMonitor {

    // Might use a better server later
    private final HttpServer server;

    public HostifyMonitor(InformationProvider provider) throws IOException {

        server = HttpServer.create(new InetSocketAddress(1337), 0);

        server.setExecutor(Executors.newCachedThreadPool());

        var informationContext = server.createContext("/hostify");

        informationContext.setHandler(exchange -> {
            var object = new JsonObject();

            provider.populate(object);

            var string = object.toString();

            try {
                exchange.sendResponseHeaders(200, string.getBytes().length);

                var responseStream = exchange.getResponseBody();

                responseStream.write(string.getBytes(StandardCharsets.UTF_8));

                responseStream.close();
            } catch (IOException e) {
                System.err.println("Failed to process data request");
                e.printStackTrace();
            }

        });

        server.start();
    }

    public void stop() {
        server.stop(2);
    }

}
