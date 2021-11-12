package playerstoragev2;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import playerstoragev2.events.JoinEvent;
import playerstoragev2.events.LeaveEvent;
import playerstoragev2.storage.PlayerS;
import playerstoragev2.storage.db.Storage;
import playerstoragev2.storage.db.mongodb;
import playerstoragev2.util.addCell;
import playerstoragev2.util.defaultCell;
import playerstoragev2.mongodb.antilogger;
import playerstoragev2.mongodb.mdbStettings;
//import java.util.logging.Logger;
//import java.util.logging.Level;

public class PlayerStorage extends JavaPlugin {

    public HashMap<String, PlayerS> cache;

    HashMap<String, Class<?>> storageCells;

    private static PlayerStorage instance;

    private static Storage storage;

    boolean debug = true;
    boolean loadOnJoin = false;

    Config configuration;

    mdbStettings Mdb;

    @Override
    public void onEnable() {
        getLogger().info("launching......");
        instance = this;
        // init variables
        cache = new HashMap<String, PlayerS>();
        storageCells = new HashMap<String, Class<?>>();

        // diffrent plugin settings (sql,...)
        saveDefaultConfig();
        configuration = new Config();

        // disable mongodb logger
        antilogger.disableMbdLogging();

        Mdb = new mdbStettings();

        // load configuration from file
        configuration.load(getConfig(), Mdb);

        // players loader
        storage = new mongodb(Mdb);

        // registerStorageCell(defaultCell.class, "test");
        // register events
        if (loadOnJoin) {
            Bukkit.getPluginManager().registerEvents(new JoinEvent(storage), this);
            Bukkit.getPluginManager().registerEvents(new LeaveEvent(storage), this);
        }

        // register commands
        CommandAPI.registerCommand(cmd.class);

        // register saver (unload all players each half an hour)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), new Runnable() {
            @Override
            public void run() {
                debug("[saver] unloading..");
                try {
                    // migration from sql to mariadb
                    // sql.openConnection();
                    // sql.migrate();
                    // sql.closeConnection();

                    PlayerStorage.getInstance().cache.forEach((name, player) -> {
                        Player Pplayer = Bukkit.getPlayer(name);
                        if (Pplayer == null) {
                            PlayerStorage.save(player);
                        }
                    });
                } catch (Exception e) {
                    debug("[saver] done!");
                }
            }
            // t/s*t/m x30
        }, 200, 20 * 60 * 30);
        // storage.loadV2("test2");
        // testing();
    }

    private void testing() {
        debug("testing...");
        PlayerS player = getPlayer("test");
        defaultCell cell = (defaultCell) player.getStorageCell("test");

        cell.array.forEach(element -> {
            debug(Integer.toString(element));
        });

        cell.array = new ArrayList<Integer>();
        cell.array.add(1);
        cell.array.add(2);
        cell.array.add(3);
        cell.addcell.add = "secondary cell";

        addCell addcell = new addCell();

        addcell.add = "1";
        addcell.doub = 2;
        addcell.flot = 3;
        // cell.array2.add(addcell);

        addcell = new addCell();
        addcell.add = "4";
        addcell.doub = 5;
        addcell.flot = 6;
        // cell.array2.add(addcell);

        storage.save(player);
        debug("done!");
    }

    @Override
    public void onDisable() {
        log("[disable] disabeling......");
        // save all
        // when last element is removed, then foreach will throw exeption
        log("[disable] saving...");
        PlayerStorage.getInstance().cache.forEach((name, player) -> {
            PlayerStorage.save(player);
        });

        log("[disable] done!");
    }

    public static void log(String log) {
        Bukkit.getLogger().info("[playerStorageV2] " + log);
    }

    public static void debug(String log) {
        if (getInstance().debug) {
            Bukkit.getLogger().info("[playerStorageV2][debug] " + log);
        }
    }

    public static PlayerStorage getInstance() {
        return instance;
    }

    public static void addStorageCell(Class<?> cell, String pluginName) {
        // add storage cell for each player
        getInstance().storageCells.put(pluginName, cell);
    }

    public static void registerStorageCell(Class<?> cell, String pluginName) {
        // add storage cell for each player
        getInstance().storageCells.put(pluginName, cell);
    }

    public static HashMap<String, Class<?>> getStorageCells() {
        return getInstance().storageCells;
    }

    // =========================API==============================
    /**
     * 
     * @param name
     * @return
     */
    public static String info(String name) {
        return storage.info(name);
    }

    /**
     * returns player. if not already loaded, load from storage. Do not use if u
     * dont need offline player!
     * 
     * @param name - name of player
     * @return player
     */
    public static PlayerS getPlayer(String name) {
        // try to load player
        PlayerS player = getInstance().cache.get(name);
        // if player isnt loaded, then player will be null
        if (player == null) {
            // load player from database(or create new one)
            player = storage.load(name);
        }
        return player;
    }

    /**
     * returns player. if not already loaded, returns null. Always try to use this
     * methot!
     * 
     * @param name - name of player
     * @return player
     */
    public static PlayerS getOnlinePlayer(String name) {
        return getInstance().cache.get(name);
    }

    /**
     * loads player (or creates new one) from db and saves it in cache
     * 
     * @param name - name of player to load
     */
    public static void load(String name) {
        PlayerS player = getInstance().cache.get(name);
        if (player == null) {
            // load player from database(or create new one)
            player = storage.load(name);
            getInstance().cache.put(name, player);
        }
    }

    /**
     * saves player by name
     * 
     * @param name - name of player to save
     */
    public static void save(String name) {
        PlayerS player = getInstance().cache.get(name);
        storage.save(player);
    }

    /**
     * saves player
     * 
     * @param player - player to save
     */
    public static void save(PlayerS player) {
        storage.save(player);
    }

    /**
     * save player to db and remove from cache
     * 
     * @param player
     */

    public static void unload(PlayerS player) {
        storage.save(player);
        getInstance().cache.remove(player.getName());
    }

    // =======================================================
    @FunctionalInterface
    public interface foreach {
        public void load(String name, PlayerS player);
    }

    public static void forEachPlayer(foreach loader) {
        getInstance().cache.forEach((name, player) -> {
            loader.load(name, player);
        });

    }

    Storage getStorage() {
        return storage;
    }

    static void reloadCfg() {
        getInstance().configuration.load(getInstance().getConfig(), getInstance().Mdb);
    }

}