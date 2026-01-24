import java.time.LocalDate;

class PremiumUser extends LibraryUser {
    public PremiumUser(String id, String name, LocalDate registrationDate) {
        super(id, name, registrationDate);
    }

    public PremiumUser(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBooksAllowed() {
        return 15;
    }

    @Override
    public int getLoanPeriodDays() {
        return 30;
    }
}
