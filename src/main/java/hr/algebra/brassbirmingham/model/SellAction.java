package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public final class SellAction extends GameAction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Slot slot;

    public SellAction(Player player, Slot slot) {
        super(player);
        this.slot = slot;
    }

    public Slot getSlot() { return slot; }
}
