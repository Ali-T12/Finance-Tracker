/**
 *علي حمال اسعيد 120220484  
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;
import util.DatabaseConnection;
import util.MD5Util;

public class AuthService {

    public static boolean signUp(int id, String firstName, String lastName, String email, String password) {
        if (findUserByEmail(email) != null) {
            return false; 
        }

        String query = "INSERT INTO users (first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)";
        String encryptedPassword = MD5Util.encrypt(password);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, encryptedPassword);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User findUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkPassword(User user, String password) {
        String encryptedPassword = MD5Util.encrypt(password);
        return user.getPasswordHash().equals(encryptedPassword);
    }
}