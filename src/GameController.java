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

    private String currentPlayerName;
    private String serverIp;

    void initData(String currentPlayerName, String serverIp) {
        this.currentPlayerName = currentPlayerName;
        this.serverIp = serverIp;

    }

    private ChatBox chatBox;
    private DrawingCanvas canvas;
    private ArrayList<Player> players;
    private TCPClient tcpClient;

    private int timeRemaining;
    private int round;
    private String[] wordOptions;


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
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;


            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;


            case 20:
                break;
            case 21:
                break;
            case 22:
                break;
            case 23:
                break;
            case 24:
                break;
            case 25:
                break;
            case 26:
                break;

                //TODO

        }
    }

    private void initialConnection(String name) {

    }

    public GameController(ArrayList<Player> players, TCPClient tcpClient) {
        this.chatBox = new ChatBox();
        this.canvas = new DrawingCanvas();
        this.players = players;
        this.tcpClient = tcpClient;
        this.round = 0;
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