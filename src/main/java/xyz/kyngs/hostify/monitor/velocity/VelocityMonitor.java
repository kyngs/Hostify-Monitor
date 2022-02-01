package xyz.kyngs.hostify.monitor.velocity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import xyz.kyngs.hostify.monitor.common.HostifyMonitor;
import xyz.kyngs.hostify.monitor.common.InformationProvider;

import java.io.IOException;

@Plugin(
        id = "hostify-monitor",
        name = "Hostify Monitor",
        version = "0.1.0",
        description = "Hostify Monitor for Velocity",
        authors = {"kyngs"}
)
public class VelocityMonitor implements InformationProvider {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer server;
    private HostifyMonitor monitor;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            monitor = new HostifyMonitor(this);
        } catch (IOException e) {
            logger.error("Failed to open HTTP server, perhaps the port is already occupied?");
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        monitor.stop();
    }

    @Override
    public void populate(JsonObject object) {
        var players = server.getAllPlayers();
        var runtime = Runtime.getRuntime();

        var playerArray = new JsonArray();

        for (Player player : players) {
            playerArray.add(player.getUsername());
        }

        var pluginArray = new JsonArray();

        for (PluginContainer container : server.getPluginManager().getPlugins()) {
            pluginArray.add(container.getDescription().getId());
        }

        var playerObject = new JsonObject();

        playerObject.addProperty("online", players.size());
        playerObject.addProperty("max", server.getConfiguration().getShowMaxPlayers());
        playerObject.add("current", playerArray);

        var memoryObject = new JsonObject();

        memoryObject.addProperty("free", runtime.freeMemory());
        memoryObject.addProperty("max", runtime.maxMemory());
        memoryObject.addProperty("total", runtime.totalMemory());
        memoryObject.addProperty("used", runtime.totalMemory() - runtime.freeMemory());

        object.addProperty("tps", 20);
        object.addProperty("version", server.getVersion().getVersion());
        object.add("players", playerObject);
        object.add("memory", memoryObject);
        object.add("plugins", pluginArray);

    }
}
