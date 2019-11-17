import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Router {

    private Stage primaryStage;

    public Router(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showPage(String title, String path) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                primaryStage.setResizable(false);
                Parent root = null;
                try {
                    root = FXMLLoader.load(getClass().getResource(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                primaryStage.setTitle(title);
                if (root != null) {
                    primaryStage.setScene(new Scene(root));
                }
                primaryStage.show();
            }
        });
    }

    public void goToLobby() {
        showPage("Game", "lobby.fxml");
    }

    public void startGame() {
        showPage("Game", "gamemenu.fxml");

    }

}
