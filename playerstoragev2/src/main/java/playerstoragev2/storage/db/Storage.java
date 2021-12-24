package playerstoragev2.storage.db;

import playerstoragev2.storage.PlayerS;

public interface Storage {

    public String info(String player);

    public PlayerS load(String player);

    public void save(PlayerS player);

}