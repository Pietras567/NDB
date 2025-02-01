package NBD;

import java.util.UUID;

/**
 * Interface defining basic CRUD (Create, Read, Update, Delete) operations for managing entities in a database.
 * This interface provides methods for performing operations on various entity types, identified
 * by their class, database table name, and unique identifiers (UUIDs).
 */
public interface CRUDManager {
    <T> void addEntity(T entity, String tableName);
    <T> void deleteEntity(Class<T> entityClass, String tableName, UUID id);
    <T> void updateEntity(T entity, String tableName);
    <T> T getEntity(Class<T> entityClass, String tableName, UUID id);
    Iterable<Rent> getRents(UUID id);
    <T> Iterable<T> getAllEntities(Class<T> entityClass, String tableName);
}
