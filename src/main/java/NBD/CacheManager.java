package NBD;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.Duration;
import java.time.LocalDateTime;
import java.lang.reflect.InvocationTargetException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class CacheManager implements CRUDManager {
    private static RedisManager redisManager = new RedisManager();
    private static DatabaseApi databaseApi = new DatabaseApi();
    private static final String[] RENTAL_CENTERS = {"CarRental", "JadymyRental", "ZygzakMcQueen"};

    public void sendToTopic(Rent rent) {
        // create Producer properties
        String bootstrapServers = "127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // create a producer record
        ObjectId rentID = rent.getId();
        String clientName = databaseApi.getEntity(Client.class, "clients", rent.getClient_id()).getName();
        String vehicleName = databaseApi.getEntity(Vehicle.class, "vehicles",rent.getVehicle_id()).getName();
        LocalDateTime startTime = rent.getStartDate();
        LocalDateTime endTime = rent.getEndDate();

        int rentalId = databaseApi.getEntity(Vehicle.class, "vehicles",rent.getVehicle_id()).getRentalId();
        String rentalCenter = RENTAL_CENTERS[rentalId];

        String rentalTime = java.time.LocalDateTime.now().toString();
        String rentInfo = String.format("{\"rentID\": %s, \"rental_center\": \"%s\", \"clientName\": \"%s\", \"vehicleName\": \"%s\", \"rental_time\": \"%s\"}",
                rentID, rentalCenter, clientName, vehicleName, startTime);

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("rents", rentInfo);

        // send data - asynchronous
        producer.send(producerRecord);

        // flush data - synchronous
        producer.flush();

        // flush and close producer
        producer.close();
    }

    @Override
    public <T> void addEntity(T entity, String collectionName) {
        int TTL;
        switch (collectionName) {
            case "clients":
                TTL = 1800;
                break;
            case "vehicles":
                TTL = 1800;
                break;
            case "rents":
                LocalDateTime startTime;
                LocalDateTime endTime;
                try {
                    startTime = (LocalDateTime) entity.getClass().getMethod("getStartDate").invoke(entity);
                    endTime = (LocalDateTime) entity.getClass().getMethod("getEndDate").invoke(entity);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                TTL = (int) Duration.between(startTime, endTime).getSeconds();
                break;
            default:
                throw new IllegalArgumentException("Unknown collection: " + collectionName);
        }
        databaseApi.addEntity(entity, collectionName);
        try {
            ObjectId id = (ObjectId) entity.getClass().getMethod("getId").invoke(entity);
            Document document = new Document();

            Class<?> currentClass = entity.getClass();

            while (currentClass != null) {
                for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    document.append(field.getName(), field.get(entity));
                    //System.out.println(field.getName() + " " + field.get(entity));
                }
                currentClass = currentClass.getSuperclass();
            }
            System.out.println(collectionName+":"+id.toHexString());
            System.out.println(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""));

            redisManager.setDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""), document, TTL);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void deleteEntity(Class<T> entityClass, String collectionName, ObjectId id) {
        databaseApi.deleteEntity(entityClass, collectionName, id);
        try {
            databaseApi.deleteEntity(entityClass, collectionName, id);
            redisManager.removeDocument(collectionName+":"+id.toString().replaceFirst("^0+(?!$)", ""));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void updateEntity(T entity, String collectionName, ObjectId id) {
        int TTL;
        switch (collectionName) {
            case "clients":
                TTL = 1800;
                break;
            case "vehicles":
                TTL = 1800;
                break;
            case "rents":
                LocalDateTime startTime;
                LocalDateTime endTime;
                try {
                    startTime = (LocalDateTime) entity.getClass().getMethod("getStartDate").invoke(entity);
                    endTime = (LocalDateTime) entity.getClass().getMethod("getEndDate").invoke(entity);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                TTL = (int) Duration.between(startTime, endTime).getSeconds();
                break;
            default:
                throw new IllegalArgumentException("Unknown collection: " + collectionName);
        }

        databaseApi.updateEntity(entity, collectionName, id);

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
            try {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                Class<?> currentClass = entity.getClass();
                while (currentClass != null) {
                    for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(entity, document.get(field.getName()));
                        //System.out.println(field.getName()+":"+document.get(field.getName()));
                    }
                    //System.out.println(entity.getClass().getDeclaredFields().toString());
                    currentClass = currentClass.getSuperclass();
                }

                switch (collectionName) {
                    case "clients":
                        TTL = 1800;
                        break;
                    case "vehicles":
                        TTL = 1800;
                        break;
                    case "rents":
                        LocalDateTime startTime;
                        LocalDateTime endTime;
                        try {
                            startTime = (LocalDateTime) entity.getClass().getMethod("getStartDate").invoke(entity);
                            endTime = (LocalDateTime) entity.getClass().getMethod("getEndDate").invoke(entity);
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }

                        TTL = (int) Duration.between(startTime, endTime).getSeconds();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown collection: " + collectionName);
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
                    LocalDateTime startTime;
                    LocalDateTime endTime;
                    try {
                        startTime = (LocalDateTime) entity.getClass().getMethod("getStartDate").invoke(entity);
                        endTime = (LocalDateTime) entity.getClass().getMethod("getEndDate").invoke(entity);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    TTL = (int) Duration.between(startTime, endTime).getSeconds();
                    break;
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
