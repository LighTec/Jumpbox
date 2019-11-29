import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class DrawingCanvas
{

    private static int test = 0;
    private Canvas canvas;
    private GraphicsContext gc;
    private ArrayList<String> cords;
    private GameController controller;

    DrawingCanvas(GameController controller, AnchorPane canvasParent) {
        cords = new ArrayList<String>();
        this.controller = controller;

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


        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this.mouseDown);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this.mouseMove);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,this.mouseUp);
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

    public void setDrawable(boolean canDraw)
    {
        Main.canDraw = canDraw;
        System.out.println("Setting Drawable to " + canDraw + ".");
        resetCanvas();
    }

    private EventHandler<MouseEvent> mouseDown =  new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            System.out.println("click " + Main.canDraw);
            if (Main.canDraw)
            {
                QuadCurve quad = new QuadCurve();
                quad.setStartX(0.0f);
                quad.setStartY(50.0f);
                quad.setEndX(50.0f);
                quad.setEndY(50.0f);
                quad.setControlX(25.0f);
                quad.setControlY(0.0f);
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                cords.add(event.getX() + "," + event.getY());
            }
        }
    };

    private EventHandler<MouseEvent> mouseMove = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (Main.canDraw)
            {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath();
                cords.add(event.getX() + "," + event.getY());
                controller.updateImage(cords.get(0) + "," + cords.get(1));
                cords.clear();
                test++;
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                cords.add(event.getX() + "," + event.getY());
            }
        }
    };

    private EventHandler<MouseEvent> mouseUp = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (Main.canDraw)
            {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
                gc.closePath();
                cords.add(event.getX() + "," + event.getY());
                controller.updateImage(cords.get(0) + "," + cords.get(1));
                cords.clear();
                test++;
                System.out.println("total packets:" + test);
            }
        }
    };
}