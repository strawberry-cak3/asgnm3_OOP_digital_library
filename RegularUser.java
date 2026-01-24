import java.time.LocalDate;

class RegularUser extends LibraryUser {
    public RegularUser(String id, String name, LocalDate registrationDate) {
        super(id, name, registrationDate);
    }

    public RegularUser(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxBooksAllowed() {
        return 5;
    }

    @Override
    public int getLoanPeriodDays() {
        return 14;
    }
}
