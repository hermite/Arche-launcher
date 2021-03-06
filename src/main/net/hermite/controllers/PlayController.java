package net.hermite.controllers;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import lombok.Setter;
import net.hermite.AppProperties;
import net.hermite.Main;
import net.hermite.status.Server;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import fr.theshark34.supdate.SUpdate;
import net.hermite.utils.ZipHandlers;

public class PlayController {


    @FXML
    private Label versionLabel;
    @FXML
    private Label updateLabel;
    @FXML
    private Label playerNameLabel;
    @FXML
    private ImageView playerImage;
    @FXML
    private Circle mojangStatusCircle;
    @FXML
    private Label playersCountLabel;

    @Setter
    private MainController mainController;

    private SUpdate su = null;


    @FXML
    public void initialize() {

        new Thread(() -> {
            Server server = new Server(Main.SERVER_IP, Main.SERVER_PORT);
            if (server.isOnline()) {
                playersCountLabel.setFont(Font.font("White Storm", 28));
                Platform.runLater(() -> {
                    playersCountLabel.setText(server.getPlayersCount() + " / " + server.getMaxPlayers());
                });
            } else {
                Platform.runLater(() -> {
                    playersCountLabel.setText("Hors ligne");
                    playersCountLabel.setTextFill(Color.RED);
                });
            }
        }).start();

        playerImage.setImage(new Image("https://minotar.net/avatar/" + Main.account.getUUID()));
        playerNameLabel.setText(Main.account.getDisplayName());
        playerNameLabel.setFont(Font.font("White Storm", 28));

        versionLabel.setText(Main.LAUNCHER_VERSION);
        checkLauncherUpdate();
    }

    @FXML
    private void onPlayerImageClicked() {
        this.mainController.loadSwitchUsers();
    }

