package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public final class PassAction extends GameAction {

    @Serial
    private static final long serialVersionUID = 1L;

    public PassAction(Player player) {
        super(player);
    }
}
