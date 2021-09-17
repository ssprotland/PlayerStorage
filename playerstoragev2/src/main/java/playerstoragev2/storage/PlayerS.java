package playerstoragev2.storage;

import java.util.HashMap;

import playerstoragev2.PlayerStorage;

public class PlayerS {

    HashMap<String, StorageCell> data;

    PlayerS() {

        data = new HashMap<String, StorageCell>();

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
