package playerstoragev2.util;

import playerstoragev2.PlayerStorage;
import playerstoragev2.sql.Input;
import playerstoragev2.storage.Loader;
import playerstoragev2.storage.StorageCell;

public class defaultLoader implements Loader {

    @Override
    public boolean onLoad(StorageCell cell, Input data) {
        // primary check
        if (!data.hasData()) {
            return false;
        }
        defaultCell Dcel = (defaultCell) cell;
        // secondary check
        if (data.getLong("id") == null) {
            // load default value
            Dcel.name = "default";
            PlayerStorage.debug("loading default..");
            return true;
        }

        Dcel.name = data.getString("name");
        PlayerStorage.debug("loading normal..");
        PlayerStorage.debug(Dcel.name);
        return true;

    }

    @Override
    public String onSave(StorageCell cell) {
        defaultCell Dcel = (defaultCell) cell;
        PlayerStorage.debug("[default loader] unloading...");
        return "name='" + Dcel.name + "'";
    }

    @Override
    public String getPluginName() {
        return "test";
    }

    @Override
    public String onInit() {

        return "name VARCHAR(16)";
    }

    @Override
    public String data() {
        return "name";
    }

    @Override
    public String value(StorageCell cell) {
        defaultCell Dcel = (defaultCell) cell;
        return "'" + Dcel.name + "'";
    }

}
