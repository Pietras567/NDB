package NBD;

public interface CRUDManager {
    <T> void addEntity(T entity);
    <T> void deleteEntity(Class<T> entityClass, long id);
    <T> void updateEntity(T entity);
    <T> T getEntity(Class<T> entityClass, long id);
}
