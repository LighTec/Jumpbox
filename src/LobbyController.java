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
    private TCPClient tcpClient;

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
        this.tcpClient = Main.tcpClient;

        tcpClient.sendFromUser(new Request(1, new Object[]{currentPlayerName, serverIp}));

        // wait for server response
        Thread t = new Thread() {
            @Override
            public void run() {
                tcpClient.handleServerCommand();
            }
        };
        t.start();
    }

    public void sendCommand(Request request) {
        int command = request.command;
        switch (command) {
            case 12: // Set game leader
                Player leader = (Player) request.arg[0];
                if (this.currentPlayerName.equals(leader.getUsername().trim())) {
                    Button startGameButton = new Button("Start Game");
                    startGameButton.setOnAction(this::onStartGame);
                    rightPane.getChildren().add(startGameButton);
                } else {
                    rightPane.getChildren().add(new Text("Waiting for your game leader to start the game..."));
                }
                break;
            case 34: // A new player joined the game
                playersObservable.add((String) request.arg[0]);
                break;
            case 31:
                playersObservable.clear();
                for (Object o : request.arg) {
                    playersObservable.add(((Player) o).getUsername());
                }
                break;
        }
    }

    private void onStartGame(Event e) {
        Main.router.startGame();
    }
}
