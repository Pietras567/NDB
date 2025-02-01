package NBD;

import org.bson.types.ObjectId;

/**
 * Interface for managing Create, Read, Update, and Delete (CRUD) operations on database entities.
 * This interface provides generic methods for handling persistent storage in a structured manner
 * within a specified collection.
 */
public interface CRUDManager {
    <T> void addEntity(T entity, String collectionName);
    <T> void deleteEntity(Class<T> entityClass, String collectionName, ObjectId id);
    <T> void updateEntity(T entity, String collectionName, ObjectId id);
    <T> T getEntity(Class<T> entityClass, String collectionName, ObjectId id);
}
