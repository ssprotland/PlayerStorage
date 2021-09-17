package playerstoragev2;

import org.bukkit.configuration.file.FileConfiguration;

import playerstoragev2.mongodb.mdbStettings;
import playerstoragev2.sql.Conn;

public class Config {
    public void load(FileConfiguration config, Conn sql, mdbStettings Mdb) {

        sql.host = config.getString("SQL.host");
        sql.port = config.getString("SQL.port");
        sql.database = config.getString("SQL.database");
        sql.username = config.getString("SQL.username");
        sql.password = config.getString("SQL.password");

        Mdb.host = config.getString("mongoDB.host");
        Mdb.port = config.getString("mongoDB.port");
        Mdb.database = config.getString("mongoDB.database");
        Mdb.username = config.getString("mongoDB.username");
        Mdb.password = config.getString("mongoDB.password");

        PlayerStorage.getInstance().debug = config.getBoolean("debug");
        PlayerStorage.getInstance().loadOnJoin = config.getBoolean("loadOnJoin");
    }
}
