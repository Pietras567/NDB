package NBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * The DatabaseApi class provides an implementation of CRUD (Create, Read, Update, Delete) operations
 * for managing entities within a database using JPA (Java Persistence API). It interacts with a
 * database through the EntityManager to perform various operations on entities.
 *
 * This class requires a correct database configuration defined in the `persistence.xml` file
 * under the persistence unit "default".
 */
public class DatabaseApi implements CRUDManager {
    private static EntityManagerFactory entityManagerFactory;

    /**
     * Initializes the EntityManagerFactory for the application using the specified persistence unit.
     * This method is responsible for setting up the JPA EntityManagerFactory which will be used
     * for database interactions. The persistence unit name must match the one defined in the
     * `persistence.xml` configuration file.
     *
     * This method should be called before performing any database operations to ensure that the
     * EntityManagerFactory is properly initialized.
     *
     * The factory created by this method is a shared resource and should be closed properly during
     * application shutdown to release the resources.
     */
    private static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
    }

    /**
     * Default constructor for the DatabaseApi class.
     *
     * This constructor initializes the necessary resources for database interaction
     * by calling the private static method {@code init()}. The method sets up the
     * EntityManagerFactory which is critical for executing database operations.
     *
     * It is recommended to create an instance of the DatabaseApi class before performing
     * any database operations to ensure that all required connections and configurations
     * are properly initialized.
     */
    public DatabaseApi() {
        init();
    }

    /**
     * Adds a new entity to the database.
     * This method uses the JPA `EntityManager` to persist the provided entity
     * into the database. The operation is performed within a transaction to ensure
     * atomicity. In case of an exception, the transaction is rolled back.
     *
     * @param <T> The type of the entity being added.
     * @param entity The entity object to be added to the database. It must not be null,
     *               and should match the entity mappings defined in the persistence context.
     */
    @Override
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
        } finally {
            em.close();
        }
    }

    /**
     * Deletes an entity from the database using its class type and unique identifier.
     * This method uses the JPA `EntityManager` to find and remove the entity associated
     * with the given ID. The operation is performed within a transaction to ensure atomicity.
     * In case of an exception, the transaction is rolled back.
     *
     * @param <T> The type of the entity being deleted.
     * @param entityClass The class of the entity to be deleted. It must match the entity
     *                    type managed by the persistence context.
     * @param id The unique identifier of the entity to be deleted.
     */
    @Override
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
        } finally {
            em.close();
        }
    }

    /**
     * Updates an existing entity in the database.
     * This method uses the JPA `EntityManager` to merge the state of the provided entity
     * into the persistence context. The operation is performed within a transaction to ensure atomicity.
     * In case of an exception, the transaction is rolled back, and the entity manager is closed.
     *
     * @param <T> The type of the entity being updated.
     * @param entity The entity object with updated data to be persisted to the database.
     *               It must not be null and should match the entity mappings defined
     *               in the persistence context.
     */
    @Override
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
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves an entity of the specified type from the database by its unique identifier.
     * This method uses the JPA `EntityManager` to find the entity associated with the given
     * ID within the persistence context. The operation is performed within a transaction.
     * If an exception occurs, the transaction is rolled back, and the entity manager is closed.
     *
     * @param <T> The type of the entity to be retrieved.
     * @param entityClass The class of the entity to be retrieved. It must match the entity type
     *                    managed by the persistence context.
     * @param id The unique identifier of the entity to be retrieved.
     * @return The entity object of the specified type that corresponds to the provided identifier,
     *         or null if the entity is not found.
     */
    @Override
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
        } finally {
            em.close();
        }
        return entity;
    }
}
