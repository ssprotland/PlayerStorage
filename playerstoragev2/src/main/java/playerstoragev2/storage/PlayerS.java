package playerstoragev2.storage;

import java.util.HashMap;

import playerstoragev2.PlayerStorage;

public class PlayerS {

    HashMap<String, StorageCell> data;
    String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerS() {

        data = new HashMap<String, StorageCell>();

        // generate new storage cels
        PlayerStorage.getStorageCells().forEach((name, cell) -> {
            try {
                data.put(name, (StorageCell) cell.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StorageCell getStorageCell(String name) {
        return data.get(name);
    }

    public HashMap<String, StorageCell> getStorageCells() {
        return data;
    }
}
