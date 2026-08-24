package hr.algebra.brassbirmingham.thread;


import hr.algebra.brassbirmingham.model.GameAction;
import javafx.application.Platform;

import java.util.function.Consumer;

public class ReadTheLastGameMoveThread extends AbstractTheLastGameMoveThread implements Runnable {

    private final Consumer<GameAction> moveApplier;

    public ReadTheLastGameMoveThread(Consumer<GameAction> moveApplier) {
        this.moveApplier = moveApplier;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                GameAction gameAction = awaitTheLastGameMove();
                if (gameAction != null) {
                    Platform.runLater(() -> moveApplier.accept(gameAction));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
