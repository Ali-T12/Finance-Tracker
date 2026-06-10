package service;

import model.User;
import repository.UserRepository;
import util.MD5Util;
import util.Session;

public class AuthService {

    private static final UserRepository userRepository = new UserRepository();

    // ================= SIGN UP =================
    public static boolean signUp(String firstName, String lastName, String email, String password) {

        if (userRepository.findByEmail(email) != null) {
            return false;
        }

        String encryptedPassword = MD5Util.encrypt(password);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(encryptedPassword);

        return userRepository.add(user) != null;
    }

    // ================= LOGIN =================
    public static boolean login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return false;
        }

        if (!checkPassword(user, password)) {
            return false;
        }

        Session.currentUser = user;
        return true;
    }

    // ================= PASSWORD CHECK =================
    public static boolean checkPassword(User user, String password) {

        if (user == null) return false;

        String encryptedPassword = MD5Util.encrypt(password);

        return user.getPasswordHash().equals(encryptedPassword);
    }

    // ================= FIND USER =================
    public static User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ================= LOGOUT =================
    public static void logout() {
        Session.currentUser = null;
    }
}