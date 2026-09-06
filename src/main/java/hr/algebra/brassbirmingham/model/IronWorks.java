package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public class IronWorks extends Industry {

    @Serial
    private static final long serialVersionUID = 1L;

    public IronWorks(Player owner, Slot slot) {
        super(owner, slot);
    }
    @Override public IndustryType type()   { return IndustryType.IRON_WORKS; }
    @Override public int income()          { return 3; }
    @Override public int victoryPoints()   { return 3; }
}
