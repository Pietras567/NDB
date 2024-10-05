package NBD;

public class RentalApi {
    public boolean oddaj() {
        try {
            boolean wypozyczony = true; ///todo pobiera z bazy, czy jest aktualnie wypozyczony
            if(wypozyczony) {
                //zwracamy, zmieniamy w bazie na false
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public boolean wypozycz() {
        try {
            boolean wypozyczony = false; ///todo pobiera z bazy, czy jest aktualnie wypozyczony
            if(!wypozyczony) {
                //zwracamy, zmieniamy w bazie na true
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
