import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class DrawingCanvas
{

    private static int test = 0;
    private Canvas canvas;
    private GraphicsContext gc;
    private ArrayList<String> cords;

    DrawingCanvas(AnchorPane canvasParent) throws InterruptedException {
        cords = new ArrayList<String>();

        // Create the Canvas
        canvas = new Canvas(590, 526);

        // Get the graphics context of the canvas
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(5);
        gc.setStroke(Color.ORANGERED);

        canvasParent.setStyle("-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 1;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: black;");

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseDown);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseMove);

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,mouseUp);



        canvasParent.getChildren().add(canvas);


    }

    public void draw(String coords){
        String[] coordList = coords.split(",");
        gc.beginPath();
        gc.moveTo(Double.parseDouble(coordList[0]), Double.parseDouble(coordList[1]));
        gc.lineTo(Double.parseDouble(coordList[2]), Double.parseDouble(coordList[3]));
        gc.stroke();
        gc.closePath();
    }

    public void resetCanvas() {
        gc.clearRect(0, 0, 300, 200);
    }

    private EventHandler<MouseEvent> mouseDown =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            cords.add(event.getX() + "," + event.getY());
        }
    };

    private EventHandler<MouseEvent> mouseMove =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            gc.closePath();
            cords.add(event.getX() + "," + event.getY());
            cords.clear();
            test++;
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            cords.add(event.getX() + "," + event.getY());
        }
    };

    private EventHandler<MouseEvent> mouseUp =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            gc.closePath();
            cords.add(event.getX() + "," + event.getY());
            cords.clear();
            test++;
            System.out.println("total packets:" + test);
        }
    };
}