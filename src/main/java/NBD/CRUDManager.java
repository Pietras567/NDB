package NBD;

import java.util.UUID;

public interface CRUDManager {
    <T> void addEntity(T entity, String tableName);
    <T> void deleteEntity(Class<T> entityClass, String tableName, UUID id);
    <T> void updateEntity(T entity, String tableName);
    <T> T getEntity(Class<T> entityClass, String tableName, UUID id);
}
