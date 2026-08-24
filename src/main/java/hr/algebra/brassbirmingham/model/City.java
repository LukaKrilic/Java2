package hr.algebra.brassbirmingham.model;

public enum City {
    BIRMINGHAM("Birmingham"),
    COALBROOKDALE("Coalbrookdale"),
    COVENTRY("Coventry"),
    DUDLEY("Dudley"),
    STOKE("Stoke"),
    WALSALL("Walsall"),
    WOLVERHAMPTON("Wolverhampton"),
    WORCESTER("Worcester");

    private final String displayName;

    City(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static City fromId(String id) {
        return valueOf(id.replace("city_", "").toUpperCase());
    }
}
