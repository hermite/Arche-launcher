package net.hermite.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import lombok.Setter;
import net.hermite.AppProperties;
import net.hermite.Main;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLConnection;

public class OptionsController {

    @FXML
    private CheckBox betaCheckbox;
    @FXML
    private Slider ramSlider;

    @Setter
    private MainController mainController;

    @FXML
    public void initialize() {
        int ram = 2;
        String ramValue = AppProperties.properties.getProperty("ram", "2");
        ram = Integer.parseInt(ramValue);
        ramSlider.setValue(ram);

        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        int maxMemory = (int)Math.min(12, memorySize / 1000000000);
        ramSlider.setMax(maxMemory);

        String betaValue = AppProperties.properties.getProperty("beta", "false");
        betaCheckbox.setSelected(Boolean.parseBoolean(betaValue));

        checkOp();
    }

    private void checkOp() {
        new Thread(() -> {
            String out = null;
            try {
                URL url = new URL(Main.WEBSITE_URL);
                URLConnection conn = url.openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
                // Lecture
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null){
                    sb.append(line);
                    if (line.contains(Main.account.getDisplayName().toLowerCase())) {
                        Platform.runLater(() -> {
                            betaCheckbox.setVisible(true);
                        });
                        break;
                    }
                }
                rd.close();


                //out = new Scanner(new URL(Main.WEBSITE_URL + "utils/ops").openStream(), "UTF-8").useDelimiter("\\A").next();
                /*for (String line : out.split("\n")) {
                    if (line.contains(Main.account.getDisplayName().toLowerCase())) {
                        Platform.runLater(() -> {
                            betaCheckbox.setVisible(true);
                        });
                        break;
                    }
                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onBackClicked() {
        this.mainController.reopenPlay();
    }

    @FXML
    private void onSaveAndExit() {
        AppProperties.properties.setProperty("ram", String.valueOf((int)ramSlider.getValue()));
        AppProperties.properties.setProperty("beta", String.valueOf(betaCheckbox.isSelected()));
        AppProperties.saveProperties();
        this.mainController.reopenPlay();
    }

    @FXML void onOpenDir(MouseEvent event) {
        String id = ((Control) event.getSource()).getId();
        try {
            File dir = null;
            switch (id) {
                case "game":
                    dir = Main.DIR;
                    break;
                case "logs":
                    dir = new File(Main.DIR, "logs");
                    break;
                case "crashlogs":
                    dir = new File(Main.DIR, "crash-reports");
                    break;
            }
            if (dir != null)
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
