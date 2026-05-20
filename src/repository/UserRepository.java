/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

/**
 *
 * @author MOHAMMAD
 */

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import model.User;

public class UserRepository {
        EntityManagerFactory emf ;
        EntityManager em ;

    public UserRepository() {
        emf = Persistence.createEntityManagerFactory("Finance_TrackerPU");
        em = emf.createEntityManager();
    }
        
    public User add(User user) {
        

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

    public User update(User user) {

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

    public User findById(int id) {

        try {
            return em.find(User.class, id);

        } finally {
            em.close();
        }
    }

    public User findByEmail(String email) {

        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)",
                    User.class
            );

            query.setParameter("email", email);

            List<User> users = query.getResultList();

            if (users.isEmpty()) {
                return null;
            }

            return users.get(0);

        } finally {
            em.close();
        }
    }

    public List<User> findAll() {

        try {
            return em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();

        } finally {
            em.close();
        }
    }
}