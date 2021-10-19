package playerstoragev2.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import org.bson.Document;

import playerstoragev2.PlayerStorage;
import playerstoragev2.mongodb.mdbStettings;

public class Storage {

    mdbStettings Mdb;

    public Storage(mdbStettings mdb) {

        this.Mdb = mdb;
    }

    // returns all data about user
    public String info(String name) {
        String info = "";

        PlayerS player = PlayerStorage.getPlayer(name);

        for (String cellName : player.getStorageCells().keySet()) {
            StorageCell cell = player.getStorageCell(cellName);
            info += cellName + ": ";

            Document doc = new Document();
            serialize(cell.getClass().getFields(), cell, doc);

            info += doc.toJson() + "\n";
        }
        return info;
    }

    public void load(String playerName) {

        PlayerStorage.debug("===========loading=========");
        PlayerS player = new PlayerS();

        // get db url
        String connectionString = "mongodb://" + Mdb.username + ":" + Mdb.password + "@" + Mdb.host + ":" + Mdb.port
                + "/?authSource=" + Mdb.database;

        // connect to db
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString))) {

            // get database
            MongoDatabase db = mongoClient.getDatabase(Mdb.database);
            // get players colection
            MongoCollection<Document> players = db.getCollection("players");

            FindIterable<Document> result = players.find(new Document("name", playerName));
            Document playerDoc = result.first();
            if (playerDoc == null) {
                // create new playerDoc
                playerDoc = new Document("name", playerName);
            }

            PlayerStorage.debug(playerDoc.toJson());
            final Document playerDocF = playerDoc;

            // foreach storage cell
            player.getStorageCells().forEach((pluginName, storagecell) -> {
                final Document cellDoc = (Document) playerDocF.get(pluginName);

                Field[] fields = storagecell.getClass().getDeclaredFields();

                // unserialization(with recursion)
                deSerialize(fields, storagecell, cellDoc);

            });
            mongoClient.close();
        }
        // return logging

        // save player
        PlayerStorage.getInstance().players.put(playerName, player);

        PlayerStorage.debug("===========loading done!=========");

    }

    public void unload(String playerName) {
        PlayerS player = PlayerStorage.getPlayer(playerName);
        PlayerStorage.debug("=============unloading===========");

        // get db url
        String connectionString = "mongodb://" + Mdb.username + ":" + Mdb.password + "@" + Mdb.host + ":" + Mdb.port
                + "/?authSource=" + Mdb.database;

        // connect to db
        try (MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString))) {

            // get database
            MongoDatabase db = mongoClient.getDatabase(Mdb.database);
            // get players colection
            MongoCollection<Document> players = db.getCollection("players");

            // generate player's document
            Document playerDoc = new Document();
            playerDoc.append("name", playerName);
            // for each storage cell create coresponding document

            player.getStorageCells().forEach((pluginName, storagecell) -> {

                Document data = new Document();
                Field[] fields = storagecell.getClass().getDeclaredFields();

                serialize(fields, storagecell, data);

                // append to player's document
                playerDoc.append(pluginName, data);
            });
            PlayerStorage.debug(playerDoc.toJson());
            // do insert in to db
            ReplaceOptions update = new ReplaceOptions();
            update = update.upsert(true);
            players.replaceOne(new Document("name", playerName), playerDoc, update);
            mongoClient.close();
        }

        // remove record
        PlayerStorage.getInstance().players.remove(playerName);
        PlayerStorage.debug("===========unloading done!========");
    }

    @SuppressWarnings("unchecked")
    void deSerialize(Field[] fields, StorageCell cell, Document doc) {

        for (Field f : fields) {
            f.setAccessible(true);
            try {

                // PlayerStorage.debug(doc.get(f.getName()).getClass().getName());

                PlayerStorage.debug(f.getName());
                // check for basic types
                if (f.getType().equals(int.class)) {
                    // integer can be null(somehow?) and throw exeption
                    getFromDocOrSet(f, cell, doc, 0);

                } else if (f.getType().equals(long.class)) {
                    // long can be null(somehow?) and throw exeption
                    getFromDocOrSet(f, cell, doc, 0L);

                } else if (f.getType().equals(float.class)) {
                    Double doub = (Double) doc.getOrDefault(f.getName(), 0.0);
                    f.set(cell, doub.floatValue()); // mongodb only stores double

                } else if (f.getType().equals(double.class)) {
                    getFromDocOrSet(f, cell, doc, 0.0);

                } else if (f.getType().equals(boolean.class)) {
                    getFromDocOrSet(f, cell, doc, false);

                } else if (f.getType().equals(String.class)) {
                    // string can be null, but map does not allow that
                    try {
                        String tmpstr = doc.getString(f.getName());
                        if (tmpstr == null) {
                            tmpstr = "";
                        }
                        f.set(cell, tmpstr);
                    } catch (Exception e) {
                        f.set(cell, "");
                    }

                } else if (f.get(cell) instanceof StorageCell) {

                    StorageCell innerCell = (StorageCell) f.get(cell);
                    Field[] innerFields = innerCell.getClass().getDeclaredFields();

                    // document can be null
                    try {
                        Document innerDoc = (Document) doc.get(f.getName());
                        PlayerStorage.debug("recursion!");
                        deSerialize(innerFields, innerCell, innerDoc);// recursion!
                        PlayerStorage.debug("exit!");
                    } catch (Exception e) {
                    }

                } else if (f.get(cell) instanceof ArrayList) {

                    // array thats needs to be filled up
                    ArrayList<Object> array = (ArrayList<Object>) f.get(cell);

                    // check type of object that is contained in arraylist
                    if (!(array.get(0) instanceof StorageCell)) {
                        if (array.get(0) instanceof Number || array.get(0) instanceof String) {
                            array.clear();// remove temporary element
                            try {

                                PlayerStorage.debug(doc.toString());
                                array = (ArrayList<Object>) doc.get(f.getName());// use embeded deserializer
                                f.set(cell, array);

                            } catch (Exception e) {
                                PlayerStorage.debug(e.toString());
                            }
                        }
                        continue;
                    }

                    // document list that represent objects
                    List<Document> list = (List<Document>) doc.get(f.getName());
                    // list can be null on first load
                    if (list == null) {
                        continue;
                    }
                    PlayerStorage.debug(list.toString());
                    PlayerStorage.debug(array.get(0).getClass().getName());

                    Class<?> clazz = array.get(0).getClass();
                    array.clear();
                    for (Document innerDoc : list) {
                        StorageCell innerCell = (StorageCell) clazz.getDeclaredConstructor().newInstance();

                        Field[] innerFields = innerCell.getClass().getDeclaredFields();
                        deSerialize(innerFields, innerCell, innerDoc);
                        array.add(innerCell);
                    }

                }
            } catch (Exception e) {
                PlayerStorage.debug("reflection error!");
                e.printStackTrace();
            }
        }

    }

    void getFromDocOrSet(Field f, StorageCell cell, Document doc, Object def) {
        try {
            f.set(cell, doc.get(f.getName()));
        } catch (Exception e) {
            try {
                f.set(cell, def);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    void serialize(Field[] fields, StorageCell cell, Document doc) {

        for (Field f : fields) {
            f.setAccessible(true);
            try {

                PlayerStorage.debug(f.getName());
                // check for basic types
                if (f.getType().equals(int.class)) {
                    doc.append(f.getName(), f.get(cell));

                } else if (f.getType().equals(long.class)) {
                    doc.append(f.getName(), f.get(cell));

                } else if (f.getType().equals(float.class)) {
                    doc.append(f.getName(), f.get(cell));

                } else if (f.getType().equals(double.class)) {
                    doc.append(f.getName(), f.get(cell));

                } else if (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class)) {
                    Boolean bool = (Boolean) f.get(cell);
                    if (bool == null) {
                        bool = false;
                    }

                    doc.append(f.getName(), bool.booleanValue());

                } else if (f.getType().equals(String.class)) {
                    doc.append(f.getName(), f.get(cell));

                } else if (f.get(cell) instanceof StorageCell) {
                    PlayerStorage.debug("recursion!");

                    StorageCell innerCell = (StorageCell) f.get(cell);
                    Field[] innerFields = innerCell.getClass().getDeclaredFields();
                    Document innerDoc = new Document();

                    serialize(innerFields, innerCell, innerDoc);// recursion
                    PlayerStorage.debug("exit!");

                    doc.append(f.getName(), innerDoc); // append created document after recursion

                } else if (f.get(cell) instanceof ArrayList) {
                    PlayerStorage.debug("Array!");

                    List<Object> array = (ArrayList<Object>) f.get(cell);
                    PlayerStorage.debug("size of array: " + array.size());

                    if (array.isEmpty()) {
                        doc.append(f.getName(), f.get(cell));// use embeded serializer(just create empty array)
                        PlayerStorage.debug("Empty array!");
                        continue;
                    }

                    if (!(array.get(0) instanceof StorageCell)) {
                        // check if array contains numbers or strings
                        if (array.get(0) instanceof Number || array.get(0) instanceof String) {
                            doc.append(f.getName(), f.get(cell));// use embeded serializer
                            PlayerStorage.debug("contains strings or numbers!");
                        }
                        PlayerStorage.debug("cant be serialized");
                        continue; // skip cuz this cant be serialized
                    }
                    PlayerStorage.debug("contains StorageCell");
                    // create document list
                    List<Document> docList = new ArrayList<Document>();
                    // recursivly serialize
                    array.forEach(innerCell -> {
                        Document innerDoc = new Document();
                        Field[] innerFields = innerCell.getClass().getDeclaredFields();

                        serialize(innerFields, (StorageCell) innerCell, innerDoc);
                        docList.add(innerDoc);
                    });
                    doc.append(f.getName(), docList);

                    // }

                }

            } catch (Exception e) {
                PlayerStorage.debug("reflection error!");
                e.printStackTrace();
            }
        }
    }
}
