package NBD;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.InetSocketAddress;
import java.util.UUID;

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

            ///todo utworzenie tabeli dla pojazdów

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
            session.execute("USE car_rental");


            Client client_manual = new Client("John Doe", 30);
            System.out.println(client_manual);

            //ręczne dodawanie działa
            session.execute(
                    "INSERT INTO clients (client_id, name, age) VALUES (?, ?, ?)",
                    client_manual.getId(), client_manual.getName(), client_manual.getAge());

            System.out.println("Test1");
            ClientMapper mapper = new ClientMapperBuilder(session).withDefaultKeyspace("car_rental").build();
            System.out.println("Test2 " + mapper.toString());
            ClientDao clientDao = mapper.clientDao();

            Client client = new Client("John Doe", 30);
            System.out.println(client);

            System.out.println("Test CRUD");

            addEntity(client, "clients");
            //clientDao.insert(client);
            System.out.println("Saved: " + client);

            Client fetchedClient = clientDao.findById(client.getId());
            System.out.println(fetchedClient);

            fetchedClient.setAge(31);
            clientDao.update(fetchedClient);
            System.out.println("Updated: " + fetchedClient);

            clientDao.delete(fetchedClient);
            System.out.println("Deleted: " + fetchedClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public DatabaseApi() {
        initSession();
        System.out.println("Database connection established.");
        
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
                    break;
                default:
                    throw new RuntimeException("Unsupported table name: " + tableName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem z zapisem danych.");
        } finally {
            System.out.println("Dane zostaly zapisane.");
        }
    }
    @Override
    public <T> void deleteEntity(Class<T> entityClass, String tableName, UUID id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        try {

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem z usunieciem danych");
        } finally {

        }
    }
    @Override
    public <T> void updateEntity(T entity, String tableName) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem z aktualizacja danych");
        } finally {

        }
    }
    @Override
    public <T> T getEntity(Class<T> entityClass, String tableName, UUID id) {
        T entity = null;
        try {

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Problem z pobraniem danych");
        } finally {

        }
        return entity;
    }
}
