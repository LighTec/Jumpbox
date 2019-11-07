package sample;

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
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView<String> chatBoxListView;
    @FXML
    private ListView<String> playerListView;
    @FXML
    private TextField chatField;
    @FXML
    private Canvas canvasParent;



    private ObservableList<String> chat = FXCollections.observableArrayList();
    private ObservableList<String> players = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.chatBoxListView.setItems(chat);
        this.playerListView.setItems(players);
        for (int i = 0; i < 10; i++)
        {
            players.add("player"+i);
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
    private void enterPressed(Event e)
    {
        TextField source = (TextField) e.getSource();
        System.out.println(source.getText());
        chatBoxListView.getItems().add(0, source.getText());
        source.clear();
    }
}
