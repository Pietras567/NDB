package NBD;

import java.time.LocalDateTime;
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
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        wypozyczony = true;
                        rent = r;
                    }
                }
            }

            if(wypozyczony) {
                if(rent.getClient().getId() == client.getId()) {
                    rent.setEndDate(LocalDateTime.now());
                    databaseApi.updateEntity(rent, "rents");
                    System.out.println("Pojazd zostal zwrocony.");
                } else {
                    System.out.println("Pojazd byl wypozyczony przez innego klienta. Nie mozesz go zwrocic.");
                    return false;
                }
            } else {
                System.out.println("Nie mozna zwrocic pojazdu, nie jest on aktualnie wypozyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wczesniejszego zakanczania wypozyczenia.");
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
                    if((LocalDateTime.now().isAfter(r.getStartDate()) || LocalDateTime.now().isEqual(r.getStartDate())) && (LocalDateTime.now().isBefore(r.getEndDate())) || LocalDateTime.now().isEqual(r.getEndDate())) {
                        wypozyczony = true;
                    }
                }
            }

            if(!wypozyczony) {
                Rent rent = new Rent(client.getId(), vehicle.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(days));
                databaseApi.addEntity(rent, "rents");
                System.out.println("Pojazd został wypożyczony.");
            } else {
                System.out.println("Nie można wypożyczyć pojazdu, jest on aktualnie wypożyczony.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Napotkano problem podczas proby wypozyczenia pojazdu.");
            return false;
        }
        return true;
    }
}
