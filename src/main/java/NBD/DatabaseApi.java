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
    }




    private static EntityManagerFactory entityManagerFactory;

    public DatabaseApi() {
        initSession();
        System.out.println("Database connection established.");
        System.out.println(session.getMetadata().getKeyspaces());

        try { // Creating Keyspaces
            String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS carRental " +
                    "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };";

            session.execute(createKeyspace);
            System.out.println(session.getMetadata().getKeyspaces());
            System.out.println("Keyspace created.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try { // Creating Tables
            String createTableClients = "CREATE TABLE IF NOT EXISTS carrental.clients (" +
                    "clientId UUID PRIMARY KEY, " +
                    "name VARCHAR, " +
                    "age INT);";
            session.execute(createTableClients);

            String createTableRents = "CREATE TABLE IF NOT EXISTS carrental.rents (" +
                    "rentId UUID PRIMARY KEY, " +
                    "clientId UUID, " +
                    "vehicleId UUID, " +
                    "startDate TIMESTAMP, " +
                    "endDate TIMESTAMP);";
            session.execute(createTableRents);

            ///todo utworzenie tabeli dla pojazdów

        } catch (Exception e) {
            e.printStackTrace();
        }

        try { // Printing Config
            String keyspaceName = "carrental";
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


    @Override
    public <T> void addEntity(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {  // ATOMICITY
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    @Override
    public <T> void deleteEntity(Class<T> entityClass, long id) { // JAKO PARAMETR PODAJEMY np. Vehicle.class
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            em.remove(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    @Override
    public <T> void updateEntity(T entity) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(entity);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    @Override
    public <T> T getEntity(Class<T> entityClass, long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        T entity = null;
        try {
            em.getTransaction().begin();
            entity = em.find(entityClass, id);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return entity;
    }
}
