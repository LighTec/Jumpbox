
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;

import java.text.SimpleDateFormat;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class GameController implements Initializable {

    @FXML
    private ListView<String> chatBoxListView;
    @FXML
    private ListView<String> playerListView;
    @FXML
    private TextField chatField;
    @FXML
    private AnchorPane canvasParent;

    private String currentPlayerName = Main.currentUsername;
    private String serverIp = Main.serverIp;

    private ChatBox chatBox;
    private DrawingCanvas canvas;
    private ArrayList<Player> players;
    private TCPClient tcpClient;

    // resets every round
    private int timeRemaining;
    private int round;
    private String[] wordOptions;
    private String currentWord = "";
    private boolean isCurrentPlayerDrawer = false;

    private ObservableList<String> chatObservable = FXCollections.observableArrayList();
    private ObservableList<String> playersObservable = FXCollections.observableArrayList();

    public GameController(){}

    private void enterPressed(Event e)
    {
        TextField source = (TextField) e.getSource();
        System.out.println(source.getText());
        chatBoxListView.getItems().add(0, source.getText());
        source.clear();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.chatBoxListView.setItems(chatObservable);
        this.playerListView.setItems(playersObservable);

        DrawingCanvas canvastest = new DrawingCanvas();

        for (int i = 0; i < 10; i++)
        {
            playersObservable.add("player"+i);
        }
        chatField.setOnAction(this::enterPressed);

        Group root = new Group();
        Scene s = new Scene(root, 300, 300, Color.BLACK);
        final Canvas canvas = new Canvas(250,250);


        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.BLUE);
        gc.fillRect(75,75,100,100);

        root.getChildren().add(canvas);


    }

    public void sendCommand(Request request) {
        int command = request.command;
        switch (command) {

            case 2: // Close connection
                Main.router.showPage("Main menu", "mainmenu.fxml");
                break;
            case 3: // Reconnect
//                 TODO tcpClient.sendToUser(new Request(3, ))
                break;
            case 4:
                break;
            case 5:
                break;


            case 10:
                break;
            case 11:
                break;
            case 12: // Send Game Leader
                Player leader = (Player) request.arg[0];
                if (leader.getUsername().equals(currentPlayerName)) {
                    // TODO show 'start game' button
                }
                break;
            case 13:
                break;
            case 14:
                break;


            case 20: // Send time left
                int timeRemaining = (int) request.arg[0];
                // TODO update clock in ui
                break;
            case 21: // send draw options
                String[] options = {(String) request.arg[0], (String) request.arg[1], (String) request.arg[2]};
                // TODO show popup
                List<String> dialogData;
                dialogData = Arrays.asList(options);

                ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
                dialog.setTitle("Pick a Word");
                dialog.setHeaderText("Select your choice");

                Optional result = dialog.showAndWait();
                String selected = "cancelled.";

                if (result.isPresent()) {
                    selected = (String) result.get();
                }

                System.out.println("Selection: " + selected);
                // TODO send selected to tcpClient
                break;
            case 22: // Send chosen draw option
                currentWord = (String) request.arg[0];
                break;
            case 23: // Send draw leader
                Player drawer = (Player) request.arg[0];
                if (currentPlayerName.equals(drawer.getUsername())) {
                    isCurrentPlayerDrawer = true;
                    // TODO show drawer UI (enable canvas)
                } else {
                    isCurrentPlayerDrawer = false;
                }
                break;
            case 24: // Current player guessed correctly
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Wow good job");
                alert.setHeaderText("You guessed correctly!");
                alert.show();
                break;
            case 25: // New round
                newRound();
                break;
            case 26: // End of game
                Alert endOfGameAlert = new Alert(Alert.AlertType.INFORMATION);
                endOfGameAlert.setTitle("Game Over");
                endOfGameAlert.setHeaderText("The game has ended. GG");
                endOfGameAlert.show();
                Main.router.showPage("Main Menu", "mainmenu.fxml");
                break;


            case 30: // Get players and scores
                // TODO request tcpClient
                break;
            case 31: // send players and scores
                List<Player> players = new ArrayList<>();
//                for (Player p : )
                // TODO update playersList with <username>, score: <score>
                break;
            case 32:
                break;
            case 33:
                break;


            case 40:
                break;
            case 41:
                break;
            case 42:
                break;
            case 43:
                break;


            case 50:
                break;
            case 51:
                break;
            case 52:
                break;
            case 53:
                break;
            case 54:
                break;
        }
    }

    private void initialConnection(String name) {

    }

    public void newRound() {
        this.round = 0;
        resetCanvas();

        for (Player p : players) p.setDrawer(false);
    }

    public void setDrawer(Player player) {
        for (Player p : players) {
            if (p.getMacAddr().equals(player.getMacAddr())) {
                p.setDrawer(true);
            }
        }
    }



    private void resetCanvas() {
        // TODO
    }

    private void sendChosenWord() {
        // TODO send chosen word to tcp client
    }

    private void sendMessage() {
        // TODO send guess to tcp client
    }





    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public void setWordOptions(String[] wordOptions) {
        this.wordOptions = wordOptions;
        // TODO update gui
    }

    public void sendAllChat(List<Message> messages) {
        this.chatBox.clearMessages();
        for (Message m : messages) this.chatBox.addMessage(m);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Message> getAllChat() {
        return chatBox.getMessages();
    }

}