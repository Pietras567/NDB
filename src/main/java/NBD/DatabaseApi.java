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
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }

    public <T> void deleteEntity(Class<T> entityClass, long id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        T entity = em.find(entityClass, id);
        em.remove(entity);
        em.getTransaction().commit();
        em.close();
    }

    public <T> void updateEntity(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
        em.close();
    }

    public <T> T getEntity(Class<T> entityClass, long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        T entity = em.find(entityClass, id);
        em.getTransaction().commit();
        em.close();
        return entity;
    }
}
