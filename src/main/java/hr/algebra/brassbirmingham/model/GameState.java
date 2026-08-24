package hr.algebra.brassbirmingham.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Player> players;
    private final Map <Slot, Industry> board = new HashMap<>();
    private final Map <Link, Player> canals = new HashMap<>();
    private final List <GameAction> history = new ArrayList<>();
    private List<String> log = new ArrayList<>();

    private int currentPlayerIndex;
    private int round = 1;
    private GamePhase phase = GamePhase.IN_PROGRESS;

    public GameState(List<Player> players) {
        this.players = List.copyOf(players);
    }

    public List<Player> getPlayers() { return players; }
    public Map<Slot, Industry> getBoard() { return board; }
    public Map<Link, Player> getCanals() { return canals; }
    public List<GameAction> getHistory() { return history; }
    public List<String> getLog() {
        if (log == null) {
            log = new ArrayList<>();
        }
        return log;
    }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public int getRound() { return round; }
    public GamePhase getPhase() { return phase; }

    public void setCurrentPlayerIndex(int i) { currentPlayerIndex = i; }
    public void setRound(int r) { round = r; }
    public void setPhase(GamePhase p) { phase = p; }
}
