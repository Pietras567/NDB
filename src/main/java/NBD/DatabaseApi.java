package NBD;

import java.sql.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class DatabaseApi {
    private final Object lock = new Object();

    private static SessionFactory factory;

    static {
        factory = new Configuration().configure().buildSessionFactory();
    }

    public void addVehicle(Vehicle vehicle) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(vehicle);
            tx.commit();
        }
    }

    public Vehicle getVehicle(long id) {
        try (Session session = factory.openSession()) {
            return session.get(Vehicle.class, id);
        }
    }

    public void updateVehicle(Vehicle vehicle) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(vehicle);
            tx.commit();
        }
    }

    public void deleteVehicle(int id) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            Vehicle vehicle = session.get(Vehicle.class, id);
            if (vehicle != null) {
                session.delete(vehicle);
            }
            tx.commit();
        }
    }

    public void addClient(Client client) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(client);
            tx.commit();
        }
    }

    public Client getClient(long id) {
        try (Session session = factory.openSession()) {
            return session.get(Client.class, id);
        }
    }

    public void updateClient(Client client) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(client);
            tx.commit();
        }
    }

    public void deleteClient(long id) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            Client client = session.get(Client.class, id);
            if (client != null) {
                session.delete(client);
            }
            tx.commit();
        }
    }


    public void addRent(Rent rent) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(rent);
            tx.commit();
        }
    }

    public Rent getRent(long id) {
        try (Session session = factory.openSession()) {
            return session.get(Rent.class, id);
        }
    }

    public void updateRent(Rent rent) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.update(rent);
            tx.commit();
        }
    }

    public void deleteRent(long id) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            Rent rent = session.get(Rent.class, id);
            if (rent != null) {
                session.delete(rent);
            }
            tx.commit();
        }
    }

}
