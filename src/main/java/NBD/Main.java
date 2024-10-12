package NBD;

public class Main {
    public static void main(String[] args) {
        System.out.println("Witamy w CarRental!");

        DatabaseApi Api = new DatabaseApi();

        // Dodawanie pojazdu
        Car car = new Car("Renault", 1200, 120, 4);
        Api.addVehicle(car);

        // Pobieranie pojazdu
        Vehicle retrievedVehicle = Api.getVehicle(car.getId());
        System.out.println("Retrieved Vehicle: " + retrievedVehicle.getName());
        System.out.println("Retrieved Vehicle power: " + retrievedVehicle.getPower());
        System.out.println("Retrieved Vehicle id: " + retrievedVehicle.getId());

        // Aktualizacja pojazdu
        retrievedVehicle.setName("Updated Renault");
        Api.updateVehicle(retrievedVehicle);

        // Usuwanie pojazdu
        Api.deleteVehicle(retrievedVehicle.getId());
    }
}