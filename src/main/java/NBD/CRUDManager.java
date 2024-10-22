package NBD;

import org.bson.types.ObjectId;

public interface CRUDManager {
    <T> void addEntity(T entity, String collectionName);
    <T> void deleteEntity(Class<T> entityClass, String collectionName, ObjectId id);
    <T> void updateEntity(T entity, String collectionName, ObjectId id);
    <T> T getEntity(Class<T> entityClass, String collectionName, ObjectId id);
}
