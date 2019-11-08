import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.w3c.dom.Text;

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
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

        confirmButton.setOnAction(this::enterPressed);

    }
    private void enterPressed(Event e)
    {
        Button source = (Button) e.getSource();

        String username = this.userName.getText();
        String ip = this.ip.getText();
        if (!this.ip.getText().isEmpty() && !this.userName.getText().isEmpty())
        {
            System.out.println(ip);
            System.out.println(username);
        }

        Main.router.startGame(username, ip);
    }
}
