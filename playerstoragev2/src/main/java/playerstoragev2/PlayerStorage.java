package playerstoragev2;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import playerstoragev2.events.JoinEvent;
import playerstoragev2.events.LeaveEvent;
import playerstoragev2.sql.Conn;
import playerstoragev2.sql.sqlHandler;
import playerstoragev2.storage.Loader;
import playerstoragev2.storage.PlayerS;
import playerstoragev2.storage.Storage;
import playerstoragev2.storage.oldStorage;
import playerstoragev2.util.addCell;
import playerstoragev2.util.defaultCell;
import playerstoragev2.mongodb.antilogger;
import playerstoragev2.mongodb.mdbStettings;
//import java.util.logging.Logger;
//import java.util.logging.Level;

public class PlayerStorage extends JavaPlugin {

    public HashMap<String, PlayerS> players;
    ArrayList<Loader> loaders;
    HashMap<String, Class<?>> storageCells;

    private static PlayerStorage instance;

    sqlHandler sql;
    protected Storage storage;
    protected playerstoragev2.storage.oldStorage oldStorage;

    boolean debug = true;
    boolean loadOnJoin = false;

    Config configuration;
    Conn connection;
    mdbStettings Mdb;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        getLogger().info("launching......");
        instance = this;
        // init variables
        players = new HashMap<String, PlayerS>();
        storageCells = new HashMap<String, Class<?>>();
        loaders = new ArrayList<Loader>();

        // diffrent plugin settings (sql,...)
        saveDefaultConfig();
        configuration = new Config();

        // disable mongodb logger
        antilogger.disableMbdLogging();

        // sql connection
        connection = new Conn();
        Mdb = new mdbStettings();

        // load configuration from file
        configuration.load(getConfig(), connection, Mdb);

        // init sql connection
        try {
            sql = new sqlHandler(connection);
            sql.createIfNot();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // players loader
        storage = new Storage(sql, Mdb);
        oldStorage = new oldStorage(sql, Mdb);

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
                    //sql.openConnection();
                    //sql.migrate();
                    //sql.closeConnection();

                    PlayerStorage.getInstance().players.forEach((name, player) -> {

                        Player Pplayer = Bukkit.getPlayer(name);
                        if (Pplayer == null) {
                            PlayerStorage.getInstance().storage.unload(name);
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

    void testing() {
        debug("testing...");
        storage.load("test");

        defaultCell cell = (defaultCell) getOnlinePlayer("test").getStorageCell("test");

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

        storage.unload("test");
        debug("done!");
    }

    @Override
    public void onDisable() {
        log("[disable] disabeling......");
        // save all
        // when last element is removed, then foreach will throw exeption
        log("[disable] saving...");
        try {
            PlayerStorage.getInstance().players.forEach((name, player) -> {
                PlayerStorage.getInstance().storage.unload(name);
            });
        } catch (Exception e) {
            log("[disable] done!");
        }
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

    @Deprecated
    public static void addLoader(Loader loader) {
        getInstance().loaders.add(loader);
    }

    public static void addStorageCell(Class cell, String pluginName) {
        // add storage cell for each player
        getInstance().storageCells.put(pluginName, cell);
    }

    @Deprecated
    public static ArrayList<Loader> getLoaders() {
         return getInstance().loaders;
        
    }

    public static void registerStorageCell(Class cell, String pluginName) {
        // add storage cell for each player
        getInstance().storageCells.put(pluginName, cell);
    }

    public static HashMap<String, Class<?>> getStorageCells() {
        return getInstance().storageCells;
    }

    /**
     * returns player. if not already loaded, load from storage. Do not use if u
     * dont need offline player!
     * 
     * @param name of player
     * @return player
     */
    public static PlayerS getPlayer(String name) {
        // try to load player
        PlayerS player = getInstance().players.get(name);
        // if player isnt loaded, then player will be null
        if (player == null) {
            // load player from database(or create new one)
            getInstance().storage.load(name);
            player = getInstance().players.get(name);
        }
        return player;
    }

    @Deprecated
    public static PlayerS oldgetPlayer(String name) {
        // try to load player
        PlayerS player = getInstance().players.get(name);
        // if player isnt loaded, then player will be null
        if (player == null) {
            // load player from database(or create new one)
            getInstance().oldStorage.load(name);
            player = getInstance().players.get(name);
        }
        return player;
    }

    /**
     * returns player. if not already loaded, returns null. Always try to use this
     * methot!
     * 
     * @param name of player
     * @return player
     */
    public static PlayerS getOnlinePlayer(String name) {
        return getInstance().players.get(name);
    }

    @FunctionalInterface
    public interface foreach {
        public void load(String name, PlayerS player);
    }

    public static void forEachPlayer(foreach loader) {
        getInstance().players.forEach((name, player) -> {
            loader.load(name, player);
        });

    }

    Storage getStorage() {
        return storage;
    }

    static void reloadCfg() {
        getInstance().configuration.load(getInstance().getConfig(), getInstance().connection, getInstance().Mdb);
    }

}