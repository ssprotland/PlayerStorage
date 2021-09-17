package playerstoragev2.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import playerstoragev2.PlayerStorage;

public class Input {
    ResultSet sqlResult;
    Boolean hasData;

    public Input(ResultSet res) {
        sqlResult = res;
        try {
            sqlResult.next();
            PlayerStorage.debug("Input ok!!");
            hasData = true;
        } catch (SQLException | NullPointerException e) {
            // e.printStackTrace();
            hasData = false;
            PlayerStorage.debug("Input err!!");
        }
    }

    public Boolean hasData() {
        return hasData;
    }

    public String getString(String name) {
        try {
            return sqlResult.getString(name);
        } catch (SQLException | NullPointerException e) {
        }
        return null;
    }

    public Long getLong(String name) {
        try {
            return sqlResult.getLong(name);
        } catch (SQLException | NullPointerException e) {
        }
        return null;
    }

    public Integer getInt(String name) {
        try {
            return sqlResult.getInt(name);
        } catch (SQLException e) {
        }
        return null;
    }

    public Boolean getBool(String name) {
        try {
            return sqlResult.getBoolean(name);
        } catch (SQLException e) {
        }
        return null;
    }
}
