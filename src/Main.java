import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    static Router router;

    @Override
    public void start(Stage primaryStage) throws Exception{
        router = new Router(primaryStage);
        router.showPage("Main Menu", "mainmenu.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
