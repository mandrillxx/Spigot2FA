package me.mandrillxx.spigot2fa.util;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.*;

public class UtilDatabase {

    private Connection connection = null;
    private final File dataFolder;

    public UtilDatabase(File dataFolder) {
        this.dataFolder = dataFolder;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            createTable();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        Preconditions.checkArgument(connection != null, "SQLite must be connected!");

        String sql = "CREATE TABLE IF NOT EXISTS \"codes\" (\"uuid\" varchar(50) NOT NULL, \"code\" varchar(255) NOT NULL);";

        try {
            Statement stmt = getConnection().createStatement();;

            stmt.executeUpdate(sql);

            stmt.close();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
