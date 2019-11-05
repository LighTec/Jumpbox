package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView<String> chatBoxListView;
    @FXML
    private ListView<String> playerListView;

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

        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task"); chat.add("First task");
        chat.add("Second task");
    }
}
