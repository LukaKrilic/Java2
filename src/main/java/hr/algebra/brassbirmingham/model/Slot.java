package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;

public record Slot(City city, int index) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static Slot fromId(String id) {
        String[] parts = id.replace("slot_", "").split("_");
        return new Slot(City.fromId(parts[0]), Integer.parseInt(parts[1]));
    }
}
