package NBD;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * DatabaseApi provides methods for initiating a database session and
 * performing CRUD operations on various entity types within the application.
 * It implements functionality provided by the base CRUDManager interface and integrates
 * specifically with underlying database handling for entities such as Client, Rent, and Vehicle.
 *
 * This class is designed to work with Cassandra database mappings and ensures
 * entities are properly persisted, updated, retrieved, and deleted from specific tables.
 */
public class DatabaseApi implements CRUDManager {
    private static CqlSession session;

    /**
     * Initializes a session to interact with a Cassandra database, configures the connection settings,
     * creates necessary keyspaces and tables, and retrieves keyspace metadata and table configurations.
     *
     * The method performs the following steps:
     * 1. Establishes a session with a Cassandra cluster using the specified contact points, data center,
     *    and authentication credentials.
     * 2. Creates a keyspace named "car_rental" with Simple Strategy replication and a replication factor of 3.
     * 3. Creates tables within the "car_rental" keyspace, including:
     *    - `clients`: A table for storing client information with a composite primary key.
     *    - `rents`: A table for storing rental transaction information with clustering based on rental dates.
     *    - `cars`, `trucks`, and `motorbikes`: Tables for storing various vehicle types.
     * 4. Prints metadata about the keyspace, such as replication details and existing tables.
     * 5. Retrieves and displays default session configuration settings, such as consistency levels.
     *
     * The method handles exceptions at each step to ensure that errors during keyspace or table creation
     * or metadata retrieval are logged without interrupting session initialization.
     */
    public void initSession() {
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("cassandra1", 9042))
                .addContactPoint(new InetSocketAddress("cassandra2", 9043))
                .addContactPoint(new InetSocketAddress("cassandra3", 9044))
                .withLocalDatacenter("DC1")
                .withAuthCredentials("carRental", "carRentalPassword")
                .build();

        System.out.println(session.getMetadata().getKeyspaces());

