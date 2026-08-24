package hr.algebra.brassbirmingham.viewmodel;

import hr.algebra.brassbirmingham.engine.ApplyResult;
import hr.algebra.brassbirmingham.engine.GameEngine;
import hr.algebra.brassbirmingham.engine.GameRules;
import hr.algebra.brassbirmingham.model.GameAction;
import hr.algebra.brassbirmingham.model.GamePhase;
import hr.algebra.brassbirmingham.model.GameState;
import hr.algebra.brassbirmingham.model.Player;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class GameViewModel {
    private final GameEngine engine;

    private final StringProperty turn = new SimpleStringProperty();
    private final IntegerProperty money = new SimpleIntegerProperty();
    private final IntegerProperty income = new SimpleIntegerProperty();
    private final StringProperty round = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("Spremno");
    private final BooleanProperty finished = new SimpleBooleanProperty();
    private final ObservableList<String> log = FXCollections.observableArrayList();
    private final int localPlayerIndex;
    private final BooleanProperty myTurn = new SimpleBooleanProperty();

    public GameViewModel(GameEngine engine, int localPlayerIndex) {
        this.engine = engine;
        this.localPlayerIndex = localPlayerIndex;
        refresh();
    }

    public boolean play(GameAction gameAction) {
        ApplyResult result = engine.apply(gameAction);
        if (result.accepted()) {
            status.set("Spremno");
        } else {
            status.set(result.message());
        }
        refresh();
        return result.accepted();
    }
    private void refresh(){
        GameState state = engine.getState();
        Player currentPlayer = state.getCurrentPlayer();

        turn.set("Na potezu: " + currentPlayer.getName());
        money.set(currentPlayer.getMoney());
        income.set(currentPlayer.getIncome());
        round.set(Math.min(state.getRound(), GameRules.ROUNDS) + "/" + GameRules.ROUNDS);
        boolean over = state.getPhase() == GamePhase.FINISHED;
        finished.set(over);
        myTurn.set(!over && (localPlayerIndex < 0 || state.getPlayers().indexOf(currentPlayer) == localPlayerIndex));

        if (!log.equals(state.getLog())) {
            log.setAll(state.getLog());
        }

        if (!over && !myTurn.get()) {
            status.set("Čekanje na potez protivnika...");
        }
    }

    public String finalScoreText() {
        StringBuilder result = new StringBuilder();
        for (Player player : engine.getState().getPlayers()) {
            result.append(player.getName())
                    .append(": ")
                    .append(engine.finalScore(player))
                    .append(" bodova\n");
        }
        return result.append('\n').append(winnerText()).toString();
    }

    private String winnerText() {
        List<Player> players = engine.getState().getPlayers();
        int best = players.stream().mapToInt(engine::finalScore).max().orElse(0);
        List<String> winners = players.stream()
                .filter(player -> engine.finalScore(player) == best)
                .map(Player::getName)
                .toList();
        return winners.size() == 1 ? "Pobjednik: " + winners.getFirst() + "!" : "Nerijeseno.";
    }

    public GameEngine getEngine() {return engine;}
    public ObservableList<String> getLog() {return log;}

    public ReadOnlyStringProperty turnProperty() { return turn; }
    public ReadOnlyIntegerProperty moneyProperty() { return money; }
    public ReadOnlyIntegerProperty incomeProperty() { return income; }
    public ReadOnlyStringProperty roundProperty() { return round; }
    public ReadOnlyStringProperty statusProperty() { return status; }
    public ReadOnlyBooleanProperty finishedProperty() { return finished; }
    public ReadOnlyBooleanProperty myTurnProperty() { return myTurn; }
}
