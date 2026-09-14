package gui;

import gui.mission1.Mission1View;
import gui.mission2.Mission2View;
import gui.mission3.Mission3View;
import gui.mission4.Mission4View;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * ============================================================
 * MainAppView
 * ============================================================
 *
 * Pantalla de seleccion de mision (requisito 7.1). Usa la imagen de
 * fondo generada (con el titulo "Cronicas Gatunas" ya incluido en
 * la propia imagen) y superpone, debajo del titulo, la imagen de
 * titulo de cada mision (MissionXTitle.png) usada como boton: al
 * pasar el mouse por encima, la imagen crece un poco (efecto
 * "hover") para dar feedback de que es interactiva, y al hacer
 * click se abre la mision correspondiente.
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

    // Patron de nombre de las imagenes de titulo de cada mision,
    // ubicadas junto al resto de imagenes del proyecto
    // (src/main/resources/images/MissionXTitle.png).
    private static final String MISSION_TITLE_IMAGE_PATH_PATTERN = "/images/Mission%dTitle.png";

    // Ancho al que se escala cada imagen de titulo cuando se usa como
    // boton (con preserveRatio, el alto se ajusta solo). Se mantiene
    // chico y consistente entre las 4 misiones para que el menu se
    // vea ordenado sin importar la relacion de aspecto de cada PNG.
    private static final double MISSION_BUTTON_WIDTH = 260;

    // Cuanto crece la imagen al pasar el mouse por encima (1.0 =
    // tamano normal, 1.1 = 10% mas grande) y cuanto dura la
    // animacion de crecimiento/regreso.
    private static final double HOVER_SCALE = 1.12;
    private static final Duration HOVER_ANIMATION_DURATION = Duration.millis(150);

    // Cuanto espacio dejar entre el borde inferior de la imagen y el
    // primer boton, como FRACCION de la altura total (no pixeles fijos),
    // para que se vea proporcional sin importar a que tamano se escale
    // la imagen en pantalla. Ajustar segun donde quede el area despejada
    // real en la imagen generada.
    private static final double BUTTONS_BOTTOM_MARGIN_RATIO = -0.16;

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
        Image background = loadImage(BACKGROUND_IMAGE_PATH);

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
        ImageView mission1Button = createMissionTitleButton(1, this::openMission1);
        ImageView mission2Button = createMissionTitleButton(2, this::openMission2);
        ImageView mission3Button = createMissionTitleButton(3, this::openMission3);
        ImageView mission4Button = createMissionTitleButton(4, this::openMission4);

        VBox box = new VBox(10, mission1Button, mission2Button, mission3Button, mission4Button);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Carga la imagen de titulo de la mision indicada y la deja lista
     * para usarse como boton: cursor de mano, tamano fijo consistente
     * entre misiones, click ejecuta la accion, y un efecto de "hover"
     * que agranda la imagen suavemente al pasar el mouse por encima
     * (y la regresa a su tamano normal al salir).
     */
    private ImageView createMissionTitleButton(int missionNumber, Runnable onClick) {
        String path = String.format(MISSION_TITLE_IMAGE_PATH_PATTERN, missionNumber);
        Image image = loadImage(path);

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(MISSION_BUTTON_WIDTH);
        imageView.setCursor(Cursor.HAND);

        imageView.setOnMouseClicked(e -> onClick.run());
        imageView.setOnMouseEntered(e -> animateScale(imageView, HOVER_SCALE));
        imageView.setOnMouseExited(e -> animateScale(imageView, 1.0));

        return imageView;
    }

    /** Anima suavemente el escalado de un nodo hasta el factor indicado (1.0 = tamano normal). */
    private void animateScale(ImageView imageView, double targetScale) {
        ScaleTransition transition = new ScaleTransition(HOVER_ANIMATION_DURATION, imageView);
        transition.setToX(targetScale);
        transition.setToY(targetScale);
        transition.playFromStart();
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

    private void openMission4() {
        Mission4View mission4View = new Mission4View(this::show);
        Scene scene = new Scene(mission4View, windowWidth, windowHeight);

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

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException(
                    "No se encontro la imagen en " + path
                            + ". Verifica que el archivo este en src/main/resources" + path
                            + " y que el nombre coincida exactamente (sensible a mayusculas/minusculas).");
        }
        return new Image(stream);
    }
}