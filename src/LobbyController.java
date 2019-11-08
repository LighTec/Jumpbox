import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
        this.playerListView.setItems(playersObservable);
        this.currentPlayerName = Main.currentUsername;
        this.serverIp = Main.serverIp;

        // TODO TEMPORARY
        Button startGameButton = new Button("Start Game (debug button)");
        startGameButton.setOnAction(this::onStartGame);
        rightPane.getChildren().add(startGameButton);
    }

    public void sendCommand(Request request) {
        int command = request.command;
        switch (command) {
            case 1: // Initial Connection
                // TODO TCPClient: tcpClient.sendCommand(new Request(1, username));
                break;
            case 12: // Set game leader
                Player leader = (Player) request.arg[0];
                if (this.currentPlayerName.equals(leader.getUsername())) {
                    Button startGameButton = new Button("Start Game");
                    startGameButton.setOnAction(this::onStartGame);
                    rightPane.getChildren().add(startGameButton);
                } else {
                    rightPane.getChildren().add(new Text("Waiting for your game leader to start the game..."));
                }
                break;
            case 34: // TODO CONTINUE HERE command for adding new user
                playersObservable.add((String) request.arg[0]);
                break;
        }
    }

    private void onStartGame(Event e) {
        Main.router.startGame();
    }
}
