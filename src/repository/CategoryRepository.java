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

    EntityManagerFactory emf;
    EntityManager em;

    public CategoryRepository() {
        emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
        em = emf.createEntityManager();

    }

    public Category add(String name) {
        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            User currentUser = em.getReference(User.class, Session.currentUser.getId());

            Category category = new Category(0, currentUser, name);

            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();

            return category;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return null;

        } 
    }

    public Category update(Category category) {

        try {
            em.getTransaction().begin();
            Category managedCategory = em.find(Category.class, category.getId());

            if (managedCategory == null) {
                em.getTransaction().rollback();
                return null;
            }

            managedCategory.setName(category.getName());

            em.getTransaction().commit();

            return managedCategory;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return null;

        } 
    }

    public boolean delete(int id) {
        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return false;
            }

            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.id = :id AND c.user.id = :userId",
                    Category.class
            );

            query.setParameter("id", id);
            query.setParameter("userId", Session.currentUser.getId());

            List<Category> categories = query.getResultList();

            if (categories.isEmpty()) {
                return false;
            }

            Category category = categories.get(0);

            em.getTransaction().begin();
            em.remove(category);
            em.getTransaction().commit();

            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } 
    }

    public Category findById(int id) {

        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.id = :id AND c.user.id = :userId",
                    Category.class
            );

            query.setParameter("id", id);
            query.setParameter("userId", Session.currentUser.getId());

            List<Category> categories = query.getResultList();

            if (categories.isEmpty()) {
                return null;
            }

            return categories.get(0);

        } finally {
        }
    }

    public List<Category> findAll() {
        try {
            if (Session.currentUser == null) {
                System.out.println("No user logged in.");
                return null;
            }

            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.user.id = :userId",
                    Category.class
            );

            query.setParameter("userId", Session.currentUser.getId());

            return query.getResultList();

        } finally {
        }
    }
}