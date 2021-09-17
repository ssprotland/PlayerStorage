package playerstoragev2.storage;

import java.lang.reflect.Field;

import playerstoragev2.PlayerStorage;
import playerstoragev2.mongodb.mdbStettings;
import playerstoragev2.sql.Input;
import playerstoragev2.sql.sqlHandler;

@Deprecated
public class oldStorage {
    sqlHandler sql;
    mdbStettings Mdb;

    public oldStorage(sqlHandler sq, mdbStettings mdb) {
        this.sql = sq;
        this.Mdb = mdb;
    }

    public void load(String playerName) {

        PlayerStorage.debug("===========loading=========");
        PlayerS player = new PlayerS();
        // get ansver from database

        sql.openConnection();
        Input result;
        // get player id
        Long playerID;
        // if cant find player create new
        result = sql.query("SELECT * FROM playerStorage_main WHERE PlayerName='" + playerName + "';");
        playerID = result.getLong("id");
        if (playerID == null) {
            playerID = createUser(playerName);
        }
        PlayerStorage.debug("player id: " + playerID.toString());
        final long playerid = playerID;

        PlayerStorage.getLoaders().forEach((loader) -> {
            // find cell with plugin name as loader
            String pluginName = loader.getPluginName();
            PlayerStorage.debug("plugin name: " + pluginName);

            Input results = sql.query("SELECT * FROM playerStorage_" + pluginName + " WHERE id=" + playerid + ";");

            // if loader cant find required table, or record about player is not found,
            // try to create new table if not exist, elsewhere only saving can help
            if (!loader.onLoad(player.getStorageCell(pluginName), results)) {
                // sql.closeConnection();
                createTable(loader);
                // sql.openConnection();
                results = sql.query("SELECT * FROM playerStorage_" + pluginName + " WHERE id=" + playerid + ";");
                loader.onLoad(player.getStorageCell(pluginName), results);
            }
        });
        // save player
        PlayerStorage.getInstance().players.put(playerName, player);
        sql.closeConnection();
        PlayerStorage.debug("===========loading done!!=========");
    }

    public void unload(String playerName) {
        PlayerS player = PlayerStorage.getPlayer(playerName);
        PlayerStorage.debug("===========unloading=========");
        sql.openConnection();

        // get player id
        Long playerID;
        // if cant find player create new
        Input result = sql.query("SELECT * FROM playerStorage_main WHERE PlayerName='" + playerName + "';");
        playerID = result.getLong("id");
        if (playerID == null) {
            playerID = createUser(playerName);
        }
        PlayerStorage.debug("player id: " + playerID.toString());
        final long playerid = playerID;

        PlayerStorage.getLoaders().forEach((loader) -> {
            String pluginName = loader.getPluginName();
            String pID = Long.toString(playerid) + ", ";
            StorageCell cell = player.getStorageCell(pluginName);
            if (cell == null) {
                return;
            }
            String data = loader.value(cell);
            String replacement = loader.onSave(player.getStorageCell(pluginName));

            sql.update("INSERT INTO playerStorage_" + pluginName + " (id, " + loader.data() + ") VALUES (" + pID + data
                    + ") ON DUPLICATE KEY UPDATE " + replacement + ";");

        });
        // remove record
        PlayerStorage.getInstance().players.remove(playerName);
        sql.closeConnection();
        PlayerStorage.debug("===========unloading done!!=========");
    }

    public long createUser(String playerName) {
        //
        // sql.openConnection();
        // create new record in main table
        sql.update("INSERT INTO playerStorage_main (PlayerName) VALUES ('" + playerName + "');");

        // get player id
        Long playerID;
        // if cant find player create new
        Input result = sql.query("SELECT * FROM playerStorage_main WHERE PlayerName='" + playerName + "';");
        playerID = result.getLong("id");
        if (playerID == null) {
            // wery wrong
            PlayerStorage.debug("wery wrong!!!  ");
        }
        // sql.closeConnection();
        return playerID;
    }

    public void createTable(Loader loader) {
        // sql.openConnection();

        sql.update("CREATE TABLE IF NOT EXISTS playerStorage_" + loader.getPluginName() + " ( id INT UNSIGNED, "
                + loader.onInit() + ", PRIMARY KEY (id));");

        // sql.closeConnection();
    }

    class Variables {
        public String names = "";
        public String types = "";
        public String values = "";
        public String nAndt = "";
        public String nAndv = "";
    }

    Variables getVariables(Class<?> cell) {

        Field[] fields = cell.getDeclaredFields();
        Variables result = new Variables();

        for (Field f : fields) {

            result.names += f.getName() + ","; // name1,name2
            result.types += f.getType() + ","; // type1,type2, result.nAndt += f.getName() + " "; //name1 type1,name2
                                               // type2,
            result.nAndv += f.getName() + "="; // name1='string',name2=10,

            String type = f.getType().getName(); // check for type name
            try {

                f.setAccessible(true);
                if (type.equalsIgnoreCase("Integer")) {
                    result.nAndt += "INT,";
                    result.values += f.getInt(cell) + ",";
                    result.nAndv += f.getInt(cell) + ",";

                } else if (type.equalsIgnoreCase("Long")) {
                    result.nAndt += "INT UNSIGNED,";
                    result.values += f.getLong(cell) + ",";
                    result.nAndv += f.getLong(cell) + ",";

                } else if (type.equalsIgnoreCase("String")) {
                    result.nAndt += "VARCHAR(256),";
                    result.values += "'" + (String) f.get(cell) + "',"; // get string == 'somestring',
                    result.nAndv += "'" + (String) f.get(cell) + "',";

                } else if (type.equalsIgnoreCase("Boolean")) {

                    result.nAndt += "BOOLEAN,";
                    result.values += f.getBoolean(cell) + ",";
                    result.nAndv += f.getBoolean(cell) + ",";
                }
            } catch (Exception e) {
                PlayerStorage.debug("reflection error!");
                e.printStackTrace();
            }
        }
        return result;
    }
}