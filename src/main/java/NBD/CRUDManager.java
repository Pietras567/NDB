package NBD;

/**
 * Interface defining basic CRUD (Create, Read, Update, Delete) operations for managing entities in a database.
 * This interface provides methods for performing operations on various entity types, identified
 * by their class, database table name, and unique identifiers (UUIDs).
 */
public interface CRUDManager {
    <T> void addEntity(T entity);
    <T> void deleteEntity(Class<T> entityClass, long id);
    <T> void updateEntity(T entity);
    <T> T getEntity(Class<T> entityClass, long id);
}
