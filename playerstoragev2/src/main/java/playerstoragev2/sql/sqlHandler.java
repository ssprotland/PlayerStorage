package playerstoragev2.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import playerstoragev2.PlayerStorage;

public class sqlHandler {

    private String host, port, database, username, password;
    private Connection connection;
    private Statement statement;

    public sqlHandler(Conn conn) throws ClassNotFoundException {
        host = conn.host;
        port = conn.port;
        database = conn.database;
        username = conn.username;
        password = conn.password;
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    public void createIfNot() {
        openConnection();
        update("CREATE TABLE IF NOT EXISTS playerStorage_main (PlayerName VARCHAR(16) NOT NULL, id INT AUTO_INCREMENT, PRIMARY KEY (id));");
        closeConnection();
    }

    public void openConnection() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Input query(String sql) {
        try {
            PlayerStorage.debug(sql);
            return new Input(statement.executeQuery(sql));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlayerStorage.debug("sql err");
        return new Input(null);
    }

    public void update(String sql) {
        try {
            PlayerStorage.debug(sql);
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void migrate() {

        try {
            ResultSet result = statement.executeQuery("SELECT * FROM playerStorage_main;");
            while (result.next()) {
                PlayerStorage.getPlayer(result.getString("PlayerName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
