package hr.algebra.brassbirmingham.thread;

import hr.algebra.brassbirmingham.model.GameAction;
import hr.algebra.brassbirmingham.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTheLastGameMoveThread {

    private static final Lock FILE_LOCK = new ReentrantLock();
    private static final Condition MOVE_AVAILABLE = FILE_LOCK.newCondition();
    private static boolean moveWaiting;

    protected void saveTheLastGameMove(GameAction gameAction) {
        FILE_LOCK.lock();
        try {
            List<GameAction> gameActions = readMoves();
            gameActions.add(gameAction);
            writeMoves(gameActions);

            moveWaiting = true;
            MOVE_AVAILABLE.signalAll();
        } finally {
            FILE_LOCK.unlock();
        }
    }

    protected GameAction awaitTheLastGameMove() throws InterruptedException {
        FILE_LOCK.lock();
        try {
            while (!moveWaiting) {
                MOVE_AVAILABLE.await();
            }
            moveWaiting = false;

            List<GameAction> gameActions = readMoves();
            return gameActions.isEmpty() ? null : gameActions.getLast();
        } finally {
            FILE_LOCK.unlock();
        }
    }

    public List<GameAction> loadGameMoves() {
        FILE_LOCK.lock();
        try {
            return readMoves();
        } finally {
            FILE_LOCK.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<GameAction> readMoves() {
        Path path = Path.of(FileUtils.GAME_MOVE_HISTORY_FILE_NAME);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {
            return new ArrayList<>((List<GameAction>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(AbstractTheLastGameMoveThread.class.getName())
                    .log(Level.SEVERE, "Move history could not be read", e);
            return new ArrayList<>();
        }
    }

    private static void writeMoves(List<GameAction> gameActions) {
        Path path = Path.of(FileUtils.GAME_MOVE_HISTORY_FILE_NAME);
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(path)))) {
                oos.writeObject(gameActions);
            }
        } catch (IOException e) {
            Logger.getLogger(AbstractTheLastGameMoveThread.class.getName())
                    .log(Level.SEVERE, "Move history could not be written", e);
        }
    }
}
