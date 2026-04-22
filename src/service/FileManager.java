package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Transaction;
import model.User;

public class FileManager {

    private static final String USERS_FILE = "src/data/users.txt";

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    int id = Integer.parseInt(parts[0]);
                    String firstName = parts[1];
                    String lastName = parts[2];
                    String email = parts[3];
                    String passwordHash = parts[4];

                    users.add(new User(id, firstName, lastName, email, passwordHash));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static void saveUser(User user) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            bw.write(user.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();
        File file = new File("src/data/categories.txt");

        if (!file.exists()) {
            return categories;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    categories.add(new Category(id, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public static void saveCategories(List<Category> categories) {
        File file = new File("src/data/categories.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Category c : categories) {
                bw.write(c.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File("src/data/transactions.txt");

        if (!file.exists()) {
            return transactions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    int id = Integer.parseInt(parts[0]);
                    int userId = Integer.parseInt(parts[1]);
                    int categoryId = Integer.parseInt(parts[2]);
                    double amount = Double.parseDouble(parts[3]);
                    String type = parts[4];
                    String date = parts[5];

                    transactions.add(new Transaction(id, userId, categoryId, amount, type, date));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public static void saveTransactions(List<Transaction> transactions) {
        File file = new File("src/data/transactions.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Transaction t : transactions) {
                bw.write(t.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
