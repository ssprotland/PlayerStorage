package playerstoragev2.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import playerstoragev2.PlayerStorage;
import playerstoragev2.storage.Storage;

public class LeaveEvent implements Listener {
    Storage storage;

    public LeaveEvent(Storage stor) {
        storage = stor;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // unload player
        String playerName = event.getPlayer().getName();
        PlayerStorage.log("unloading...");
        // save player asynchornisly
        Bukkit.getScheduler().runTaskAsynchronously(PlayerStorage.getInstance(), new Runnable() {
            @Override
            public void run() {
                storage.unload(playerName);
            }
        });
    }
}