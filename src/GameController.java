
import javafx.application.Platform;
import javafx.scene.control.*;

import java.io.IOException;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private ListView<String> chatBoxListView;
    @FXML
    private ListView<String> playerListView;
    @FXML
    private TextField chatField;
    @FXML
    private AnchorPane canvasParent;
    @FXML
    private Label gameTitle;
    @FXML
    private Label gameStatusText;
    @FXML
    private Button clearDrawing;

    private String currentPlayerName = Main.currentUsername;
    private String serverIp = Main.serverIp;

    private ChatBox chatBox;
    private DrawingCanvas canvas;
    private ArrayList<Player> players;
    private TCPClient tcpClient = Main.tcpClient;
    private Thread t;

    // resets every round
    private int timeRemaining;
    private int round;
    private String[] wordOptions;
    private String currentWord = "";

    private ObservableList<String> chatObservable = FXCollections.observableArrayList();
    private ObservableList<String> playersObservable = FXCollections.observableArrayList();

    public GameController(){}

    private void sendMessage(Event e)
    {
        TextField source = (TextField) e.getSource();
        String message = source.getText();
        System.out.println(message);
        source.clear();

        Message newMessage = new Message(message, currentPlayerName);
        tcpClient.sendFromUser(new Request(42, new Object[]{newMessage}));
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        TCPClient.gameController = this;
        this.chatBoxListView.setItems(chatObservable);
        this.playerListView.setItems(playersObservable);

        chatField.setOnAction(this::sendMessage);

        canvas = new DrawingCanvas(this, canvasParent);

        // wait for server response
        t = new Thread() {
            @Override
            public void run() {
                IOException e = tcpClient.handleServerCommand();
                while (e == null) {
                    e = tcpClient.handleServerCommand();
                }
            }
        };
        t.start();
    }

    public void sendCommand(Request request) {
        int command = request.command;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (command) {

                    case 2: // Close connection
                        goToMainMenu();
                        break;
                    case 4: // An error occurred
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        String s = "Something went wrong.";
                        alert.setContentText(s);
                        alert.showAndWait();
                        break;


                    case 20: // Send time left
                        timeRemaining = (int) request.arg[0];
                        String defaultText = "Draw here";
                        gameTitle.setText(defaultText + "\t Time Remaining: " + timeRemaining);
                        t.start();
                        break;
                    case 21: // send draw options
                        String[] options = {(String) request.arg[0], (String) request.arg[1], (String) request.arg[2]};
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
                        tcpClient.sendFromUser(new Request(22, new Object[]{selected}));
                        break;
                    case 22: // Send chosen draw option
                        currentWord = (String) request.arg[0];
                        break;
                    case 23: // Send draw leader
                        Player drawer = (Player) request.arg[0];
                        setDrawer(drawer);
                        break;
                    case 24: // Current player guessed correctly
                        Alert error = new Alert(Alert.AlertType.INFORMATION);
                        error.setTitle("Wow good job");
                        error.setHeaderText("You guessed correctly!");
                        error.show();
                        break;
                    case 25: // New round
                        newRound();
                        break;
                    case 26: // End of game
                        Alert endOfGameAlert = new Alert(Alert.AlertType.INFORMATION);
                        endOfGameAlert.setTitle("Game Over");
                        endOfGameAlert.setHeaderText("The game has ended. GG");
                        endOfGameAlert.show();
                        goToMainMenu();
                        break;


                    case 31: // send players and scores
                        List<Player> updatedPlayers = new ArrayList<>();
                        int i = 0;
                        while (true) {
                            Player p = (Player) request.arg[i];
                            if (p == null) break;
                            updatedPlayers.add(p);
                            i++;
                        }
                        players.clear();
                        players.addAll(updatedPlayers);

                        playersObservable.clear();
                        for (Player p0 : players) {
                            playersObservable.add(p0.getUsername() + ", score: " + p0.getScore());
                        }
                        break;


                    case 41: // Send all chat
                        List<String> formattedMessages = new ArrayList<>();
                        int c = 0;
                        while (true) {
                            Message m = (Message) request.arg[c];
                            if (m == null) break;
                            formattedMessages.add(m.getSentBy() + ": " + m.getMessageBody());
                            c++;
                        }
                        chatObservable.clear();
                        chatObservable.addAll(formattedMessages);
                        break;
                    case 43: // New message
                        Message newMessage = (Message) request.arg[0];
                        String formatted = newMessage.getSentBy() + ": " + newMessage.getMessageBody();
                        chatBoxListView.getItems().add(0, formatted.trim());
                        break;


                    case 50:
                        canvas.resetCanvas();
                        break;
                    case 53:
                        String coords = (String) request.arg[0];
                        canvas.draw(coords);
                        break;
                }
            }
        });
    }

    public void updateImage(String coords) {
        tcpClient.sendFromUser(new Request(51, new Object[]{coords}));
        System.out.println("CLIENT COORDS:" + coords);
    }

    public void newRound() {
        this.round = 0;
        canvas.resetCanvas();
        chatObservable.clear();

        // Get players and scores
        tcpClient.sendFromUser(new Request(30, null));
    }

    private void goToMainMenu() {
        t.interrupt();
        Main.router.showPage("Main menu", "mainmenu.fxml");
    }

    private void setDrawer(Player drawer) {
//        for (Player p : players) {
//            if (p.getMacAddr().equals(drawer.getMacAddr())) {
//                p.setDrawer(true);
//            }
//        }

        if (currentPlayerName.equals(drawer.getUsername())) {
            canvas.setDrawable(true);
            gameStatusText.setText("Game Status: (Drawer)");
        } else {
            canvas.setDrawable(false);
            gameStatusText.setText("Game Status: (Guesser)");
        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Message> getAllChat() {
        return chatBox.getMessages();
    }

}