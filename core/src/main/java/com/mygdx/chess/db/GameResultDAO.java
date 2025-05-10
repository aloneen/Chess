package com.mygdx.chess.db;

import java.sql.*;

public class GameResultDAO {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/chess";
    private static final String USER = "postgres";
    private static final String PASS = "1234";

    public static void saveGameResult(String winner, String botSide, String gameMode) {
        String sql = "INSERT INTO game_results (winner, bot_side, game_mode) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, winner);
            stmt.setString(2, botSide);
            stmt.setString(3, gameMode);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
