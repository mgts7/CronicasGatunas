package gui;

import gui.mission1.Mission1View;
import gui.mission2.Mission2View;
import gui.mission3.Mission3View;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * ============================================================
 * MainAppView
 * ============================================================
 *
 * Pantalla de seleccion de mision (requisito 7.1). Usa la imagen de
 * fondo generada (con el titulo "Cronicas Gatunas" ya incluido en
 * la propia imagen) y superpone los 4 botones de mision debajo del
 * titulo.
 *
 * Sobre el tamano de ventana fijo:
 *   La ventana se fija EXACTAMENTE al tamano nativo de la imagen
 *   (leido automaticamente de background.getWidth()/getHeight(), no
 *   hardcodeado) y se desactiva el resize. Esto evita por completo
 *   cualquier logica de escalado o distorsion: la imagen siempre se
 *   ve exactamente como fue disenada. El unico costo es que el
 *   usuario no puede maximizar/redimensionar esta pantalla en
 *   particular (las pantallas de cada mision si pueden ser
 *   redimensionables, ver openMission1()).
 */
public final class MainAppView {

    private static final String BACKGROUND_IMAGE_PATH = "/images/MainMenuTitle.jpeg";

    // Cuanto espacio dejar entre el borde inferior de la imagen y el
    // primer boton, como FRACCION de la altura total (no pixeles fijos),
    // para que se vea proporcional sin importar a que tamano se escale
    // la imagen en pantalla. Ajustar segun donde quede el area despejada
    // real en la imagen generada.
    private static final double BUTTONS_BOTTOM_MARGIN_RATIO = 0.10;

    // Cuanto de la pantalla disponible puede ocupar como maximo la
    // ventana (para dejar margen visible del sistema operativo:
    // barra de tareas, dock, etc.).
    private static final double MAX_SCREEN_FRACTION = 0.85;

    private final Stage stage;
    private double windowWidth;
    private double windowHeight;

    public MainAppView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Image background = loadBackgroundImage();

        double scale = computeScaleToFitScreen(background.getWidth(), background.getHeight());
        double width = background.getWidth() * scale;
        double height = background.getHeight() * scale;
        this.windowWidth = width;
        this.windowHeight = height;

        ImageView imageView = new ImageView(background);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        // Se escalan ambas dimensiones por el mismo factor, asi que el
        // aspect ratio se preserva automaticamente sin distorsion.

        VBox buttonBox = buildMissionButtons();
        StackPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(buttonBox, new Insets(0, 0, height * BUTTONS_BOTTOM_MARGIN_RATIO, 0));

        StackPane root = new StackPane(imageView, buttonBox);
        root.setPrefSize(width, height);

        Scene scene = new Scene(root, width, height);

        stage.setTitle("Cronicas Gatunas");
        stage.setScene(scene);
        stage.setResizable(false); // fija el aspect ratio: la imagen nunca se distorsiona
        stage.show();
    }

    private VBox buildMissionButtons() {
        Button mission1Button = new Button("Mision 1: Minefield");
        mission1Button.setOnAction(e -> openMission1());

        Button mission2Button = new Button("Mision 2: Recuperando Claude");
        mission2Button.setOnAction(e -> openMission2());

        Button mission3Button = new Button("Mision 3: El Food Stash");
        mission3Button.setOnAction(e -> openMission3());

        Button mission4Button = new Button("Mision 4: Reconectando la Red");
        mission4Button.setDisable(true);

        VBox box = new VBox(10, mission1Button, mission2Button, mission3Button, mission4Button);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void openMission1() {
        Mission1View mission1View = new Mission1View(this::show);
        Scene scene = new Scene(mission1View, windowWidth, windowHeight);

        stage.setScene(scene);
        stage.setResizable(false);
    }

    private void openMission2() {
        Mission2View mission2View = new Mission2View(this::show);
        Scene scene = new Scene(mission2View, windowWidth, windowHeight);

        stage.setScene(scene);
        stage.setResizable(false);
    }

    private void openMission3() {
        Mission3View mission3View = new Mission3View(this::show);
        Scene scene = new Scene(mission3View, windowWidth, windowHeight);

        stage.setScene(scene);
        stage.setResizable(false);
    }

    /**
     * Calcula un factor de escala (siempre <= 1.0, nunca agranda la
     * imagen mas alla de su tamano original) para que la ventana
     * quepa dentro de un MAX_SCREEN_FRACTION de la pantalla
     * disponible del usuario, sin distorsionar el aspect ratio
     * (mismo factor aplicado a ancho y alto).
     */
    private double computeScaleToFitScreen(double imageWidth, double imageHeight) {
        var screenBounds = Screen.getPrimary().getVisualBounds();
        double maxWidth = screenBounds.getWidth() * MAX_SCREEN_FRACTION;
        double maxHeight = screenBounds.getHeight() * MAX_SCREEN_FRACTION;

        double scaleForWidth = maxWidth / imageWidth;
        double scaleForHeight = maxHeight / imageHeight;

        return Math.min(1.0, Math.min(scaleForWidth, scaleForHeight));
    }

    private Image loadBackgroundImage() {
        InputStream stream = getClass().getResourceAsStream(BACKGROUND_IMAGE_PATH);
        if (stream == null) {
            throw new IllegalStateException(
                    "No se encontro la imagen de fondo en " + BACKGROUND_IMAGE_PATH
                            + ". Verifica que el archivo este en src/main/resources" + BACKGROUND_IMAGE_PATH
                            + " y que el nombre coincida exactamente (sensible a mayusculas/minusculas).");
        }
        return new Image(stream);
    }
}