package NBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseApi {
    private final Object lock = new Object();

    private static EntityManagerFactory entityManagerFactory;

    private static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
    }

    public DatabaseApi() {
        init();
    }

    public <T> void addEntity(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {  // ATOMICITY
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    public <T> void deleteEntity(Class<T> entityClass, long id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            em.remove(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    public <T> void updateEntity(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
    }

    public <T> T getEntity(Class<T> entityClass, long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        T entity = null;
        try {
            em.getTransaction().begin();
            entity = em.find(entityClass, id);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        return entity;
    }
}
