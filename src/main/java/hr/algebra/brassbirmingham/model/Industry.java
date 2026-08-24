package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;

public abstract class Industry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Player owner;
    private final Slot slot;
    private boolean flipped;

    protected Industry(Player owner, Slot slot) {
        this.owner = owner;
        this.slot = slot;
    }

    public abstract IndustryType type();
    public int cost() { return type().getCost(); }
    public abstract int income();
    public abstract int victoryPoints();

    public boolean consumesCoal() { return true; }
    public boolean sellable()     { return false; }

    public Player getOwner()  { return owner; }
    public Slot getSlot()     { return slot; }
    public boolean isFlipped(){ return flipped; }
    public void flip()        { flipped = true; }

    public static Industry of(IndustryType type, Player owner, Slot slot) {
        return switch (type) {
            case COAL_MINE   -> new CoalMine(owner, slot);
            case IRON_WORKS  -> new IronWorks(owner, slot);
            case COTTON_MILL -> new CottonMill(owner, slot);
        };
    }
}
