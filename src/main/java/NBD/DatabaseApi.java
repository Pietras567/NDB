package NBD;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DatabaseApi implements CRUDManager {
    private static CqlSession session;

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

    public void test() {
        try {
            System.out.println("\n\nClient test with manual inserting and client dao");

            Client client_manual = new Client("John Doe", 30);
            System.out.println(client_manual);

            session.execute(
                    "INSERT INTO clients (client_id, name, age) VALUES (?, ?, ?)",
                    client_manual.getId(), client_manual.getName(), client_manual.getAge());

            ClientMapper mapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
            ClientDao clientDao = mapper.clientDao();

            Client client = new Client("John Doe", 30);
            System.out.println(client);

            System.out.println("\nTest dao CRUD");

            clientDao.insert(client);
            System.out.println("\nSaved: " + client);

            Client fetchedClient = clientDao.findById(client.getId());
            System.out.println("\nFetched: " + fetchedClient);

            fetchedClient.setAge(31);
            clientDao.update(fetchedClient);
            System.out.println("\nUpdated: " + fetchedClient);

            clientDao.delete(fetchedClient);
            System.out.println("\nDeleted: " + fetchedClient);

            System.out.println("\n\nVehicle test with database api");
            System.out.println("\nSaving test");
            Car car = new Car("Honda Civic", 1200, 120, 4);
            addEntity(car, "vehicles");

            System.out.println("\n\nFetching test");
            Vehicle car2 = getEntity(Vehicle.class, "vehicles", car.getId());
            System.out.println(car);
            System.out.println(car2);

            System.out.println("\n\nUpdating test");

            car2.setName("Zygzak McQueen");
            updateEntity(car2, "vehicles");

            Vehicle car3 = getEntity(Vehicle.class, "vehicles", car2.getId());
            System.out.println(car2);
            System.out.println(car3);

            System.out.println("\n\nDeleting test");
            deleteEntity(Vehicle.class, "vehicles", car3.getId());
            Vehicle car4 = getEntity(Vehicle.class, "vehicles", car3.getId());
            System.out.println(car3);
            System.out.println(car4);



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public DatabaseApi() {
        initSession();
        System.out.println("Database connection established.");
        session.execute("USE car_rental");
        this.test();
    }


    @Override
    public <T> void addEntity(T entity, String tableName) {
        try {
            switch (tableName) {
                case "clients":
                    ClientMapper clientMapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    ClientDao clientDao = clientMapper.clientDao();

                    if ((UUID) entity.getClass().getMethod("getId").invoke(entity) == null) {
                        System.out.println("client_id is null, generating UUID");
                        UUID uuid = UUID.randomUUID();
                        entity.getClass().getMethod("setId", UUID.class).invoke(entity, uuid);
                        System.out.println("New id: " + uuid);
                    }

                    clientDao.insert((Client) entity);
                    break;
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();

                    if ((UUID) entity.getClass().getMethod("getId").invoke(entity) == null) {
                        System.out.println("rent_id is null, generating UUID");
                        UUID uuid = UUID.randomUUID();
                        entity.getClass().getMethod("setId", UUID.class).invoke(entity, uuid);
                        System.out.println("New id: " + uuid);
                    }

                    rentDao.insert((Rent) entity);
                    break;
                case "vehicles":
                    VehicleMapper vehicleMapper = new VehicleMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    VehicleDao vehicleDao = vehicleMapper.vehicleDao();
                    if(entity instanceof Car) {
                        vehicleDao.insert((Car) entity);
                        System.out.println("Saved: car");
                    } else if(entity instanceof Truck) {
                        vehicleDao.insert((Truck) entity);
                        System.out.println("Saved: truck");
                    } else if(entity instanceof Motorbike) {
                        vehicleDao.insert((Motorbike) entity);
                        System.out.println("Saved: motorbike");
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException("Problem z zapisem danych.");
        } finally {
            System.out.println("Zakonczono dodawanie danych.");
        }
    }
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
            //e.printStackTrace();
            throw new RuntimeException("Problem z usunieciem danych");
        } finally {
            System.out.println("Zakonczono usuwanie danych.");
        }
    }
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
            //e.printStackTrace();
            throw new RuntimeException("Problem z aktualizacja danych");
        } finally {
            System.out.println("Zakonczono aktualizowanie danych.");
        }
    }
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
            //e.printStackTrace();
            throw new RuntimeException("Problem z pobraniem danych");
        } finally {
            System.out.println("Zakonczono pobieranie danych.");
        }
        System.out.println("Nie znaleziono zadanych danych");
        return null;
    }

    @Override
    public Iterable<Rent> getRents(UUID id) {
        try {
            RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
            RentDao rentDao = rentMapper.rentDao();
            return rentDao.findByVehicleId(id);
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas wyszukiwania wypozyczen");
            //e.printStackTrace();
            return null;
        }
    }

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
                case "rents":
                    RentMapper rentMapper = new RentMapperBuilder(session).withDefaultKeyspace("car_rental").build();
                    RentDao rentDao = rentMapper.rentDao();
                    entities = (Iterable<T>) rentDao.findAll();
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

                    //entities.forEach(System.out::println);

                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas wyszukiwania wszystkich encji podanej klasy");
            //e.printStackTrace();
            return List.of();
        }
        return entities;
    }
}
