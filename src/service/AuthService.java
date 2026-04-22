/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.util.List;
import model.User;
import util.MD5Util;


/**
 *
 * @author Ali
 */
public class AuthService {
     public static boolean signUp(int id, String firstName, String lastName, String email, String password) {

        List<User> users = FileManager.loadUsers();

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        String encryptedPassword = MD5Util.encrypt(password);
        User newUser = new User(id, firstName, lastName, email, encryptedPassword);
        FileManager.saveUser(newUser);

        return true;
    }

    public static User findUserByEmail(String email) {
        List<User> users = FileManager.loadUsers();

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }

        return null;
    }

    public static boolean checkPassword(User user, String password) {
        String encryptedPassword = MD5Util.encrypt(password);
        return user.getPasswordHash().equals(encryptedPassword);
    }
}
