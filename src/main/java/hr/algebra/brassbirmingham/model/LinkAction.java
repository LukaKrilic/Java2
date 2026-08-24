package hr.algebra.brassbirmingham.model;

import java.io.Serial;

public final class LinkAction extends GameAction {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Link link;

    public LinkAction(Player player, Link link) {
        super(player);
        this.link = link;
    }
    public Link getLink() { return link; }
}
