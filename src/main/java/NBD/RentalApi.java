package NBD;

import java.time.Instant;
import java.util.UUID;


public class RentalApi {
    private DatabaseApi databaseApi = new DatabaseApi();

    public boolean oddaj(Vehicle vehicle, Client client) {
        try {
            UUID vehicleId = vehicle.getId();

            Iterable<Rent> list = databaseApi.getRents(vehicleId);
            boolean wypozyczony = false;
            Rent rent = null;
            if(list.iterator().hasNext()) {
                for (Rent r : list) {
                    if((Instant.now().isAfter(r.getStartDate()) || Instant.now().equals(r.getStartDate())) && (Instant.now().isBefore(r.getEndDate())) || Instant.now().equals(r.getEndDate())) {
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient_id().equals(client.getId())) {
                    rent.setEndDate(Instant.now());
                    databaseApi.updateEntity(rent, "rents");
                    System.out.println("Pojazd zostal zwrocony.");
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    //System.out.println(rent.getClient_id());
                    //System.out.println(rent.getClient_id().getClass());
                    //System.out.println(client.getId());
                    //System.out.println(client.getId().getClass());
                    return false;
                }
            } else {
                System.out.println("Nie mozna zwrocic pojazdu, nie jest on aktualnie wypozyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wczesniejszego zakanczania wypozyczenia.");
            //e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean wypozycz(Vehicle vehicle, Client client, int days) {
        try {
            UUID vehicleId = vehicle.getId();

            Iterable<Rent> list = databaseApi.getRents(vehicleId);

            boolean wypozyczony = false;
            if(list.iterator().hasNext()) {
                for (Rent r : list) {
                    if((Instant.now().isAfter(r.getStartDate()) || Instant.now().equals(r.getStartDate())) && (Instant.now().isBefore(r.getEndDate())) || Instant.now().equals(r.getEndDate())) {
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                Rent rent = new Rent(client.getId(), vehicle.getId(), Instant.now(), Instant.now().plusSeconds((long) days *24*60*60));
                databaseApi.addEntity(rent, "rents");
                System.out.println("Pojazd zostal wypozyczony.");
            } else {
                System.out.println("Nie mozna wypozyczyc pojazdu, jest on aktualnie wypozyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wypozyczenia pojazdu.");
            return false;
        }
        return true;
    }
}
