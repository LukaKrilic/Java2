package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public class CoalMine extends Industry {

    @Serial
    private static final long serialVersionUID = 1L;

    public CoalMine(Player owner, Slot slot) {
        super(owner, slot); }
    @Override public IndustryType type()   { return IndustryType.COAL_MINE; }
    @Override public int income()          { return 4; }
    @Override public int victoryPoints()   { return 1; }
    @Override public boolean consumesCoal(){ return false; }
}
