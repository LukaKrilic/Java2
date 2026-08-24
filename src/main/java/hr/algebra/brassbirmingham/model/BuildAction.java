package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public final class BuildAction extends GameAction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Slot slot;
    private final IndustryType industryType;

    public BuildAction(Player player,Slot slot, IndustryType industryType) {
        super(player);
        this.slot = slot;
        this.industryType = industryType;
    }
    public Slot getSlot() { return slot; }
    public IndustryType getIndustryType() { return industryType; }

}
