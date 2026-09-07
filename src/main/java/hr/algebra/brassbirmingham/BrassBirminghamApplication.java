package hr.algebra.brassbirmingham;

import hr.algebra.brassbirmingham.controller.BrassBirminghamController;
import hr.algebra.brassbirmingham.jndi.ConfigurationKey;
import hr.algebra.brassbirmingham.jndi.ConfigurationReader;
import hr.algebra.brassbirmingham.model.GameState;
import hr.algebra.brassbirmingham.model.PlayerType;
import hr.algebra.brassbirmingham.rmi.RMIServer;
import hr.algebra.brassbirmingham.utils.DialogUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrassBirminghamApplication extends Application {

    public static PlayerType playerType;
    public static final int PORT_PLAYER_1 = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_ONE_SERVER_PORT);
    public static final int PORT_PLAYER_2 = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.PLAYER_TWO_SERVER_PORT);
    public static final String HOST = ConfigurationReader.getStringValueForKey(ConfigurationKey.HOST_NAME);
    private static BrassBirminghamController controller;

    @Override
    public void start(Stage stage) throws IOException {
        RMIServer.start();
        FXMLLoader fxmlLoader = new FXMLLoader(BrassBirminghamApplication.class.getResource("brass-birmingham-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        controller = fxmlLoader.getController();
        stage.setTitle("Brass Birmingham - " + playerType);
        stage.setScene(scene);
        stage.show();

        if (playerType != PlayerType.SINGLE_PLAYER) {
            Runnable server = playerType == PlayerType.PLAYER_1
                    ? this::acceptRequestsPlayerOne
                    : this::acceptRequestsPlayerTwo;
            Thread serverThread = new Thread(server);
            serverThread.setDaemon(true);
            serverThread.start();
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    private void acceptRequestsPlayerTwo() {
        try (ServerSocket serverSocket = new ServerSocket(PORT_PLAYER_2)) {
            System.err.printf("[%s] Server listening on port %d%n", playerType, serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.printf("[%s] Client connected from port %s%n", playerType, clientSocket.getPort());
                new Thread(() -> processSerializableClient(clientSocket)).start();
            }
        } catch (IOException e) {
            Logger.getLogger(BrassBirminghamApplication.class.getName())
                    .log(Level.SEVERE, "Send failed", e);
            Platform.runLater(() -> DialogUtils.showAlertDialog("Mreza",
                    "Protivnik nije dostupan: " + e.getMessage(),
                    Alert.AlertType.ERROR));
        }
    }

    private void acceptRequestsPlayerOne() {
        try (ServerSocket serverSocket = new ServerSocket(PORT_PLAYER_1)) {
            System.err.printf("[%s] Server listening on port %d%n", playerType, serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.printf("[%s] Client connected from port %s%n", playerType, clientSocket.getPort());
                new Thread(() -> processSerializableClient(clientSocket)).start();
            }
        } catch (IOException e) {
            Logger.getLogger(BrassBirminghamApplication.class.getName())
                    .log(Level.SEVERE, "Send failed", e);
            Platform.runLater(() -> DialogUtils.showAlertDialog("Mreza",
                    "Protivnik nije dostupan: " + e.getMessage(),
                    Alert.AlertType.ERROR));
        }
    }

    private void processSerializableClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream((clientSocket).getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream((clientSocket).getOutputStream())
        ) {
            GameState gameState = (GameState) ois.readObject();
            controller.restoreGameState(gameState);

            System.out.println("[" + playerType + "] Received game state: " + gameState);
            oos.writeObject("success");

        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            playerType = PlayerType.SINGLE_PLAYER;
        } else {
            try {
                playerType = PlayerType.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                Logger.getLogger(BrassBirminghamApplication.class.getName())
                        .log(Level.SEVERE, "Invalid player type argument: " + args[0]);
                System.exit(1);
            }
        }
        launch();

    }
    public static void sendRequestPlayerOne(GameState gameState) {
        try(Socket clientSocket = new Socket(HOST, PORT_PLAYER_2)){
            System.err.printf("[%s] Connected to server at %s:%d%n", playerType, clientSocket.getInetAddress(), clientSocket.getPort());

            sendSerializableRequest(clientSocket, gameState);

        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public static void sendRequestPlayerTwo(GameState gameState) {
        try(Socket clientSocket = new Socket(HOST, PORT_PLAYER_1)){
            System.err.printf("[%s] Connected to server at %s:%d%n", playerType, clientSocket.getInetAddress(), clientSocket.getPort());

            sendSerializableRequest(clientSocket, gameState);

        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    private static void sendSerializableRequest(Socket clientSocket, GameState gameState) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

        oos.writeObject(gameState);
        System.out.println("[" + playerType + "] Sent game state: ");
        System.out.println("[" + playerType + "]" + ois.readObject());

    }
}
