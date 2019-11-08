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

    public void startGame(String username, String serverIp) {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("gamemenu.fxml")
        );
        primaryStage.setTitle("Game");
        try {
            primaryStage.setScene(new Scene(
                    loader.load()
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }

        GameController controller = new GameController();
        controller.initData(username, serverIp);

        loader.setController(controller);
        primaryStage.show();
    }

}
