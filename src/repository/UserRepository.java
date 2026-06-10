package repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.User;

public class UserRepository {

    private EntityManagerFactory emf;

    public UserRepository() {
        this.emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
    }

    // ================= ADD =================
    public User add(User user) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return user;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;

        } finally {
            em.close();
        }
    }

    // ================= UPDATE =================
    public User update(User user) {

        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            User updatedUser = em.merge(user);
            em.getTransaction().commit();

            return updatedUser;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;

        } finally {
            em.close();
        }
    }

    // ================= FIND BY ID =================
    public User findById(int id) {

        EntityManager em = emf.createEntityManager();

        try {
            return em.find(User.class, id);

        } finally {
            em.close();
        }
    }

    // ================= FIND BY EMAIL =================
    public User findByEmail(String email) {

        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                    User.class
            );

            query.setParameter("email", email);

            List<User> users = query.getResultList();

            return users.isEmpty() ? null : users.get(0);

        } finally {
            em.close();
        }
    }

    // ================= FIND ALL =================
    public List<User> findAll() {

        EntityManager em = emf.createEntityManager();

        try {
            return em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}