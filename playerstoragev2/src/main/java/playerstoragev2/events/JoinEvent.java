package playerstoragev2.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import playerstoragev2.PlayerStorage;
import playerstoragev2.storage.db.Storage;

public class JoinEvent implements Listener {
    Storage storage;

    public JoinEvent(Storage stor) {
        storage = stor;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // load player form storage
        String playerName = event.getPlayer().getName();
        PlayerStorage.log("loading players data...");
        // load player asynchornisly
        Bukkit.getScheduler().runTaskAsynchronously(PlayerStorage.getInstance(), new Runnable() {
            @Override
            public void run() {
                storage.load(playerName);
            }
        });
    }
}