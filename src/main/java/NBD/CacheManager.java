package NBD;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.Duration;
import java.time.LocalDateTime;
import java.lang.reflect.InvocationTargetException;

/**
 * Manages caching and storage operations for entities using a combination of Redis and a primary database.
 * This class implements the {@code CRUDManager} interface to handle Create, Read, Update, and Delete (CRUD) operations.
 * It ensures that entities are synchronized between the database and Redis caching system.
 */
public class CacheManager implements CRUDManager {
    private static RedisManager redisManager = new RedisManager();
    private static DatabaseApi databaseApi = new DatabaseApi();


    /**
     * Adds an entity to a specified collection in the database and optionally caches it in the Redis datastore
     * with a time-to-live (TTL) value for certain collections.
     *
     * @param <T>           the generic type of the entity being added
     * @param entity        the entity object to be persisted in the database and optionally cached
     * @param collectionName the name of the collection where the entity will be stored
     *
     * @throws IllegalArgumentException if the specified collection name is not recognized
     * @throws RuntimeException         if there is an error accessing or invoking methods on the entity
     */
    @Override
    public <T> void addEntity(T entity, String collectionName) {
        databaseApi.addEntity(entity, collectionName);

        int TTL;
        switch (collectionName) {
            case "clients":
                TTL = 1800;
                break;
            case "vehicles":
                TTL = 1800;
                break;
            case "rents":
                return;
            default:
                throw new IllegalArgumentException("Unknown collection: " + collectionName);
        }

        try {
            ObjectId id = (ObjectId) entity.getClass().getMethod("getId").invoke(entity);
            Document document = new Document();

            Class<?> currentClass = entity.getClass();

            while (currentClass != null) {
                for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    document.append(field.getName(), field.get(entity));
                }
                currentClass = currentClass.getSuperclass();
            }

            redisManager.setDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""), document, TTL);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an entity from both the specified database collection and its associated cache in Redis.
     * The removal operation ensures the entity is removed from persistent storage as well as the cache.
     *
     * @param <T>            the type of the entity to be deleted
     * @param entityClass    the class type of the entity
     * @param collectionName the name of the collection from which the entity is to be deleted
     * @param id             the unique identifier of the entity to be deleted
     *
     * @throws RuntimeException if an error occurs while removing the entity or updating the Redis cache
     */
    @Override
    public <T> void deleteEntity(Class<T> entityClass, String collectionName, ObjectId id) {
        try {
            databaseApi.deleteEntity(entityClass, collectionName, id);
            redisManager.removeDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates an existing entity in the specified collection within the database and optionally updates
     * its representation in the Redis cache based on predefined conditions. The method ensures caching
     * is only applied for certain collections and handles serialization of entity fields into a document format
     * compatible with Redis.
     *
     * @param <T>            the type of the entity being updated
     * @param entity         the entity object containing updated data
     * @param collectionName the name of the collection where the entity is stored
     * @param id             the unique identifier of the entity to update
     *
     * @throws IllegalArgumentException if the specified collection name is not recognized
     * @throws RuntimeException         if an error occurs during entity field access or caching
     */
    @Override
    public <T> void updateEntity(T entity, String collectionName, ObjectId id) {
        databaseApi.updateEntity(entity, collectionName, id);

        int TTL;
        switch (collectionName) {
            case "clients":
                TTL = 1800;
                break;
            case "vehicles":
                TTL = 1800;
                break;
            case "rents":
                return;
            default:
                throw new IllegalArgumentException("Unknown collection: " + collectionName);
        }

        try {
            Document document = new Document();
            Class<?> currentClass = entity.getClass();

            while (currentClass != null) {
                for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        document.append(field.getName(), field.get(entity));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            redisManager.setDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""), document, TTL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves an entity from a specified collection either from the Redis cache or the database.
     * If the entity exists in the Redis cache, it is fetched and returned. If it does not exist in the cache,
     * the entity is retrieved from the database, stored in the Redis cache, and then returned.
     *
     * @param <T>            the generic type of the entity being retrieved
     * @param entityClass    the class type of the entity
     * @param collectionName the name of the collection (e.g., "clients", "vehicles") where the entity resides
     * @param id             the unique identifier of the entity to retrieve
     * @return the entity object of the specified type if found, or null if no matching entity exists
     * @throws IllegalArgumentException if the specified collection name is not recognized
     * @throws RuntimeException         if an error occurs during entity field access or instantiation
     */
    @Override
    public <T> T getEntity(Class<T> entityClass, String collectionName, ObjectId id) {
        Document document = null;
        int TTL;

        try {
            document = redisManager.getDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""));
        } catch (Exception e) {
            System.out.println("Problemy z połączeniem z usługą Redis.");
            return databaseApi.getEntity(entityClass, collectionName, id);
        }

        if (document != null) {
            switch (collectionName) {
                case "clients":
                    TTL = 1800;
                    break;
                case "vehicles":
                    TTL = 1800;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown collection: " + collectionName);
            }

            try {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                Class<?> currentClass = entity.getClass();
                while (currentClass != null) {
                    for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(entity, document.get(field.getName()));
                    }
                    currentClass = currentClass.getSuperclass();
                }

                redisManager.setDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""), document, TTL);

                return entity;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            T entity = databaseApi.getEntity(entityClass, collectionName, id);
            if (entity == null) {
                return null;
            }

            switch (collectionName) {
                case "clients":
                    TTL = 1800;
                    break;
                case "vehicles":
                    TTL = 1800;
                    break;
                case "rents":
                    return entity;
                default:
                    throw new IllegalArgumentException("Unknown collection: " + collectionName);
            }

            try {
                document = new Document();
                Class<?> currentClass = entity.getClass();

                while (currentClass != null) {
                    for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        document.append(field.getName(), field.get(entity));
                    }
                    currentClass = currentClass.getSuperclass();
                }

                redisManager.setDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""), document, TTL);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return entity;
        }
    }
}