        try { // Creating Keyspaces
            String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS car_rental " +
                    "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };";

            session.execute(createKeyspace);
            System.out.println(session.getMetadata().getKeyspaces());
            System.out.println("Keyspace created.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { // Creating Tables
            String createTableClients = "CREATE TABLE IF NOT EXISTS car_rental.clients (" +
                    "client_id UUID, " +
                    "name TEXT, " +
                    "age INT, " +
                    "PRIMARY KEY (client_id, name)) " +
                    "WITH CLUSTERING ORDER BY (name ASC);";
            session.execute(createTableClients);

            String createTableRents = "CREATE TABLE IF NOT EXISTS car_rental.rents (" +
                    "rent_id UUID, " +
                    "client_id UUID, " +
                    "vehicle_id UUID, " +
                    "start_date TIMESTAMP, " +
                    "end_date TIMESTAMP, " +
                    "PRIMARY KEY (rent_id, start_date, end_date)) " +
                    "WITH CLUSTERING ORDER BY (start_date DESC, end_date DESC);";
            session.execute(createTableRents);

            String createTableCars = "CREATE TABLE IF NOT EXISTS car_rental.cars (" +
                    "vehicle_id UUID, " +
                    "name TEXT, " +
                    "weight INT, " +
                    "power INT, " +
                    "seats INT, " +
                    "PRIMARY KEY (vehicle_id))";
            session.execute(createTableCars);

            String createTableTrucks = "CREATE TABLE IF NOT EXISTS car_rental.trucks (" +
                    "vehicle_id UUID, " +
                    "name TEXT, " +
                    "weight INT, " +
                    "power INT, " +
                    "load_capacity INT, " +
                    "PRIMARY KEY (vehicle_id))";
            session.execute(createTableTrucks);

            String createTableMotorbike = "CREATE TABLE IF NOT EXISTS car_rental.motorbikes (" +
                    "vehicle_id UUID, " +
                    "name TEXT, " +
                    "weight INT, " +
                    "power INT, " +
                    "engine_capacity INT, " +
                    "PRIMARY KEY (vehicle_id))";
            session.execute(createTableMotorbike);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try { // Printing Config
            String keyspaceName = "car_rental";
            KeyspaceMetadata keyspaceMetadata = session.getMetadata().getKeyspace(keyspaceName).orElse(null);

            if (keyspaceMetadata != null) {
                System.out.println("Keyspace: " + keyspaceName);
                System.out.println("Replication details: " + keyspaceMetadata.getReplication());
                System.out.println("Tables:");
                keyspaceMetadata.getTables().forEach((tableName, tableMetadata) -> {
                    System.out.println(" - Table: " + tableName);
                });
            } else {
                System.out.println("Keyspace '" + keyspaceName + "' does not exist.");
            }

            DriverExecutionProfile profile = session.getContext().getConfig().getDefaultProfile();

            String defaultConsistencyLevel = profile.getString(DefaultDriverOption.REQUEST_CONSISTENCY);
            String defaultSerialConsistencyLevel = profile.getString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY);

            System.out.println("Default consistency level: " + defaultConsistencyLevel);
            System.out.println("Default consistency level for serial operations: " + defaultSerialConsistencyLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a new instance of the `DatabaseApi` class and initializes the database session.
     *
     * This constructor performs the following steps:
     * 1. Calls the `initSession` method to establish a session with a Cassandra database.
     * 2. Outputs a message indicating that the database connection has been successfully established.
     * 3. Executes a statement to set the active keyspace to `car_rental`.
     *
     * This ensures that the `DatabaseApi` instance is ready to interact with the database
     * upon creation.
     */
    public DatabaseApi() {
        initSession();
        System.out.println("Database connection established.");
        session.execute("USE car_rental");
    }


    /**
     * Adds an entity to the specified table in the database. The method first checks
     * whether the provided entity has a UUID assigned to it by invoking its `getId` method.
     * If the UUID is null, a new UUID is generated and set on the entity using the `setId` method.
     * The entity is then inserted into the designated table according to the corresponding database mapper and DAO.
     *
     * Supported tables:
     * - "clients" for client entities.
     * - "rents" for rent entities.
     * - "vehicles" for vehicle entities (supports different vehicle types).
     *
     * Note: Throws a runtime exception if the table name is unsupported or if any
     * issue occurs during data persistence.
     *
     * @param <T>       The type of the entity to be added.
     * @param entity    The entity to be added to the database.
     * @param tableName The name of the table where the entity should be added.
     *                  Accepted values are "clients", "rents", or "vehicles".
     */
    @Override
    public <T> void addEntity(T entity, String tableName) {
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();

                    if ((UUID) entity.getClass().getMethod("getId").invoke(entity) == null) {
                        UUID uuid = UUID.randomUUID();
                        entity.getClass().getMethod("setId", UUID.class).invoke(entity, uuid);
                    }

                    clientDao.insert((Client) entity);
                    break;
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();

                    if ((UUID) entity.getClass().getMethod("getId").invoke(entity) == null) {
                        UUID uuid = UUID.randomUUID();
                        entity.getClass().getMethod("setId", UUID.class).invoke(entity, uuid);
                    }

                    rentDao.insert((Rent) entity);
                    break;
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    if(entity instanceof Car) {
                        vehicleDao.insert((Car) entity);
                    } else if(entity instanceof Truck) {
                        vehicleDao.insert((Truck) entity);
                    } else if(entity instanceof Motorbike) {
                        vehicleDao.insert((Motorbike) entity);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem z zapisem danych.");
        }
    }

    /**
     * Deletes an entity from the specified table in the database. This method uses
     * the provided table name to determine the appropriate Data Access Object (DAO)
     * and performs the delete operation based on the entity's UUID.
     *
     * Supported tables:
     * - "clients": Deletes a Client entity.
     * - "rents": Deletes a Rent entity.
     * - "vehicles": Deletes a Vehicle entity, resolving the specific type (Car, Truck, or Motorbike).
     *
     * Note: Throws a runtime exception if the table name is unsupported, if the entity
     * is not found, or if any issue occurs during deletion.
     *
     * @param <T>        The type of the entity to be deleted.
     * @param entityClass The class of the entity type being deleted (e.g., Vehicle.class).
     * @param tableName   The name of the table where the entity should be deleted.
     *                    Accepted values are "clients", "rents", or "vehicles".
     * @param id          The UUID of the entity to be deleted.
     */
    @Override
    public <T> void deleteEntity(Class<T> entityClass, String tableName, UUID id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();
                    Client client = clientDao.findById(id);
                    clientDao.delete(client);
                    break;
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();
                    Rent rent = rentDao.findById(id);
                    rentDao.delete(rent);
                    break;
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    Vehicle vehicle = null;

                    vehicle = vehicleDao.findCarById(id);

                    if (vehicle != null) {
                        vehicleDao.delete((Car) vehicle);
                        break;
                    } else {
                        vehicle = vehicleDao.findTruckById(id);
                        if (vehicle != null) {
                            vehicleDao.delete((Truck) vehicle);
                            break;
                        } else {
                            vehicle = vehicleDao.findMotorbikeById(id);
                            if (vehicle != null) {
                                vehicleDao.delete((Motorbike) vehicle);
                                break;
                            } else {
                                throw new RuntimeException("Cannot find ordered to delete vehicle");
                            }
                        }
                    }
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem z usunieciem danych");
        }
    }

    /**
     * Updates an existing entity in the specified table in the database.
     *
     * This method determines the appropriate Data Access Object (DAO) and
     * performs the update operation based on the provided table name. It supports
     * different table types and resolves specific implementations based on the entity's class.
     *
     * Supported tables:
     * - "clients" for updating Client entities.
     * - "rents" for updating Rent entities.
     * - "vehicles" for updating Vehicle entities, including Car, Truck, and Motorbike types.
     *
     * Note: Throws a runtime exception if the table name is unsupported or if
     * any issue occurs during the update process.
     *
     * @param <T>       The type of the entity to be updated.
     * @param entity    The entity object containing the updated information.
     * @param tableName The name of the table where the entity should be updated.
     *                  Accepted values are "clients", "rents", or "vehicles".
     */
    @Override
    public <T> void updateEntity(T entity, String tableName) {
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();
                    clientDao.update((Client) entity);
                    break;
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();
                    rentDao.update((Rent) entity);
                    break;
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    if(entity instanceof Car) {
                        vehicleDao.update((Car) entity);
                    } else if(entity instanceof Truck) {
                        vehicleDao.update((Truck) entity);
                    } else if(entity instanceof Motorbike) {
                        vehicleDao.update((Motorbike) entity);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem z aktualizacja danych");
        }
    }

    /**
     * Retrieves an entity of the specified type from the database based on the table name and UUID.
     *
     * The method identifies the appropriate Data Access Object (DAO) to interact with the database
     * depending on the table name. It supports fetching entities from the "clients", "rents", and
     * "vehicles" tables. For the "vehicles" table, the method distinguishes between different vehicle
     * types (Car, Truck, or Motorbike).
     *
     * If the entity could not be found or if the table name is not supported, a runtime exception
     * is thrown.
     *
     * @param <T>          The type of the entity to be retrieved.
     * @param entityClass  The class of the entity type to be retrieved (e.g., Client.class).
     * @param tableName    The name of the table to query. Accepted values are "clients", "rents",
     *                     or "vehicles".
     * @param id           The UUID of the entity to retrieve.
     * @return The entity of the specified type and UUID, or null if not found.
     * @throws RuntimeException if the table name is unsupported or any issue occurs during retrieval.
     */
    @Override
    public <T> T getEntity(Class<T> entityClass, String tableName, UUID id) {
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();
                    Client client = clientDao.findById(id);
                    return entityClass.cast(client);
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();
                    Rent rent = rentDao.findById(id);
                    return entityClass.cast(rent);
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    Vehicle vehicle = null;
                    vehicle = vehicleDao.findCarById(id);
                    if (vehicle != null) {
                        return entityClass.cast(vehicle);
                    }
                    vehicle = vehicleDao.findTruckById(id);
                    if (vehicle != null) {
                        return entityClass.cast(vehicle);
                    }
                    vehicle = vehicleDao.findMotorbikeById(id);
                    if (vehicle != null) {
                        return entityClass.cast(vehicle);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem z pobraniem danych");
        }
        return null;
    }

    /**
     * Retrieves a collection of Rent entities associated with a specific vehicle ID from the database,
     * ensuring duplicate entries are filtered out.
     *
     * @param id The UUID of the vehicle for which to retrieve rental records.
     * @return An Iterable containing unique Rent entities associated with the specified vehicle ID,
     *         or null if an error occurs during the retrieval process.
     */
    @Override
    public Iterable<Rent> getRents(UUID id) {
        try {
            RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
            RentDao rentDao = rentMapper.rentDao();
            Iterable<Rent> rents = rentDao.findByVehicleId(id);

            List<Rent> list = new ArrayList<>();

            for (Rent item : rents) {
                list.add(item);
            }

            Map<UUID, Long> idCount = list.stream().collect(Collectors.groupingBy(obj -> {
                try {
                    return (UUID) obj.getClass().getMethod("getId").invoke(obj);
                } catch (Exception e) {
                    throw new RuntimeException("Napotkano problem podczas usuwania duplikatow");
                }
            }, Collectors.counting()));

            List<Rent> filteredList = list.stream().filter(obj -> {
                try {
                    return idCount.get(obj.getClass().getMethod("getId").invoke(obj)) == 1;
                } catch (Exception e) {
                    throw new RuntimeException("Napotkano problem podczas usuwania duplikatow");
                }
            }).toList();

            rents = filteredList::iterator;

            return rents;
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas wyszukiwania wypozyczen");
            return null;
        }
    }

    /**
     * Retrieves all entities of a specific class from a specified database table.
     *
     * This method fetches entities across different table mappings and removes duplicates
     * in certain cases depending on the logic associated with the table. It ensures that
     * aggregated results from multiple queries, such as for vehicles, are properly combined.
     *
     * @param <T> The type of the entity to be retrieved.
     * @param entityClass The class of the entities to retrieve.
     * @param tableName The name of the database table from which entities are fetched.
     * @return An iterable collection of entities of the specified class,
     *         or an empty list if an error occurs or the table name is not supported.
     */
    @Override
    public <T> Iterable<T> getAllEntities(Class<T> entityClass, String tableName) {
        Iterable<T> entities = null;
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();
                    entities = (Iterable<T>) clientDao.findAll();
                    break;
                case "test_rents":
                    RentMapper rentMapper1 = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao1 = rentMapper1.rentDao();
                    entities = (Iterable<T>) rentDao1.findAll();
                    break;
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();
                    entities = (Iterable<T>) rentDao.findAll();

                    List<T> list = new ArrayList<>();

                    for (T item : entities) {
                        list.add(item);
                    }

                    Map<UUID, Long> idCount = list.stream().collect(Collectors.groupingBy(obj -> {
                        try {
                            return (UUID) obj.getClass().getMethod("getId").invoke(obj);
                        } catch (Exception e) {
                            throw new RuntimeException("Napotkano problem podczas usuwania duplikatow");
                        }
                    }, Collectors.counting()));

                    List<T> filteredList = list.stream().filter(obj -> {
                        try {
                            return idCount.get(obj.getClass().getMethod("getId").invoke(obj)) == 1;
                        } catch (Exception e) {
                            throw new RuntimeException("Napotkano problem podczas usuwania duplikatow");
                        }
                    }).toList();

                    entities = filteredList::iterator;

                    break;
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    Iterable<T> entities1 = (Iterable<T>) vehicleDao.findAllCar();
                    Iterable<T> entities2 = (Iterable<T>) vehicleDao.findAllTruck();
                    Iterable<T> entities3 = (Iterable<T>) vehicleDao.findAllMotorbike();

                    entities = Stream.concat(
                            Stream.concat(
                                    StreamSupport.stream(entities1.spliterator(), false),
                                    StreamSupport.stream(entities2.spliterator(), false)
                            ),
                            StreamSupport.stream(entities3.spliterator(), false)
                    ).collect(Collectors.toList());

                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas wyszukiwania wszystkich encji podanej klasy");
            return List.of();
        }
        return entities;
    }
}
