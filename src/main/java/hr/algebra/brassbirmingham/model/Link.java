package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;

public record Link (City from, City to) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public Link {
        if (from .ordinal() > to.ordinal()) {
            City temp = from;
            from = to;
            to = temp;
        }
    }
    public static Link fromId(String id) {
        String[] parts = id.replace("link_", "").split("_");
        return new Link(City.fromId(parts[0]), City.fromId(parts[1]));
    }
}
