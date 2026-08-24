package hr.algebra.brassbirmingham.model;

public class IronWorks extends Industry {
    public IronWorks(Player owner, Slot slot) {
        super(owner, slot);
    }
    @Override public IndustryType type()   { return IndustryType.IRON_WORKS; }
    @Override public int income()          { return 3; }
    @Override public int victoryPoints()   { return 3; }
}
