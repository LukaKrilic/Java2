package hr.algebra.brassbirmingham.model;

public enum IndustryType {
    COAL_MINE("Rudnik ugljena", 5),
    IRON_WORKS("Tvornica željeza", 5),
    COTTON_MILL("Tvornica pamuka", 12);

    private final String displayName;
    private final int cost;

    IndustryType (String displayName, int cost) {
        this.cost = cost;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    public int getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return displayName + " (" + cost + " £)";
    }

}
