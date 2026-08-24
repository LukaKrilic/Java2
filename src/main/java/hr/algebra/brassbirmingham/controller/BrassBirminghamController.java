package hr.algebra.brassbirmingham.controller;

import hr.algebra.brassbirmingham.BrassBirminghamApplication;
import hr.algebra.brassbirmingham.engine.GameEngine;
import hr.algebra.brassbirmingham.model.*;
import hr.algebra.brassbirmingham.rmi.ChatRemoteService;
import hr.algebra.brassbirmingham.rmi.LobbyRemoteService;
import hr.algebra.brassbirmingham.rmi.RMIServer;
import hr.algebra.brassbirmingham.thread.ReadTheLastGameMoveThread;
import hr.algebra.brassbirmingham.thread.SaveTheLastGameMoveThread;
import hr.algebra.brassbirmingham.utils.DialogUtils;
import hr.algebra.brassbirmingham.utils.DocumentationUtils;
import hr.algebra.brassbirmingham.utils.GameUtils;
import hr.algebra.brassbirmingham.utils.XmlUtils;
import hr.algebra.brassbirmingham.viewmodel.GameViewModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrassBirminghamController {

    @FXML
    public MenuItem loadMovesItem;
    public MenuItem chooseOpponentItem;
    @FXML
    private Pane mapPane;

    @FXML
    private Button loanBtn;
    @FXML
    private Button skipBtn;
    @FXML
    private TextArea chatList;
    @FXML
    private TextField chatInput;
    @FXML
    private ListView<String> logList;

    @FXML
    private Label turnLbl;

    @FXML
    private Label moneyLbl;

    @FXML
    private Label incomeLbl;

    @FXML
    private Label roundsLbl;

    @FXML
    private Label statusLbl;

    private GameViewModel viewModel;
    private final Map<Slot, Rectangle> slotNodes = new HashMap<>();
    private final Map<Link, Line> linkNodes = new HashMap<>();

    private ChatRemoteService chatRemoteService;

    private boolean movesThroughFile;
    
    private LobbyRemoteService lobbyRemoteService;

    @FXML
    private void initialize() {

        for (Node node : mapPane.getChildren()) {
            String id = node.getId();
            if (id == null) {
                continue;
            }
            if (node instanceof Rectangle rectangle && id.startsWith("slot_")) {
                slotNodes.put(Slot.fromId(id), rectangle);
            } else if (node instanceof Line line && id.startsWith("link_")) {
                linkNodes.put(Link.fromId(id), line);
            }
        }
            logList.setCellFactory(list -> new ListCell<>() {
                private final Label cellLabel = new Label();

                {
                    cellLabel.setWrapText(true);
                    cellLabel.maxWidthProperty().bind(list.widthProperty().subtract(24));
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        cellLabel.setText(item);
                        setGraphic(cellLabel);
                    }
                }
            });

        connectToChat();
        connectToLobby();

        if (BrassBirminghamApplication.playerType == PlayerType.SINGLE_PLAYER) {
            movesThroughFile = true;
            Thread moveReader = new Thread(new ReadTheLastGameMoveThread(this::applyMove), "move-reader");
            moveReader.setDaemon(true);
            moveReader.start();
        }

        loadMovesItem.setDisable(!movesThroughFile);

        startSession(GameUtils.newGame());
    }

    private void connectToLobby() {
        if (BrassBirminghamApplication.playerType == PlayerType.SINGLE_PLAYER) {
            chooseOpponentItem.setDisable(true);
            return;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(RMIServer.HOSTNAME, RMIServer.RMI_PORT);
            lobbyRemoteService = (LobbyRemoteService)
                    registry.lookup(LobbyRemoteService.REMOTE_OBJECT_NAME);
            lobbyRemoteService.register(
                    BrassBirminghamApplication.playerType.name(),
                    BrassBirminghamApplication.localPort());
        } catch (RemoteException | NotBoundException e) {
            lobbyRemoteService = null;
            chooseOpponentItem.setDisable(true);
            Logger.getLogger(BrassBirminghamController.class.getName())
                    .log(Level.WARNING, "Lobby unavailable", e);
        }
    }

    private void connectToChat() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIServer.HOSTNAME, RMIServer.RMI_PORT);
            chatRemoteService = (ChatRemoteService) registry.lookup(ChatRemoteService.REMOTE_OBJECT_NAME);
            getChatRefreshTimeline().play();
        } catch (RemoteException | NotBoundException e) {
            chatRemoteService = null;
            Logger.getLogger(BrassBirminghamController.class.getName())
                    .log(Level.WARNING, "Chat unavailable", e);
        }
    }

    private Timeline getChatRefreshTimeline() {
        Timeline chatMessagesRefreshTimeline = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                List<String> chatMessages = chatRemoteService.getAllMessages();
                StringBuilder textMessageBuilder = new StringBuilder();

                for (String message : chatMessages) {
                    textMessageBuilder.append(message).append("\n");
                }
                chatList.setText(textMessageBuilder.toString());
                chatList.setScrollTop(Double.MAX_VALUE);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }), new KeyFrame(Duration.seconds(1)));

        chatMessagesRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        return chatMessagesRefreshTimeline;
    }

    private int localPlayerIndex(){
        return switch (BrassBirminghamApplication.playerType) {
            case SINGLE_PLAYER -> -1;
            case PLAYER_1 -> 0;
            case PLAYER_2 -> 1;
        };
    }

    private void startSession(GameState state) {
        viewModel = new GameViewModel(new GameEngine(state), localPlayerIndex());

        turnLbl.textProperty().bind(viewModel.turnProperty());
        moneyLbl.textProperty().bind(viewModel.moneyProperty().asString());
        incomeLbl.textProperty().bind(viewModel.incomeProperty().asString());
        roundsLbl.textProperty().bind(viewModel.roundProperty());
        statusLbl.textProperty().bind(viewModel.statusProperty());

        logList.setItems(viewModel.getLog());

        loanBtn.disableProperty().bind(viewModel.myTurnProperty().not());
        skipBtn.disableProperty().bind(viewModel.myTurnProperty().not());
        mapPane.disableProperty().bind(viewModel.myTurnProperty().not());

        viewModel.finishedProperty().addListener((obs, wasFinished, isFinished) -> {
            if (Boolean.TRUE.equals(isFinished)) {
                Platform.runLater(this::finishGame);
            }
        });
        paintBoard();
    }

    private void finishGame() {
        DialogUtils.showAlertDialog("Kraj igre", viewModel.finalScoreText(),
                Alert.AlertType.INFORMATION);
        XmlUtils.deleteMoveHistory();
        GameState state = GameUtils.newGame();
        startSession(state);
        broadcast(state);
    }


    private Player currentPlayer() {
        return viewModel.getEngine().getState().getCurrentPlayer();
    }

    private void play(GameAction action) {
        if (movesThroughFile) {
            Thread moveWriter = new Thread(new SaveTheLastGameMoveThread(action), "move-writer");
            moveWriter.setDaemon(true);
            moveWriter.start();
            return;
        }
        applyMove(action);
    }

    private void applyMove(GameAction action) {
        GameAction move = rebind(action, currentPlayer());
        if (viewModel.play(move) && movesThroughFile) {
            XmlUtils.saveNewMove(move);
        }
        paintBoard();
        broadcast(viewModel.getEngine().getState());
    }

    private GameAction rebind(GameAction action, Player player) {
        return switch (action) {
            case BuildAction a -> new BuildAction(player, a.getSlot(), a.getIndustryType());
            case LinkAction a  -> new LinkAction(player, a.getLink());
            case SellAction a  -> new SellAction(player, a.getSlot());
            case LoanAction a  -> new LoanAction(player);
            case PassAction a  -> new PassAction(player);
        };
    }

    private void broadcast(GameState state) {
        if (BrassBirminghamApplication.playerType == PlayerType.SINGLE_PLAYER) {
            return;
        }
        new Thread(() -> BrassBirminghamApplication.sendGameState(state)).start();
    }

    private void promptBuild(Slot slot) {
        DialogUtils.chooseIndustryDialog()
                .ifPresent(type -> play(new BuildAction(currentPlayer(), slot, type)));
    }

    private void paintBoard() {
        GameState state = viewModel.getEngine().getState();

        for (Map.Entry<Slot, Rectangle> entry : slotNodes.entrySet()) {
            Industry industry = state.getBoard().get(entry.getKey());
            entry.getValue().getStyleClass().setAll(slotClasses(industry));
        }

        for (Map.Entry<Link, Line> entry : linkNodes.entrySet()) {
            Player owner = state.getCanals().get(entry.getKey());
            entry.getValue().getStyleClass().setAll(routeClasses(owner));
        }
    }

    private List<String> slotClasses(Industry industry) {
        if (industry == null) {
            return List.of("slot", "empty");
        }
        List<String> classes = new ArrayList<>(
                List.of("slot", "filled", ownerClass(industry.getOwner()), typeClass(industry.type())));
        if (industry.isFlipped()) {
            classes.add("flipped");
        }
        return classes;
    }

    private List<String> routeClasses(Player owner) {
        if (owner == null) {
            return List.of("route");
        }
        return List.of("route", "route-built", ownerClass(owner));
    }

    private String ownerClass(Player owner) {
        return "owner-" + (viewModel.getEngine().getState().getPlayers().indexOf(owner) + 1);
    }

    private String typeClass(IndustryType type) {
        return type.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @FXML
    protected void onLoan() {
        play(new LoanAction(currentPlayer()));
    }

    @FXML
    protected void onSkip() {
        play(new PassAction(currentPlayer()));

    }
    @FXML
    protected void onSendChat() {
        String chatMessage = chatInput.getText();
        if (chatRemoteService == null) {
            return;
        }
        try {
            chatRemoteService.sendChatMessage(BrassBirminghamApplication.playerType + ": " + chatMessage);
            chatInput.clear();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void circlePressed(MouseEvent mouseEvent) {
        City city = City.fromId(((Node) mouseEvent.getSource()).getId());
        Map<Slot, Industry> board = viewModel.getEngine().getState().getBoard();

        Optional<Slot> free = slotNodes.keySet().stream()
                .filter(slot -> slot.city() == city && !board.containsKey(slot))
                .min(Comparator.comparingInt(Slot::index));

        if (free.isEmpty()) {
            DialogUtils.showAlertDialog("Izgradnja",
                    "U gradu " + city.getDisplayName() + " nema slobodnog mjesta.",
                    Alert.AlertType.WARNING);
            return;
        }
        promptBuild(free.get());
    }

    @FXML
    protected void linePressed(MouseEvent mouseEvent) {
        Link link = Link.fromId(((Node) mouseEvent.getSource()).getId());
        play(new LinkAction(currentPlayer(), link));
    }

    @FXML
    public void slotPressed(MouseEvent mouseEvent) {
        Slot slot = Slot.fromId(((Node) mouseEvent.getSource()).getId());
        Industry industry = viewModel.getEngine().getState().getBoard().get(slot);

        if (industry == null) {
            promptBuild(slot);
        } else {
            play(new SellAction(currentPlayer(), slot));
        }
    }

    public void startNewGame() {
        if (DialogUtils.confirm("Nova igra", "Trenutna igra ce biti izgubljena. Nastaviti?")) {
            XmlUtils.deleteMoveHistory();
            GameState state = GameUtils.newGame();
            startSession(state);
            broadcast(state);
        }
    }

    public void saveGame() {
        GameUtils.save(viewModel.getEngine().getState());
        try {
            DialogUtils.showAlertDialog("Spremanje", "Igra je spremljena.",
                    Alert.AlertType.INFORMATION);
        } catch (RuntimeException e) {
            DialogUtils.showAlertDialog("Greška prilikom spremanja!", "Došlo je do greške prilikom spremanja igre: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    public void loadGame() {
        XmlUtils.deleteMoveHistory();
        GameState state = GameUtils.load();
        startSession(state);
        broadcast(state);
        DialogUtils.showAlertDialog("Učitavanje", "Igra je učitana.",
                Alert.AlertType.INFORMATION);
    }

    public void generateDocumentation() {
        try{
            DocumentationUtils.generateDocumentation();
            DialogUtils.showAlertDialog("Uspješno generirana dokumentacija!",
                    "HTML dokumentacija za aplikaciju je uspješno generirana!",
                    Alert.AlertType.INFORMATION);
        }catch(IOException e){
            DialogUtils.showAlertDialog("Greška prilikom generiranja dokumentacije!",
                    "Došlo je do greške prilikom generiranja dokumentacije: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    public void exitGame() {
        if (DialogUtils.confirm("Izlaz", "Jeste li sigurni da želite izaći iz igre?")) {
            Platform.exit();
        }
    }

    public void restoreGameState(GameState gameState) {
            Platform.runLater(() -> loadGameState(gameState));
    }

    private void loadGameState(GameState gameState) {
        startSession(gameState);
    }

    public void loadMoveHistory() {
        if (!movesThroughFile){
            return;
        }
        if (!DialogUtils.confirm("Ucitavanje poteza",
                "Trenutna igra ce biti zamijenjena povijescu poteza. Nastaviti?")) {
            return;
        }
        try {
            List<GameAction> moves = XmlUtils.loadGameMoves();
            startSession(GameUtils.newGame());
            for (GameAction move : moves) {
                viewModel.play(rebind(move, currentPlayer()));
            }
            paintBoard();
            broadcast(viewModel.getEngine().getState());
            DialogUtils.showAlertDialog("Ucitavanje poteza",
                    moves.size() + " poteza je ucitano.", Alert.AlertType.INFORMATION);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            DialogUtils.showAlertDialog("Greska prilikom ucitavanja poteza",
                    e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void chooseOpponent(ActionEvent actionEvent) {
        if (lobbyRemoteService == null) {
            return;
        }
        try {
            Map<String, Integer> players = new TreeMap<>(lobbyRemoteService.getAvailablePlayers());
            players.remove(BrassBirminghamApplication.playerType.name());

            if (players.isEmpty()) {
                DialogUtils.showAlertDialog("Odabir suigraca",
                        "Trenutno nema dostupnih suigraca.", Alert.AlertType.INFORMATION);
                return;
            }

            DialogUtils.chooseOpponentDialog(players.keySet()).ifPresent(name -> {
                BrassBirminghamApplication.opponentPort = players.get(name);
                DialogUtils.showAlertDialog("Odabir suigraca",
                        "Odabrani suigrac: " + name, Alert.AlertType.INFORMATION);
            });
        } catch (RemoteException e) {
            DialogUtils.showAlertDialog("Odabir suigraca",
                    "Lobby nije dostupan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
