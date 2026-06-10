package repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.Category;
import model.Transaction;
import model.User;
import util.Session;

public class TransactionRepository {

    private EntityManagerFactory emf;

    public TransactionRepository() {
        this.emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
    }

    // ===================== ADD (ASYNC) =====================
    public void addAsync(int categoryId, double amount, String type, String date) {

        new Thread(() -> {

            EntityManager em = emf.createEntityManager();

            try {
                if (Session.currentUser == null) {
                    System.out.println("No user logged in.");
                    return;
                }

                User currentUser = em.getReference(User.class, Session.currentUser.getId());

                Category category = findCategoryForCurrentUser(categoryId);

                if (category == null) {
                    System.out.println("Category not found.");
                    return;
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

                System.out.println("Transaction added ✔");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
            } finally {
                em.close();
            }

        }).start();
    }

    // ===================== UPDATE (ASYNC) =====================
    public void updateAsync(int transactionId, int categoryId, double amount, String type, String date) {

        new Thread(() -> {

            EntityManager em = emf.createEntityManager();

            try {
                if (Session.currentUser == null) return;

                Transaction managedTransaction = em.find(Transaction.class, transactionId);

                if (managedTransaction == null) return;

                Category category = findCategoryForCurrentUser(categoryId);

                if (category == null) return;

                em.getTransaction().begin();

                Category managedCategory = em.getReference(Category.class, category.getId());

                managedTransaction.setCategory(managedCategory);
                managedTransaction.setAmount(amount);
                managedTransaction.setType(type);
                managedTransaction.setDate(date);

                em.getTransaction().commit();

                System.out.println("Transaction updated ✔");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
            } finally {
                em.close();
            }

        }).start();
    }

    // ===================== DELETE (ASYNC) =====================
    public void deleteAsync(int id) {

        new Thread(() -> {

            EntityManager em = emf.createEntityManager();

            try {
                if (Session.currentUser == null) return;

                TypedQuery<Transaction> query = em.createQuery(
                        "SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId",
                        Transaction.class
                );

                query.setParameter("id", id);
                query.setParameter("userId", Session.currentUser.getId());

                List<Transaction> list = query.getResultList();

                if (list.isEmpty()) return;

                Transaction transaction = list.get(0);

                em.getTransaction().begin();
                em.remove(transaction);
                em.getTransaction().commit();

                System.out.println("Transaction deleted ✔");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
            } finally {
                em.close();
            }

        }).start();
    }

    // ===================== FIND BY ID (SYNC) =====================
    public Transaction findById(int id) {

        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId",
                    Transaction.class
            );

            query.setParameter("id", id);
            query.setParameter("userId", Session.currentUser.getId());

            List<Transaction> list = query.getResultList();

            return list.isEmpty() ? null : list.get(0);

        } finally {
            em.close();
        }
    }

    // ===================== FIND ALL (SYNC) =====================
    public List<Transaction> findAll() {

        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId",
                    Transaction.class
            );

            query.setParameter("userId", Session.currentUser.getId());

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    // ===================== FIND BY TYPE (SYNC) =====================
    public List<Transaction> findByType(String type) {

        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<Transaction> query = em.createQuery(
                    "SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type",
                    Transaction.class
            );

            query.setParameter("userId", Session.currentUser.getId());
            query.setParameter("type", type);

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    // ===================== CATEGORY CHECK =====================
    private Category findCategoryForCurrentUser(int categoryId) {

        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.id = :categoryId AND c.user.id = :userId",
                    Category.class
            );

            query.setParameter("categoryId", categoryId);
            query.setParameter("userId", Session.currentUser.getId());

            List<Category> list = query.getResultList();

            return list.isEmpty() ? null : list.get(0);

        } finally {
            em.close();
        }
    }
}