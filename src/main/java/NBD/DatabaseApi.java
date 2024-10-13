package NBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseApi {
    private final Object lock = new Object();

    private static EntityManagerFactory entityManagerFactory;

    private static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
    }

    public DatabaseApi() {
        init();
    }

    public void addVehicle(Vehicle vehicle) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(vehicle);
        em.getTransaction().commit();
        em.close();
    }

    public Vehicle getVehicle(long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Vehicle vehicle = em.find(Vehicle.class, id);
        em.getTransaction().commit();
        em.close();
        return vehicle;
    }

    public void updateVehicle(Vehicle vehicle) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.merge(vehicle);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteVehicle(int id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Vehicle vehicle = em.find(Vehicle.class, id);
        em.remove(vehicle);
        em.getTransaction().commit();
        em.close();
    }

    public void addClient(Client client) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(client);
        em.getTransaction().commit();
        em.close();
    }

    public Client getClient(long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Client client = em.find(Client.class, id);
        em.getTransaction().commit();
        em.close();
        return client;
    }

    public void updateClient(Client client) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.merge(client);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteClient(long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Client client = em.find(Client.class, id);
        em.remove(client);
        em.getTransaction().commit();
        em.close();
    }


    public void addRent(Rent rent) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(rent);
        em.getTransaction().commit();
        em.close();
    }

    public Rent getRent(long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Rent rent = em.find(Rent.class, id);
        em.getTransaction().commit();
        em.close();
        return rent;
    }

    public void updateRent(Rent rent) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.merge(rent);
        em.getTransaction().commit();
        em.close();
    }

    public void deleteRent(long id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Rent rent = em.find(Rent.class, id);
        em.remove(rent);
        em.getTransaction().commit();
        em.close();
    }

}
