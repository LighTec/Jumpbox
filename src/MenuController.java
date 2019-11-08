import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    @FXML
    private Button confirmButton;
    @FXML
    private TextField userName;
    @FXML
    private TextField ip;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        confirmButton.setOnAction(this::enterPressed);

    }

    private void enterPressed(Event e) {
        Button source = (Button) e.getSource();

        String username = this.userName.getText();
        String ip = this.ip.getText();
        if (!this.ip.getText().isEmpty() && !this.userName.getText().isEmpty()) {
            System.out.println(ip);
            System.out.println(username);
        }

        Main.currentUsername = username;
        Main.serverIp = ip;
        Main.router.goToLobby();
    }
}
