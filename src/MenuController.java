import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML
    private Button newGameButton;
    @FXML
    private Button joinGameButton;
    @FXML
    private TextField userName;
    @FXML
    private TextField ip;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        newGameButton.setOnAction(this::onNewGameClick);
        joinGameButton.setOnAction(this::onJoinGameClick);

    }

    private void grabUserData(int cmd) {
        String username = this.userName.getText();
        String ip = this.ip.getText();
        if (!this.ip.getText().isEmpty() && !this.userName.getText().isEmpty()) {
            System.out.println(ip);
            System.out.println(username);
        }

        Main.currentUsername = username;
        Main.serverIp = ip;
        Main.INITIAL_CMD = cmd;
    }

    private void onNewGameClick(Event e) {
        grabUserData(1);
        Main.router.goToLobby();
    }

    private void onJoinGameClick(Event e) {
        grabUserData(3);
        Main.router.goToLobby();
    }


}
