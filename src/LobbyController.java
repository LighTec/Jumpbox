import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class LobbyController implements Initializable {

    private String currentPlayerName;
    private String serverIp;

    @FXML
    private ListView<String> playerListView;
    @FXML
    private HBox rightPane;

    private ObservableList<String> playersObservable = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TCPClient.lobbyController = this;
        this.playerListView.setItems(playersObservable);
        this.currentPlayerName = Main.currentUsername;
        this.serverIp = Main.serverIp;

        TCPClient.sendFromUser(new Request(Main.INITIAL_CMD, new Object[]{currentPlayerName, serverIp}));
    }

    public void sendCommand(Request request) {
        System.out.println(Thread.currentThread().getName() + " in control");

        int command = request.command;
        switch (command) {
            case 12: // Set game leader
                Player leader = (Player) request.arg[0];
                if (this.currentPlayerName.equals(leader.getUsername().trim())) {
                    Button startGameButton = new Button("Start Game");
                    startGameButton.setOnAction(this::onStartGame);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            rightPane.getChildren().add(startGameButton);
                        }
                    });
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            rightPane.getChildren().add(new Text("Waiting for your game leader to start the game..."));
                        }
                    });
                }
                break;
            case 14: // Start of game
                //Main.t.pauseTCP();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Running game...");
                        Main.router.startGame();
                    }
                });
                break;
            case 34: // A new player joined the game
                playersObservable.add((String) request.arg[0]);
                Main.playerNames.add((String) request.arg[0]);
                break;
            case 31:
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        playersObservable.clear();
                        Main.playerNames.clear();
                        for (Object o : request.arg) {
                            playersObservable.add(((Player) o).getUsername());
                            Main.playerNames.add(((Player) o).getUsername());
                        }
                    }
                });

                break;
        }
    }

    private void onStartGame(Event e) {
        TCPClient.sendFromUser(
                new Request(
                        13,
                        new Object[]{"skribble"}
                )
        );
        Main.router.startGame();
    }
}
