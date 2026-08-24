package hr.algebra.brassbirmingham.utils;

import hr.algebra.brassbirmingham.engine.GameRules;
import hr.algebra.brassbirmingham.model.GameState;
import hr.algebra.brassbirmingham.model.Player;

import java.io.*;
import java.util.List;

public class GameUtils {
    private static final String SAVE_GAME_FILE_PATH = "game/save.ser";
    private static final String PLAYER_1_NAME = "Player 1";
    private static final String PLAYER_2_NAME = "Player 2";

    private GameUtils() {}

    public static void save(GameState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_GAME_FILE_PATH))) {
            oos.writeObject(state);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static GameState load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_GAME_FILE_PATH))) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static GameState newGame() {
        return new GameState(List.of(
                new Player(PLAYER_1_NAME, GameRules.STARTING_MONEY),
                new Player(PLAYER_2_NAME, GameRules.STARTING_MONEY)
        ));
    }

}
