package net.hermite;

import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.text.Font;
import net.hermite.nologin.account.Account;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    public static final String APPLICATION_TITLE = "Launcher de l'Arche";
    public static final String WEBSITE_URL = "https://arche-rp.fr/";
    public static final String SERVER_IP = "193.70.80.225";
    public static final int SERVER_PORT = 35565;
    private static final GameVersion VERSION = new GameVersion("1.12.2", GameType.V1_8_HIGHER);

    public static final GameInfos INFOS = new GameInfos("Arche_RP", VERSION, new GameTweak[] { GameTweak.FORGE });
    public static final File DIR = INFOS.getGameDir();
    public static final String UPDATE_URL = "https://launcheur.arche-rp.fr/client/";

    private static final GameVersion BETA_VERSION = new GameVersion("1.12.2", GameType.V1_8_HIGHER);
    public static final GameInfos BETA_INFOS = new GameInfos("", BETA_VERSION, new GameTweak[] { GameTweak.FORGE });
    public static final File BETA_DIR = BETA_INFOS.getGameDir();
    public static final String BETA_UPDATE_URL = "";

    public static final String LAUNCHER_VERSION = "1.3.0";
    public static final String LAUNCHER_CHECK_URL = "https://launcheur.arche-rp.fr/client/resources/launcher.version";
    public static final String LAUNCHER_DOWNLOAD_EXE_URL = "https://launcheur.arche-rp.fr/client/resources/Arche_RP.exe";
    public static final String LAUNCHER_DOWNLOAD_JAR_URL = "https://launcheur.arche-rp.fr/client/resources/Arche_RP.jar";

    public static Account account;
    public static List<Account> accountList = new ArrayList<Account>();

    private double  xOffset = 0.0;
    private double  yOffset = 0.0;

    public static Stage primaryStage = null;

    @Override
    public void start(Stage stage) throws Exception{

        Main.primaryStage = stage;

        if (System.getProperty("os.name").contains("Windows")) {
            if(System.getenv("ProgramFiles(x86)") != null) {
                if (System.getProperty("sun.arch.data.model").equals("32")) {
                    JOptionPane.showMessageDialog(null, "Java 32bit est installé sur un windows" +
                            "64bit. Le jeu ne pourra pas se lancer ! Le navigateur va s'ouvrir sur le site de " +
                            "téléchargement de Java. Il faut télécharger la version \"Windows Hors ligne (64 bits)\"");
                    Desktop.getDesktop().browse(new URI("https://www.java.com/fr/download/manual.jsp"));
                    System.exit(1);
                }
            }
        }

        DIR.mkdir();

        Font.loadFont(getClass().getResourceAsStream("/fonts/BrownRegular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Lulo.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/WhiteStorm.otf"), 14);
        Parent root = FXMLLoader.load(getClass().getResource("/views/main.fxml"));
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {

            if (yOffset < 36) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }

        });

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle(Main.APPLICATION_TITLE);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
        primaryStage.setResizable(false);

        Scene scene = new Scene(root, 1200, 737);
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.runLater(root::requestFocus);
    }




    public static void main(String[] args) {
        launch(args);
    }
}