    @FXML
    private void onExternalLinkClicked(MouseEvent event) {
        String id = ((ImageView) event.getSource()).getId();

        try {
            URI uri = null;
            switch (id) {
                case "w":
                    uri = new URI(Main.WEBSITE_URL);
                    break;
                case "ds":
                    uri = new URI("https://discord.gg/BtNbjh9F5G");
                    break;
               /* case "tw":
                    uri = new URI("");
                    break;
                case "fb":
                    uri = new URI("");
                    break;*/
                case "yt":
                    uri = new URI("https://www.youtube.com/channel/UCZeUUIC57Ququk9hwN8IUlA");
                    break;
            }

            if (uri != null) {
                Desktop.getDesktop().browse(uri);
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOptionsClicked(MouseEvent event) {
        mainController.openOptions();
    }

    @FXML
    private void onPlayClicked() {
        new Thread(this::update).start();
    }

    private void update() {

        Platform.runLater(() -> {
            updateLabel.setText("V??rification de l'int??grit?? des fichiers");
            updateLabel.setVisible(true);
        });

        if (!AppProperties.properties.getProperty("beta", "false").equals("true")) {
            su = new SUpdate(Main.UPDATE_URL, Main.DIR);
        } else {
            su = new SUpdate(Main.BETA_UPDATE_URL, Main.BETA_DIR);
        }



        //Ajout Sauvegarde des fichiers de moreplayermodels
        File Option = new File(Main.DIR + "/options.txt");
        /*
        File SaveDir = new File(Main.DIR.getParent() + "/.Arche_RP_Save");
        File SaveModelFile = new File(SaveDir + "/SaveModelPlayer.zip");
        File ModelsDir = new File(Main.DIR + "/moreplayermodels");

        if (!SaveDir.exists() || !SaveDir.isDirectory())
        {
            SaveDir.mkdir();
        }

        if(ModelsDir.exists()) {
            ZipHandlers.SaveFilesOnZip(Main.DIR + "/moreplayermodels", SaveModelFile.toString());
        }
        //Fin Ajout SaveFiles*/
       su.addApplication(new FileDeleter());


        Thread t = new Thread(new Runnable() {

            private Label l;

            public Runnable init(Label label) {
                l = label;
                return this;
            }

            @Override
            public void run() {
                boolean flag = true;
                while (flag) {
                    int val = (int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000);
                    int max = (int) (BarAPI.getNumberOfTotalBytesToDownload() / 1000);

                    if (max != 0) {
                        Platform.runLater(() -> {
                            l.setText("T??l??chargement en cours : " + BarAPI.getNumberOfDownloadedFiles() + "/" + BarAPI.getNumberOfFileToDownload()
                                    + " (" + val * 100 / max + " %).");
                        });
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        flag = false;
                    }
                }
            }
        }.init(updateLabel));

        t.start();

        try {
            su.start();
            t.interrupt();
            Platform.runLater(() -> {
                updateLabel.setVisible(false);
            });



            //Ajout SaveFiles
            if(!Option.exists()) {
                ZipHandlers.unzip(new File(Main.DIR + "/config.zip"), Main.DIR);
            }/*
            if(ModelsDir.exists()) {
                ZipHandlers.unzip(SaveModelFile,new File(Main.DIR + "/moreplayermodels"));
            }

            //Fin Ajout SaveFiles

*/
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launch() {
        try {
            ExternalLaunchProfile profile;
            ExternalLauncher mcLauncher;
            AuthInfos authInfos = new AuthInfos(Main.account.getDisplayName(), Main.account.getAccessToken(),
                    Main.account.getUUID());

            if (!AppProperties.properties.getProperty("beta", "false").equals("true"))
                profile = MinecraftLauncher.createExternalProfile(Main.INFOS, GameFolder.BASIC, authInfos);
            else
                profile = MinecraftLauncher.createExternalProfile(Main.BETA_INFOS, GameFolder.BASIC, authInfos);

            File discordRPC = new File(profile.getDirectory().toString() + "/mods/customdiscordrpc-2.21.jar");
            System.out.println(discordRPC);
            if (!System.getProperty("os.name").contains("Windows") && discordRPC.exists()) {
                discordRPC.delete();
            }

            String ram = AppProperties.properties.getProperty("ram", "2");
            profile.getVmArgs().addAll(Arrays.asList("-Xms" + ram + "G", "-Xmx" + ram + "G"));
            mcLauncher = new ExternalLauncher(profile);
            mcLauncher.launch();

            System.exit(0);
        } catch (LaunchException exc) {
            exc.printStackTrace();
        }
    }

    private void checkLauncherUpdate() {

        /*new Thread(() -> {

            try {
                String out = null;
                out = new Scanner(new URL(Main.LAUNCHER_CHECK_URL).openStream(), "UTF-8").useDelimiter("\\A").next();
                if (!out.equals(Main.LAUNCHER_VERSION)) {
                    Platform.runLater(() -> {
                        versionLabel.setText(versionLabel.getText() + " (une mise ?? jour est disponible !)");
                        versionLabel.setStyle("-fx-underline: true;-fx-cursor: hand;");
                        versionLabel.setOnMousePressed(event -> {
                            try {
                                URI uri;
                                String os = System.getProperty("os.name").toLowerCase();
                                if (os.contains("win")) {
                                    uri = new URI(Main.LAUNCHER_DOWNLOAD_EXE_URL);
                                } else {
                                    uri = new URI(Main.LAUNCHER_DOWNLOAD_JAR_URL);
                               // }
                                Desktop.getDesktop().browse(uri);
                            } catch (IOException | URISyntaxException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();*/

        new Thread(() -> {

            try {
                    URL url = new URL(Main.LAUNCHER_CHECK_URL);
                    URLConnection conn = url.openConnection();
                    conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                    // Lecture
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = rd.readLine()) != null){
                        sb.append(line);
                    }
                    rd.close();

                    //String out = null;
                   // out = new Scanner(new URL(Main.LAUNCHER_CHECK_URL).openStream(), "UTF-8").useDelimiter("\\A").next();
                    if (!sb.toString().equals(Main.LAUNCHER_VERSION)) {
                        Platform.runLater(() -> {
                            versionLabel.setText(versionLabel.getText() + " (une mise ?? jour est disponible !)");
                            versionLabel.setStyle("-fx-underline: true;-fx-cursor: hand;");
                            versionLabel.setOnMousePressed(event -> {
                            try {
                                    URI uri;
                                    String os = System.getProperty("os.name").toLowerCase();
                                   if (os.contains("win")) {
                                        uri = new URI(Main.LAUNCHER_DOWNLOAD_EXE_URL);
                                   } else {
                                        uri = new URI(Main.LAUNCHER_DOWNLOAD_JAR_URL);
                                   }
                                    //uri = new URI(Main.LAUNCHER_DOWNLOAD_JAR_URL);
                                        Desktop.getDesktop().browse(uri);
                                   } catch (IOException | URISyntaxException e) {
                                        e.printStackTrace();
                            }
                            });
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
    }


}

