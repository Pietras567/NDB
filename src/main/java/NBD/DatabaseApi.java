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

    public Vehicle getVehicle(int id) {
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
}
