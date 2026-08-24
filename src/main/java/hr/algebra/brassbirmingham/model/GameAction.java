package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;

public abstract sealed class GameAction implements Serializable
        permits BuildAction, LinkAction, SellAction, LoanAction, PassAction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Player player;

    protected GameAction(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }

}

