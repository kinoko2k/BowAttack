package net.kinoko2k.bowAttack.database;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQL {
    private Connection connection;
    private final JavaPlugin plugin;

    public MySQL(JavaPlugin plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("mysql.host", "localhost");
        int port = config.getInt("mysql.port", 3306);
        String database = config.getString("mysql.database", "BowAttack");
        String user = config.getString("mysql.user", "bowattacker");
        String password = config.getString("mysql.password", "bowbow");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";

        try {
            connection = DriverManager.getConnection(url, user, password);
            plugin.getLogger().info("DBに接続しました！");
        } catch (SQLException e) {
            plugin.getLogger().severe("DBに接続できませんでした！");
            e.printStackTrace();
        }
    }

    public List<String> getTopPlayers(String category) {
        List<String> topPlayers = new ArrayList<>();
        String column = category.equalsIgnoreCase("wins") ? "wins" : "total_games";

        String query = "SELECT mcid, " + column + " FROM player_stats ORDER BY " + column + " DESC LIMIT 5";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            int rank = 1;
            while (resultSet.next()) {
                String mcid = resultSet.getString("mcid");
                int value = resultSet.getInt(column);
                topPlayers.add(rank + ". " + mcid + " - " + value);
                rank++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }

    public void updatePlayerStats(String mcid, String uuid) {
        try {
            String checkQuery = "SELECT wins, total_games FROM player_stats WHERE uuid = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, uuid);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                int currentWins = resultSet.getInt("wins");
                int totalGames = resultSet.getInt("total_games");

                String updateQuery = "UPDATE player_stats SET wins = ?, total_games = ? WHERE uuid = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, currentWins + 1);
                updateStmt.setInt(2, totalGames + 1);
                updateStmt.setString(3, uuid);
                updateStmt.executeUpdate();
            } else {
                String insertQuery = "INSERT INTO player_stats (mcid, uuid, wins, total_games) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, mcid);
                insertStmt.setString(2, uuid);
                insertStmt.setInt(3, 1);
                insertStmt.setInt(4, 1);
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementTotalGames(String mcid, String uuid) {
        String query = "INSERT INTO player_stats (mcid, uuid, total_games) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE total_games = total_games + 1";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, mcid);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("DB接続を閉じました。");
            } catch (SQLException e) {
                plugin.getLogger().severe("DB接続を閉じるのに失敗しました。");
                e.printStackTrace();
            }
        }
    }
}