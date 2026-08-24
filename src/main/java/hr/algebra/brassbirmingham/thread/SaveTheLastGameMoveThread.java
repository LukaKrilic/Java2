package hr.algebra.brassbirmingham.thread;

import hr.algebra.brassbirmingham.model.GameAction;

public class SaveTheLastGameMoveThread extends AbstractTheLastGameMoveThread implements Runnable{

    private GameAction gameAction;

    public SaveTheLastGameMoveThread(GameAction gameAction) {
        this.gameAction = gameAction;
    }

    public GameAction getGameAction() {
        return gameAction;
    }
    public void setGameAction(GameAction gameAction) {
        this.gameAction = gameAction;
    }

    @Override
    public void run() {
        saveTheLastGameMove(gameAction);
    }
}
