package repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.Category;
import model.Transaction;
import model.User;
import util.Session;

public class TransactionRepository {

    EntityManagerFactory emf;
    EntityManager em;

    public TransactionRepository() {
        emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
                em = emf.createEntityManager();

    }

    public Transaction add(int categoryId, double amount, String type, String date) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            User currentUser = em.getReference(User.class, Session.currentUser.getId());

            Category category = findCategoryForCurrentUser(categoryId);

            if (category == null) {
                System.out.println("Category not found for current user.");
                return null;
            }

            Category managedCategory = em.getReference(Category.class, category.getId());

            Transaction transaction = new Transaction(
                    0,
                    currentUser,
                    managedCategory,
                    amount,
                    type,
                    date
            );

            em.getTransaction().begin();
            em.persist(transaction);
            em.getTransaction().commit();

            return transaction;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return null;

        } finally {
        }
    }

    public Transaction update(int transactionId, int categoryId, double amount, String type, String date) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            Transaction transaction = findById(transactionId);

            if (transaction == null) {
                return null;
            }

            Category category = findCategoryForCurrentUser(categoryId);

            if (category == null) {
                System.out.println("Category not found for current user.");
                return null;
            }

            em.getTransaction().begin();

            Transaction managedTransaction = em.find(Transaction.class, transactionId);
            Category managedCategory = em.getReference(Category.class, category.getId());

            if (managedTransaction == null) {
                em.getTransaction().rollback();
                return null;
            }

            managedTransaction.setCategory(managedCategory);
            managedTransaction.setAmount(amount);
            managedTransaction.setType(type);
            managedTransaction.setDate(date);

            em.getTransaction().commit();

            return managedTransaction;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return null;

        } finally {
        }
    }

    public boolean delete(int id) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return false;
            }

            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId",
                    Transaction.class
            );

            query.setParameter("id", id);
            query.setParameter("userId", Session.currentUser.getId());

            List<Transaction> transactions = query.getResultList();

            if (transactions.isEmpty()) {
                return false;
            }

            Transaction transaction = transactions.get(0);

            em.getTransaction().begin();
            em.remove(transaction);
            em.getTransaction().commit();

            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
        }
    }

    public Transaction findById(int id) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId",
                    Transaction.class
            );

            query.setParameter("id", id);
            query.setParameter("userId", Session.currentUser.getId());

            List<Transaction> transactions = query.getResultList();

            if (transactions.isEmpty()) {
                return null;
            }

            return transactions.get(0);

        } finally {
        }
    }

    public List<Transaction> findAll() {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId",
                    Transaction.class
            );

            query.setParameter("userId", Session.currentUser.getId());

            return query.getResultList();

        } finally {
        }
    }

    public List<Transaction> findByType(String type) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type",
                    Transaction.class
            );

            query.setParameter("userId", Session.currentUser.getId());
            query.setParameter("type", type);

            return query.getResultList();

        } finally {
        }
    }

    private Category findCategoryForCurrentUser(int categoryId) {
        EntityManager tempEm = emf.createEntityManager();

        try {
            TypedQuery<Category> query = tempEm.createQuery(
                    "SELECT c FROM Category c WHERE c.id = :categoryId AND c.user.id = :userId",
                    Category.class
            );

            query.setParameter("categoryId", categoryId);
            query.setParameter("userId", Session.currentUser.getId());

            List<Category> categories = query.getResultList();

            if (categories.isEmpty()) {
                return null;
            }

            return categories.get(0);

        } finally {
            tempEm.close();
        }
    }
}