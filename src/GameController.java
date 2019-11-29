
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

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
    @FXML
    private ColorPicker colorPicker;

    private String serverIp = Main.serverIp;

    private ChatBox chatBox;
    private DrawingCanvas canvas;
    private ArrayList<Player> players;

    // resets every round
    private int timeRemaining;
    private int round;
    private String[] wordOptions;
    private String currentWord = "";

    private ObservableList<String> chatObservable = FXCollections.observableArrayList();
    private ObservableList<String> playersObservable = FXCollections.observableArrayList();

    public GameController() {
    }

    private void sendMessage(Event e) {
        TextField source = (TextField) e.getSource();
        String message = source.getText();
        System.out.println(message);
        source.clear();

        Message newMessage = new Message(message, Main.currentUsername);
        TCPClient.sendFromUser(new Request(42, new Object[]{newMessage}));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TCPClient.gameController = this;
        this.chatBoxListView.setItems(chatObservable);
        this.playerListView.setItems(playersObservable);
        playersObservable.addAll(Main.playerNames);
        chatField.setOnAction(this::sendMessage);
        clearDrawing.setOnAction(this::clearButton);
        colorPicker.setOnAction(this::changeColor);

        canvas = new DrawingCanvas(this, canvasParent);
    }

    private void changeColor(ActionEvent actionEvent) {
        canvas.setColor(colorPicker.getValue());
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
                        gameTitle.setText(defaultText + "\t\t\t\t\t\t\t Time Remaining: " + timeRemaining);
                        break;
                    case 21: // send draw options
                        setDrawer(new Player(Main.currentUsername, "", false));
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
                        TCPClient.sendFromUser(new Request(22, new Object[]{selected}));
                        break;
                    case 22: // Send chosen draw option
                        currentWord = (String) request.arg[0];
                        break;
                    case 23: // Send draw leader
                        Player drawer = (Player) request.arg[0];
                        chatBoxListView.getItems().add(drawer.getUsername() + " is drawing next!");
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
                        while (i < request.arg.length) {
                            System.out.println("inside the loop");
                            Player p = (Player) request.arg[i];
                            //if (p == null) break;
                            updatedPlayers.add(p);
                            i++;
                        }
                       // players.clear();
                        //players.addAll(updatedPlayers);

                        playersObservable.clear();
                        for (Player p0 : updatedPlayers) {
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
                        break;

                    case 43:
                        Message message = (Message) request.arg[0];
                        String formattedMsg = message.getSentBy() + ": " + message.getMessageBody();
                        chatObservable.add(formattedMsg);
                        break;

                    case 50: // reset image
                        canvas.resetCanvas();
                        break;
                    case 54: // send whole image
                        // TODO FOR BRAD
                        break;
                    case 53: // send updated image
                        String toDraw = (String) request.arg[0];
                        System.out.println(toDraw);
                        canvas.draw(toDraw);
                        System.out.println("RECEIVED TIMESTAMP: " + System.currentTimeMillis());
                        break;
                    default:
                        System.err.println("Unknown command attempted to be sent.");
                        break;
                }
            }
        });
    }


    public void updateImage(String coords) {
        TCPClient.sendFromUser(new Request(51, new Object[]{coords}));
        System.out.println("SENT TIMESTAMP: " + System.currentTimeMillis());
    }

    public void newRound() {
        this.round = 0;
        canvas.resetCanvas();
        chatObservable.clear();

        chatBoxListView.getItems().add("New Round!!!");
        // Get players and scores
       TCPClient.sendFromUser(new Request(30, null));
    }

    private void goToMainMenu() {
        Main.playerNames.clear();
        Main.router.showPage("Main menu", "mainmenu.fxml");
    }

    private void clearButton(ActionEvent actionEvent)
    {
        canvas.resetCanvas();
    }



    private void setDrawer(Player drawer) {
        if(drawer.getUsername().equals(Main.currentUsername)){
            gameStatusText.setText("Game Status: (Drawer)");
            canvas.setDrawable(true);
            clearDrawing.setDisable(false);
            chatField.setDisable(true);
            colorPicker.setDisable(false);
        }else{
            gameStatusText.setText("Game Status: (Guesser)");
            canvas.setDrawable(false);
            clearDrawing.setDisable(true);
            chatField.setDisable(false);
            colorPicker.setDisable(true);
        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Message> getAllChat() {
        return chatBox.getMessages();
    }

}