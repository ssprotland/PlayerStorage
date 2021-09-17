package playerstoragev2;

import org.bukkit.configuration.file.FileConfiguration;

import playerstoragev2.mongodb.mdbStettings;
public class Config {
    public void load(FileConfiguration config, mdbStettings Mdb) {

        Mdb.host = config.getString("mongoDB.host");
        Mdb.port = config.getString("mongoDB.port");
        Mdb.database = config.getString("mongoDB.database");
        Mdb.username = config.getString("mongoDB.username");
        Mdb.password = config.getString("mongoDB.password");

        PlayerStorage.getInstance().debug = config.getBoolean("debug");
        PlayerStorage.getInstance().loadOnJoin = config.getBoolean("loadOnJoin");
    }
}
