package service;

import model.User;
import repository.UserRepository;
import util.MD5Util;
import util.Session;

public class AuthService {

    public static boolean signUp(String firstName, String lastName, String email, String password) {
        UserRepository userRepository = new UserRepository();

        if (userRepository.findByEmail(email) != null) {
            return false;
        }

        String encryptedPassword = MD5Util.encrypt(password);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(encryptedPassword);

        User addedUser = userRepository.add(user);

        return addedUser != null;
    }

    public static User findUserByEmail(String email) {
        UserRepository userRepository = new UserRepository();
        return userRepository.findByEmail(email);
    }

    public static boolean checkPassword(User user, String password) {
        if (user == null) {
            return false;
        }

        String encryptedPassword = MD5Util.encrypt(password);
        return user.getPasswordHash().equals(encryptedPassword);
    }

    public static boolean login(String email, String password) {
        UserRepository userRepository = new UserRepository();

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

    public static void logout() {
        Session.currentUser = null;
    }
}
