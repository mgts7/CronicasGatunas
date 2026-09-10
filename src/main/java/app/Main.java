package app;

import gui.MainAppView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion. Lanza la pantalla de seleccion
 * de mision (requisito 7.1).
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainAppView mainAppView = new MainAppView(primaryStage);
        mainAppView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}