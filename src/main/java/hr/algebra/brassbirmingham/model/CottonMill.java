package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public class CottonMill extends Industry {

    @Serial
    private static final long serialVersionUID = 1L;

    public CottonMill(Player owner, Slot slot) {
        super(owner, slot);
    }
    @Override public IndustryType type()   { return IndustryType.COTTON_MILL; }
    @Override public int income()          { return 5; }
    @Override public int victoryPoints()   { return 5; }
    @Override public boolean sellable() { return true; }
}
