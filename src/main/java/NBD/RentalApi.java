package NBD;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class RentalApi {
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");

    public boolean oddaj(Vehicle vehicle, Client client) {
        try {
            long vehicleId = vehicle.getId();
            System.out.println(vehicleId);
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT r FROM Rent r WHERE r.vehicle_id = :vehicleId";
            TypedQuery<Rent> query = entityManager.createQuery(jpql, Rent.class);
            query.setParameter("vehicleId", vehicleId);

            List<Rent> list = query.getResultList();
            boolean wypozyczony = false;
            Rent rent = null;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        System.out.println("zwracamy");
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient().getId() == client.getId()) {
                    DatabaseApi databaseApi = new DatabaseApi();
                    rent.setEndDate(LocalDateTime.now());
                    databaseApi.updateEntity(rent);
//                    databaseApi.updateRent(rent);
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    return false;
                }
            } else {
                System.out.println("nie zwracamy");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }


    public boolean wypozycz(Vehicle vehicle, Client client, int days) {
        try {
            long vehicleId = vehicle.getId();
            System.out.println(vehicleId);
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            String jpql = "SELECT r FROM Rent r WHERE r.vehicle_id = :vehicleId";
            TypedQuery<Rent> query = entityManager.createQuery(jpql, Rent.class);
            query.setParameter("vehicleId", vehicleId);

            List<Rent> list = query.getResultList();
            boolean wypozyczony = false;
            if(!list.isEmpty()) {
                for (Rent r : list) {
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        System.out.println("nie jadymy");
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                System.out.println("jadymy");
                DatabaseApi Api = new DatabaseApi();
                Rent rent = new Rent(client.getId(), vehicle.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(days));
                rent.setClient(client);
                rent.setVehicle(vehicle);
                Api.addEntity(rent);
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public <T> List<T> getAllEntities(Class<T> entityClass) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String jpql = "SELECT r FROM %s r".formatted(entityClass.getSimpleName());
        TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
        return query.getResultList();
    }
}
