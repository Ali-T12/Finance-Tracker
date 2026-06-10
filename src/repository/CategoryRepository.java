package repository;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.Category;
import model.User;
import util.Session;

public class CategoryRepository {

    private EntityManagerFactory emf;

    public CategoryRepository() {
        this.emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
    }

    // ===================== ADD =====================
    public void addAsync(String name) {
        new Thread(() -> {
            EntityManager em = emf.createEntityManager();

            try {
                if (Session.currentUser == null) {
                    System.out.println("No user logged in.");
                    return;
                }

                User currentUser = em.getReference(User.class, Session.currentUser.getId());

                Category category = new Category(0, currentUser, name);

                em.getTransaction().begin();
                em.persist(category);
                em.getTransaction().commit();

                System.out.println("Category added ✔");

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

    // ===================== UPDATE =====================
    public void updateAsync(Category category) {
        new Thread(() -> {
            EntityManager em = emf.createEntityManager();

            try {
                em.getTransaction().begin();

                Category managed = em.find(Category.class, category.getId());

                if (managed == null) {
                    em.getTransaction().rollback();
                    return;
                }

                managed.setName(category.getName());

                em.getTransaction().commit();

                System.out.println("Category updated ✔");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            } finally {
                em.close();
            }

        }).start();
    }

    // ===================== DELETE =====================
    public void deleteAsync(int id) {
        new Thread(() -> {
            EntityManager em = emf.createEntityManager();

            try {
                if (Session.currentUser == null) {
                    System.out.println("No user logged in.");
                    return;
                }

                TypedQuery<Category> query = em.createQuery(
                        "SELECT c FROM Category c WHERE c.id = :id AND c.user.id = :userId",
                        Category.class
                );

                query.setParameter("id", id);
                query.setParameter("userId", Session.currentUser.getId());

                List<Category> list = query.getResultList();

                if (list.isEmpty()) return;

                Category category = list.get(0);

                em.getTransaction().begin();
                em.remove(category);
                em.getTransaction().commit();

                System.out.println("Category deleted ✔");

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

    // ===================== FIND BY ID =====================
    public void findByIdAsync(int id) {
        new Thread(() -> {
            EntityManager em = emf.createEntityManager();

            try {
                TypedQuery<Category> query = em.createQuery(
                        "SELECT c FROM Category c WHERE c.id = :id AND c.user.id = :userId",
                        Category.class
                );

                query.setParameter("id", id);
                query.setParameter("userId", Session.currentUser.getId());

                List<Category> list = query.getResultList();

                if (list.isEmpty()) {
                    System.out.println("Not found");
                    return;
                }

                System.out.println(list.get(0).getName());

            } finally {
                em.close();
            }

        }).start();
    }

    // ===================== FIND ALL =====================
    public List<Category> findAllSync() {
    EntityManager em = emf.createEntityManager();

    try {
        TypedQuery<Category> query = em.createQuery(
                "SELECT c FROM Category c WHERE c.user.id = :userId",
                Category.class
        );

        query.setParameter("userId", Session.currentUser.getId());

        return query.getResultList();

    } finally {
        em.close();
    }
}
}