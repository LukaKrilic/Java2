package hr.algebra.brassbirmingham.model;

import java.util.Arrays;
import java.util.Optional;

public enum GameActionTag {
    GAME_MOVES("gameMoves"),
    BUILD("build"),
    LINK("link"),
    SELL("sell"),
    LOAN("loan"),
    PASS("pass"),
    PLAYER("player"),
    CITY("city"),
    SLOT_INDEX("slotIndex"),
    INDUSTRY_TYPE("industryType"),
    FROM_CITY("fromCity"),
    TO_CITY("toCity");

    private final String tagName;

    GameActionTag(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public static Optional<GameActionTag> from(String tagName) {
        return Arrays.stream(values())
                .filter(tag -> tag.tagName.equals(tagName))
                .findFirst();
    }
}
