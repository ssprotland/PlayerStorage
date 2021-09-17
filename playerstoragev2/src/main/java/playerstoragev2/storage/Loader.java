package playerstoragev2.storage;

import playerstoragev2.sql.Input;

@Deprecated
public interface Loader {

    /**
     * @return name of plugin that created this loader
     */
    public String getPluginName();

    /**
     * method that maps input to cell. Use "data.getLong("id") == null" to check if
     * record for this loader exist
     */
    public boolean onLoad(StorageCell cell, Input input);

    /**
     * @return string that contains variable name and it value: playername='test',
     *         playerid=123. variable name must be same as in onLoad() function
     */
    public String onSave(StorageCell cell);

    /**
     * @return string that contains name of all variables and it types: playername
     *         VARCHAR(16), playerid INT, variable name must be same as in onLoad()
     *         and onSave() functions
     */
    public String onInit();

    /**
     * @return string that contains name of all variables: player name, id. must be
     *         the same as in onInit()
     */
    public String data();

    /**
     * @return string that contains value of all variables: 1,'test'. mus be in same
     *         order as in data()
     */
    public String value(StorageCell cell);

}
